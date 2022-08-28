package com.nphase.service;

import com.nphase.entity.Product;
import com.nphase.entity.ShoppingCart;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ShoppingCartService {

    private final double HUNDRED = 100;
    private double discountPercentage;
    private int discountOnMinItems;

    ShoppingCartService(){
        this.discountOnMinItems = 3;
        this.discountPercentage = 10;
    }

    public BigDecimal calculateTotalPrice(ShoppingCart shoppingCart) {

        Map<String, Integer> productsQuantityByGroup = shoppingCart.getProducts().stream()
                .filter(product->!Objects.isNull(product.getCategory()))
                .collect(Collectors.groupingBy(Product::getCategory,
                        Collectors.summingInt(Product::getQuantity)));

        BigDecimal result = shoppingCart.getProducts()
                .stream()
                .map(executePriceCalculatorFunction(productsQuantityByGroup))
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
        return BigDecimal.valueOf(Double.parseDouble(result.toPlainString()));
    }

    private Function<Product, BigDecimal> executePriceCalculatorFunction(Map<String, Integer> productsQuantityByGroup) {
        return product -> {
            BigDecimal totalProductWisePrice = product.getPricePerUnit().multiply(BigDecimal.valueOf(product.getQuantity()));
            if ((productsQuantityByGroup.getOrDefault(product.getCategory(), product.getQuantity()) > discountOnMinItems)) {
                return totalProductWisePrice
                        .subtract(totalProductWisePrice
                                .multiply(new BigDecimal(discountPercentage / HUNDRED)));
            }
            return totalProductWisePrice;
        };
    }

    public void setDiscountOnMinItems(int discountOnMinItems) {
        this.discountOnMinItems = discountOnMinItems;
    }

    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }
}
