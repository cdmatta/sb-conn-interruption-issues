package com.github.cdmatta.sbconninterruptionissues.mvc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ForkJoinPool;

@SpringBootApplication
@RestController
@Slf4j
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @GetMapping("/five")
    public String five() {
        log.info("Executing regular blocking call /five");

        LocalDateTime start = LocalDateTime.now();
        sleep(5);
        return "Call took " + Duration.between(start, LocalDateTime.now()).getSeconds() + " seconds";
    }

    private void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("Sleep was interrupted");
        }
        log.info("Sleep done returning");
    }

    @GetMapping("/twenty")
    public DeferredResult<String> twenty() {
        log.info("Executing Deferred result call for /twenty");
        LocalDateTime start = LocalDateTime.now();

        DeferredResult<String> output = new DeferredResult<>();
        ForkJoinPool.commonPool().submit(() -> {
            log.info("Processing in separate thread. sleeping");
            sleep(20);
            output.setResult("Call took " + Duration.between(start, LocalDateTime.now()).getSeconds() + " seconds");
        });

        log.info("servlet thread freed");
        return output;
    }
}
