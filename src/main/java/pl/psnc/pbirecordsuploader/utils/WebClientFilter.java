package pl.psnc.pbirecordsuploader.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

@Slf4j
@UtilityClass
public class WebClientFilter {

    public ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            log.debug("{} to {}", request.method().name(), request.url());
            request.headers().forEach((name, values) -> values.forEach(value -> logNameAndValuePair(name, value)));
            return Mono.just(request);
        });
    }

    public ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            log.debug("Returned status code {}", response.statusCode().value());
            response.headers().asHttpHeaders().forEach((name, values) -> values.forEach(value -> logNameAndValuePair(name, value)));
            return logBody(response);
        });
    }

    private Mono<ClientResponse> logBody(ClientResponse response) {
        if (response.statusCode().is4xxClientError() || response.statusCode().is5xxServerError()) {
            return response.bodyToMono(String.class).doOnNext(body -> log.debug("Body is {}", body))
                    .then(Mono.just(response));
        }
        return Mono.just(response);
    }

    private void logNameAndValuePair(String name, String value) {
        log.trace("{}={}", name, value);
    }
}
