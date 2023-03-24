package com.epam.xm.task1.model;

/**
 * Adapter for retrieving required data for endpoint
 */
public final class MetaDataAdapter {

    private final CryptoMetaData data;

    public MetaDataAdapter(CryptoMetaData data) {
        this.data = data;
    }

    public double getOldestPrice() {
        return data.oldestPrice();
    }

    public double getNewestPrice() {
        return data.newestPrice();
    }

    public double getMinPrice() {
        return data.minPrice();
    }

    public double getMaxPrice() {
        return data.maxPrice();
    }
}
