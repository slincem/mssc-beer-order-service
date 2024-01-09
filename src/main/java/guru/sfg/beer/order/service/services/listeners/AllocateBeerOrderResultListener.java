package guru.sfg.beer.order.service.services.listeners;

import guru.sfg.beer.order.service.config.RabbitMQConfig;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.events.AllocateBeerOrderResult;
import guru.sfg.beer.order.service.services.BeerOrderManager;
import guru.sfg.beer.order.service.web.mappers.BeerOrderMapper;
import guru.sfg.beer.order.service.web.model.BeerOrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class AllocateBeerOrderResultListener {

    private final BeerOrderMapper beerOrderMapper;
    private final BeerOrderManager beerOrderManager;

    @Transactional
    @RabbitListener(queues = RabbitMQConfig.ALLOCATE_BEER_ORDER_RESULT_QUEUE)
    public void listenBeerOrderAllocationResult(AllocateBeerOrderResult allocateBeerOrderResult) {

        final BeerOrderDto beerOrderDto = allocateBeerOrderResult.getBeerOrderDto();

        if(allocateBeerOrderResult.getAllocationError()) {
            beerOrderManager.beerOrderAllocationFailed(beerOrderDto);
        } else if(allocateBeerOrderResult.getPendingInventory()) {
            beerOrderManager.beerOrderAllocationPendingInventory(beerOrderDto);
        } else {
            beerOrderManager.beerOrderAllocationPassed(beerOrderDto);
        }
    }
}
