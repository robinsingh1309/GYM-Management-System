package com.example.fitmanager.service;

import java.util.List;

import com.example.fitmanager.dto.KnowledgeMatch;


public interface KnowledgeRetrievalService {

    List<KnowledgeMatch> retrieve(String question);
}
