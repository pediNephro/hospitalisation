package com.esprit.microservice.hospitalisation;

import com.esprit.microservice.hospitalisation.dto.PatientRequestDTO;
import com.esprit.microservice.hospitalisation.entities.Patient;
import com.esprit.microservice.hospitalisation.repositories.PatientRepository;
import com.esprit.microservice.hospitalisation.services.PatientServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PatientServiceImplTest {

    private final PatientRepository repo = mock(PatientRepository.class);
    private final PatientServiceImpl service = new PatientServiceImpl(repo);

    @Test
    void testCreatePatient() {

        Patient saved = new Patient();
        saved.setId(1L);
        saved.setNom("Ali");

        when(repo.save(any())).thenReturn(saved);

        PatientRequestDTO dto = new PatientRequestDTO();
        dto.setNom("Ali");

        var result = service.addPatient(dto);

        assertNotNull(result);
        assertEquals("Ali", result.getNom());
    }

    @Test
    void testGetPatient() {

        Patient p = new Patient();
        p.setId(1L);

        when(repo.findById(1L)).thenReturn(Optional.of(p));

        var result = service.getPatient(1L);

        assertNotNull(result);
    }

    @Test
    void testDeletePatient() {

        service.deletePatient(1L);

        verify(repo, times(1)).deleteById(1L);
    }
}