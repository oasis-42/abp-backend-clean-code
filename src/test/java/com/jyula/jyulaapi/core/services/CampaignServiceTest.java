package com.jyula.jyulaapi.core.services;

import com.jyula.jyulaapi.core.enterprise.BussinessException;
import com.jyula.jyulaapi.core.entities.*;
import com.jyula.jyulaapi.core.providers.MailSenderProvider;
import com.jyula.jyulaapi.core.repositories.*;
import com.jyula.jyulaapi.core.representations.CampaignRepresentation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignServiceTest {

    @Mock
    private MailSenderProvider mailSenderProvider;
    @Mock
    private TemplateContentRepository templateContentRepository;
    @Mock
    private ContactRepository contactRepository;
    @Mock
    private SegmentRepository segmentRepository;
    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private SentEmailRepository sentEmailRepository;

    @Captor
    private ArgumentCaptor<Campaign> campaignCaptor;
    @Captor
    private ArgumentCaptor<SentEmail> sentEmailCaptor;

    private CampaignService campaignService;

    @BeforeEach
    void setUp() {
        campaignService = new CampaignService(
                mailSenderProvider,
                templateContentRepository,
                contactRepository,
                segmentRepository,
                campaignRepository,
                sentEmailRepository
        );
    }

    @Test
    @DisplayName("Should successfully send campaign to contacts")
    void shouldSuccessfullySendCampaignToContacts() {
        TemplateContent template = new TemplateContent();
        template.setContent("Test content");
        template.setAbout("Test subject");
        template.setTemplate(new Template());

        Contact contact = new Contact();
        contact.setId(1L);
        contact.setEmail("test@example.com");

        when(templateContentRepository.findLatest(any())).thenReturn(template);
        when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));
        when(sentEmailRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CampaignRepresentation.CreateCampaign representation = new CampaignRepresentation.CreateCampaign();
        representation.setName("Test Campaign");
        representation.setTemplateId(1L);
        representation.setSendTo(new CampaignRepresentation.CreateCampaign.SendTo());
        representation.getSendTo().setContacts(Set.of(1L));

        campaignService.send(representation);

        verify(mailSenderProvider).send(any());
        verify(sentEmailRepository, times(2)).save(sentEmailCaptor.capture());
        verify(campaignRepository).save(campaignCaptor.capture());

        Campaign savedCampaign = campaignCaptor.getValue();
        assertEquals("Test Campaign", savedCampaign.getName());
        assertEquals(1, savedCampaign.getContacts().size());
    }

    @Test
    @DisplayName("Should throw exception when contact not found")
    void shouldThrowExceptionWhenContactNotFound() {
        when(templateContentRepository.findLatest(any())).thenReturn(new TemplateContent());

        CampaignRepresentation.CreateCampaign representation = new CampaignRepresentation.CreateCampaign();
        representation.setTemplateId(1L);
        representation.setSendTo(new CampaignRepresentation.CreateCampaign.SendTo());
        representation.getSendTo().setContacts(Set.of(1L));

        BussinessException exception = assertThrows(BussinessException.class,
                () -> campaignService.send(representation));

        assertEquals("Contact with id = 1 not found", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when segment not found")
    void shouldThrowExceptionWhenSegmentNotFound() {
        when(templateContentRepository.findLatest(any())).thenReturn(new TemplateContent());

        CampaignRepresentation.CreateCampaign representation = new CampaignRepresentation.CreateCampaign();
        representation.setTemplateId(1L);
        representation.setSendTo(new CampaignRepresentation.CreateCampaign.SendTo());
        representation.getSendTo().setSegments(Set.of(1L));

        BussinessException exception = assertThrows(BussinessException.class,
                () -> campaignService.send(representation));

        assertEquals("Segment with id = 1 not found", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle mail sending failure")
    void shouldHandleMailSendingFailure() {
        TemplateContent template = new TemplateContent();
        template.setContent("Test content");
        template.setAbout("Test subject");
        template.setTemplate(new Template());

        Contact contact = new Contact();
        contact.setId(1L);
        contact.setEmail("test@example.com");

        MailSenderProvider.SendMailRequest sendMailRequest = MailSenderProvider.SendMailRequest.builder()
                .from("onboarding@resend.dev")
                .content(template.getContent())
                .to(contact.getEmail())
                .subject(template.getAbout())
                .build();

        when(templateContentRepository.findLatest(any())).thenReturn(template);
        when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));
        when(sentEmailRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        doThrow(new MailSenderProvider.SendMailException(sendMailRequest, new RuntimeException("Failed to send")))
                .when(mailSenderProvider).send(any());

        CampaignRepresentation.CreateCampaign representation = new CampaignRepresentation.CreateCampaign();
        representation.setName("Test Campaign");
        representation.setTemplateId(1L);
        representation.setSendTo(new CampaignRepresentation.CreateCampaign.SendTo());
        representation.getSendTo().setContacts(Set.of(1L));

        campaignService.send(representation);

        verify(sentEmailRepository, times(2)).save(sentEmailCaptor.capture());
        SentEmail savedEmail = sentEmailCaptor.getValue();
        assertEquals(SentEmail.EmailStatus.FAILED, savedEmail.getStatus());
    }

    @Test
    @DisplayName("Should successfully send campaign to segments")
    void shouldSuccessfullySendCampaignToSegments() {
        TemplateContent template = new TemplateContent();
        template.setContent("Test content");
        template.setAbout("Test subject");
        template.setTemplate(new Template());

        Contact contact = new Contact();
        contact.setId(1L);
        contact.setEmail("test@example.com");

        Segment segment = new Segment();
        segment.setId(1L);
        segment.setContacts(Set.of(contact));

        when(templateContentRepository.findLatest(any())).thenReturn(template);
        when(segmentRepository.findById(1L)).thenReturn(Optional.of(segment));
        when(sentEmailRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CampaignRepresentation.CreateCampaign representation = new CampaignRepresentation.CreateCampaign();
        representation.setName("Test Campaign");
        representation.setTemplateId(1L);
        representation.setSendTo(new CampaignRepresentation.CreateCampaign.SendTo());
        representation.getSendTo().setSegments(Set.of(1L));

        campaignService.send(representation);

        verify(mailSenderProvider).send(any());
        verify(sentEmailRepository, times(2)).save(sentEmailCaptor.capture());
        verify(campaignRepository).save(campaignCaptor.capture());

        Campaign savedCampaign = campaignCaptor.getValue();
        assertEquals("Test Campaign", savedCampaign.getName());
        assertEquals(1, savedCampaign.getSegments().size());
    }
} 