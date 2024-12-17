package com.estelle.hangman.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CourseCreateRequest {
    private String name;
    private String description;
    private String level;     // Phonics, Beginner, Elementary
}