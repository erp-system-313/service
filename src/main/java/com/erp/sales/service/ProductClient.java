package com.erp.sales.service;

import com.erp.inventory.entity.Product;
import com.erp.sales.entity.SalesOrder;

public interface ProductClient {
    
    Product getProductById(Long id);
    
    void validateStock(SalesOrder order);
    
    void reduceStock(SalesOrder order);
}