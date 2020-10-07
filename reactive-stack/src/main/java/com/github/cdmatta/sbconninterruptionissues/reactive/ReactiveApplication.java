package com.github.cdmatta.sbconninterruptionissues.reactive;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@SpringBootApplication
@RestController
public class ReactiveApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReactiveApplication.class, args);
    }

    @GetMapping("/five")
    public Mono<String> five() {
        LocalDateTime start = LocalDateTime.now();
        return Mono.just("start")
                .delayElement(Duration.ofSeconds(5))
                .then(Mono.defer(() -> Mono.just("Call took " + Duration.between(start, LocalDateTime.now()).getSeconds() + " seconds")));
    }

    @GetMapping("/twenty")
    public Mono<String> twenty() {
        LocalDateTime start = LocalDateTime.now();
        return Mono.just("start")
                .delayElement(Duration.ofSeconds(20))
                .then(Mono.defer(() -> Mono.just("Call took " + Duration.between(start, LocalDateTime.now()).getSeconds() + " seconds")));
    }

    @GetMapping("/fifty")
    public Mono<String> fifty() {
        LocalDateTime start = LocalDateTime.now();
        return Mono.fromRunnable(() -> {
                    for (int i = 0; i < 10; i++) {
                        System.out.println("Iteration " + i);
                        try {
                            Thread.sleep(5_000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            System.out.println("Thread was interrupted");
                        }
                    }
                }
        ).then(Mono.defer(() -> Mono.just("Call took " + Duration.between(start, LocalDateTime.now()).getSeconds() + " seconds")));
    }

    @Bean
    public WebServerFactoryCustomizer serverFactoryCustomizer() {
        return new NettyTimeoutCustomizer();
    }

    class NettyTimeoutCustomizer implements WebServerFactoryCustomizer<NettyReactiveWebServerFactory> {
        @Override
        public void customize(NettyReactiveWebServerFactory factory) {
            int timeout = 10_000;
            factory.addServerCustomizers(server -> server.tcpConfiguration(tcp ->
                    tcp.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout)
                            .doOnConnection(connection ->
                                    connection
                                            .addHandlerLast(new WriteTimeoutHandler(timeout, MILLISECONDS))
                                            .addHandlerLast(new ReadTimeoutHandler(timeout, MILLISECONDS))
                            )));
        }
    }
}
