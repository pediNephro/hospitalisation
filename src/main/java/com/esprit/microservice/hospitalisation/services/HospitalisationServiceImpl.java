package com.esprit.microservice.hospitalisation.services;

import com.esprit.microservice.hospitalisation.dto.HospitalisationRequestDTO;
import com.esprit.microservice.hospitalisation.dto.HospitalisationResponseDTO;
import com.esprit.microservice.hospitalisation.entities.Hospitalisation;
import com.esprit.microservice.hospitalisation.entities.Patient;
import com.esprit.microservice.hospitalisation.repositories.HospitalisationRepository;
import com.esprit.microservice.hospitalisation.repositories.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class HospitalisationServiceImpl implements IHospitalisationService {

    private final HospitalisationRepository hospitalisationRepository;
    private final PatientRepository patientRepository;
    private final TwilioSmsService twilioSmsService;

    private static final int CAPACITE_SERVICE = 50;
    private static final long DUREE_NETTOYAGE_HEURES = 2;

    public HospitalisationServiceImpl(HospitalisationRepository hospitalisationRepository,
                                      PatientRepository patientRepository,
                                      TwilioSmsService twilioSmsService) {
        this.hospitalisationRepository = hospitalisationRepository;
        this.patientRepository = patientRepository;
        this.twilioSmsService = twilioSmsService;
    }

    // ================= MAPPING =================

    private HospitalisationResponseDTO convertToDTO(Hospitalisation h) {
        String statut = (h.getDateSortie() == null) ? "EN_COURS" : "TERMINEE";
        Long patientId = null;
        String nom = null;
        String prenom = null;
        if (h.getPatient() != null) {
            patientId = h.getPatient().getId();
            nom = h.getPatient().getNom();
            prenom = h.getPatient().getPrenom();
        }
        return new HospitalisationResponseDTO(
                h.getId(), h.getDateEntree(), h.getDateSortie(),
                h.getMotif(), h.getService(),
                patientId, nom, prenom, statut
        );
    }

    // ================= CRUD =================

    @Override
    public HospitalisationResponseDTO addHospitalisation(HospitalisationRequestDTO dto) {
        if (dto.getDateSortie() != null && dto.getDateSortie().before(dto.getDateEntree()))
            throw new RuntimeException("La date de sortie doit etre apres la date d'entree");

        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient introuvable"));

        long litsOccupes = hospitalisationRepository.findByService(dto.getService())
                .stream().filter(h -> h.getDateSortie() == null).count();

        if (litsOccupes >= CAPACITE_SERVICE)
            throw new RuntimeException("Capacite maximale atteinte pour ce service");

        boolean dejaHospitalise = hospitalisationRepository.findByService(dto.getService())
                .stream().anyMatch(h -> h.getDateSortie() == null
                        && h.getPatient() != null
                        && h.getPatient().getId().equals(dto.getPatientId()));

        if (dejaHospitalise)
            throw new RuntimeException("Ce patient est deja hospitalise dans le service " + dto.getService());

        Hospitalisation h = new Hospitalisation();
        h.setDateEntree(dto.getDateEntree());
        h.setDateSortie(dto.getDateSortie());
        h.setMotif(dto.getMotif());
        h.setService(dto.getService());
        h.setPatient(patient);

        Hospitalisation saved = hospitalisationRepository.save(h);

        twilioSmsService.envoyerSmsAdmission(
                patient.getNom() + " " + patient.getPrenom(),
                dto.getService(),
                dto.getDateEntree().toString()
        );

        long litsRestants = CAPACITE_SERVICE - litsOccupes - 1;
        if (litsRestants < 5)
            twilioSmsService.envoyerAlerteManqueLits(dto.getService(), litsRestants);

        return convertToDTO(saved);
    }

    @Override
    public HospitalisationResponseDTO updateHospitalisation(Long id, HospitalisationRequestDTO dto) {
        Hospitalisation existing = hospitalisationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hospitalisation introuvable"));

        if (dto.getDateSortie() != null && dto.getDateSortie().before(dto.getDateEntree()))
            throw new RuntimeException("La date de sortie doit etre apres la date d'entree");

        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient introuvable"));

        existing.setDateEntree(dto.getDateEntree());
        existing.setDateSortie(dto.getDateSortie());
        existing.setMotif(dto.getMotif());
        existing.setService(dto.getService());
        existing.setPatient(patient);

        return convertToDTO(hospitalisationRepository.save(existing));
    }

    @Override
    public void deleteHospitalisation(Long id) {
        hospitalisationRepository.deleteById(id);
    }

    @Override
    public HospitalisationResponseDTO getHospitalisation(Long id) {
        return hospitalisationRepository.findById(id).map(this::convertToDTO).orElse(null);
    }

    @Override
    public List<HospitalisationResponseDTO> getAllHospitalisations() {
        return ((List<Hospitalisation>) hospitalisationRepository.findAll())
                .stream().map(this::convertToDTO).toList();
    }

    @Override
    public List<HospitalisationResponseDTO> getHospitalisationsByMotif(String motif) {
        return hospitalisationRepository.findByMotif(motif).stream().map(this::convertToDTO).toList();
    }

    @Override
    public List<HospitalisationResponseDTO> getHospitalisationsByService(String service) {
        return hospitalisationRepository.findByService(service).stream().map(this::convertToDTO).toList();
    }

    @Override
    public List<HospitalisationResponseDTO> getHospitalisationsByDateEntree(Date dateEntree) {
        return hospitalisationRepository.findByDateEntree(dateEntree).stream().map(this::convertToDTO).toList();
    }

    @Override
    public List<HospitalisationResponseDTO> getHospitalisationsBetweenDates(Date dateDebut, Date dateFin) {
        return hospitalisationRepository.findByDateEntreeBetween(dateDebut, dateFin)
                .stream().map(this::convertToDTO).toList();
    }

    @Override
    public List<HospitalisationResponseDTO> getHospitalisationsEnCours() {
        return hospitalisationRepository.findByDateSortieIsNull().stream().map(this::convertToDTO).toList();
    }

    @Override
    public List<HospitalisationResponseDTO> getHospitalisationsParStatut(String statut) {
        List<Hospitalisation> toutes = (List<Hospitalisation>) hospitalisationRepository.findAll();
        if ("EN_COURS".equalsIgnoreCase(statut))
            return toutes.stream().filter(h -> h.getDateSortie() == null).map(this::convertToDTO).toList();
        if ("TERMINEE".equalsIgnoreCase(statut))
            return toutes.stream().filter(h -> h.getDateSortie() != null).map(this::convertToDTO).toList();
        return toutes.stream().map(this::convertToDTO).toList();
    }

    // ================= LIBÉRER UN LIT =================

    @Override
    public void libererLit(Long id) {
        Hospitalisation h = hospitalisationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hospitalisation introuvable"));
        if (h.getDateSortie() == null) {
            h.setDateSortie(new Date());
            hospitalisationRepository.save(h);
            if (h.getPatient() != null)
                twilioSmsService.envoyerSmsSortie(
                        h.getPatient().getNom() + " " + h.getPatient().getPrenom(), h.getService());
        }
    }

    // ================= GESTION LITS TEMPS RÉEL =================

    @Override
    public Map<String, Object> getStatistiquesLitsTempsReel(String service) {
        List<Hospitalisation> hospitalisations = hospitalisationRepository.findByService(service);
        Date maintenant = new Date();

        long litsOccupes = 0, litsNettoyage = 0;
        for (Hospitalisation h : hospitalisations) {
            if (h.getDateSortie() == null) {
                litsOccupes++;
            } else {
                long heures = (maintenant.getTime() - h.getDateSortie().getTime()) / (1000 * 60 * 60);
                if (heures < DUREE_NETTOYAGE_HEURES) litsNettoyage++;
            }
        }

        long litsLibres = Math.max(0, CAPACITE_SERVICE - litsOccupes - litsNettoyage);
        long hospitalisationsTerminees = hospitalisations.stream().filter(h -> h.getDateSortie() != null).count();

        List<Map<String, Object>> detailLits = new ArrayList<>();
        for (Hospitalisation h : hospitalisations) {
            String statutLit;
            if (h.getDateSortie() == null) {
                statutLit = "OCCUPE";
            } else {
                long heures = (maintenant.getTime() - h.getDateSortie().getTime()) / (1000 * 60 * 60);
                statutLit = (heures < DUREE_NETTOYAGE_HEURES) ? "NETTOYAGE" : "LIBRE";
            }
            if (!"LIBRE".equals(statutLit)) {
                Map<String, Object> lit = new HashMap<>();
                lit.put("hospitalisationId", h.getId());
                lit.put("statutLit", statutLit);
                lit.put("dateEntree", h.getDateEntree());
                lit.put("dateSortie", h.getDateSortie());
                if (h.getPatient() != null) {
                    lit.put("patientId", h.getPatient().getId());
                    lit.put("patientNom", h.getPatient().getNom() + " " + h.getPatient().getPrenom());
                }
                detailLits.add(lit);
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("service", service);
        stats.put("capaciteTotale", CAPACITE_SERVICE);
        stats.put("litsOccupes", litsOccupes);
        stats.put("litsEnNettoyage", litsNettoyage);
        stats.put("litsLibres", litsLibres);
        stats.put("hospitalisationsTerminees", hospitalisationsTerminees);
        stats.put("tauxOccupation", Math.round((litsOccupes * 100.0) / CAPACITE_SERVICE));
        stats.put("detailLits", detailLits);
        return stats;
    }

    // ================= ANALYSE HISTORIQUE PATIENT =================

    @Override
    public Map<String, Object> analyseHistoriquePatient(Long patientId) {
        List<Hospitalisation> hospitalisations = ((List<Hospitalisation>) hospitalisationRepository.findAll())
                .stream()
                .filter(h -> h.getPatient() != null && h.getPatient().getId().equals(patientId))
                .filter(h -> h.getDateEntree() != null)
                .sorted(Comparator.comparing(Hospitalisation::getDateEntree))
                .toList();

        long total = hospitalisations.size();
        List<Hospitalisation> terminees = hospitalisations.stream().filter(h -> h.getDateSortie() != null).toList();

        double dureeMoyenne = 0;
        if (!terminees.isEmpty()) {
            dureeMoyenne = terminees.stream()
                    .mapToLong(h -> Math.max(0, (h.getDateSortie().getTime() - h.getDateEntree().getTime()) / (1000 * 60 * 60 * 24)))
                    .average().orElse(0);
        }

        Map<String, Long> frequenceParService = hospitalisations.stream()
                .filter(h -> h.getService() != null)
                .collect(Collectors.groupingBy(Hospitalisation::getService, Collectors.counting()));

        String serviceLePlusFrequente = frequenceParService.entrySet().stream()
                .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("N/A");

        Hospitalisation derniere = hospitalisations.isEmpty() ? null : hospitalisations.get(hospitalisations.size() - 1);

        int nombreRecurrences = 0;
        try { nombreRecurrences = identifierRecurrences(patientId).size(); } catch (Exception ignored) {}

        String niveauRisque = (total >= 5 || nombreRecurrences > 0) ? "ELEVE"
                : (total >= 3) ? "MOYEN" : (total >= 1) ? "FAIBLE" : "AUCUN";

        Map<String, Object> analyse = new HashMap<>();
        analyse.put("patientId", patientId);
        analyse.put("totalHospitalisations", total);
        analyse.put("hospitalisationsTerminees", terminees.size());
        analyse.put("hospitalisationsEnCours", total - terminees.size());
        analyse.put("dureeMoyenneSejour_jours", Math.round(dureeMoyenne * 10.0) / 10.0);
        analyse.put("frequenceParService", frequenceParService);
        analyse.put("serviceLePlusFrequente", serviceLePlusFrequente);
        analyse.put("niveauRisque", niveauRisque);
        analyse.put("nombreRecurrences", nombreRecurrences);
        analyse.put("alerteRecurrence", nombreRecurrences > 0);

        if (derniere != null) {
            Map<String, Object> d = new HashMap<>();
            d.put("id", derniere.getId());
            d.put("dateEntree", derniere.getDateEntree());
            d.put("dateSortie", derniere.getDateSortie());
            d.put("service", derniere.getService());
            d.put("motif", derniere.getMotif());
            analyse.put("derniereHospitalisation", d);
        }
        return analyse;
    }

    @Override
    public List<Map<String, Object>> identifierRecurrences(Long patientId) {
        List<Hospitalisation> hospitalisations = ((List<Hospitalisation>) hospitalisationRepository.findAll())
                .stream()
                .filter(h -> h.getPatient() != null && h.getPatient().getId().equals(patientId)
                        && h.getDateSortie() != null && h.getDateEntree() != null)
                .sorted(Comparator.comparing(Hospitalisation::getDateEntree))
                .toList();

        List<Map<String, Object>> recurrences = new ArrayList<>();
        for (int i = 1; i < hospitalisations.size(); i++) {
            Hospitalisation precedente = hospitalisations.get(i - 1);
            Hospitalisation actuelle = hospitalisations.get(i);
            long jours = (actuelle.getDateEntree().getTime() - precedente.getDateSortie().getTime()) / (1000 * 60 * 60 * 24);
            if (jours >= 0 && jours <= 30) {
                Map<String, Object> rec = new HashMap<>();
                rec.put("patientId", patientId);
                rec.put("hospitalisationPrecedente_id", precedente.getId());
                rec.put("hospitalisationPrecedente_sortie", precedente.getDateSortie());
                rec.put("hospitalisationActuelle_id", actuelle.getId());
                rec.put("hospitalisationActuelle_entree", actuelle.getDateEntree());
                rec.put("joursEntreDeuxSejours", jours);
                rec.put("servicePrecedent", precedente.getService());
                rec.put("serviceActuel", actuelle.getService());
                rec.put("alerte", "Re-hospitalisation en " + jours + " jour(s) - a surveiller");
                if (actuelle.getPatient() != null)
                    twilioSmsService.envoyerAlerteRecurrence(
                            actuelle.getPatient().getNom() + " " + actuelle.getPatient().getPrenom(),
                            jours, actuelle.getService());
                recurrences.add(rec);
            }
        }
        return recurrences;
    }

    // ================= STATISTIQUES PAR PÉRIODE =================

    @Override
    public Map<String, Object> getStatistiquesService(String service, Date dateDebut, Date dateFin) {
        List<Hospitalisation> list = hospitalisationRepository.findByDateEntreeBetween(dateDebut, dateFin)
                .stream().filter(h -> h.getService().equalsIgnoreCase(service)).toList();
        Map<String, Object> stats = new HashMap<>();
        stats.put("service", service);
        stats.put("periodeDebut", dateDebut);
        stats.put("periodeFin", dateFin);
        stats.put("nombreHospitalisations", list.size());
        stats.put("enCours", list.stream().filter(h -> h.getDateSortie() == null).count());
        stats.put("terminees", list.stream().filter(h -> h.getDateSortie() != null).count());
        return stats;
    }

    // ================= PRÉDICTIONS =================

    @Override
    public Map<String, Object> predireOccupationService(String service) {
        List<Hospitalisation> toutes = hospitalisationRepository.findByService(service);
        Date maintenant = new Date();

        long occupationActuelle = toutes.stream().filter(h -> h.getDateSortie() == null).count();
        double tauxActuel = (occupationActuelle * 100.0) / CAPACITE_SERVICE;

        // Calcul taux d'entrées par jour sur les 30 derniers jours
        long msParJour = 24L * 60 * 60 * 1000;
        long il30Jours = maintenant.getTime() - 30L * msParJour;
        long entreesRecentes = toutes.stream()
                .filter(h -> h.getDateEntree() != null && h.getDateEntree().getTime() >= il30Jours)
                .count();
        double entreesParJour = entreesRecentes / 30.0;

        // Calcul durée moyenne de séjour
        List<Hospitalisation> terminees = toutes.stream().filter(h -> h.getDateSortie() != null).toList();
        double dureeMoyenne = terminees.isEmpty() ? 7 :
                terminees.stream()
                        .mapToLong(h -> Math.max(1, (h.getDateSortie().getTime() - h.getDateEntree().getTime()) / msParJour))
                        .average().orElse(7);
        double sortiesParJour = dureeMoyenne > 0 ? 1.0 / dureeMoyenne : 0;

        // Prédictions
        double predJ1 = Math.min(CAPACITE_SERVICE, occupationActuelle + entreesParJour - (occupationActuelle * sortiesParJour));
        double predJ7 = Math.min(CAPACITE_SERVICE, occupationActuelle + (entreesParJour * 7) - (occupationActuelle * sortiesParJour * 7));
        double predJ30 = Math.min(CAPACITE_SERVICE, occupationActuelle + (entreesParJour * 30) - (occupationActuelle * sortiesParJour * 30));

        predJ1 = Math.max(0, predJ1);
        predJ7 = Math.max(0, predJ7);
        predJ30 = Math.max(0, predJ30);

        // Mois pic historique
        Map<Integer, Long> parMois = toutes.stream()
                .filter(h -> h.getDateEntree() != null)
                .collect(Collectors.groupingBy(h -> {
                    Calendar c = Calendar.getInstance();
                    c.setTime(h.getDateEntree());
                    return c.get(Calendar.MONTH) + 1;
                }, Collectors.counting()));

        int moisPic = parMois.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse(1);

        String[] moisNoms = {"Jan", "Fév", "Mar", "Avr", "Mai", "Jun", "Jul", "Aoû", "Sep", "Oct", "Nov", "Déc"};

        String risque = predJ7 / CAPACITE_SERVICE >= 0.95 ? "CRITIQUE"
                : predJ7 / CAPACITE_SERVICE >= 0.80 ? "ELEVE"
                : predJ7 / CAPACITE_SERVICE >= 0.60 ? "MOYEN" : "FAIBLE";

        Map<String, Object> result = new HashMap<>();
        result.put("service", service);
        result.put("occupationActuelle", occupationActuelle);
        result.put("tauxActuel", Math.round(tauxActuel));
        result.put("predictionJ1", Math.round(predJ1));
        result.put("predictionJ7", Math.round(predJ7));
        result.put("predictionJ30", Math.round(predJ30));
        result.put("tauxPredJ1", Math.round((predJ1 / CAPACITE_SERVICE) * 100));
        result.put("tauxPredJ7", Math.round((predJ7 / CAPACITE_SERVICE) * 100));
        result.put("tauxPredJ30", Math.round((predJ30 / CAPACITE_SERVICE) * 100));
        result.put("risqueSaturation", risque);
        result.put("moisPicHistorique", moisNoms[moisPic - 1]);
        result.put("capaciteTotale", CAPACITE_SERVICE);
        return result;
    }

    @Override
    public Map<String, Object> predireDureeSejour(String service, String motif) {
        List<Hospitalisation> similaires = ((List<Hospitalisation>) hospitalisationRepository.findAll())
                .stream()
                .filter(h -> h.getDateSortie() != null && h.getDateEntree() != null)
                .filter(h -> service == null || service.isBlank() || service.equalsIgnoreCase(h.getService()))
                .filter(h -> motif == null || motif.isBlank() ||
                        (h.getMotif() != null && h.getMotif().toLowerCase().contains(motif.toLowerCase())))
                .toList();

        if (similaires.isEmpty()) {
            Map<String, Object> r = new HashMap<>();
            r.put("dureePredite_jours", 7);
            r.put("fiabilite", "FAIBLE");
            r.put("nombreCasSimilaires", 0);
            r.put("message", "Aucun cas similaire trouvé - estimation par défaut");
            return r;
        }

        long msParJour = 24L * 60 * 60 * 1000;
        LongSummaryStatistics stats = similaires.stream()
                .mapToLong(h -> Math.max(1, (h.getDateSortie().getTime() - h.getDateEntree().getTime()) / msParJour))
                .summaryStatistics();

        double moyenne = stats.getAverage();
        String categorie = moyenne <= 3 ? "SEJOUR_COURT" : moyenne <= 10 ? "SEJOUR_MOYEN" : "SEJOUR_LONG";
        String fiabilite = similaires.size() >= 10 ? "HAUTE" : similaires.size() >= 5 ? "MOYENNE" : "FAIBLE";

        Map<String, Object> r = new HashMap<>();
        r.put("service", service);
        r.put("motif", motif);
        r.put("dureePredite_jours", Math.round(moyenne * 10.0) / 10.0);
        r.put("dureeMin_jours", stats.getMin());
        r.put("dureeMax_jours", stats.getMax());
        r.put("categorieSejour", categorie);
        r.put("fiabilite", fiabilite);
        r.put("nombreCasSimilaires", similaires.size());
        return r;
    }

    @Override
    public Map<String, Object> getTableauDeBordPredictif() {
        List<Hospitalisation> toutes = (List<Hospitalisation>) hospitalisationRepository.findAll();
        Set<String> services = toutes.stream()
                .filter(h -> h.getService() != null)
                .map(Hospitalisation::getService)
                .collect(Collectors.toSet());

        List<Map<String, Object>> predictions = new ArrayList<>();
        List<Map<String, Object>> alertesSaturation = new ArrayList<>();

        for (String svc : services) {
            Map<String, Object> pred = predireOccupationService(svc);
            predictions.add(pred);
            int tauxJ7 = ((Number) pred.get("tauxPredJ7")).intValue();
            if (tauxJ7 >= 80) {
                Map<String, Object> alerte = new HashMap<>();
                alerte.put("service", svc);
                alerte.put("tauxPredit", tauxJ7);
                alerte.put("risque", pred.get("risqueSaturation"));
                alertesSaturation.add(alerte);
            }
        }

        Map<String, Object> tableau = new HashMap<>();
        tableau.put("predictions", predictions);
        tableau.put("alertesSaturation", alertesSaturation);
        tableau.put("nombreServices", services.size());
        tableau.put("dateGeneration", new Date());
        return tableau;
    }

    // ================= RECHERCHE AVANCÉE =================

    @Override
    public Map<String, Object> rechercheAvancee(String service, String statut, String motif,
                                                Long patientId, Date dateDebut, Date dateFin,
                                                Integer dureeMin, Integer dureeMax,
                                                int page, int size, String sortBy) {
        long msParJour = 24L * 60 * 60 * 1000;

        List<Hospitalisation> toutes = (List<Hospitalisation>) hospitalisationRepository.findAll();

        List<Hospitalisation> filtrees = toutes.stream()
                .filter(h -> service == null || service.isBlank() || service.equalsIgnoreCase(h.getService()))
                .filter(h -> {
                    if (statut == null || statut.isBlank()) return true;
                    boolean enCours = h.getDateSortie() == null;
                    return "EN_COURS".equalsIgnoreCase(statut) ? enCours : !enCours;
                })
                .filter(h -> motif == null || motif.isBlank() ||
                        (h.getMotif() != null && h.getMotif().toLowerCase().contains(motif.toLowerCase())))
                .filter(h -> patientId == null || patientId == 0 ||
                        (h.getPatient() != null && h.getPatient().getId().equals(patientId)))
                .filter(h -> dateDebut == null || (h.getDateEntree() != null && !h.getDateEntree().before(dateDebut)))
                .filter(h -> dateFin == null || (h.getDateEntree() != null && !h.getDateEntree().after(dateFin)))
                .filter(h -> {
                    if (dureeMin == null && dureeMax == null) return true;
                    if (h.getDateEntree() == null) return false;
                    Date fin = h.getDateSortie() != null ? h.getDateSortie() : new Date();
                    long duree = (fin.getTime() - h.getDateEntree().getTime()) / msParJour;
                    if (dureeMin != null && duree < dureeMin) return false;
                    if (dureeMax != null && duree > dureeMax) return false;
                    return true;
                })
                .collect(Collectors.toList());

        // Tri
        if ("duree".equalsIgnoreCase(sortBy)) {
            filtrees.sort((a, b) -> {
                Date finA = a.getDateSortie() != null ? a.getDateSortie() : new Date();
                Date finB = b.getDateSortie() != null ? b.getDateSortie() : new Date();
                long dA = a.getDateEntree() != null ? finA.getTime() - a.getDateEntree().getTime() : 0;
                long dB = b.getDateEntree() != null ? finB.getTime() - b.getDateEntree().getTime() : 0;
                return Long.compare(dB, dA);
            });
        } else if ("service".equalsIgnoreCase(sortBy)) {
            filtrees.sort(Comparator.comparing(h -> h.getService() != null ? h.getService() : ""));
        } else {
            filtrees.sort(Comparator.comparing(h -> h.getDateEntree() != null ? h.getDateEntree() : new Date(), Comparator.reverseOrder()));
        }

        int total = filtrees.size();
        int debut = Math.min(page * size, total);
        int fin = Math.min(debut + size, total);
        List<HospitalisationResponseDTO> paginated = filtrees.subList(debut, fin)
                .stream().map(this::convertToDTO).toList();

        Map<String, Object> result = new HashMap<>();
        result.put("resultats", paginated);
        result.put("totalElements", total);
        result.put("totalPages", (int) Math.ceil((double) total / size));
        result.put("pageActuelle", page);
        result.put("taillePage", size);
        return result;
    }

    // ================= CARTE DE CHALEUR =================

    @Override
    public Map<String, Object> getCarteDeChалeur(String service, int annee) {
        List<Hospitalisation> toutes = (List<Hospitalisation>) hospitalisationRepository.findAll();

        List<Hospitalisation> filtrees = toutes.stream()
                .filter(h -> h.getDateEntree() != null)
                .filter(h -> service == null || service.isBlank() || service.equalsIgnoreCase(h.getService()))
                .filter(h -> {
                    Calendar c = Calendar.getInstance();
                    c.setTime(h.getDateEntree());
                    return c.get(Calendar.YEAR) == annee;
                })
                .toList();

        // Matrice semaine × jour (53 semaines × 7 jours)
        int[][] matrice = new int[53][7];
        for (Hospitalisation h : filtrees) {
            Calendar c = Calendar.getInstance();
            c.setTime(h.getDateEntree());
            int semaine = c.get(Calendar.WEEK_OF_YEAR) - 1;
            int jour = c.get(Calendar.DAY_OF_WEEK) - 1; // 0=Dim
            if (semaine >= 0 && semaine < 53) matrice[semaine][jour]++;
        }

        // Répartition par mois
        Map<String, Long> parMois = filtrees.stream()
                .collect(Collectors.groupingBy(h -> {
                    Calendar c = Calendar.getInstance();
                    c.setTime(h.getDateEntree());
                    String[] mois = {"Jan","Fév","Mar","Avr","Mai","Jun","Jul","Aoû","Sep","Oct","Nov","Déc"};
                    return mois[c.get(Calendar.MONTH)];
                }, Collectors.counting()));

        // Jour le plus chargé
        int[] totauxParJour = new int[7];
        for (int[] semaine : matrice)
            for (int j = 0; j < 7; j++) totauxParJour[j] += semaine[j];

        String[] joursNoms = {"Dim","Lun","Mar","Mer","Jeu","Ven","Sam"};
        int maxJourIdx = 0;
        for (int j = 1; j < 7; j++) if (totauxParJour[j] > totauxParJour[maxJourIdx]) maxJourIdx = j;

        String moisPic = parMois.entrySet().stream()
                .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("N/A");

        Map<String, Object> result = new HashMap<>();
        result.put("service", service);
        result.put("annee", annee);
        result.put("matrice", matrice);
        result.put("totalAdmissions", filtrees.size());
        result.put("parMois", parMois);
        result.put("moisPic", moisPic);
        result.put("jourPic", joursNoms[maxJourIdx]);
        result.put("moyenneParSemaine", filtrees.isEmpty() ? 0 : Math.round(filtrees.size() / 52.0 * 10.0) / 10.0);
        return result;
    }

    // ================= PROFIL MÉDICAL COMPLET 360° =================

    @Override
    public Map<String, Object> getProfilMedicalComplet(Long patientId) {
        Optional<Patient> patientOpt = patientRepository.findById(patientId);
        if (patientOpt.isEmpty()) throw new RuntimeException("Patient introuvable");
        Patient patient = patientOpt.get();

        List<Hospitalisation> hospitalisations = ((List<Hospitalisation>) hospitalisationRepository.findAll())
                .stream()
                .filter(h -> h.getPatient() != null && h.getPatient().getId().equals(patientId))
                .filter(h -> h.getDateEntree() != null)
                .sorted(Comparator.comparing(Hospitalisation::getDateEntree))
                .toList();

        long total = hospitalisations.size();
        long msParJour = 24L * 60 * 60 * 1000;
        List<Hospitalisation> terminees = hospitalisations.stream().filter(h -> h.getDateSortie() != null).toList();

        // Durée totale passée à l'hôpital
        long joursTotaux = terminees.stream()
                .mapToLong(h -> Math.max(0, (h.getDateSortie().getTime() - h.getDateEntree().getTime()) / msParJour))
                .sum();

        // Score de complexité médicale (0-100)
        int scoreComplexite = 0;
        if (total >= 10) scoreComplexite += 40;
        else if (total >= 5) scoreComplexite += 25;
        else if (total >= 2) scoreComplexite += 10;

        long recurrences = identifierRecurrences(patientId).size();
        if (recurrences >= 3) scoreComplexite += 30;
        else if (recurrences >= 1) scoreComplexite += 15;

        double dureeMoy = terminees.isEmpty() ? 0 :
                terminees.stream().mapToLong(h -> Math.max(0, (h.getDateSortie().getTime() - h.getDateEntree().getTime()) / msParJour))
                        .average().orElse(0);
        if (dureeMoy > 21) scoreComplexite += 20;
        else if (dureeMoy > 10) scoreComplexite += 10;

        Set<String> servicesVisites = hospitalisations.stream()
                .filter(h -> h.getService() != null).map(Hospitalisation::getService).collect(Collectors.toSet());
        if (servicesVisites.size() >= 4) scoreComplexite += 10;
        else if (servicesVisites.size() >= 2) scoreComplexite += 5;
        scoreComplexite = Math.min(100, scoreComplexite);

        String niveauComplexite = scoreComplexite >= 70 ? "TRES_COMPLEXE"
                : scoreComplexite >= 40 ? "COMPLEXE"
                : scoreComplexite >= 20 ? "MODERE" : "SIMPLE";

        // Narratif clinique automatique
        String narratif;
        if (total == 0) {
            narratif = "Aucune hospitalisation enregistrée pour ce patient.";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Le patient ").append(patient.getNom()).append(" ").append(patient.getPrenom());
            sb.append(" a été hospitalisé ").append(total).append(" fois");
            if (!servicesVisites.isEmpty())
                sb.append(" dans les services : ").append(String.join(", ", servicesVisites));
            if (dureeMoy > 0)
                sb.append(". Durée moyenne de séjour : ").append(Math.round(dureeMoy)).append(" jours");
            if (recurrences > 0)
                sb.append(". ").append(recurrences).append(" ré-hospitalisation(s) rapprochée(s) détectée(s)");
            sb.append(".");
            narratif = sb.toString();
        }

        // Top 5 motifs
        Map<String, Long> motifCount = hospitalisations.stream()
                .filter(h -> h.getMotif() != null)
                .collect(Collectors.groupingBy(Hospitalisation::getMotif, Collectors.counting()));
        List<Map<String, Object>> topMotifs = motifCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> { Map<String, Object> m = new HashMap<>(); m.put("motif", e.getKey()); m.put("count", e.getValue()); return m; })
                .toList();

        // Timeline
        List<Map<String, Object>> timeline = new ArrayList<>();
        for (int i = 0; i < hospitalisations.size(); i++) {
            Hospitalisation h = hospitalisations.get(i);
            Map<String, Object> entry = new HashMap<>();
            entry.put("index", i + 1);
            entry.put("id", h.getId());
            entry.put("dateEntree", h.getDateEntree());
            entry.put("dateSortie", h.getDateSortie());
            entry.put("service", h.getService());
            entry.put("motif", h.getMotif());
            entry.put("statut", h.getDateSortie() == null ? "EN_COURS" : "TERMINEE");
            if (h.getDateSortie() != null)
                entry.put("dureeJours", (h.getDateSortie().getTime() - h.getDateEntree().getTime()) / msParJour);

            // Détecter ré-hospitalisation rapide
            if (i > 0) {
                Hospitalisation prec = hospitalisations.get(i - 1);
                if (prec.getDateSortie() != null) {
                    long joursEcart = (h.getDateEntree().getTime() - prec.getDateSortie().getTime()) / msParJour;
                    entry.put("joursSortieEntree", joursEcart);
                    entry.put("reHospitalisationRapide", joursEcart <= 30);
                }
            }
            timeline.add(entry);
        }

        // Tendance
        String tendance = "STABLE";
        if (hospitalisations.size() >= 4) {
            List<Hospitalisation> moitie1 = hospitalisations.subList(0, hospitalisations.size() / 2);
            List<Hospitalisation> moitie2 = hospitalisations.subList(hospitalisations.size() / 2, hospitalisations.size());
            Calendar now = Calendar.getInstance();
            long periodeMs = Math.max(1, now.getTimeInMillis() - hospitalisations.get(0).getDateEntree().getTime());
            double freq1 = moitie1.size() / (periodeMs / 2.0);
            double freq2 = moitie2.size() / (periodeMs / 2.0);
            if (freq2 > freq1 * 1.3) tendance = "EN_AUGMENTATION";
            else if (freq2 < freq1 * 0.7) tendance = "EN_DIMINUTION";
        }

        // Répartition par service avec pourcentage
        List<Map<String, Object>> servicesDetail = servicesVisites.stream()
                .map(svc -> {
                    long cnt = hospitalisations.stream().filter(h -> svc.equals(h.getService())).count();
                    Map<String, Object> m = new HashMap<>();
                    m.put("service", svc);
                    m.put("count", cnt);
                    m.put("pourcentage", total > 0 ? Math.round((cnt * 100.0) / total) : 0);
                    return m;
                })
                .sorted((a, b) -> Long.compare((Long) b.get("count"), (Long) a.get("count")))
                .toList();

        Map<String, Object> profil = new HashMap<>();
        profil.put("patientId", patientId);
        profil.put("nomPatient", patient.getNom() + " " + patient.getPrenom());
        profil.put("totalHospitalisations", total);
        profil.put("joursTotauxHospitalise", joursTotaux);
        profil.put("dureeMoyenneSejour_jours", Math.round(dureeMoy * 10.0) / 10.0);
        profil.put("scoreComplexite", scoreComplexite);
        profil.put("niveauComplexite", niveauComplexite);
        profil.put("narratifClinique", narratif);
        profil.put("tendance", tendance);
        profil.put("recurrences", recurrences);
        profil.put("servicesVisites", servicesDetail);
        profil.put("topMotifs", topMotifs);
        profil.put("timeline", timeline);
        return profil;
    }
}