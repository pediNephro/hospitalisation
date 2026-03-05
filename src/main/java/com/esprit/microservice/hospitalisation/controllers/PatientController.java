package com.esprit.microservice.hospitalisation.controllers;

import com.esprit.microservice.hospitalisation.dto.PatientRequestDTO;
import com.esprit.microservice.hospitalisation.dto.PatientResponseDTO;
import com.esprit.microservice.hospitalisation.services.IPatientService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/patient")
@CrossOrigin(origins = "http://localhost:4200")
public class PatientController {

    private final IPatientService service;

    public PatientController(IPatientService service) {
        this.service = service;
    }

    // ===============================
    // CREATE
    // ===============================
    @PostMapping
    public PatientResponseDTO create(@RequestBody PatientRequestDTO dto) {
        return service.addPatient(dto);
    }

    // ===============================
    // GET ALL
    // ===============================
    @GetMapping
    public List<PatientResponseDTO> getAll() {
        return service.getAllPatients();
    }

    // ===============================
    // GET BY ID
    // ===============================
    @GetMapping("/{id}")
    public PatientResponseDTO getById(@PathVariable Long id) {
        return service.getPatient(id);
    }

    // ===============================
    // UPDATE
    // ===============================
    @PutMapping("/{id}")
    public PatientResponseDTO update(@PathVariable Long id,
                                     @RequestBody PatientRequestDTO dto) {
        return service.updatePatient(id, dto);
    }

    // ===============================
    // DELETE
    // ===============================
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.deletePatient(id);
    }
}
