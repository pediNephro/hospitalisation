package com.esprit.microservice.hospitalisation.dto;

import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class HospitalisationRequestDTO {

    @NotNull(message = "La date d'entrée est obligatoire")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "UTC")
    private Date dateEntree;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "UTC")
    private Date dateSortie;

    @NotBlank(message = "Le motif est obligatoire")
    @Size(min = 3, max = 100, message = "Le motif doit contenir entre 3 et 100 caractères")
    private String motif;

    @NotBlank(message = "Le service est obligatoire")
    private String service;  // ✅ Supprimé le @Pattern qui causait des problèmes d'encodage

    @NotNull(message = "Le patient est obligatoire")
    @Positive(message = "ID patient invalide")
    private Long patientId;

    public HospitalisationRequestDTO() {}

    public Date getDateEntree() { return dateEntree; }
    public void setDateEntree(Date dateEntree) { this.dateEntree = dateEntree; }

    public Date getDateSortie() { return dateSortie; }
    public void setDateSortie(Date dateSortie) { this.dateSortie = dateSortie; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public String getService() { return service; }
    public void setService(String service) { this.service = service; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
}