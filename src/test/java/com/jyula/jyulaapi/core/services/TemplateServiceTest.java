package com.jyula.jyulaapi.core.services;

import com.jyula.jyulaapi.core.entities.Template;
import com.jyula.jyulaapi.core.entities.TemplateContent;
import com.jyula.jyulaapi.core.entities.security.User;
import com.jyula.jyulaapi.core.repositories.TemplateContentRepository;
import com.jyula.jyulaapi.core.repositories.TemplateRepository;
import com.jyula.jyulaapi.core.repositories.security.UserRepository;
import com.jyula.jyulaapi.core.representations.TemplateRepresentation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TemplateServiceTest {
    @Mock
    private TemplateRepository templateRepository;
    @Mock
    private TemplateContentRepository templateContentRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TemplateService templateService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should successfully save a new template with valid user")
    void testSave() {
        TemplateRepresentation.CreateOrUpdateTemplate representation = mock(TemplateRepresentation.CreateOrUpdateTemplate.class);
        when(representation.getUsername()).thenReturn("testuser");
        when(representation.getName()).thenReturn("Test Template");
        when(representation.isFavorite()).thenReturn(true);
        when(representation.getAbout()).thenReturn("About");
        when(representation.getContent()).thenReturn("Content");

        User user = new User();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        Template template = Template.builder().build();
        when(templateRepository.save(any(Template.class))).thenReturn(template);

        Template result = templateService.save(representation);

        assertNotNull(result);
        verify(templateRepository).save(any(Template.class));
        verify(templateContentRepository).save(any(TemplateContent.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when saving template with non-existent user")
    void testSave_throwsException_whenUserNotFound() {
        TemplateRepresentation.CreateOrUpdateTemplate representation = mock(TemplateRepresentation.CreateOrUpdateTemplate.class);
        when(representation.getUsername()).thenReturn("missinguser");
        when(userRepository.findByUsername("missinguser")).thenReturn(Optional.empty());

        Exception exception = assertThrows(com.jyula.jyulaapi.core.enterprise.BussinessException.class, () -> {
            templateService.save(representation);
        });
        assertTrue(exception.getMessage().contains("User with username = missinguser not found"));
    }

    @Test
    @DisplayName("Should throw BusinessException when updating non-existent template")
    void testUpdate_throwsException_whenTemplateNotFound() {
        Long templateId = 99L;
        TemplateRepresentation.CreateOrUpdateTemplate representation = mock(TemplateRepresentation.CreateOrUpdateTemplate.class);
        when(templateRepository.findById(templateId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(com.jyula.jyulaapi.core.enterprise.BussinessException.class, () -> {
            templateService.update(templateId, representation);
        });
        assertTrue(exception.getMessage().contains("Template with id = id not found"));
    }
} 