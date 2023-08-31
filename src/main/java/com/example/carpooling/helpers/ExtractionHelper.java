package com.example.carpooling.helpers;

import org.springframework.stereotype.Component;

import java.util.Map;
@Component
public class ExtractionHelper {

    public   String extractParametersSection(Map<String, String[]> parameterMap) {
        StringBuilder builder = new StringBuilder();
        for (String key : parameterMap.keySet()) {
            String value = parameterMap.get(key)[0];
            if (value.trim().isEmpty() || key.equals("page")) {
                continue;
            }
            builder.append("&").append(key).append("=").append(value);
        }
        return builder.toString();
    }
}
