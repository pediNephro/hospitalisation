package com.esprit.microservice.hospitalisation.services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TwilioSmsService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String fromPhone;

    @Value("${twilio.phone.verified}")
    private String toPhone;

    @PostConstruct
    public void initTwilio() {
        Twilio.init(accountSid, authToken);
    }

    public void envoyerSmsAdmission(String nomPatient, String service, String dateEntree) {
        String message = "Hopital - ADMISSION\n" +
                "Patient: " + nomPatient + "\n" +
                "Service: " + service + "\n" +
                "Date: " + dateEntree;
        envoyer(message);
    }

    public void envoyerSmsSortie(String nomPatient, String service) {
        String message = "Hopital - SORTIE\n" +
                "Patient: " + nomPatient + "\n" +
                "Service: " + service + "\n" +
                "Lit libere avec succes.";
        envoyer(message);
    }

    public void envoyerAlerteManqueLits(String service, long litsLibres) {
        String message = "ALERTE LITS\n" +
                "Service: " + service + "\n" +
                "Lits libres: " + litsLibres + "\n" +
                "Action requise !";
        envoyer(message);
    }

    public void envoyerAlerteRecurrence(String nomPatient, long jours, String service) {
        String message = "ALERTE RECURRENCE\n" +
                "Patient: " + nomPatient + "\n" +
                "Re-hospitalise apres " + jours + " jour(s)\n" +
                "Service: " + service;
        envoyer(message);
    }

    private void envoyer(String body) {
        try {
            Message.creator(
                    new PhoneNumber(toPhone),
                    new PhoneNumber(fromPhone),
                    body
            ).create();
            System.out.println("SMS envoye avec succes vers " + toPhone);
        } catch (Exception e) {
            System.err.println("Erreur SMS: " + e.getMessage());
        }
    }
}