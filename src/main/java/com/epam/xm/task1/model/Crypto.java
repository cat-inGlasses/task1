package com.epam.xm.task1.model;

import java.util.Objects;

/**
 * Storing data read from csv file
 */
public record Crypto(long timestamp, String symbol, double price) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Crypto crypto = (Crypto) o;
        return timestamp == crypto.timestamp && Double.compare(crypto.price, price) == 0 && Objects.equals(symbol, crypto.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, symbol, price);
    }
}
