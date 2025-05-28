package com.jyula.jyulaapi.core.services;

import com.jyula.jyulaapi.core.enterprise.BussinessException;
import com.jyula.jyulaapi.core.entities.Contact;
import com.jyula.jyulaapi.core.repositories.ContactRepository;
import com.jyula.jyulaapi.core.representations.ContactRepresentation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    @Mock
    private ContactRepository repository;

    @Mock
    private ModelMapper mapper;

    private ContactService contactService;

    @BeforeEach
    void setUp() {
        contactService = new ContactService(mapper, repository);
    }

    @Test
    @DisplayName("Should throw BusinessException when contact is not found during update")
    void update_WhenContactNotFound_ShouldThrowBusinessException() {
        Long nonExistentId = 1L;
        ContactRepresentation.CreateOrUpdateContact representation = new ContactRepresentation.CreateOrUpdateContact();
        when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

        BussinessException exception = assertThrows(BussinessException.class, () ->
            contactService.update(nonExistentId, representation)
        );

        assertEquals("Contact with id = 1 not found", exception.getMessage());
        verify(repository, times(1)).findById(nonExistentId);
        verify(repository, never()).save(any(Contact.class));
    }

    @Test
    @DisplayName("Should successfully update contact when it exists")
    void update_WhenContactExists_ShouldUpdateAndSave() {
        Long existingId = 1L;
        Contact existingContact = new Contact();
        ContactRepresentation.CreateOrUpdateContact representation = new ContactRepresentation.CreateOrUpdateContact();
        
        when(repository.findById(existingId)).thenReturn(Optional.of(existingContact));
        when(repository.save(any(Contact.class))).thenReturn(existingContact);

        Contact updatedContact = contactService.update(existingId, representation);

        assertNotNull(updatedContact);
        verify(repository, times(1)).findById(existingId);
        verify(mapper, times(1)).map(representation, existingContact);
        verify(repository, times(1)).save(existingContact);
        assertNotNull(existingContact.getUpdatedAt());
    }
} 