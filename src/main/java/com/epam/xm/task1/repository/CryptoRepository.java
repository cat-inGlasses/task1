package com.epam.xm.task1.repository;

import com.epam.xm.task1.enums.CryptoSortingTypeEnum;
import com.epam.xm.task1.model.Crypto;
import com.epam.xm.task1.model.CryptoMetaData;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.ApplicationScope;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;

@Component
@ApplicationScope
public class CryptoRepository {

    private final Map<String, CryptoMetaData> cryptoMetaData = new HashMap<>();
    private final Map<LocalDate, Map<String, Set<Crypto>>> cryptosGroupedByDate = new HashMap<>();

    /**
     * Stores metadata for crypto in memory
     *
     * @param cryptoMetaData crypto's metadata
     */
    public void addMetadataForCrypto(CryptoMetaData cryptoMetaData) {
        this.cryptoMetaData.put(cryptoMetaData.cryptoName(), cryptoMetaData);
    }

    /**
     * Sorts cryptos with provided algorithm
     *
     * @param sorting provided algorithm for sorting of type {@link CryptoSortingTypeEnum}
     * @return sorted names of cryptos
     */
    public List<String> getSortedCryptosByPassedAlgo(CryptoSortingTypeEnum sorting) {
        return cryptoMetaData.values().stream()
                .sorted(sorting.comparator)
                .map(CryptoMetaData::cryptoName)
                .toList();
    }

    /**
     * Groups crypto data by date
     *
     * @param crypto {@link Crypto} data
     */
    public void addByDate(Crypto crypto) {
        LocalDate localDate = LocalDate.ofInstant(
                Instant.ofEpochMilli(crypto.timestamp()),
                TimeZone.getDefault().toZoneId());

        if (!cryptosGroupedByDate.containsKey(localDate)) {
            Set<Crypto> cryptoData = new HashSet<>();
            cryptoData.add(crypto);
            Map<String, Set<Crypto>> cryptoDataMap = new HashMap<>();
            cryptoDataMap.put(crypto.symbol(), cryptoData);
            cryptosGroupedByDate.put(localDate, cryptoDataMap);
        } else {
            Map<String, Set<Crypto>> cryptoDataMap = cryptosGroupedByDate.get(localDate);
            if (!cryptoDataMap.containsKey(crypto.symbol())) {
                Set<Crypto> cryptoData = new HashSet<>();
                cryptoData.add(crypto);
                cryptoDataMap.put(crypto.symbol(), cryptoData);
            } else {
                Set<Crypto> cryptoData = cryptoDataMap.get(crypto.symbol());
                cryptoData.add(crypto);
            }
        }
    }

    /**
     * Retrieves crypto with the highest normalized range on specific date
     *
     * @param specificDate provided date of type {@link LocalDate}
     * @return name of crypto with the highest specific dte
     */
    public String getHighestNormalizedRangesForDay(LocalDate specificDate) {
        String cryptoName = "";
        Map<String, Set<Crypto>> stringSetMap = cryptosGroupedByDate.get(specificDate);
        double highestNormalized = 0;
        if (!Objects.isNull(stringSetMap)) {
            for (Map.Entry<String, Set<Crypto>> e : stringSetMap.entrySet()) {
                if (!Objects.isNull(e.getValue())) {
                    double minPrice = e.getValue().stream().mapToDouble(Crypto::price).min().orElse(1d);
                    double maxPrice = e.getValue().stream().mapToDouble(Crypto::price).max().orElse(0d);
                    double normalized = (maxPrice - minPrice) / minPrice;
                    if (highestNormalized < normalized) {
                        cryptoName = e.getKey();
                        highestNormalized = normalized;
                    }
                }
            }
        }
        return cryptoName;
    }

    /**
     * Retrieving metadata for desired crypto
     *
     * @param cryptoName crypto's name
     * @return {@link CryptoMetaData} object with data for desired crypto
     */
    public CryptoMetaData getMetadataForCrypto(String cryptoName) {
        return cryptoMetaData.get(cryptoName);
    }
}
