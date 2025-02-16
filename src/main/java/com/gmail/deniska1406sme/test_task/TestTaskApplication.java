package com.gmail.deniska1406sme.test_task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@SpringBootApplication
public class TestTaskApplication implements CommandLineRunner {

    @Autowired
    private ProductService productService;

    public static void main(String[] args) {
        SpringApplication.run(TestTaskApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Resource resource = new ClassPathResource("largeSizeProduct.csv");
        File productFile  = resource.getFile();

        CompletableFuture<Void> future = productService.loadAndSaveProductsAsync(productFile);
        future.thenRun(() -> System.out.println("Product names loaded and saved in Redis."));
    }
}
