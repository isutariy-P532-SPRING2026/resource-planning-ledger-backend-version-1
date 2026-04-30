package edu.indiana.p532.rpl.dto;

import java.util.Map;

public record TransitionRequest(String event, Map<String, String> params) {
    public Map<String, String> safeParams() {
        return params != null ? params : Map.of();
    }
}
