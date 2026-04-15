package com.esprit.microservice.hospitalisation;

import com.esprit.microservice.hospitalisation.dto.HospitalisationRequestDTO;
import com.esprit.microservice.hospitalisation.entities.*;
import com.esprit.microservice.hospitalisation.repositories.*;
import com.esprit.microservice.hospitalisation.services.HospitalisationServiceImpl;
import com.esprit.microservice.hospitalisation.services.TwilioSmsService;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HospitalisationServiceImplTest {

    private final HospitalisationRepository repo = mock(HospitalisationRepository.class);
    private final PatientRepository patientRepo = mock(PatientRepository.class);
    private final TwilioSmsService sms = mock(TwilioSmsService.class);

    private final HospitalisationServiceImpl service =
            new HospitalisationServiceImpl(repo, patientRepo, sms);

    @Test
    void testAddHospitalisationSuccess() {

        Patient p = new Patient();
        p.setId(1L);
        p.setNom("Ali");

        when(patientRepo.findById(1L)).thenReturn(Optional.of(p));
        when(repo.findByService(any())).thenReturn(new ArrayList<>());
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        HospitalisationRequestDTO dto = new HospitalisationRequestDTO();
        dto.setPatientId(1L);
        dto.setService("Cardio");
        dto.setDateEntree(new Date());
        dto.setMotif("Test");

        var result = service.addHospitalisation(dto);

        assertNotNull(result);
        assertEquals("Cardio", result.getService());
    }

    @Test
    void testCapaciteMax() {

        Patient p = new Patient();
        p.setId(1L);

        List<Hospitalisation> list = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Hospitalisation h = new Hospitalisation();
            h.setDateSortie(null);
            list.add(h);
        }

        when(patientRepo.findById(1L)).thenReturn(Optional.of(p));
        when(repo.findByService(any())).thenReturn(list);

        HospitalisationRequestDTO dto = new HospitalisationRequestDTO();
        dto.setPatientId(1L);
        dto.setService("Cardio");
        dto.setDateEntree(new Date());

        assertThrows(RuntimeException.class, () -> service.addHospitalisation(dto));
    }

    @Test
    void testLibererLit() {

        Hospitalisation h = new Hospitalisation();
        h.setId(1L);
        h.setDateSortie(null);

        when(repo.findById(1L)).thenReturn(Optional.of(h));

        service.libererLit(1L);

        assertNotNull(h.getDateSortie());
    }

    @Test
    void testStatistiques() {

        when(repo.findByService("Cardio")).thenReturn(new ArrayList<>());

        var result = service.getStatistiquesLitsTempsReel("Cardio");

        assertEquals("Cardio", result.get("service"));
    }
}