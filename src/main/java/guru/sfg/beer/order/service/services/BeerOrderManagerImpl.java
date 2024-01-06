package guru.sfg.beer.order.service.services;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class BeerOrderManagerImpl implements BeerOrderManager {

    private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachineFactory;
    private final BeerOrderRepository beerOrderRepository;

    @Transactional
    @Override
    public BeerOrder newBeerOrder(BeerOrder beerOrder) {

        //Deffensive programming
        beerOrder.setId(null);
        beerOrder.setOrderStatus(BeerOrderStatusEnum.NEW);

        BeerOrder savedBeerOrder = beerOrderRepository.save(beerOrder);
        sendBeerOrderEvent(savedBeerOrder, BeerOrderEventEnum.VALIDATE_ORDER);
        return savedBeerOrder;
    }

    private void sendBeerOrderEvent(BeerOrder beerOrder, BeerOrderEventEnum eventEnum) {
        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine = build(beerOrder);

        Message msg = MessageBuilder.withPayload(eventEnum)
                .build();
        stateMachine.sendEvent(msg);
    }

    private StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> build(BeerOrder beerOrder) {
        // Take the state machine from the cache Spring has, or create a new one.
        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine = stateMachineFactory.getStateMachine(beerOrder.getId());

        // Stop because we wont take into account its State, but we will set the current one we took from DB
        stateMachine.stopReactively();

        //We rehydrate the StateMachine with the state that comes from DB.
        stateMachine.getStateMachineAccessor().doWithAllRegions(sma -> {
            // Force the state machine to set this status.
            sma.resetStateMachine(new DefaultStateMachineContext<>(beerOrder.getOrderStatus(), null, null, null));
        });

        stateMachine.startReactively();
        return stateMachine;
    }
}
