package com.epam.xm.task1.controller;

import com.epam.xm.task1.enums.CryptoSortingTypeEnum;
import com.epam.xm.task1.model.MetaDataAdapter;
import com.epam.xm.task1.service.CryptoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/cryptos/")
@RequiredArgsConstructor
public class CryptoController {

    private final CryptoService cryptoService;

    /**
     * Uploads file content to server
     *
     * @param file contains uploaded file
     * @return error in header "ErrorMsg" in case of any error occurred during data saving process
     */
    @PostMapping("/upload")
    public ResponseEntity<Void> uploadFile(@RequestParam("file") MultipartFile file) {
        return cryptoService.processUploadedFile(file);
    }

    /**
     * Returns names of cryptos in sorted order, which were uploaded earlier
     *
     * @param sortingType {@link CryptoSortingTypeEnum} string representation (case ignored)
     * @return List of sorted crypto names
     */
    @GetMapping("/sorted/{sortingType}")
    public ResponseEntity<List<String>> getSortedListOfCryptos(@PathVariable("sortingType") String sortingType) {
        return cryptoService.getSortedCryptos(sortingType);
    }

    /**
     * Returns metadata for desired crypto, such as min, max, oldest and newest prices.
     *
     * @param cryptoName desired crypto name
     * @return calculated data about crypto
     */
    @GetMapping("/metadata/{cryptoName}")
    public ResponseEntity<MetaDataAdapter> getMetadataForCrypto(@PathVariable("cryptoName") String cryptoName) {
        return cryptoService.getMetadataForCrypto(cryptoName);
    }

    /**
     * Determines crypto with the highest normalized range in specific day
     *
     * @param day in format YYYY-MM-DD
     * @return crypto name
     */
    @GetMapping("/highestNormalizedForDay/{day}")
    public ResponseEntity<String> getHighestNormalizedCryptoForDay(@PathVariable("day") String day) {
        return cryptoService.getCryptoWithHighestNormalizedRangeForDate(day);
    }
}
