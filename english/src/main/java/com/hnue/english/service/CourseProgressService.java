package com.hnue.english.service;

import com.hnue.english.repository.CourseProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseProgressService {
    private final CourseProgressRepository courseProgressRepository;
}
