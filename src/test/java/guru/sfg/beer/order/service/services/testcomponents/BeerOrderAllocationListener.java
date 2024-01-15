package guru.sfg.beer.order.service.services.testcomponents;

import guru.sfg.beer.order.service.config.RabbitMQConfig;
import guru.sfg.beer.order.service.events.AllocateBeerOrderRequest;
import guru.sfg.beer.order.service.events.AllocateBeerOrderResult;
import guru.sfg.beer.order.service.web.model.BeerOrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderAllocationListener {

    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.ALLOCATE_BEER_ORDER_QUEUE)
    private void listenBeerOrderToAllocate(AllocateBeerOrderRequest allocateBeerOrderRequest) {
        BeerOrderDto beerOrderDto = allocateBeerOrderRequest.getBeerOrderDto();
        beerOrderDto.getBeerOrderLines().forEach(beerOrderLineDto -> {
            beerOrderLineDto.setQuantityAllocated(beerOrderLineDto.getOrderQuantity());
        });
        AllocateBeerOrderResult allocateBeerOrderResult = AllocateBeerOrderResult.builder()
                .beerOrderDto(allocateBeerOrderRequest.getBeerOrderDto())
                .pendingInventory(false)
                .allocationError(false)
                .build();

        log.debug("AllocationTest Result: " + allocateBeerOrderResult);
        rabbitTemplate.convertAndSend(RabbitMQConfig.BEER_ORDER_EXCHANGE, RabbitMQConfig.BEER_ORDER_ALLOCATION_RESULT_ROUTING_KEY, allocateBeerOrderResult);
    }
}
