package com.esprit.microservice.hospitalisation.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.esprit.microservice.hospitalisation.entities.Patient;

import java.util.List;

@Repository
public interface PatientRepository extends CrudRepository<Patient, Long> {

    // Méthode personnalisée pour trouver un patient par code
    Patient findByCodePatient(String codePatient);

    // Méthode personnalisée pour trouver les patients par nom
    List<Patient> findByNom(String nom);

    // Méthode personnalisée pour trouver les patients par nom et prénom
    List<Patient> findByNomAndPrenom(String nom, String prenom);

    // Méthode personnalisée pour trouver les patients par sexe
    List<Patient> findBySexe(String sexe);
}