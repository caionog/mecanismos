package com.example.servicea.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import java.util.function.Supplier;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/")
public class ForwardController {

    private static final Logger log = LoggerFactory.getLogger(ForwardController.class);

    @Autowired
    private RestTemplate restTemplate;

    private static final String CB_NAME = "serviceB";

    @GetMapping("forward")
    @Bulkhead(name = CB_NAME) //BULKHEAD
    @Retry(name = CB_NAME, fallbackMethod = "fallback") // RETRY
    @CircuitBreaker(name = CB_NAME, fallbackMethod = "fallback")
    public String forward() {
        log.info("forward(): calling service-b via RestTemplate");
        String url = "http://service-b:8080/process";
        return restTemplate.getForObject(url, String.class);
    }

    @GetMapping("forward-decorated")
    public String forwardDecorated() {
        String url = "http://service-b:8080/process";

        // 1) Create a CircuitBreakerRegistry
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.ofDefaults();

        // 2) Create a CircuitBreaker (named 'decoratorCB')
        CircuitBreaker circuitBreaker = registry.circuitBreaker("decoratorCB");

        // 3) Decorate a functional interface (Supplier)
        Supplier<String> supplier = () -> {
            log.info("decorated supplier: calling service-b at {}", url);
            String resp = restTemplate.getForObject(url, String.class);
            log.info("decorated supplier: received response: {}", resp);
            return resp;
        };

        Supplier<String> decorated = Decorators.ofSupplier(supplier)
                .withCircuitBreaker(circuitBreaker)
                .withFallback(Collections.singletonList(CallNotPermittedException.class), throwable -> {
                    log.warn("decorator fallback triggered: {}", throwable.toString());
                    return "fallback-decorated: " + throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
                })
                .decorate();

        // 4) Recover from exception when executing
        try {
            return decorated.get();
        } catch (Exception ex) {
            return "fallback-decorated: " + ex.getClass().getSimpleName() + ": " + ex.getMessage();
        }
    }

    public String fallback(Throwable ex) {
        log.warn("fallback() invoked: {}", ex.toString());
        return "fallback: service-b is unavailable -> " + ex.getClass().getSimpleName() + ": " + ex.getMessage();
    }
}
