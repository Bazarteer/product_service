package org.bazarteer.productservice.service;

import org.bazarteer.productservice.repository.ProductReposiotry;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.protobuf.ProtocolStringList;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import org.bazarteer.productservice.model.Product;
import org.bazarteer.productservice.proto.ProductServiceGrpc;
import org.bazarteer.productservice.proto.PublishRequest;
import org.bazarteer.productservice.proto.PublishResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@GrpcService
public class ProductService extends ProductServiceGrpc.ProductServiceImplBase {

    @Autowired
    private ProductReposiotry productReposiotry;

    @Override
    public void publish(PublishRequest req, StreamObserver<PublishResponse> responseObserver) {
        try {

            org.bazarteer.productservice.model.Condition condition = org.bazarteer.productservice.model.Condition.valueOf(req.getCondition().toString());
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

            PublishResponse res = PublishResponse.newBuilder().setProductId(product.getId()).build();
            responseObserver.onNext(res);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription("Intenal server error").withCause(e).asRuntimeException());
        }
    }

}
