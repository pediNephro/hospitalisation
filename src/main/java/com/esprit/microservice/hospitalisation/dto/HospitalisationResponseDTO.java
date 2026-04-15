package com.esprit.microservice.hospitalisation.dto;


import java.util.Date;

public class HospitalisationResponseDTO {

    private Long id;
    private Date dateEntree;
    private Date dateSortie;
    private String motif;
    private String service;

    private Long patientId;
    private String nomPatient;
    private String prenomPatient;

    private String statut;

    public HospitalisationResponseDTO() {}

    public HospitalisationResponseDTO(Long id, Date dateEntree, Date dateSortie,
                                      String motif, String service,
                                      Long patientId, String nomPatient,
                                      String prenomPatient, String statut) {
        this.id = id;
        this.dateEntree = dateEntree;
        this.dateSortie = dateSortie;
        this.motif = motif;
        this.service = service;
        this.patientId = patientId;
        this.nomPatient = nomPatient;
        this.prenomPatient = prenomPatient;
        this.statut = statut;
    }

    // getters & setters
    public Long getId() { return id; }
    public Date getDateEntree() { return dateEntree; }
    public Date getDateSortie() { return dateSortie; }
    public String getMotif() { return motif; }
    public String getService() { return service; }
    public Long getPatientId() { return patientId; }
    public String getNomPatient() { return nomPatient; }
    public String getPrenomPatient() { return prenomPatient; }
    public String getStatut() { return statut; }

    public void setId(Long id) { this.id = id; }
    public void setDateEntree(Date dateEntree) { this.dateEntree = dateEntree; }
    public void setDateSortie(Date dateSortie) { this.dateSortie = dateSortie; }
    public void setMotif(String motif) { this.motif = motif; }
    public void setService(String service) { this.service = service; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public void setNomPatient(String nomPatient) { this.nomPatient = nomPatient; }
    public void setPrenomPatient(String prenomPatient) { this.prenomPatient = prenomPatient; }
    public void setStatut(String statut) { this.statut = statut; }
}