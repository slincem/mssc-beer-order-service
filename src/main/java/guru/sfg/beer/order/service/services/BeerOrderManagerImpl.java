package guru.sfg.beer.order.service.services;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.statemachine.BeerOrderStateChangeInterceptor;
import guru.sfg.beer.order.service.web.model.BeerOrderDto;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RequiredArgsConstructor
@Service
public class BeerOrderManagerImpl implements BeerOrderManager {

    public static final String BEER_ORDER_ID_HEADER = "BEER_ORDER_ID_HEADER";
    private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachineFactory;
    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderStateChangeInterceptor beerOrderStateChangeInterceptor;

    @Transactional
    @Override
    public BeerOrder newBeerOrder(BeerOrder beerOrder) {

        //Deffensive programming
        beerOrder.setId(null);
        beerOrder.setOrderStatus(BeerOrderStatusEnum.NEW);

        BeerOrder savedBeerOrder = beerOrderRepository.saveAndFlush(beerOrder);
        sendBeerOrderEvent(savedBeerOrder, BeerOrderEventEnum.VALIDATE_ORDER);
        return savedBeerOrder;
    }

    @Transactional
    @Override
    public void processValidationResult(UUID beerOrderId, Boolean isValid) {
        log.debug("Process Validation Result for BeerOrderId: " + beerOrderId + " with result isValid?: " + isValid);

        beerOrderRepository.findById(beerOrderId).ifPresentOrElse(beerOrder -> {
            if(isValid) {
                sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_PASSED);

                //Await for status to change.
                awaitForStatus(beerOrderId, BeerOrderStatusEnum.VALIDATED);
                // Look for the object again, because the beerOrder from before is now in a "stale state", because it's older.
                // and there is a new version, because the event interceptor changed it.
                BeerOrder validatedBeerOrder = beerOrderRepository.findById(beerOrderId).get();
                sendBeerOrderEvent(validatedBeerOrder, BeerOrderEventEnum.ALLOCATE_ORDER);
            } else {
                sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_FAILED);
            }
        }, () -> log.error("ProcessValidationResult. Beer Order Not Found. ID: " + beerOrderId));
    }

    @Transactional
    @Override
    public void beerOrderAllocationPassed(BeerOrderDto beerOrderDto) {
        beerOrderRepository.findById(beerOrderDto.getId()).ifPresentOrElse((beerOrder -> {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_SUCCESS);
            awaitForStatus(beerOrder.getId(), BeerOrderStatusEnum.ALLOCATED);
            updateAllocatedQuantity(beerOrderDto);
        }), () -> log.error("BeerOrderAllocationPassed. Beer Order Not Found. ID: " + beerOrderDto.getId()));
    }

    @Transactional
    @Override
    public void beerOrderAllocationPendingInventory(BeerOrderDto beerOrderDto) {
        beerOrderRepository.findById(beerOrderDto.getId()).ifPresentOrElse((beerOrder) -> {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_NO_INVENTORY);
            awaitForStatus(beerOrder.getId(), BeerOrderStatusEnum.PENDING_INVENTORY);
            updateAllocatedQuantity(beerOrderDto);
            BeerOrder beerOrderUpdated = beerOrderRepository.findById(beerOrderDto.getId()).get();
            sendBeerOrderEvent(beerOrderUpdated, BeerOrderEventEnum.BEERORDER_PICK_UP);
        }, () -> log.error("BeerOrderAllocationPendingInventory. BeerBeer Order Not Found. ID: " + beerOrderDto.getId()));

    }

    private void updateAllocatedQuantity(BeerOrderDto beerOrderDto) {
        beerOrderRepository.findById(beerOrderDto.getId()).ifPresentOrElse((allocatedOrder -> {
            allocatedOrder.getBeerOrderLines().forEach(beerOrderLine -> {
                beerOrderDto.getBeerOrderLines().forEach(beerOrderLineDto -> {
                    if(beerOrderLine.getId().equals(beerOrderLineDto.getId())) {
                        beerOrderLine.setQuantityAllocated(beerOrderLineDto.getQuantityAllocated());
                    }
                });
            });

            //TODO this differs from the original sample. This should be the correct way.
            beerOrderRepository.saveAndFlush(allocatedOrder);
        }), () -> log.error("UpdateAllocatedQuantity.Beer Order Not Found. ID: " + beerOrderDto.getId()));
    }

    @Transactional
    @Override
    public void beerOrderAllocationFailed(BeerOrderDto beerOrderDto) {
        beerOrderRepository.findById(beerOrderDto.getId()).ifPresentOrElse((beerOrder) -> {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_FAILED);
        }, () -> log.error("BeerOrderAllocationFailed. Beer Order Not Found. ID: " + beerOrderDto.getId()));
    }

    @Override
    public void beerOrderPickedUp(UUID beerOrderId) {
        beerOrderRepository.findById(beerOrderId).ifPresentOrElse((beerOrder) -> {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.BEERORDER_PICK_UP);
        }, () -> log.error("BeerOrderPickedUp. Beer Order Not Found. ID: " + beerOrderId));
    }

    @Override
    public void beerOrderCancelled(UUID beerOrderId) {
        beerOrderRepository.findById(beerOrderId).ifPresentOrElse((beerOrder) -> {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.CANCEL_ORDER);
        }, () -> log.error("BeerOrderCancelled. Beer Order Not Found. ID: " + beerOrderId));
    }

    private void sendBeerOrderEvent(BeerOrder beerOrder, BeerOrderEventEnum eventEnum) {
        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine = build(beerOrder);

        Message<BeerOrderEventEnum> msg = MessageBuilder.withPayload(eventEnum).setHeader(BEER_ORDER_ID_HEADER, beerOrder.getId())
                .build();
        stateMachine.sendEvent(msg);
    }

    /**
     * Builds or returns a StateMachine for a given BeerOrder.
     *
     * @param  beerOrder	the BeerOrder to build the StateMachine for
     * @return         	the built StateMachine
     */
    private StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> build(BeerOrder beerOrder) {
        // Take the state machine from the cache Spring has, or create a new one.
        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine = stateMachineFactory.getStateMachine(beerOrder.getId());

        // Stop because we wont take into account its State, but we will set the current one we took from DB
        stateMachine.stopReactively().subscribe();

        //We rehydrate the StateMachine with the state that comes from DB.
        stateMachine.getStateMachineAccessor().doWithAllRegions(sma -> {

            //As part of the rehydration we set an interceptor to "intercept" changes over the state machine
            sma.addStateMachineInterceptor(beerOrderStateChangeInterceptor);
            // Force the state machine to set this status.
            sma.resetStateMachineReactively(new DefaultStateMachineContext<>(beerOrder.getOrderStatus(), null, null, null)).subscribe();
        });

        stateMachine.startReactively().subscribe();
        return stateMachine;
    }

    /**
     * Waits for the spring state machine to change its status, trying to get the last status that was sent.
     * in order to avoid race conditions to continue with the next step of the state machine.
     * @param beerOrderId
     * @param statusEnum
     */
    private void awaitForStatus(UUID beerOrderId, BeerOrderStatusEnum statusEnum) {

        AtomicBoolean found = new AtomicBoolean(false);
        AtomicInteger loopCount = new AtomicInteger(0);

        while(!found.get()) {
            if(loopCount.incrementAndGet() > 10) {
                found.set(true);
                log.debug("Loop Retries Exceeded");
            }

            beerOrderRepository.findById(beerOrderId).ifPresentOrElse(beerOrder -> {
                if(beerOrder.getOrderStatus().equals(statusEnum)) {
                    found.set(true);
                    log.debug("Order Found");
                } else {
                    log.debug("Order Status Not Equal. Expected: " + statusEnum.name() + " Found: " + beerOrder.getOrderStatus().name());
                }
            }, () -> log.debug("Order Id Not Found in Await For Status."));

            if(!found.get()) {
                try {
                    log.debug("awaitForStatus. Sleeping for retry");
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    //do nothing
                }
            }
        }
    }
}
