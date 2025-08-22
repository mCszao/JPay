package com.challenge.JPay.interfaces;

import com.challenge.JPay.model.Category;

import java.math.BigDecimal;

public interface CategoryTotals {
    Category getCategory();
    BigDecimal getTotalAmount();
}