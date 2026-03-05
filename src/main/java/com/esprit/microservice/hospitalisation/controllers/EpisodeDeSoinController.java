package com.esprit.microservice.hospitalisation.controllers;

import com.esprit.microservice.hospitalisation.dto.EpisodeRequestDTO;
import com.esprit.microservice.hospitalisation.dto.EpisodeResponseDTO;
import com.esprit.microservice.hospitalisation.services.IEpisodeDeSoinService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/episodedesoin")
public class EpisodeDeSoinController {

    private final IEpisodeDeSoinService episodeService;

    public EpisodeDeSoinController(IEpisodeDeSoinService episodeService) {
        this.episodeService = episodeService;
    }

    // ================= CRUD =================

    @PostMapping
    public EpisodeResponseDTO create(@Valid @RequestBody EpisodeRequestDTO dto) {
        return episodeService.addEpisode(dto);
    }

    @GetMapping
    public List<EpisodeResponseDTO> getAll() {
        return episodeService.getAllEpisodes();
    }

    @GetMapping("/{id}")
    public EpisodeResponseDTO getById(@PathVariable Long id) {
        return episodeService.getEpisode(id);
    }

    @PutMapping("/{id}")
    public EpisodeResponseDTO update(@PathVariable Long id,
                                     @Valid @RequestBody EpisodeRequestDTO dto) {
        return episodeService.updateEpisode(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        episodeService.deleteEpisode(id);
    }

    // ================= FILTRES =================

    @GetMapping("/type/{type}")
    public List<EpisodeResponseDTO> getByType(@PathVariable String type) {
        return episodeService.getByType(type);
    }

    @GetMapping("/hospitalisation/{id}")
    public List<EpisodeResponseDTO> getByHospitalisation(@PathVariable Long id) {
        return episodeService.getByHospitalisation(id);
    }

    @GetMapping("/date/{date}")
    public List<EpisodeResponseDTO> getByDate(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        return episodeService.getByDate(date);
    }

    @GetMapping("/between")
    public List<EpisodeResponseDTO> getBetweenDates(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date end) {
        return episodeService.getBetweenDates(start, end);
    }

    // ================= FONCTION AVANCÉE 1 : File d'attente =================

    @GetMapping("/file-attente/{serviceName}")
    public List<Map<String, Object>> getFileAttente(@PathVariable String serviceName) {
        return episodeService.getFileAttente(serviceName);
    }

    // ================= FONCTION AVANCÉE 2 : Timeline =================

    @GetMapping("/timeline/{patientId}")
    public Map<String, Object> getTimeline(@PathVariable Long patientId) {
        return episodeService.getTimelineParcours(patientId);
    }
    // ================= SIGNATURE MEDICALE =================
    @PostMapping("/{id}/signature")
    public EpisodeResponseDTO signerEpisode(
            @PathVariable Long id,
            @RequestParam String medecin) {

        return episodeService.signerEpisode(id, medecin);
    }
}