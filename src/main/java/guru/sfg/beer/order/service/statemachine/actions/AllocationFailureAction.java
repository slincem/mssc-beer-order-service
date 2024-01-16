package guru.sfg.beer.order.service.statemachine.actions;

import guru.sfg.beer.order.service.config.RabbitMQConfig;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.events.AllocateBeerOrderFailureEvent;
import guru.sfg.beer.order.service.services.BeerOrderManagerImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class AllocationFailureAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> stateContext) {
        UUID beerOrderId = Objects.requireNonNull(stateContext.getMessage().getHeaders()
                .get(BeerOrderManagerImpl.BEER_ORDER_ID_HEADER, UUID.class));

        log.error("Compensating Transaction ... Allocation Failed: " + beerOrderId);

        Object response = rabbitTemplate.convertSendAndReceive(RabbitMQConfig.BEER_ORDER_EXCHANGE,
                RabbitMQConfig.BEER_ORDER_ALLOCATION_FAILURE_ROUTING_KEY,
                AllocateBeerOrderFailureEvent.builder().beerOrderId(beerOrderId).build());

        log.debug("Sent AllocationFailure Message To Queue for OrderID: " + beerOrderId);
    }
}
