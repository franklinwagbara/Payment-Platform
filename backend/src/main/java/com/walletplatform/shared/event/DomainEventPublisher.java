package com.walletplatform.shared.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class DomainEventPublisher {
    
    private final ApplicationEventPublisher springEventPublisher;
    
    public DomainEventPublisher(ApplicationEventPublisher springEventPublisher) {
        this.springEventPublisher = springEventPublisher;
    }
    
    public void publish(DomainEvent event) {
        springEventPublisher.publishEvent(event);
    }
    
    public void publishAsync(DomainEvent event) {
        springEventPublisher.publishEvent(new AsyncDomainEvent(event));
    }
    
    public static class AsyncDomainEvent {
        private final DomainEvent event;
        
        public AsyncDomainEvent(DomainEvent event) { this.event = event; }
        public DomainEvent getEvent() { return event; }
    }
}
