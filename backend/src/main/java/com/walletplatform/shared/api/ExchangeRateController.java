package com.walletplatform.shared.api;

import com.walletplatform.wallet.domain.Currency;
import com.walletplatform.shared.config.ExchangeRateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/rates")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    public ExchangeRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllRates() {
        ExchangeRateService.RateInfo info = exchangeRateService.getRateInfo();
        
        Map<String, Object> response = new HashMap<>();
        response.put("source", info.source());
        response.put("lastUpdated", info.lastUpdated());
        response.put("baseCurrency", "USD");
        
        Map<String, BigDecimal> formattedRates = new HashMap<>();
        for (Currency from : Currency.values()) {
            for (Currency to : Currency.values()) {
                if (from != to) {
                    String key = from.name() + "/" + to.name();
                    formattedRates.put(key, exchangeRateService.getExchangeRate(from, to));
                }
            }
        }
        response.put("rates", formattedRates);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/convert")
    public ResponseEntity<Map<String, Object>> convert(
            @RequestParam Currency from,
            @RequestParam Currency to,
            @RequestParam BigDecimal amount) {
        
        BigDecimal rate = exchangeRateService.getExchangeRate(from, to);
        BigDecimal converted = exchangeRateService.convert(amount, from, to);
        
        Map<String, Object> response = new HashMap<>();
        response.put("from", from.name());
        response.put("to", to.name());
        response.put("amount", amount);
        response.put("rate", rate);
        response.put("convertedAmount", converted);
        response.put("source", exchangeRateService.getRateInfo().source());
        
        return ResponseEntity.ok(response);
    }
}
