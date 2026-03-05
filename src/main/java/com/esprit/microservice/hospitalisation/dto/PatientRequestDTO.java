package com.esprit.microservice.hospitalisation.dto;

import java.util.Date;

public class PatientRequestDTO {

    private String codePatient;
    private String nom;
    private String prenom;
    private Date dateNaissance;
    private String sexe;

    public PatientRequestDTO() {}

    public String getCodePatient() { return codePatient; }
    public void setCodePatient(String codePatient) { this.codePatient = codePatient; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public Date getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(Date dateNaissance) { this.dateNaissance = dateNaissance; }

    public String getSexe() { return sexe; }
    public void setSexe(String sexe) { this.sexe = sexe; }
}
