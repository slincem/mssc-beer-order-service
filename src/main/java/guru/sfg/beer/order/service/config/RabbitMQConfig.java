package guru.sfg.beer.order.service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String VALIDATE_ORDER_EXCHANGE = "validate-order-exchange";
    public static final String VALIDATE_ORDER_QUEUE = "validate-order-queue";
    public static final String ROUTING_KEY = "validate-order";

    @Bean
    Queue validateOrderQueue() {
        return new Queue(VALIDATE_ORDER_QUEUE, false);
    }

    @Bean
    DirectExchange validateOrderExchange() {
        return new DirectExchange(VALIDATE_ORDER_EXCHANGE);
    }

    @Bean
    Binding bindingValidateOrder(Queue validateOrderQueue, DirectExchange validateOrderExchange) {
        return BindingBuilder.bind(validateOrderQueue).to(validateOrderExchange).with(ROUTING_KEY);
    }
}
