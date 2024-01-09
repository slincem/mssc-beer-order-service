package guru.sfg.beer.order.service.statemachine.actions;

import guru.sfg.beer.order.service.config.RabbitMQConfig;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.events.AllocateBeerOrderRequest;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.services.BeerOrderManagerImpl;
import guru.sfg.beer.order.service.web.mappers.BeerOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class AllocateBeerOrderAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final RabbitTemplate rabbitTemplate;
    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderMapper beerOrderMapper;

    @Transactional
    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> stateContext) {

        String beerOrderId = stateContext.getMessage().getHeaders().get(BeerOrderManagerImpl.BEER_ORDER_ID_HEADER,
                String.class);
        BeerOrder beerOrder = beerOrderRepository.findOneById(UUID.fromString(beerOrderId));

        AllocateBeerOrderRequest allocationRequest = AllocateBeerOrderRequest.builder()
                .beerOrderDto(beerOrderMapper.beerOrderToDto(beerOrder))
                .build();

        rabbitTemplate.convertAndSend(RabbitMQConfig.BEER_ORDER_ALLOCATION_ROUTING_KEY,
                RabbitMQConfig.BEER_ORDER_EXCHANGE, allocationRequest);

        log.debug("Sent Allocation request to queue for order id: " + beerOrderId);
    }
}
