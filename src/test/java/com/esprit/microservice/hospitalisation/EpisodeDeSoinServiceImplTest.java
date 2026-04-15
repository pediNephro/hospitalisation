package com.esprit.microservice.hospitalisation;

import com.esprit.microservice.hospitalisation.dto.EpisodeRequestDTO;
import com.esprit.microservice.hospitalisation.entities.*;
import com.esprit.microservice.hospitalisation.repositories.*;
import com.esprit.microservice.hospitalisation.services.EpisodeDeSoinServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EpisodeDeSoinServiceImplTest {

    private final EpisodeDeSoinRepository repo = mock(EpisodeDeSoinRepository.class);
    private final HospitalisationRepository hospRepo = mock(HospitalisationRepository.class);

    private final EpisodeDeSoinServiceImpl service =
            new EpisodeDeSoinServiceImpl(repo, hospRepo);

    @Test
    void testAddEpisodeSuccess() {

        Hospitalisation h = new Hospitalisation();
        h.setId(1L);
        h.setDateEntree(new Date());

        when(hospRepo.findById(1L)).thenReturn(Optional.of(h));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        EpisodeRequestDTO dto = new EpisodeRequestDTO();
        dto.setType("Consultation");
        dto.setDate(new Date());
        dto.setDescription("test consultation");
        dto.setHospitalisationId(1L);

        var result = service.addEpisode(dto);

        assertNotNull(result);
        assertEquals("Consultation", result.getType());
    }

    @Test
    void testAddEpisodeDateError() {

        Hospitalisation h = new Hospitalisation();
        h.setDateEntree(new Date());

        when(hospRepo.findById(1L)).thenReturn(Optional.of(h));

        EpisodeRequestDTO dto = new EpisodeRequestDTO();
        dto.setDate(new Date(0)); // ancienne date
        dto.setHospitalisationId(1L);

        assertThrows(RuntimeException.class, () -> service.addEpisode(dto));
    }

    @Test
    void testFileAttente() {

        EpisodeDeSoin e = new EpisodeDeSoin();
        Hospitalisation h = new Hospitalisation();
        h.setService("Cardio");

        e.setHospitalisation(h);
        e.setType("Urgence");
        e.setDate(new Date());

        when(repo.findAll()).thenReturn(List.of(e));

        var result = service.getFileAttente("Cardio");

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testTimeline() {

        EpisodeDeSoin e = new EpisodeDeSoin();

        Patient p = new Patient();
        p.setId(1L);

        Hospitalisation h = new Hospitalisation();
        h.setPatient(p);

        e.setHospitalisation(h);
        e.setDate(new Date());

        when(repo.findAll()).thenReturn(List.of(e));

        var result = service.getTimelineParcours(1L);

        assertEquals(1L, result.get("patientId"));
    }

    @Test
    void testSignature() {

        EpisodeDeSoin e = new EpisodeDeSoin();
        e.setId(1L);

        when(repo.findById(1L)).thenReturn(Optional.of(e));

        var result = service.signerEpisode(1L, "Dr Ali");

        assertEquals("VALIDE", result.getStatus());
    }
}