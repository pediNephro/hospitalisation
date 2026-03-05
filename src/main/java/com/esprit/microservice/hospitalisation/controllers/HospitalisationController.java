package com.esprit.microservice.hospitalisation.controllers;

import com.esprit.microservice.hospitalisation.dto.HospitalisationRequestDTO;
import com.esprit.microservice.hospitalisation.dto.HospitalisationResponseDTO;
import com.esprit.microservice.hospitalisation.services.IHospitalisationService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hospitalisation")
public class HospitalisationController {

    private final IHospitalisationService hospitalisationService;

    public HospitalisationController(IHospitalisationService hospitalisationService) {
        this.hospitalisationService = hospitalisationService;
    }

    // ================= CRUD =================

    @PostMapping
    public HospitalisationResponseDTO create(@Valid @RequestBody HospitalisationRequestDTO dto) {
        return hospitalisationService.addHospitalisation(dto);
    }

    @GetMapping
    public List<HospitalisationResponseDTO> getAll() {
        return hospitalisationService.getAllHospitalisations();
    }

    @GetMapping("/{id}")
    public HospitalisationResponseDTO getById(@PathVariable Long id) {
        return hospitalisationService.getHospitalisation(id);
    }

    @PutMapping("/{id}")
    public HospitalisationResponseDTO update(@PathVariable Long id,
                                             @Valid @RequestBody HospitalisationRequestDTO dto) {
        return hospitalisationService.updateHospitalisation(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        hospitalisationService.deleteHospitalisation(id);
    }

    // ================= FILTRES =================

    @GetMapping("/statut/{statut}")
    public List<HospitalisationResponseDTO> getByStatut(@PathVariable String statut) {
        return hospitalisationService.getHospitalisationsParStatut(statut);
    }

    @GetMapping("/service/{service}")
    public List<HospitalisationResponseDTO> getByService(@PathVariable String service) {
        return hospitalisationService.getHospitalisationsByService(service);
    }

    @GetMapping("/en-cours")
    public List<HospitalisationResponseDTO> getEnCours() {
        return hospitalisationService.getHospitalisationsEnCours();
    }

    // ================= LIBÉRER UN LIT =================

    @PutMapping("/libererLit/{id}")
    public void libererLit(@PathVariable Long id) {
        hospitalisationService.libererLit(id);
    }

    // ================= GESTION LITS TEMPS RÉEL =================

    @GetMapping("/statistiques/lits/{service}")
    public Map<String, Object> getStatistiquesLits(@PathVariable String service) {
        return hospitalisationService.getStatistiquesLitsTempsReel(service);
    }

    // ================= ANALYSE HISTORIQUE PATIENT =================

    @GetMapping("/patient/analyse/{patientId}")
    public Map<String, Object> analyseHistorique(@PathVariable Long patientId) {
        return hospitalisationService.analyseHistoriquePatient(patientId);
    }

    @GetMapping("/patient/recurrences/{patientId}")
    public List<Map<String, Object>> getRecurrences(@PathVariable Long patientId) {
        return hospitalisationService.identifierRecurrences(patientId);
    }

    // ================= STATISTIQUES PAR PÉRIODE =================

    @GetMapping("/statistiques/service/{service}")
    public Map<String, Object> getStatistiquesService(
            @PathVariable String service,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateDebut,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateFin) {
        return hospitalisationService.getStatistiquesService(service, dateDebut, dateFin);
    }

    // ================= PRÉDICTIONS =================

    @GetMapping("/prediction/occupation/{service}")
    public Map<String, Object> predireOccupation(@PathVariable String service) {
        return hospitalisationService.predireOccupationService(service);
    }

    @GetMapping("/prediction/duree-sejour")
    public Map<String, Object> predireDuree(
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String motif) {
        return hospitalisationService.predireDureeSejour(service, motif);
    }

    @GetMapping("/prediction/tableau-de-bord")
    public Map<String, Object> tableauDeBord() {
        return hospitalisationService.getTableauDeBordPredictif();
    }

    // ================= RECHERCHE AVANCÉE =================

    @GetMapping("/recherche")
    public Map<String, Object> rechercheAvancee(
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String motif,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateDebut,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateFin,
            @RequestParam(required = false) Integer dureeMin,
            @RequestParam(required = false) Integer dureeMax,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date") String sortBy) {
        return hospitalisationService.rechercheAvancee(
                service, statut, motif, patientId, dateDebut, dateFin,
                dureeMin, dureeMax, page, size, sortBy);
    }

    // ================= CARTE DE CHALEUR =================

    @GetMapping("/analytics/heatmap")
    public Map<String, Object> carteDeChалeur(
            @RequestParam(required = false) String service,
            @RequestParam(defaultValue = "2025") int annee) {
        return hospitalisationService.getCarteDeChалeur(service, annee);
    }

    // ================= PROFIL MÉDICAL COMPLET =================

    @GetMapping("/patient/profil/{patientId}")
    public Map<String, Object> getProfilMedical(@PathVariable Long patientId) {
        return hospitalisationService.getProfilMedicalComplet(patientId);
    }
}