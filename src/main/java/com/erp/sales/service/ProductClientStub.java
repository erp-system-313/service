package com.erp.sales.service;

import com.erp.common.exception.ResourceNotFoundException;
import com.erp.entity.Product;
import com.erp.sales.entity.SalesOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class ProductClientStub implements ProductClient {

    @Override
    public Product getProductById(Long id) {
        log.warn("ProductClientStub: getProductById called with id={} - returning stub", id);
        
        Product product = new Product();
        product.setId(id);
        product.setSku("SKU-" + id);
        product.setName("Product " + id);
        product.setUnitPrice(BigDecimal.valueOf(99.99));
        product.setIsActive(true);
        
        return product;
    }

    @Override
    public void validateStock(SalesOrder order) {
        log.warn("ProductClientStub: validateStock called - skipping validation in stub mode");
    }

    @Override
    public void reduceStock(SalesOrder order) {
        log.warn("ProductClientStub: reduceStock called - skipping stock reduction in stub mode");
    }
}