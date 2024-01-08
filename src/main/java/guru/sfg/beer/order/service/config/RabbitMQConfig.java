package guru.sfg.beer.order.service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String VALIDATE_ORDER_EXCHANGE = "validate-order-exchange";
    public static final String VALIDATE_ORDER_QUEUE = "validate-order-queue";
    public static final String ROUTING_KEY = "validate-order";

    // Esta cola fue creada en Beer Service, por lo que aqui solo necesito saber su nombre.
    public static final String VALIDATE_ORDER_RESULT_QUEUE = "validate-order-result-queue";


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
