package com.esprit.microservice.hospitalisation.dto;

import java.util.Date;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonFormat;

public class EpisodeRequestDTO {

    @NotBlank(message = "Le type est obligatoire")
    @Size(min = 3, max = 50, message = "Le type doit contenir entre 3 et 50 caractères")
    private String type;

    @NotNull(message = "La date est obligatoire")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date date;

    @NotBlank(message = "La description est obligatoire")
    @Size(min = 5, max = 200, message = "La description doit contenir entre 5 et 200 caractères")
    private String description;

    @NotNull(message = "Hospitalisation obligatoire")
    @Positive(message = "ID hospitalisation invalide")
    private Long hospitalisationId;

    public EpisodeRequestDTO() {}

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getHospitalisationId() { return hospitalisationId; }
    public void setHospitalisationId(Long hospitalisationId) { this.hospitalisationId = hospitalisationId; }
}
