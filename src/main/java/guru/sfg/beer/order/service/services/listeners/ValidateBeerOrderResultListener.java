package guru.sfg.beer.order.service.services.listeners;


import guru.sfg.beer.order.service.config.RabbitMQConfig;
import guru.sfg.beer.order.service.events.ValidateBeerOrderResult;
import guru.sfg.beer.order.service.services.BeerOrderManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class ValidateBeerOrderResultListener {

    private final BeerOrderManager beerOrderManager;


    @Transactional
    @RabbitListener(queues = RabbitMQConfig.VALIDATE_BEER_ORDER_RESULT_QUEUE)
    public void listenBeerOrderValidationResult(ValidateBeerOrderResult validateBeerOrderResult) {
        final UUID beerOrderId = validateBeerOrderResult.getBeerOrderId();

        log.debug("Validation Result for Beer Order Id: " + beerOrderId + " received");

        beerOrderManager.processValidationResult(beerOrderId, validateBeerOrderResult.getIsValid());
    }
}
