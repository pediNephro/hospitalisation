package com.esprit.microservice.hospitalisation.services;

import com.esprit.microservice.hospitalisation.dto.PatientRequestDTO;
import com.esprit.microservice.hospitalisation.dto.PatientResponseDTO;
import com.esprit.microservice.hospitalisation.entities.Patient;
import com.esprit.microservice.hospitalisation.repositories.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientServiceImpl implements IPatientService {

    private final PatientRepository repository;

    public PatientServiceImpl(PatientRepository repository) {
        this.repository = repository;
    }

    // ================= MAPPING =================

    private PatientResponseDTO convertToDTO(Patient p) {
        return new PatientResponseDTO(
                p.getId(),
                p.getCodePatient(),
                p.getNom(),
                p.getPrenom(),
                p.getDateNaissance(),
                p.getSexe()
        );
    }

    private Patient convertToEntity(PatientRequestDTO dto) {

        Patient p = new Patient();
        p.setCodePatient(dto.getCodePatient());
        p.setNom(dto.getNom());
        p.setPrenom(dto.getPrenom());
        p.setDateNaissance(dto.getDateNaissance());
        p.setSexe(dto.getSexe());

        return p;
    }

    // ================= CRUD =================

    @Override
    public PatientResponseDTO addPatient(PatientRequestDTO dto) {
        Patient saved = repository.save(convertToEntity(dto));
        return convertToDTO(saved);
    }

    @Override
    public PatientResponseDTO getPatient(Long id) {
        return repository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Override
    public List<PatientResponseDTO> getAllPatients() {
        return ((List<Patient>) repository.findAll())
                .stream()
                .map(this::convertToDTO)
                .toList();
    }
    // ================= UPDATE =================

    @Override
    public PatientResponseDTO updatePatient(Long id, PatientRequestDTO dto) {

        Patient existing = repository.findById(id).orElse(null);

        if (existing == null) {
            return null;
        }

        existing.setCodePatient(dto.getCodePatient());
        existing.setNom(dto.getNom());
        existing.setPrenom(dto.getPrenom());
        existing.setDateNaissance(dto.getDateNaissance());
        existing.setSexe(dto.getSexe());

        Patient updated = repository.save(existing);

        return convertToDTO(updated);
    }

// ================= DELETE =================

    @Override
    public void deletePatient(Long id) {
        repository.deleteById(id);
    }

}
