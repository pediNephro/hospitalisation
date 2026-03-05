package com.esprit.microservice.hospitalisation.services;

import com.esprit.microservice.hospitalisation.dto.HospitalisationRequestDTO;
import com.esprit.microservice.hospitalisation.dto.HospitalisationResponseDTO;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface IHospitalisationService {

    // ================= CRUD =================
    HospitalisationResponseDTO addHospitalisation(HospitalisationRequestDTO dto);
    HospitalisationResponseDTO getHospitalisation(Long id);
    HospitalisationResponseDTO updateHospitalisation(Long id, HospitalisationRequestDTO dto);
    void deleteHospitalisation(Long id);
    List<HospitalisationResponseDTO> getAllHospitalisations();

    // ================= FILTRES =================
    List<HospitalisationResponseDTO> getHospitalisationsByMotif(String motif);
    List<HospitalisationResponseDTO> getHospitalisationsByService(String service);
    List<HospitalisationResponseDTO> getHospitalisationsByDateEntree(Date dateEntree);
    List<HospitalisationResponseDTO> getHospitalisationsBetweenDates(Date dateDebut, Date dateFin);
    List<HospitalisationResponseDTO> getHospitalisationsEnCours();
    List<HospitalisationResponseDTO> getHospitalisationsParStatut(String statut);

    // ================= LIBÉRER UN LIT =================
    void libererLit(Long hospitalisationId);

    // ================= GESTION LITS TEMPS RÉEL =================
    Map<String, Object> getStatistiquesLitsTempsReel(String service);

    // ================= ANALYSE HISTORIQUE PATIENT =================
    Map<String, Object> analyseHistoriquePatient(Long patientId);
    List<Map<String, Object>> identifierRecurrences(Long patientId);

    // ================= STATISTIQUES PAR PÉRIODE =================
    Map<String, Object> getStatistiquesService(String service, Date dateDebut, Date dateFin);

    // ================= PRÉDICTIONS =================
    Map<String, Object> predireOccupationService(String service);
    Map<String, Object> predireDureeSejour(String service, String motif);
    Map<String, Object> getTableauDeBordPredictif();

    // ================= RECHERCHE AVANCÉE =================
    Map<String, Object> rechercheAvancee(String service, String statut, String motif,
                                         Long patientId, Date dateDebut, Date dateFin,
                                         Integer dureeMin, Integer dureeMax,
                                         int page, int size, String sortBy);

    // ================= CARTE DE CHALEUR =================
    Map<String, Object> getCarteDeChалeur(String service, int annee);

    // ================= PROFIL MÉDICAL 360° =================
    Map<String, Object> getProfilMedicalComplet(Long patientId);
}