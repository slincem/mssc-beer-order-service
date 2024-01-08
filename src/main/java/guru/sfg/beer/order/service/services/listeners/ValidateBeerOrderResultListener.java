package guru.sfg.beer.order.service.services.listeners;


import guru.sfg.beer.order.service.config.RabbitMQConfig;
import guru.sfg.beer.order.service.events.ValidateOrderResult;
import guru.sfg.beer.order.service.services.BeerOrderManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class ValidateBeerOrderResultListener {

    private final BeerOrderManager beerOrderManager;


    @Transactional
    @RabbitListener(queues = RabbitMQConfig.VALIDATE_ORDER_RESULT_QUEUE)
    public void listenBeerOrderValidationResult(ValidateOrderResult validateOrderResult) {
        final UUID beerOrderId = validateOrderResult.getBeerOrderId();

        log.debug("Validation Result for Beer Order Id: " + beerOrderId + " received");

        beerOrderManager.processValidationResult(beerOrderId, validateOrderResult.getIsValid());
    }
}
