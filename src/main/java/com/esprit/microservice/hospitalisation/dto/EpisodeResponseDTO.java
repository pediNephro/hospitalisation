package com.esprit.microservice.hospitalisation.dto;

import java.util.Date;

public class EpisodeResponseDTO {

    private Long id;
    private String type;
    private Date date;
    private String description;
    private Long hospitalisationId;
    private Long patientId;
    private String nomPatient;
    private String prenomPatient;
    private String service;

    // SIGNATURE MEDICALE
    private String status;
    private String medecinValidation;
    private Date dateValidation;

    public EpisodeResponseDTO() {}

    public EpisodeResponseDTO(Long id, String type, Date date, String description,
                              Long hospitalisationId, Long patientId,
                              String nomPatient, String prenomPatient, String service) {
        this.id = id;
        this.type = type;
        this.date = date;
        this.description = description;
        this.hospitalisationId = hospitalisationId;
        this.patientId = patientId;
        this.nomPatient = nomPatient;
        this.prenomPatient = prenomPatient;
        this.service = service;
    }

    public Long getId() { return id; }
    public String getType() { return type; }
    public Date getDate() { return date; }
    public String getDescription() { return description; }
    public Long getHospitalisationId() { return hospitalisationId; }
    public Long getPatientId() { return patientId; }
    public String getNomPatient() { return nomPatient; }
    public String getPrenomPatient() { return prenomPatient; }
    public String getService() { return service; }

    public String getStatus() { return status; }
    public String getMedecinValidation() { return medecinValidation; }
    public Date getDateValidation() { return dateValidation; }

    public void setId(Long id) { this.id = id; }
    public void setType(String type) { this.type = type; }
    public void setDate(Date date) { this.date = date; }
    public void setDescription(String description) { this.description = description; }
    public void setHospitalisationId(Long hospitalisationId) { this.hospitalisationId = hospitalisationId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public void setNomPatient(String nomPatient) { this.nomPatient = nomPatient; }
    public void setPrenomPatient(String prenomPatient) { this.prenomPatient = prenomPatient; }
    public void setService(String service) { this.service = service; }

    public void setStatus(String status) { this.status = status; }
    public void setMedecinValidation(String medecinValidation) { this.medecinValidation = medecinValidation; }
    public void setDateValidation(Date dateValidation) { this.dateValidation = dateValidation; }
}