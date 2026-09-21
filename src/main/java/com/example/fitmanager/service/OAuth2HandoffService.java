package com.example.fitmanager.service;


public interface OAuth2HandoffService {

    String createCode(Long userId);

    Long consumeCode(String code);
}
