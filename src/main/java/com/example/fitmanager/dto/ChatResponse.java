package com.example.fitmanager.dto;

import java.util.List;


public record ChatResponse(String answer, List<ChatSource> sources) {
}
