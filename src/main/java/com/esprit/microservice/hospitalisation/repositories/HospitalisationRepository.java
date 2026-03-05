package com.esprit.microservice.hospitalisation.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.esprit.microservice.hospitalisation.entities.Hospitalisation;

import java.util.Date;
import java.util.List;

@Repository
public interface HospitalisationRepository extends CrudRepository<Hospitalisation, Long> {

    // Méthode personnalisée pour trouver les hospitalisations par motif
    List<Hospitalisation> findByMotif(String motif);

    // Méthode personnalisée pour trouver les hospitalisations par service
    List<Hospitalisation> findByService(String service);

    // TODO: Décommenter cette méthode quand l'entité Patient sera créée
    // List<Hospitalisation> findByPatient_Id(Long patientId);

    // Méthode personnalisée pour trouver les hospitalisations par date d'entrée
    List<Hospitalisation> findByDateEntree(Date dateEntree);

    // Méthode personnalisée pour trouver les hospitalisations entre deux dates
    List<Hospitalisation> findByDateEntreeBetween(Date dateDebut, Date dateFin);

    // Méthode pour trouver les hospitalisations en cours (dateSortie est null)
    List<Hospitalisation> findByDateSortieIsNull();
}