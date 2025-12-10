package org.bazarteer.productservice.service;

import org.bazarteer.productservice.config.RabbitMQConfig;
import org.bazarteer.productservice.model.OrderPlacedMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQListener {
    
    @Autowired
    private ProductService productService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ORDER_PLACED)
    public void consumeOrderPlaced(OrderPlacedMessage message) {
        productService.handleOrderPlaced(message);
    }
}
