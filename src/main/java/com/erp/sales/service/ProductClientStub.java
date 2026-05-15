package com.erp.sales.service;

import com.erp.common.exception.ResourceNotFoundException;
import com.erp.inventory.entity.Product;
import com.erp.inventory.repository.ProductRepository;
import com.erp.sales.entity.SalesOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductClientStub implements ProductClient {

    private final ProductRepository productRepository;

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    @Override
    public void validateStock(SalesOrder order) {
        if (order.getLines() != null) {
            for (var line : order.getLines()) {
                Product product = line.getProduct();
                if (product != null && product.getCurrentStock() < line.getQuantity()) {
                    throw new com.erp.common.exception.BusinessException("STOCK_001",
                            "Insufficient stock for product: " + product.getName());
                }
            }
        }
    }

    @Override
    public void reduceStock(SalesOrder order) {
        if (order.getLines() != null) {
            for (var line : order.getLines()) {
                Product product = line.getProduct();
                if (product != null) {
                    product.setCurrentStock(product.getCurrentStock() - line.getQuantity());
                    productRepository.save(product);
                    log.debug("Reduced stock for product {}: {} remaining",
                            product.getId(), product.getCurrentStock());
                }
            }
        }
    }
}
