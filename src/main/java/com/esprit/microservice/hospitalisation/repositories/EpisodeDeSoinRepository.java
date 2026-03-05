package com.esprit.microservice.hospitalisation.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.esprit.microservice.hospitalisation.entities.EpisodeDeSoin;

import java.util.Date;
import java.util.List;

@Repository
public interface EpisodeDeSoinRepository extends CrudRepository<EpisodeDeSoin, Long> {

    // Méthode personnalisée pour trouver les épisodes par type
    List<EpisodeDeSoin> findByType(String type);

    // Méthode personnalisée pour trouver les épisodes par hospitalisation
    List<EpisodeDeSoin> findByHospitalisation_Id(Long hospitalisationId);

    // Méthode personnalisée pour trouver les épisodes par date
    List<EpisodeDeSoin> findByDate(Date date);

    // Méthode personnalisée pour trouver les épisodes entre deux dates
    List<EpisodeDeSoin> findByDateBetween(Date dateDebut, Date dateFin);
}