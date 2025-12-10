package org.bazarteer.productservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    public static final String PRODUCT_EXCHANGE_NAME = "product-exchange";
    public static final String ORDER_EXCHANGE_NAME = "order-exchange";
    public static final String QUEUE_PUBLISHED = "product-published-queue";
    public static final String QUEUE_ORDER_PLACED = "order-placed-queue-product";
    public static final String ROUTING_KEY_PUBLISHED = "product.published";
    public static final String ROUTING_KEY_ORDER_PLACED = "order.placed";


    @Bean
    TopicExchange productExchange() {
        return new TopicExchange(PRODUCT_EXCHANGE_NAME);
    }

    @Bean
    TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE_NAME);
    }

    @Bean
    Queue orderPlacedQueueProduct(){
        return new Queue(QUEUE_ORDER_PLACED);
    }

    //za vec exchange, queue in posledično bindings samo definiraj nove beane

    @Bean
    Binding orderPlacedBinding(Queue orderPlacedQueueProduct, TopicExchange orderExchange){
        return BindingBuilder.bind(orderPlacedQueueProduct).to(orderExchange).with(ROUTING_KEY_ORDER_PLACED);
    }

    @Bean
    Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean 
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
