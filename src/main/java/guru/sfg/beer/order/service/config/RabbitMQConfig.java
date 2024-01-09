package guru.sfg.beer.order.service.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String BEER_ORDER_EXCHANGE = "beer-order-exchange";

    public static final String VALIDATE_BEER_ORDER_RESULT_QUEUE = "validate-beer-order-result-queue";
    public static final String VALIDATE_BEER_ORDER_QUEUE = "validate-beer-order-queue";
    public static final String ALLOCATE_BEER_ORDER_QUEUE = "allocate-beer-order-queue";
    public static final String ALLOCATE_BEER_ORDER_RESULT_QUEUE = "allocate-beer-oder-result-queue";


    //Routing Keys for Order Events
    public static final String BEER_ORDER_VALIDATION_ROUTING_KEY = "beer-order.validate";
    public static final String BEER_ORDER_ALLOCATION_ROUTING_KEY = "beer-order.allocate";


    @Bean
    TopicExchange beerOrderExchange() {
        return new TopicExchange(BEER_ORDER_EXCHANGE);
    }

    @Bean
    Queue validateOrderQueue() {
        return new Queue(VALIDATE_BEER_ORDER_QUEUE, false);
    }
    @Bean
    Binding bindingValidateOrder(Queue validateOrderQueue, TopicExchange beerOrderExchange) {
        return BindingBuilder.bind(validateOrderQueue).to(beerOrderExchange).with(BEER_ORDER_VALIDATION_ROUTING_KEY);
    }

    @Bean
    Queue allocateOrderQueue() {
        return new Queue(ALLOCATE_BEER_ORDER_QUEUE, false);
    }

    @Bean
    Binding bindingAllocateOrder(Queue allocateOrderQueue, TopicExchange beerOrderExchange) {
        return BindingBuilder.bind(allocateOrderQueue).to(beerOrderExchange).with(BEER_ORDER_ALLOCATION_ROUTING_KEY);
    }


    @Bean
    Queue validateOrderResultQueue() {
        return new Queue(VALIDATE_BEER_ORDER_RESULT_QUEUE, false);
    }
    @Bean
    Queue allocateOrderResultQueue() {
        return new Queue(ALLOCATE_BEER_ORDER_RESULT_QUEUE, false);
    }
}
