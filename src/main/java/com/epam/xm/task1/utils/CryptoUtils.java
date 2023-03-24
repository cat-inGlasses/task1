package com.epam.xm.task1.utils;

import com.epam.xm.task1.model.Crypto;
import com.epam.xm.task1.model.CryptoMetaData;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Utils methods for processing crypto data
 */
public final class CryptoUtils {

    private CryptoUtils() {
        throw new IllegalStateException();
    }

    /**
     * Calculates metadata (min, max, old, new prices) for provided crypto's data
     *
     * @param cryptoName name of crypto
     * @param cryptoData list of crypto's data {@link Crypto}
     * @return {@link CryptoMetaData} calculated from provided data
     *
     * @throws NullPointerException if {@param cryptoData} is null
     */
    @SneakyThrows
    public static CryptoMetaData calculateCryptoMetadata(String cryptoName, List<Crypto> cryptoData) {

        Objects.requireNonNull(cryptoData);

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Future<Double> minPriceTask = executorService.submit(() -> getMinPrice(cryptoData));
        Future<Double> maxPriceTask = executorService.submit(() -> getMaxPrice(cryptoData));

        ArrayList<Crypto> cryptoArrList = new ArrayList<>(cryptoData);
        cryptoArrList.sort(Comparator.comparingLong(Crypto::timestamp));
        double oldestPrice = cryptoArrList.get(0).price();
        double newestPrice = cryptoArrList.get(cryptoArrList.size() - 1).price();
        double minPrice = minPriceTask.get();
        double maxPrice = maxPriceTask.get();
        double normalizedRange = (maxPrice - minPrice) / minPrice;

        return new CryptoMetaData(cryptoName, oldestPrice, newestPrice, minPrice, maxPrice, normalizedRange);
    }

    private static double getMinPrice(List<Crypto> cryptoData) {
        return cryptoData.stream().mapToDouble(Crypto::price).min().orElse(1d);
    }

    private static double getMaxPrice(List<Crypto> cryptoData) {
        return cryptoData.stream().mapToDouble(Crypto::price).max().orElse(0d);
    }
}
