package org.bazarteer.productservice.service;

import org.bazarteer.productservice.config.RabbitMQConfig;
import org.bazarteer.productservice.model.Product;
import org.bazarteer.productservice.model.ProductPublishedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQPublisher {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishProductPublished(Product product) {
        ProductPublishedMessage message = new ProductPublishedMessage(product.getId(), product.getName());
        rabbitTemplate.convertAndSend(RabbitMQConfig.PRODUCT_EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_PUBLISHED, message);
    }
}
