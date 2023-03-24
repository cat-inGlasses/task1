package com.epam.xm.task1.service;

import com.epam.xm.task1.enums.CryptoSortingTypeEnum;
import com.epam.xm.task1.exceptions.WrongCryptoNameException;
import com.epam.xm.task1.model.Crypto;
import com.epam.xm.task1.model.CryptoMetaData;
import com.epam.xm.task1.model.MetaDataAdapter;
import com.epam.xm.task1.repository.CryptoRepository;
import com.epam.xm.task1.utils.CryptoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CryptoService {

    public static final String ERR_HEADER_NAME = "ErrorMsg";
    private final CryptoRepository cryptoRepository;
    private final Set<String> allowedCryptos = Set.of("btc", "doge", "eth", "ltc", "xrp");

    public ResponseEntity<Void> processUploadedFile(MultipartFile file) {

        if (file.isEmpty()) {
            return badRequest("File is empty");
        }

        if (file.getOriginalFilename() == null || !file.getOriginalFilename().matches("[\\w\\d]+_values\\.csv")) {
            return badRequest("Wrong file name format. Right format is: CRYPTO_NAME_values.csv");
        }

        String cryptoName = file.getOriginalFilename().split("_")[0].toLowerCase();
        if (!allowedCryptos.contains(cryptoName.toLowerCase())) {
            return badRequest(String.format("Currently crypto %s is not allowed", cryptoName));
        }

        List<Crypto> cryptos;
        try {
            cryptos = getParsedListOfCryptoData(cryptoName, file);
            if (cryptos.isEmpty()) {
                return internalError("No data was retrieved from file. Please check the file.");
            }
            processParsedData(cryptoName, cryptos);
        } catch (WrongCryptoNameException e) {
            return badRequest(e.getMessage());
        } catch (NumberFormatException e) {
            return badRequest("Wrong number provided in file. Please, check the file for number formats");
        } catch (Exception e) {
            log.error("Error occurred while parsing file. " + e.getMessage(), e);
            return internalError("Error while processing uploaded file. Please, refer logs for more information");
        }

        return ResponseEntity.ok().build();
    }

    private void processParsedData(String cryptoName, List<Crypto> cryptos) {
        CryptoMetaData cryptoMetaData = CryptoUtils.calculateCryptoMetadata(cryptoName, cryptos);
        cryptoRepository.addMetadataForCrypto(cryptoMetaData);
        cryptos.forEach(cryptoRepository::addByDate);
    }

    /**
     * Returns list of {@link Crypto}s data for provided crypto file
     *
     * @param cryptoName crypto's name
     * @param file       {@link MultipartFile} uploaded file
     * @return list ot {@link Crypto}s for crypto data
     * @throws Exception in case of file parsing problems
     */
    private List<Crypto> getParsedListOfCryptoData(String cryptoName, MultipartFile file) throws Exception {
        List<Crypto> cryptos = new LinkedList<>();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(file.getInputStream()));
        String line;
        bufferedReader.readLine(); // skipping the first line
        while ((line = bufferedReader.readLine()) != null) {
            String[] values = line.split(",");
            long timestamp = Long.parseLong(values[0]);
            String parsedCryptoName = values[1].toLowerCase();
            double price = Double.parseDouble(values[2]);

            if (!cryptoName.equalsIgnoreCase(parsedCryptoName)) {
                throw new WrongCryptoNameException(String.format("Expected crypto %s, but got %s", cryptoName, parsedCryptoName));
            }

            cryptos.add(new Crypto(timestamp, cryptoName, price));
        }
        return cryptos;
    }

    /**
     * Returns cryptos sorted with provided algorithm type
     *
     * @param sortingType case ignored string version from one of {@link CryptoSortingTypeEnum}
     * @return {@link ResponseEntity} with sorted crypto names
     * <br> or with badRequest status and error message in header if bad sorting type provided
     * <br> or with noContent status and error message in header if there is no crypto to sort
     */
    public ResponseEntity<List<String>> getSortedCryptos(String sortingType) {
        List<String> sortedCryptos;
        try {
            CryptoSortingTypeEnum sorting = CryptoSortingTypeEnum.valueOf(sortingType.toUpperCase());
            sortedCryptos = cryptoRepository.getSortedCryptosByPassedAlgo(sorting);
        } catch (IllegalArgumentException e) {
            String errorMsg = "Wrong sorting type. Available sorting: " + Arrays.toString(CryptoSortingTypeEnum.values());
            return badRequest(errorMsg);
        }

        if (sortedCryptos.isEmpty()) {
            return noContent("There is no cryptos added");
        }

        return ResponseEntity.ok().body(sortedCryptos);
    }

    /**
     * Retrieved crypto with the highest normalized range in date
     *
     * @param date provided date in format YYYY-MM-DD
     * @return {@link ResponseEntity} with retrieved data or
     * <br> with noContent status and error message in header if there is no data for provided date
     * <br> with badRequest status ane error message in header if bad formatted date passed in
     */
    public ResponseEntity<String> getCryptoWithHighestNormalizedRangeForDate(String date) {
        try {
            LocalDate parsedDate = LocalDate.parse(date);
            String result = cryptoRepository.getHighestNormalizedRangesForDay(parsedDate);
            if (result.isEmpty()) {
                return noContent("No crypto data registered for date: " + date);
            }
            return ResponseEntity.ok().body(result);
        } catch (DateTimeParseException e) {
            return badRequest("Error while parsing provided time: " + date);
        }
    }

    /**
     * Retrieves crypto metadata.
     *
     * @param cryptoName crypto's name (case ignored)
     * @return {@link ResponseEntity} with retrieved data if data was found,
     * or with noContent status and error message in header.
     */
    public ResponseEntity<MetaDataAdapter> getMetadataForCrypto(String cryptoName) {
        CryptoMetaData metadata = cryptoRepository.getMetadataForCrypto(cryptoName.toLowerCase());
        if (Objects.isNull(metadata)) {
            return noContent("Nothing was found for crypto " + cryptoName);
        }
        return ResponseEntity.ok(new MetaDataAdapter(metadata));
    }

    private <T> ResponseEntity<T> badRequest(String errorMsg) {
        return ResponseEntity
                .badRequest()
                .header(ERR_HEADER_NAME, errorMsg)
                .build();
    }

    private <T> ResponseEntity<T> internalError(String errorMsg) {
        return ResponseEntity
                .internalServerError()
                .header(ERR_HEADER_NAME, errorMsg)
                .build();
    }

    private <T> ResponseEntity<T> noContent(String errorMsg) {
        return ResponseEntity
                .noContent()
                .header(ERR_HEADER_NAME, errorMsg)
                .build();
    }
}
