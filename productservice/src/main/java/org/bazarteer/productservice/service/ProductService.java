package org.bazarteer.productservice.service;

import org.bazarteer.productservice.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import com.google.protobuf.ProtocolStringList;
import com.rabbitmq.client.GetResponse;

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
import org.bazarteer.productservice.proto.GetProductsRequest;
import org.bazarteer.productservice.proto.GetProductsResponse;
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

    private org.bazarteer.productservice.proto.Product mapToProto(Product product) {
        return org.bazarteer.productservice.proto.Product.newBuilder()
                .setId(product.getId().toString())
                .setName(product.getName() != null ? product.getName() : "")
                .setDescription(product.getDescription() != null ? product.getDescription() : "")
                .setCondition(org.bazarteer.productservice.proto.Condition.valueOf(product.getCondition().name()))
                .setOwnerId(product.getOwnerid() != null ? product.getOwnerid() : "")
                .setOwnerUsername(product.getOwner_username() != null ? product.getOwner_username() : "")
                .setLocation(product.getLocation() != null ? product.getLocation() : "")
                .setPrice(product.getPrice() != null ? product.getPrice() : 0.0)
                .setStock(product.getStock() != null ? product.getStock() : 0)
                .addAllContent(product.getContent() != null ? product.getContent() : List.of())
                .setCreatedAt(product.getCreatedAt() != null ? product.getCreatedAt().toString() : "")
                .build();
    }

    @Override
    public void getRecommendedProducts(GetProductsRequest req, StreamObserver<GetProductsResponse> responseObserver) {
        try {
            System.out.println("Received getRecommendedProducts request");

            List<Product> products = productReposiotry.findTop10ByOrderByCreatedAtDescWithContent();
            System.out.println("Products found: " + products.size());

            GetProductsResponse res = GetProductsResponse.newBuilder().addAllProducts(
                    products.stream()
                            .map(this::mapToProto)
                            .toList())
                    .build();

            responseObserver.onNext(res);
            responseObserver.onCompleted();
        } catch (Exception e) {
            System.err.println("Exception in getRecommendedProducts: " + e.getMessage());
            e.printStackTrace();
            responseObserver.onError(
                    Status.INTERNAL.withDescription("Intenal server error").withCause(e).asRuntimeException());
        }
    }

    @Override
    public void getProductsByOwner(GetProductsRequest req, StreamObserver<GetProductsResponse> responseObserver) {
        try {
            List<Product> products = productReposiotry.findByOwnerid(req.getUserId());

            GetProductsResponse res = GetProductsResponse.newBuilder().addAllProducts(
                    products.stream()
                            .map(this::mapToProto)
                            .toList())
                    .build();

            responseObserver.onNext(res);
            responseObserver.onCompleted();
        } catch (Exception e) {
            System.err.println("Exception in getProductsByOwner: " + e.getMessage());
            e.printStackTrace();
            responseObserver.onError(
                    Status.INTERNAL.withDescription("Intenal server error").withCause(e).asRuntimeException());
        }
    }

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
        try {
            Optional<Product> optionalProduct = productReposiotry.findById(message.getProductId());
            if (optionalProduct.isEmpty()) {
                return;
            }

            Product product = optionalProduct.get();
            int stock = product.getStock();
            if (stock > 1) {
                product.setStock(stock - 1);
                productReposiotry.save(product);
            } else {
                productReposiotry.deleteById(message.getProductId());
            }
        } catch (Exception e) {
            System.out.println("Exception for product : " + message.getProductId() + e);
        }
    }

}
