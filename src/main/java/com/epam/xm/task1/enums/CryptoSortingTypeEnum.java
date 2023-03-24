package com.epam.xm.task1.enums;

import com.epam.xm.task1.model.CryptoMetaData;

import java.util.Comparator;

/**
 * Stored sorting comparators in predefined sorting types
 */
public enum CryptoSortingTypeEnum {

    /**
     * Sorting cryptos by normalized range in descending order
     */
    NORMALIZED_DESC(
            (cmd1, cmd2) -> -1 * Double.compare(cmd1.normalizedRange(), cmd2.normalizedRange())
    );

    public final Comparator<CryptoMetaData> comparator;

    CryptoSortingTypeEnum(Comparator<CryptoMetaData> comparator) {
        this.comparator = comparator;
    }
}
