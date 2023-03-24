package com.epam.xm.task1.model;

/**
 * Storing all metadata (calculated data) about specific crypto
 */
public record CryptoMetaData(String cryptoName, double oldestPrice,
                             double newestPrice, double minPrice,
                             double maxPrice, double normalizedRange) {
}
