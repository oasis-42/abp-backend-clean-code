package com.jyula.jyulaapi.core.services;

import com.jyula.jyulaapi.core.enterprise.BussinessException;
import com.jyula.jyulaapi.core.entities.Contact;
import com.jyula.jyulaapi.core.entities.Segment;
import com.jyula.jyulaapi.core.entities.security.User;
import com.jyula.jyulaapi.core.repositories.ContactRepository;
import com.jyula.jyulaapi.core.repositories.SegmentRepository;
import com.jyula.jyulaapi.core.repositories.security.UserRepository;
import com.jyula.jyulaapi.core.representations.SegmentRepresentation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SegmentServiceTest {

    @Mock
    private ModelMapper mapper;

    @Mock
    private SegmentRepository segmentRepository;

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SegmentService segmentService;

    private User testUser;
    private Contact testContact;
    private Segment testSegment;
    private SegmentRepresentation.CreateOrUpdateSegment createSegmentRepresentation;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .build();

        testContact = Contact.builder()
                .id(1L)
                .email("test@example.com")
                .build();

        testSegment = Segment.builder()
                .id(1L)
                .name("Test Segment")
                .user(testUser)
                .contacts(new HashSet<>())
                .build();

        createSegmentRepresentation = new SegmentRepresentation.CreateOrUpdateSegment();
        createSegmentRepresentation.setUsername("testuser");
        createSegmentRepresentation.setName("Test Segment");
        createSegmentRepresentation.setContacts(Set.of("test@example.com"));
    }

    @Test
    @DisplayName("Should successfully create a new segment with valid user and contact data")
    void save_ShouldCreateNewSegment_WhenValidDataProvided() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(contactRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testContact));
        when(segmentRepository.save(any(Segment.class))).thenReturn(testSegment);

        Segment result = segmentService.save(createSegmentRepresentation);

        assertNotNull(result);
        assertEquals(testSegment.getId(), result.getId());
        assertEquals(testSegment.getName(), result.getName());
        verify(segmentRepository).save(any(Segment.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when attempting to create segment with non-existent user")
    void save_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        BussinessException exception = assertThrows(BussinessException.class, () -> {
            createSegmentRepresentation.setUsername("nonexistent");
            segmentService.save(createSegmentRepresentation);
        });

        assertEquals("User with username = nonexistent not found", exception.getMessage());
    }

    @Test
    @DisplayName("Should successfully update an existing segment with new data")
    void update_ShouldUpdateSegment_WhenValidDataProvided() {
        when(segmentRepository.findById(1L)).thenReturn(Optional.of(testSegment));
        when(contactRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testContact));
        when(segmentRepository.save(any(Segment.class))).thenReturn(testSegment);

        Segment result = segmentService.update(1L, createSegmentRepresentation);

        assertNotNull(result);
        assertEquals(testSegment.getId(), result.getId());
        assertEquals(testSegment.getName(), result.getName());
        verify(segmentRepository).save(any(Segment.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when attempting to update non-existent segment")
    void update_ShouldThrowException_WhenSegmentNotFound() {
        when(segmentRepository.findById(999L)).thenReturn(Optional.empty());

        BussinessException exception = assertThrows(BussinessException.class, () -> {
            segmentService.update(999L, createSegmentRepresentation);
        });

        assertEquals("Segment with id = 999 not found", exception.getMessage());
    }

    @Test
    @DisplayName("Should successfully add new contacts to an existing segment")
    void addContacts_ShouldAddContactsToSegment_WhenValidEmailsProvided() {
        Set<String> newEmails = Set.of("new@example.com");
        Contact newContact = Contact.builder()
                .id(2L)
                .email("new@example.com")
                .build();

        when(segmentRepository.findById(1L)).thenReturn(Optional.of(testSegment));
        when(contactRepository.findByEmail("new@example.com")).thenReturn(Optional.of(newContact));
        when(segmentRepository.save(any(Segment.class))).thenReturn(testSegment);

        Segment result = segmentService.addContacts(1L, newEmails);

        assertNotNull(result);
        verify(segmentRepository).save(any(Segment.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when attempting to add non-existent contacts to segment")
    void addContacts_ShouldThrowException_WhenContactNotFound() {
        Set<String> invalidEmails = Set.of("nonexistent@example.com");
        when(segmentRepository.findById(1L)).thenReturn(Optional.of(testSegment));
        when(contactRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        BussinessException exception = assertThrows(BussinessException.class, () -> {
            segmentService.addContacts(1L, invalidEmails);
        });

        assertEquals("Contacts with email = nonexistent@example.com not found", exception.getMessage());
    }
} 