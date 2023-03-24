package com.epam.xm.task1.service;

import com.epam.xm.task1.enums.CryptoSortingTypeEnum;
import com.epam.xm.task1.model.CryptoMetaData;
import com.epam.xm.task1.model.MetaDataAdapter;
import com.epam.xm.task1.repository.CryptoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class CryptoServiceTest {

    public final static String ERR_HEADER_NAME = "ErrorMsg";

    @MockBean
    private CryptoRepository cryptoRepositoryMock;

    @Autowired
    private CryptoService cryptoService;

    @Test
    void processUploadedFile_ShouldReturnBadRequest_WhenEmptyFilePassed() {
        // Given
        MultipartFile multipartFileMock = Mockito.mock(MultipartFile.class);
        Mockito.when(multipartFileMock.isEmpty()).thenReturn(true);
        String errMsg = "File is empty";

        // When
        ResponseEntity<Void> responseEntity = cryptoService.processUploadedFile(multipartFileMock);

        // Then
        assertAll(
                () -> assertEquals(400, responseEntity.getStatusCode().value()),
                () -> assertNull(responseEntity.getBody()),
                () -> assertEquals(errMsg, Objects.requireNonNull(responseEntity.getHeaders().get(ERR_HEADER_NAME)).get(0))
        );
    }

    @Test
    void processUploadedFile_ShouldReturnBadRequest_WhenNoOriginalFileNameFound() {
        // Given
        MultipartFile multipartFileMock = Mockito.mock(MultipartFile.class);
        Mockito.when(multipartFileMock.getOriginalFilename()).thenReturn(null);
        String errMsg = "Wrong file name format. Right format is: CRYPTO_NAME_values.csv";

        // When
        ResponseEntity<Void> responseEntity = cryptoService.processUploadedFile(multipartFileMock);

        // Then
        assertAll(
                () -> assertEquals(400, responseEntity.getStatusCode().value()),
                () -> assertNull(responseEntity.getBody()),
                () -> assertEquals(errMsg, Objects.requireNonNull(responseEntity.getHeaders().get(ERR_HEADER_NAME)).get(0))
        );
    }

    @Test
    void processUploadedFile_ShouldReturnBadRequest_WhenWrongOriginalFileNameFound() {
        // Given
        MultipartFile multipartFileMock = Mockito.mock(MultipartFile.class);
        String wrongOriginalFileName = "abraKadabra";
        Mockito.when(multipartFileMock.getOriginalFilename()).thenReturn(wrongOriginalFileName);
        String errMsg = "Wrong file name format. Right format is: CRYPTO_NAME_values.csv";

        // When
        ResponseEntity<Void> responseEntity = cryptoService.processUploadedFile(multipartFileMock);

        // Then
        assertAll(
                () -> assertEquals(400, responseEntity.getStatusCode().value()),
                () -> assertNull(responseEntity.getBody()),
                () -> assertEquals(errMsg, Objects.requireNonNull(responseEntity.getHeaders().get(ERR_HEADER_NAME)).get(0))
        );
    }

    @Test
    void processUploadedFile_ShouldReturnBadRequest_WhenNotAllowedCryptoUploading() {
        // Given
        MultipartFile multipartFileMock = Mockito.mock(MultipartFile.class);
        String wrongOriginalFileName = "abraKadabra_values.csv";
        Mockito.when(multipartFileMock.getOriginalFilename()).thenReturn(wrongOriginalFileName);
        String cryptoName = wrongOriginalFileName.split("_")[0].toLowerCase();
        String errMsg = String.format("Currently crypto %s is not allowed", cryptoName);

        // When
        ResponseEntity<Void> responseEntity = cryptoService.processUploadedFile(multipartFileMock);

        // Then
        assertAll(
                () -> assertEquals(400, responseEntity.getStatusCode().value()),
                () -> assertNull(responseEntity.getBody()),
                () -> assertEquals(errMsg, Objects.requireNonNull(responseEntity.getHeaders().get(ERR_HEADER_NAME)).get(0))
        );
    }

    @Test
    void processUploadedFile_ShouldReturnBadRequest_WhenWrongCryptoNameInFile() throws IOException {
        // Given
        MultipartFile multipartFileMock = Mockito.mock(MultipartFile.class);
        String fileName = "BTC_values.csv";
        String fileContent = "timestamp,symbol,price\n1641009600000,ETH,46813.21\n";
        InputStream targetStream = new ByteArrayInputStream(fileContent.getBytes());
        Mockito.when(multipartFileMock.getOriginalFilename()).thenReturn(fileName);
        Mockito.when(multipartFileMock.getInputStream()).thenReturn(targetStream);
        String cryptoName = fileName.split("_")[0].toLowerCase();
        String errMsg = String.format("Expected crypto %s, but got %s", cryptoName, "eth");

        // When
        ResponseEntity<Void> responseEntity = cryptoService.processUploadedFile(multipartFileMock);

        // Then
        assertAll(
                () -> assertEquals(400, responseEntity.getStatusCode().value()),
                () -> assertNull(responseEntity.getBody()),
                () -> assertEquals(errMsg, Objects.requireNonNull(responseEntity.getHeaders().get(ERR_HEADER_NAME)).get(0))
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"16410960000o,BTC,46813.21\n", "1641009600000,BTC,46i13.21\n"})
    void processUploadedFile_ShouldReturnBadRequest_WhenWrongDateTime(String secondRow) throws IOException {
        // Given
        MultipartFile multipartFileMock = Mockito.mock(MultipartFile.class);
        String fileName = "BTC_values.csv";
        String dataStream = String.format("timestamp,symbol,price\n%s\n", secondRow);
        InputStream targetStream = new ByteArrayInputStream(dataStream.getBytes());
        Mockito.when(multipartFileMock.getOriginalFilename()).thenReturn(fileName);
        Mockito.when(multipartFileMock.getInputStream()).thenReturn(targetStream);
        String errMsg = "Wrong number provided in file. Please, check the file for number formats";

        // When
        ResponseEntity<Void> responseEntity = cryptoService.processUploadedFile(multipartFileMock);

        // Then
        assertAll(
                () -> assertEquals(400, responseEntity.getStatusCode().value()),
                () -> assertNull(responseEntity.getBody()),
                () -> assertEquals(errMsg, Objects.requireNonNull(responseEntity.getHeaders().get(ERR_HEADER_NAME)).get(0))
        );
    }

    @Test
    void processUploadedFile_ShouldReturnBadRequest_WhenWrongDateTime() throws IOException {
        // Given
        MultipartFile multipartFileMock = Mockito.mock(MultipartFile.class);
        String fileName = "BTC_values.csv";
        String dataStream = "timestamp,symbol,price\n1641009600000,BTC,46813.21\n";
        Mockito.when(multipartFileMock.getOriginalFilename()).thenReturn(fileName);
        Mockito.when(multipartFileMock.getInputStream()).thenThrow(RuntimeException.class);
        String errMsg = "Error while processing uploaded file. Please, refer logs for more information";

        // When
        ResponseEntity<Void> responseEntity = cryptoService.processUploadedFile(multipartFileMock);

        // Then
        assertAll(
                () -> assertEquals(500, responseEntity.getStatusCode().value()),
                () -> assertNull(responseEntity.getBody()),
                () -> assertEquals(errMsg, Objects.requireNonNull(responseEntity.getHeaders().get(ERR_HEADER_NAME)).get(0))
        );
    }

    // TODO called cryptoRepository.addMetadataForCrypto
    // TODO called cryptoRepository::addByDate
    @Test
    void processUploadedFile_ShouldProcessFile() throws IOException {
        // Given
        MultipartFile multipartFileMock = Mockito.mock(MultipartFile.class);
        String fileName = "BTC_values.csv";
        String fileContent = "timestamp,symbol,price\n1641009600000,BTC,46813.21\n";
        InputStream dataStream = new ByteArrayInputStream(fileContent.getBytes());
        Mockito.when(multipartFileMock.getOriginalFilename()).thenReturn(fileName);
        Mockito.when(multipartFileMock.getInputStream()).thenReturn(dataStream);
        String errMsg = "Error while processing uploaded file. Please, refer logs for more information";

        // When
        ResponseEntity<Void> responseEntity = cryptoService.processUploadedFile(multipartFileMock);

        // Then
        Mockito.verify(cryptoRepositoryMock).addMetadataForCrypto(Mockito.any());
        Mockito.verify(cryptoRepositoryMock).addByDate(Mockito.any());

        assertAll(
                () -> assertEquals(200, responseEntity.getStatusCode().value()),
                () -> assertNull(responseEntity.getBody())
        );
    }

    @Test
    void getSortedCryptos_ShouldReturnBadRequest_WhenBadSortingTypeReceived() {
        // Given
        String wrongSortingType = "abra-kadbra";
        assert !Arrays.stream(CryptoSortingTypeEnum.values()).map(String::valueOf).toList().contains(wrongSortingType);
        String errMsg = "Wrong sorting type. Available sorting: " + Arrays.toString(CryptoSortingTypeEnum.values());
        // When
        ResponseEntity<List<String>> responseEntity = cryptoService.getSortedCryptos("abra-kadbra");

        // Then
        assertAll(
                () -> assertEquals(400, responseEntity.getStatusCode().value()),
                () -> assertNull(responseEntity.getBody()),
                () -> assertEquals(errMsg, Objects.requireNonNull(responseEntity.getHeaders().get(ERR_HEADER_NAME)).get(0))
        );
    }

    @Test
    void getSortedCryptos_ShouldReturnNoContent_WhenNoCryptosReturned() {
        // Given
        String rightSortingType = Arrays.stream(CryptoSortingTypeEnum.values()).map(String::valueOf).findFirst().get();
        CryptoSortingTypeEnum sorting = CryptoSortingTypeEnum.valueOf(rightSortingType.toUpperCase());
        Mockito.when(cryptoRepositoryMock.getSortedCryptosByPassedAlgo(sorting)).thenReturn(Collections.emptyList());
        String errMsg = "There is no cryptos added";

        // When
        ResponseEntity<List<String>> responseEntity = cryptoService.getSortedCryptos(rightSortingType);

        // Then
        assertAll(
                () -> assertEquals(204, responseEntity.getStatusCode().value()),
                () -> assertNull(responseEntity.getBody()),
                () -> assertEquals(errMsg, Objects.requireNonNull(responseEntity.getHeaders().get(ERR_HEADER_NAME)).get(0))
        );
    }

    @Test
    void getSortedCryptos_ShouldReturnListOfCryptos() {
        // Given
        String rightSortingType = Arrays.stream(CryptoSortingTypeEnum.values()).map(String::valueOf).findFirst().get();
        CryptoSortingTypeEnum sorting = CryptoSortingTypeEnum.valueOf(rightSortingType.toUpperCase());
        List<String> listOfCryptos = List.of("btc", "eth", "xrp");
        Mockito.when(cryptoRepositoryMock.getSortedCryptosByPassedAlgo(sorting)).thenReturn(listOfCryptos);

        // When
        ResponseEntity<List<String>> responseEntity = cryptoService.getSortedCryptos(rightSortingType);

        // Then
        assertAll(
                () -> assertEquals(200, responseEntity.getStatusCode().value()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(String.join("", listOfCryptos), String.join("", responseEntity.getBody()))
        );
    }

    @Test
    void getCryptoWithHighestNormalizedRangeForDate_ShouldReturnBadRequest_WhenWrongFormatDate() {
        // Given
        String wrongFormattedDate = "22-01-5";
        String errMsg = "Error while parsing provided time: " + wrongFormattedDate;
        // When
        ResponseEntity<String> responseEntity = cryptoService.getCryptoWithHighestNormalizedRangeForDate(wrongFormattedDate);

        // Then
        assertAll(
                () -> assertEquals(400, responseEntity.getStatusCode().value()),
                () -> assertNull(responseEntity.getBody()),
                () -> assertEquals(errMsg, Objects.requireNonNull(responseEntity.getHeaders().get(ERR_HEADER_NAME)).get(0))
        );
    }

    @Test
    void getCryptoWithHighestNormalizedRangeForDate_ShouldReturnNoContent_WhenNoCryptoFound() {
        // Given
        String rightFormattedDate = "2022-01-05";
        LocalDate parsedDate = LocalDate.parse(rightFormattedDate);
        Mockito.when(cryptoRepositoryMock.getHighestNormalizedRangesForDay(parsedDate)).thenReturn("");
        String errMsg = "No crypto data registered for date: " + rightFormattedDate;

        // When
        ResponseEntity<String> responseEntity = cryptoService.getCryptoWithHighestNormalizedRangeForDate(rightFormattedDate);

        // Then
        assertAll(
                () -> assertEquals(204, responseEntity.getStatusCode().value()),
                () -> assertNull(responseEntity.getBody()),
                () -> assertEquals(errMsg, Objects.requireNonNull(responseEntity.getHeaders().get(ERR_HEADER_NAME)).get(0))
        );
    }

    @Test
    void getCryptoWithHighestNormalizedRangeForDate_ShouldReturnCrypto() {
        // Given
        String rightFormattedDate = "2022-01-05";
        LocalDate parsedDate = LocalDate.parse(rightFormattedDate);
        String expectedCrypto = "btc";
        Mockito.when(cryptoRepositoryMock.getHighestNormalizedRangesForDay(parsedDate)).thenReturn(expectedCrypto);

        // When
        ResponseEntity<String> responseEntity = cryptoService.getCryptoWithHighestNormalizedRangeForDate(rightFormattedDate);

        // Then
        assertAll(
                () -> assertEquals(200, responseEntity.getStatusCode().value()),
                () -> assertEquals(expectedCrypto, responseEntity.getBody())
        );
    }

    @Test
    void getMetadataForCrypto_ShouldReturnNoContent_WhenCryptoNotInRepo() {
        // Given
        String cryptoName = "abraKadabra";
        Mockito.when(cryptoRepositoryMock.getMetadataForCrypto(cryptoName)).thenReturn(null);
        String errMsg = "Nothing was found for crypto " + cryptoName;

        // When
        ResponseEntity<MetaDataAdapter> responseEntity = cryptoService.getMetadataForCrypto(cryptoName);

        // Then
        assertAll(
                () -> assertEquals(204, responseEntity.getStatusCode().value()),
                () -> assertNull(responseEntity.getBody()),
                () -> assertEquals(errMsg, Objects.requireNonNull(responseEntity.getHeaders().get(ERR_HEADER_NAME)).get(0))
        );
    }

    @Test
    void getMetadataForCrypto_ShouldReturnCryptosMetadata() {
        // Given
        CryptoMetaData btcMetadata = new CryptoMetaData("btc", 1d, 2d, 3d, 4d, 5d);
        MetaDataAdapter expectedResult = new MetaDataAdapter(btcMetadata);
        String cryptoName = "btc";
        Mockito.when(cryptoRepositoryMock.getMetadataForCrypto(cryptoName)).thenReturn(btcMetadata);

        // When
        ResponseEntity<MetaDataAdapter> responseEntity = cryptoService.getMetadataForCrypto(cryptoName);

        MetaDataAdapter actualResult = responseEntity.getBody();
        // Then
        assertAll(
                () -> assertEquals(200, responseEntity.getStatusCode().value()),
                () -> assertNotNull(actualResult),
                () -> assertEquals(expectedResult.getMinPrice(), actualResult.getMinPrice()),
                () -> assertEquals(expectedResult.getMaxPrice(), actualResult.getMaxPrice()),
                () -> assertEquals(expectedResult.getOldestPrice(), actualResult.getOldestPrice()),
                () -> assertEquals(expectedResult.getNewestPrice(), actualResult.getNewestPrice())
        );
    }
}