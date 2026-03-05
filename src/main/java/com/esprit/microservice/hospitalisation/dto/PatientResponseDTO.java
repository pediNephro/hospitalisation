package com.esprit.microservice.hospitalisation.dto;

import java.util.Date;

public class PatientResponseDTO {

    private Long id;
    private String codePatient;
    private String nom;
    private String prenom;
    private Date dateNaissance;
    private String sexe;

    public PatientResponseDTO() {}

    public PatientResponseDTO(Long id, String codePatient,
                              String nom, String prenom,
                              Date dateNaissance, String sexe) {
        this.id = id;
        this.codePatient = codePatient;
        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
        this.sexe = sexe;
    }

    // getters & setters
    public Long getId() { return id; }
    public String getCodePatient() { return codePatient; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public Date getDateNaissance() { return dateNaissance; }
    public String getSexe() { return sexe; }

    public void setId(Long id) { this.id = id; }
    public void setCodePatient(String codePatient) { this.codePatient = codePatient; }
    public void setNom(String nom) { this.nom = nom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public void setDateNaissance(Date dateNaissance) { this.dateNaissance = dateNaissance; }
    public void setSexe(String sexe) { this.sexe = sexe; }
}
