package com.hnue.english.service;

import com.hnue.english.repository.TopicProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TopicProgressService {
    private final TopicProgressRepository topicProgressRepository;

}
