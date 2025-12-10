package org.bazarteer.productservice.service;

import org.bazarteer.productservice.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.protobuf.ProtocolStringList;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.persistence.EntityNotFoundException;
import net.devh.boot.grpc.server.service.GrpcService;

import org.bazarteer.productservice.model.OrderPlacedMessage;
import org.bazarteer.productservice.model.Product;
import org.bazarteer.productservice.model.ProductPublishedMessage;
import org.bazarteer.productservice.proto.ProductServiceGrpc;
import org.bazarteer.productservice.proto.PublishRequest;
import org.bazarteer.productservice.proto.PublishResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@GrpcService
public class ProductService extends ProductServiceGrpc.ProductServiceImplBase {

    @Autowired
    private RabbitMQPublisher publisher;

    @Autowired
    private ProductRepository productReposiotry;

    @Override
    public void publish(PublishRequest req, StreamObserver<PublishResponse> responseObserver) {
        try {

            org.bazarteer.productservice.model.Condition condition = org.bazarteer.productservice.model.Condition
                    .valueOf(req.getCondition().toString());
            ProtocolStringList protoList = req.getContentUrlsList();
            List<String> content = new ArrayList<>(protoList);

            Product product = new Product();
            product.setName(req.getName());
            product.setDescription(req.getDescription());
            product.setCondition(condition);
            product.setOwnerid(req.getUserId());
            product.setOwner_username(req.getUsername());
            product.setLocation(req.getLocation());
            product.setPrice(req.getPrice());
            product.setStock(req.getStock());
            product.setContent(content);
            product.setCreatedAt(LocalDateTime.now());

            productReposiotry.save(product);

            publisher.publishProductPublished(product);

            PublishResponse res = PublishResponse.newBuilder().setProductId(product.getId()).build();
            responseObserver.onNext(res);
            responseObserver.onCompleted();
        } catch (Exception e) {
            System.out.println(e);
            responseObserver.onError(
                    Status.INTERNAL.withDescription("Intenal server error").withCause(e).asRuntimeException());
        }
    }

    public void handleOrderPlaced(OrderPlacedMessage message) {
        try{ 
            Optional<Product> optionalProduct = productReposiotry.findById(message.getProductId());
            if (optionalProduct.isEmpty()){
                return;
            }

            Product product = optionalProduct.get();
            int stock = product.getStock();
            if (stock > 1){
                product.setStock(stock - 1);
                productReposiotry.save(product);
            }
            else {
                productReposiotry.deleteById(message.getProductId());
            } 
        } catch (Exception e) {
            System.out.println("Exception for product : " + message.getProductId() + e);
        }
    }

}
