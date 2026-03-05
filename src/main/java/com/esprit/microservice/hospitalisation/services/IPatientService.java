package com.esprit.microservice.hospitalisation.services;

import com.esprit.microservice.hospitalisation.dto.PatientRequestDTO;
import com.esprit.microservice.hospitalisation.dto.PatientResponseDTO;

import java.util.List;

public interface IPatientService {

    PatientResponseDTO addPatient(PatientRequestDTO dto);

    PatientResponseDTO getPatient(Long id);

    List<PatientResponseDTO> getAllPatients();

    PatientResponseDTO updatePatient(Long id, PatientRequestDTO dto);

    void deletePatient(Long id);
}
