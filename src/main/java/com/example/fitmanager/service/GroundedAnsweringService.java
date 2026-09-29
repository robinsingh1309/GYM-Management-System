package com.example.fitmanager.service;

import com.example.fitmanager.dto.ChatResponse;


public interface GroundedAnsweringService {

    ChatResponse answer(String question);
}
