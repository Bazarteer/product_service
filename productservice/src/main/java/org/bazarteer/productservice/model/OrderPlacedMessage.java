package org.bazarteer.productservice.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrderPlacedMessage implements Serializable{
    private Integer id;
    private String sellerId;
    private Integer productId;
}