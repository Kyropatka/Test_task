package com.gmail.deniska1406sme.test_task;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/v1")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping("/enrich")
    public ResponseEntity<Resource> enrichTrades(@RequestParam("file") MultipartFile file) throws IOException {
        File inputFile = File.createTempFile("temp_trade", ".csv");
        file.transferTo(inputFile);

        File outputFile = File.createTempFile("enriched_trades", ".csv");
        tradeService.enrichTradeAndGenerateCsv(inputFile, outputFile);

        InputStreamResource resource = new InputStreamResource(new FileInputStream(outputFile));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + outputFile.getName())
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }
}
