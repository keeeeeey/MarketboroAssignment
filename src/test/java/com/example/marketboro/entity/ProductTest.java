package com.example.marketboro.entity;

import com.example.marketboro.dto.request.ProductRequestDto;
import com.example.marketboro.dto.request.ProductRequestDto.CreateProduct;
import com.example.marketboro.dto.request.UserRequestDto;
import com.example.marketboro.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProductTest {

    @Autowired
    EntityManager em;

    private String productname;
    private String productinfo;
    private int productprice;
    private int leftproduct;
    private ProductEnum productEnum;

    @BeforeEach
    public void setUp() {
        productname = "당근";
        productinfo = "신선한 당근";
        productprice = 5000;
        leftproduct = 100;
        productEnum = ProductEnum.SELLING;
    }

    @Test
    @DisplayName("정상 케이스")
    public void createProduct() {

        // given
        CreateProduct requestDto = new CreateProduct(
                productname,
                productinfo,
                productprice,
                leftproduct
        );

        //when
        Product product = Product.builder()
                .productname(requestDto.getProductname())
                .productinfo(requestDto.getProductinfo())
                .productprice(requestDto.getProductprice())
                .leftproduct(requestDto.getLeftproduct())
                .productEnum(productEnum)
                .build();

        //then
        assertEquals(productname, product.getProductname());
        assertEquals(productinfo, product.getProductinfo());
        assertEquals(productprice, product.getProductprice());
        assertEquals(leftproduct, product.getLeftproduct());
        assertEquals(productEnum, product.getProductEnum());
    }
}
