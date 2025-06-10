package com.example.battery_management;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;


//Hjälper till att läsa json och jag behöver inte upprepa lika mycket kod,
// eftersom servern skickar text/html så får jag inte fram det.
public class JsonHelper {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static <T> Mono<T> parse(String json, TypeReference<T> type){
        try {
            return Mono.just(mapper.readValue(json, type));
        } catch (Exception exception){
            return Mono.error(exception);
        }
    }
}
