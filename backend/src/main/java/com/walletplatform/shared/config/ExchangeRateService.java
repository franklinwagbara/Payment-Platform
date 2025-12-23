package com.walletplatform.shared.config;

import com.walletplatform.wallet.domain.Currency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Exchange rate service with live rates from external API.
 * Configuration is externalized to application.yml for flexibility.
 */
@Component
public class ExchangeRateService {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateService.class);
    
    @Value("${exchange-rate.api.url}")
    private String apiUrl;
    
    @Value("${exchange-rate.api.enabled:true}")
    private boolean liveRatesEnabled;
    
    private final RestTemplate restTemplate;
    private final Map<String, BigDecimal> liveRates = new ConcurrentHashMap<>();
    private LocalDateTime lastFetched = null;
    private boolean useLiveRates = false;
    
    // Fallback static rates (updated Dec 2025)
    private static final Map<String, BigDecimal> STATIC_RATES = new HashMap<>();

    static {
        STATIC_RATES.put("USD_USD", BigDecimal.ONE);
        STATIC_RATES.put("USD_EUR", new BigDecimal("0.92"));
        STATIC_RATES.put("USD_GBP", new BigDecimal("0.79"));
        STATIC_RATES.put("EUR_USD", new BigDecimal("1.09"));
        STATIC_RATES.put("EUR_EUR", BigDecimal.ONE);
        STATIC_RATES.put("EUR_GBP", new BigDecimal("0.86"));
        STATIC_RATES.put("GBP_USD", new BigDecimal("1.27"));
        STATIC_RATES.put("GBP_EUR", new BigDecimal("1.16"));
        STATIC_RATES.put("GBP_GBP", BigDecimal.ONE);
    }

    public ExchangeRateService() {
        this.restTemplate = new RestTemplate();
    }
    
    @PostConstruct
    public void init() {
        refreshRates();
    }

    @Scheduled(fixedRateString = "${exchange-rate.refresh-interval-ms:3600000}")
    public void refreshRates() {
        if (!liveRatesEnabled) {
            log.info("Live exchange rates disabled. Using static rates.");
            return;
        }
        
        try {
            log.info("Fetching live exchange rates from: {}", apiUrl);
            ResponseEntity<ExchangeRateResponse> response = 
                restTemplate.getForEntity(apiUrl, ExchangeRateResponse.class);
            
            if (response.getBody() != null && response.getBody().rates != null) {
                Map<String, Double> rates = response.getBody().rates;
                
                for (Currency from : Currency.values()) {
                    for (Currency to : Currency.values()) {
                        String key = from.name() + "_" + to.name();
                        BigDecimal rate = calculateCrossRate(rates, from.name(), to.name());
                        liveRates.put(key, rate);
                    }
                }
                
                lastFetched = LocalDateTime.now();
                useLiveRates = true;
                log.info("Live exchange rates updated successfully. USD/EUR: {}, USD/GBP: {}", 
                    liveRates.get("USD_EUR"), liveRates.get("USD_GBP"));
            }
        } catch (Exception e) {
            log.warn("Failed to fetch live exchange rates: {}. Using fallback rates.", e.getMessage());
            useLiveRates = false;
        }
    }
    
    private BigDecimal calculateCrossRate(Map<String, Double> usdRates, String from, String to) {
        if (from.equals(to)) {
            return BigDecimal.ONE;
        }
        
        Double fromRate = from.equals("USD") ? 1.0 : usdRates.get(from);
        Double toRate = to.equals("USD") ? 1.0 : usdRates.get(to);
        
        if (fromRate == null || toRate == null) {
            // Fallback for unsupported currencies
            return STATIC_RATES.getOrDefault(from + "_" + to, BigDecimal.ONE);
        }
        
        // Cross rate: to_USD / from_USD
        double crossRate = toRate / fromRate;
        return BigDecimal.valueOf(crossRate).setScale(6, RoundingMode.HALF_UP);
    }

    public BigDecimal getExchangeRate(Currency from, Currency to) {
        String key = from.name() + "_" + to.name();
        
        // Use live rates if available and recent
        if (useLiveRates && liveRates.containsKey(key)) {
            return liveRates.get(key);
        }
        
        // Fallback to static rates
        BigDecimal rate = STATIC_RATES.get(key);
        if (rate == null) {
            throw new IllegalArgumentException("Exchange rate not found for " + from + " to " + to);
        }
        return rate;
    }

    public BigDecimal convert(BigDecimal amount, Currency from, Currency to) {
        BigDecimal rate = getExchangeRate(from, to);
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
    

    public RateInfo getRateInfo() {
        return new RateInfo(
            useLiveRates ? "LIVE" : "STATIC",
            lastFetched,
            liveRates.isEmpty() ? STATIC_RATES : liveRates
        );
    }
    
    public static class ExchangeRateResponse {
        public String base;
        public Map<String, Double> rates;
    }
    
    public record RateInfo(String source, LocalDateTime lastUpdated, Map<String, BigDecimal> rates) {}
}
