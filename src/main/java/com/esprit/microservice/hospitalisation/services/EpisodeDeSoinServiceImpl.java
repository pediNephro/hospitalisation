package com.esprit.microservice.hospitalisation.services;

import com.esprit.microservice.hospitalisation.dto.EpisodeRequestDTO;
import com.esprit.microservice.hospitalisation.dto.EpisodeResponseDTO;
import com.esprit.microservice.hospitalisation.entities.EpisodeDeSoin;
import com.esprit.microservice.hospitalisation.entities.Hospitalisation;
import com.esprit.microservice.hospitalisation.repositories.EpisodeDeSoinRepository;
import com.esprit.microservice.hospitalisation.repositories.HospitalisationRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EpisodeDeSoinServiceImpl implements IEpisodeDeSoinService {

    private final EpisodeDeSoinRepository repository;
    private final HospitalisationRepository hospitalisationRepository;

    // ================= DURÉE PAR TYPE =================

    private static final Map<String, Integer> DUREE_PAR_TYPE = new HashMap<>();

    static {
        DUREE_PAR_TYPE.put("Consultation", 15);
        DUREE_PAR_TYPE.put("Analyse", 20);
        DUREE_PAR_TYPE.put("Chirurgie", 60);
        DUREE_PAR_TYPE.put("Radiologie", 25);
        DUREE_PAR_TYPE.put("Urgence", 10);
        DUREE_PAR_TYPE.put("Kinesitherapie", 30);
        DUREE_PAR_TYPE.put("Dialyse", 240);
        DUREE_PAR_TYPE.put("Suivi", 15);
    }

    public EpisodeDeSoinServiceImpl(EpisodeDeSoinRepository repository,
                                    HospitalisationRepository hospitalisationRepository) {
        this.repository = repository;
        this.hospitalisationRepository = hospitalisationRepository;
    }

    // ================= MAPPING =================

    private EpisodeResponseDTO convertToDTO(EpisodeDeSoin e) {

        Long hospitalisationId = null;
        Long patientId = null;
        String nom = null;
        String prenom = null;
        String service = null;

        if (e.getHospitalisation() != null) {
            hospitalisationId = e.getHospitalisation().getId();
            service = e.getHospitalisation().getService();

            if (e.getHospitalisation().getPatient() != null) {
                patientId = e.getHospitalisation().getPatient().getId();
                nom = e.getHospitalisation().getPatient().getNom();
                prenom = e.getHospitalisation().getPatient().getPrenom();
            }
        }

        return new EpisodeResponseDTO(
                e.getId(),
                e.getType(),
                e.getDate(),
                e.getDescription(),
                hospitalisationId,
                patientId,
                nom,
                prenom,
                service
        );
    }

    // ================= CRUD =================

    @Override
    public EpisodeResponseDTO addEpisode(EpisodeRequestDTO dto) {

        Hospitalisation hospitalisation = hospitalisationRepository
                .findById(dto.getHospitalisationId())
                .orElseThrow(() -> new RuntimeException("Hospitalisation introuvable"));

        if (dto.getDate().before(hospitalisation.getDateEntree())) {
            throw new RuntimeException("La date de l'épisode ne peut pas être avant la date d'entrée");
        }

        EpisodeDeSoin e = new EpisodeDeSoin();
        e.setType(dto.getType());
        e.setDate(dto.getDate());
        e.setDescription(dto.getDescription());
        e.setHospitalisation(hospitalisation);

        return convertToDTO(repository.save(e));
    }

    @Override
    public EpisodeResponseDTO getEpisode(Long id) {
        return repository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Override
    public EpisodeResponseDTO updateEpisode(Long id, EpisodeRequestDTO dto) {

        EpisodeDeSoin existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Episode introuvable"));

        Hospitalisation hospitalisation = hospitalisationRepository
                .findById(dto.getHospitalisationId())
                .orElseThrow(() -> new RuntimeException("Hospitalisation introuvable"));

        if (dto.getDate().before(hospitalisation.getDateEntree())) {
            throw new RuntimeException("La date de l'épisode ne peut pas être avant la date d'entrée");
        }

        existing.setType(dto.getType());
        existing.setDate(dto.getDate());
        existing.setDescription(dto.getDescription());
        existing.setHospitalisation(hospitalisation);

        return convertToDTO(repository.save(existing));
    }

    @Override
    public void deleteEpisode(Long id) {
        repository.deleteById(id);
    }

    @Override
    public List<EpisodeResponseDTO> getAllEpisodes() {
        return ((List<EpisodeDeSoin>) repository.findAll())
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Override
    public List<EpisodeResponseDTO> getByType(String type) {
        return repository.findByType(type)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Override
    public List<EpisodeResponseDTO> getByHospitalisation(Long hospitalisationId) {
        return repository.findByHospitalisation_Id(hospitalisationId)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Override
    public List<EpisodeResponseDTO> getByDate(Date date) {
        return repository.findByDate(date)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Override
    public List<EpisodeResponseDTO> getBetweenDates(Date start, Date end) {
        return repository.findByDateBetween(start, end)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    // ============================================================
    // FILE D'ATTENTE DIGITALE
    // ============================================================

    @Override
    public List<Map<String, Object>> getFileAttente(String serviceName) {

        Date maintenant = new Date();

        List<EpisodeDeSoin> episodesService = ((List<EpisodeDeSoin>) repository.findAll())
                .stream()
                .filter(e -> e.getHospitalisation() != null
                        && e.getHospitalisation().getService() != null
                        && e.getHospitalisation().getService().equalsIgnoreCase(serviceName)
                        && e.getHospitalisation().getDateSortie() == null)
                .sorted(Comparator
                        .comparingInt((EpisodeDeSoin e) -> getPriorite(e.getType()))
                        .thenComparing(EpisodeDeSoin::getDate))
                .toList();

        List<Map<String, Object>> file = new ArrayList<>();

        int tempsAttenteCumule = 0;
        int position = 1;

        for (EpisodeDeSoin e : episodesService) {

            int dureeEstimee = DUREE_PAR_TYPE.getOrDefault(e.getType(), 20);

            Map<String, Object> entree = new HashMap<>();
            entree.put("position", position);
            entree.put("episodeId", e.getId());
            entree.put("type", e.getType());
            entree.put("priorite", getPriorite(e.getType()));
            entree.put("niveauPriorite", getNiveauPriorite(e.getType()));
            entree.put("description", e.getDescription());
            entree.put("dureeEstimeeMinutes", dureeEstimee);
            entree.put("tempsAttenteMinutes", tempsAttenteCumule);
            entree.put("heurePassageEstimee", calculerHeurePassage(maintenant, tempsAttenteCumule));

            file.add(entree);

            tempsAttenteCumule += dureeEstimee;
            position++;
        }

        Map<String, Object> resume = new HashMap<>();
        resume.put("service", serviceName);
        resume.put("nombrePatientsEnAttente", episodesService.size());
        resume.put("tempsAttenteTotal", tempsAttenteCumule);
        resume.put("file", file);

        return List.of(resume);
    }

    // ============================================================
    // TIMELINE PARCOURS PATIENT
    // ============================================================

    @Override
    public Map<String, Object> getTimelineParcours(Long patientId) {

        List<EpisodeDeSoin> tousEpisodes = ((List<EpisodeDeSoin>) repository.findAll())
                .stream()
                .filter(e -> e.getHospitalisation() != null
                        && e.getHospitalisation().getPatient() != null
                        && e.getHospitalisation().getPatient().getId().equals(patientId)
                        && e.getDate() != null)
                .sorted(Comparator.comparing(EpisodeDeSoin::getDate))
                .toList();

        List<Map<String, Object>> timeline = new ArrayList<>();
        Date datePrecedente = null;

        for (EpisodeDeSoin e : tousEpisodes) {

            Map<String, Object> jalon = new HashMap<>();
            jalon.put("episodeId", e.getId());
            jalon.put("type", e.getType());
            jalon.put("date", e.getDate());
            jalon.put("description", e.getDescription());

            if (datePrecedente != null) {
                long diff = e.getDate().getTime() - datePrecedente.getTime();
                long jours = diff / (1000 * 60 * 60 * 24);
                jalon.put("joursDepuisEpisodePrecedent", jours);
            } else {
                jalon.put("joursDepuisEpisodePrecedent", 0);
                jalon.put("premierEpisode", true);
            }

            timeline.add(jalon);
            datePrecedente = e.getDate();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("patientId", patientId);
        result.put("totalEpisodes", tousEpisodes.size());
        result.put("timeline", timeline);

        return result;
    }

    // ================= HELPERS =================

    private int getPriorite(String type) {

        if (type == null) return 7;

        switch (type.toLowerCase()) {
            case "urgence": return 1;
            case "chirurgie": return 2;
            case "dialyse": return 3;
            case "analyse": return 4;
            case "radiologie": return 5;
            case "consultation": return 6;
            default: return 7;
        }
    }

    private String calculerHeurePassage(Date maintenant, int minutesAttente) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(maintenant);
        cal.add(Calendar.MINUTE, minutesAttente);

        return String.format("%02d:%02d",
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE));
    }

    private String getNiveauPriorite(String type) {
        int p = getPriorite(type);
        if (p <= 2) return "CRITIQUE";
        if (p <= 4) return "NORMAL";
        return "FAIBLE";
    }

    @Override
    public EpisodeResponseDTO signerEpisode(Long episodeId, String medecin) {

        EpisodeDeSoin episode = repository.findById(episodeId)
                .orElseThrow(() -> new RuntimeException("Episode introuvable"));

        episode.setStatus("VALIDE");
        episode.setMedecinValidation(medecin);
        episode.setDateValidation(new Date());

        String signature = medecin + "_" + episodeId + "_" + System.currentTimeMillis();
        episode.setSignature(signature);

        repository.save(episode);

        EpisodeResponseDTO dto = convertToDTO(episode);

        dto.setStatus(episode.getStatus());
        dto.setMedecinValidation(episode.getMedecinValidation());
        dto.setDateValidation(episode.getDateValidation());

        return dto;
    }
}