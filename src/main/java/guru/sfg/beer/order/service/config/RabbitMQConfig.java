package guru.sfg.beer.order.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String BEER_ORDER_EXCHANGE = "beer-order-exchange";

    public static final String VALIDATE_BEER_ORDER_QUEUE = "validate-beer-order-queue";
    public static final String ALLOCATE_BEER_ORDER_QUEUE = "allocate-beer-order-queue";
    public static final String ALLOCATE_BEER_ORDER_RESULT_QUEUE = "allocate-beer-order-result-queue";
    public static final String ALLOCATE_BEER_ORDER_FAILURE_QUEUE = "allocate-beer-order-failure-queue";


    //Routing Keys for Order Events
    public static final String BEER_ORDER_VALIDATION_ROUTING_KEY = "beer-order.validate";
    public static final String BEER_ORDER_ALLOCATION_ROUTING_KEY = "beer-order.allocate";
    public static final String BEER_ORDER_ALLOCATION_RESULT_ROUTING_KEY = "beer-order.allocate.result";
    public static final String BEER_ORDER_ALLOCATION_FAILURE_ROUTING_KEY = "beer-order.allocate.failure";


    public static final String VALIDATE_BEER_ORDER_RESULT_EXCHANGE = "validate-beer-order-result-exchange";
    public static final String VALIDATE_BEER_ORDER_RESULT_QUEUE = "validate-beer-order-result-queue";
    public static final String VALIDATE_BEER_ORDER_RESULT_ROUTING_KEY = "validate-beer-order-result";


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


    /*
     * VALIDATE ORDER QUEUE CONFIG, WITH ITS OWN DIRECT EXCHANGE.
     */
    @Bean
    DirectExchange validateOrderResultExchange() {
        return new DirectExchange(VALIDATE_BEER_ORDER_RESULT_EXCHANGE);
    }

    @Bean
    Queue validateOrderResultQueue() {
        return new Queue(VALIDATE_BEER_ORDER_RESULT_QUEUE, false);
    }

    @Bean
    Binding validateOrderResultBinding(Queue validateOrderResultQueue, DirectExchange validateOrderResultExchange) {
        return BindingBuilder.bind(validateOrderResultQueue).to(validateOrderResultExchange).with(VALIDATE_BEER_ORDER_RESULT_ROUTING_KEY);
    }




    @Bean
    Queue allocateOrderResultQueue() {
        return new Queue(ALLOCATE_BEER_ORDER_RESULT_QUEUE, false);
    }

    @Bean
    Binding bindingAllocateOrderResult(Queue allocateOrderResultQueue, TopicExchange beerOrderExchange) {
        return BindingBuilder.bind(allocateOrderResultQueue).to(beerOrderExchange).with(BEER_ORDER_ALLOCATION_RESULT_ROUTING_KEY);
    }


    @Bean
    Queue allocateOrderFailureQueue() {
        return new Queue(ALLOCATE_BEER_ORDER_FAILURE_QUEUE, false);
    }

    @Bean
    Binding bindingAllocateOrderFailure(Queue allocateOrderFailureQueue, TopicExchange beerOrderExchange) {
        return BindingBuilder.bind(allocateOrderFailureQueue).to(beerOrderExchange).with(BEER_ORDER_ALLOCATION_FAILURE_ROUTING_KEY);
    }




    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper springInternalObjectMapper) {
        springInternalObjectMapper.findAndRegisterModules();
        return new Jackson2JsonMessageConverter(springInternalObjectMapper);
    }
}
