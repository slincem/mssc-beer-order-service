package guru.sfg.beer.order.service.services.testcomponents;

import guru.sfg.beer.order.service.config.RabbitMQConfig;
import guru.sfg.beer.order.service.events.ValidateBeerOrderRequest;
import guru.sfg.beer.order.service.events.ValidateBeerOrderResult;
import guru.sfg.beer.order.service.services.BeerOrderManagerImplIT;
import guru.sfg.beer.order.service.web.model.BeerOrderDto;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class BeerOrderValidationListener {

    private final RabbitTemplate rabbitTemplate;


    @Transactional
    @RabbitListener(queues = RabbitMQConfig.VALIDATE_BEER_ORDER_QUEUE)
    public void listenBeerOrderToValidate(ValidateBeerOrderRequest validateBeerOrderRequest) {
        boolean isValid = true;
        BeerOrderDto beerOrderDto = validateBeerOrderRequest.getBeerOrderDto();
        //Condition to test fail validation.
        if(BeerOrderManagerImplIT.CUSTOMER_REF_FAIL_VALIDATION.equals(beerOrderDto.getCustomerRef())) {
            isValid = false;
        }

        ValidateBeerOrderResult message = ValidateBeerOrderResult.builder().beerOrderId(beerOrderDto.getId()).isValid(isValid).build();
        rabbitTemplate.convertAndSend(RabbitMQConfig.VALIDATE_BEER_ORDER_RESULT_EXCHANGE, RabbitMQConfig.VALIDATE_BEER_ORDER_RESULT_ROUTING_KEY, message);
    }
}
