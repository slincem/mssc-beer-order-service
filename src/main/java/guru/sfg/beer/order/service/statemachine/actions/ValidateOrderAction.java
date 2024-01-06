package guru.sfg.beer.order.service.statemachine.actions;

import guru.sfg.beer.order.service.config.RabbitMQConfig;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.events.ValidateBeerOrderRequest;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.services.BeerOrderManagerImpl;
import guru.sfg.beer.order.service.web.mappers.BeerOrderMapper;
import guru.sfg.beer.order.service.web.model.BeerOrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class ValidateOrderAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final RabbitTemplate rabbitTemplate;
    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderMapper beerOrderMapper;

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> stateContext) {

        String beerOrderId = stateContext.getMessage().getHeaders().get(BeerOrderManagerImpl.BEER_ORDER_ID_HEADER, String.class);
        BeerOrder beerOrder = beerOrderRepository.findOneById(UUID.fromString(beerOrderId));

        BeerOrderDto beerOrderDto = beerOrderMapper.beerOrderToDto(beerOrder);
        ValidateBeerOrderRequest validateBeerOrderRequest = ValidateBeerOrderRequest.builder().beerOrderDto(beerOrderDto).build();
        rabbitTemplate.convertAndSend(RabbitMQConfig.VALIDATE_ORDER_EXCHANGE, RabbitMQConfig.ROUTING_KEY, validateBeerOrderRequest);

        log.debug("Sent Validation request to queue for order id: " + beerOrderId);
    }
}
