package com.esprit.microservice.hospitalisation.services;

import com.esprit.microservice.hospitalisation.dto.EpisodeRequestDTO;
import com.esprit.microservice.hospitalisation.dto.EpisodeResponseDTO;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface IEpisodeDeSoinService {

    EpisodeResponseDTO addEpisode(EpisodeRequestDTO dto);

    EpisodeResponseDTO getEpisode(Long id);

    EpisodeResponseDTO updateEpisode(Long id, EpisodeRequestDTO dto);

    void deleteEpisode(Long id);

    List<EpisodeResponseDTO> getAllEpisodes();

    List<EpisodeResponseDTO> getByType(String type);

    List<EpisodeResponseDTO> getByHospitalisation(Long hospitalisationId);

    List<EpisodeResponseDTO> getByDate(Date date);

    List<EpisodeResponseDTO> getBetweenDates(Date dateDebut, Date dateFin);

    List<Map<String, Object>> getFileAttente(String serviceName);

    Map<String, Object> getTimelineParcours(Long patientId);

    // SIGNATURE MEDICALE
    EpisodeResponseDTO signerEpisode(Long episodeId, String medecin);
}