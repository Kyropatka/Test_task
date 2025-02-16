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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping("/enrich")
    public CompletableFuture<ResponseEntity<Resource>> enrichTrades(
            @RequestParam("file") MultipartFile file,
            @RequestParam("format") String format) throws IOException {

        File inputFile = File.createTempFile("temp_trade", "." + format);
        file.transferTo(inputFile);

        File outputFile = File.createTempFile("enriched_trades", ".csv");

        TradeParser parser = TradeParserFactory.getTradeParser(format);
        CompletableFuture<File> futureFile = tradeService.enrichTradeAndGenerateCsvAsync(inputFile, outputFile, parser);

        return futureFile.thenApply(resultFile ->{
            try {
                InputStreamResource resource = new InputStreamResource(new FileInputStream(resultFile));
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + resultFile.getName())
                        .contentType(MediaType.parseMediaType("text/csv"))
                        .body(resource);
            }catch (FileNotFoundException e){
                throw new RuntimeException("File not found after enrichment", e);
            }
        });
    }
}
