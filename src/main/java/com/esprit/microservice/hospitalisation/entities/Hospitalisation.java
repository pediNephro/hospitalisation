package com.esprit.microservice.hospitalisation.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Hospitalisation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date dateEntree;

    private Date dateSortie;

    private String motif;
    private String service;

    // Relation ManyToOne avec Patient
    @ManyToOne
    @JsonIgnore
    private Patient patient;

    // Relation OneToMany avec EpisodeDeSoin
    @OneToMany(mappedBy = "hospitalisation", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<EpisodeDeSoin> episodeDeSoins;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDateEntree() {
        return dateEntree;
    }

    public void setDateEntree(Date dateEntree) {
        this.dateEntree = dateEntree;
    }

    public Date getDateSortie() {
        return dateSortie;
    }

    public void setDateSortie(Date dateSortie) {
        this.dateSortie = dateSortie;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public List<EpisodeDeSoin> getEpisodeDeSoins() {
        return episodeDeSoins;
    }

    public void setEpisodeDeSoins(List<EpisodeDeSoin> episodeDeSoins) {
        this.episodeDeSoins = episodeDeSoins;
    }
}