package com.jackdaw.portfolio.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "contact_messages")
@Schema(description = "A message submitted through the portfolio contact form")
public class ContactMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Database identifier")
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 100)
    @Schema(description = "Sender's name", example = "Jane Doe", maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email")
    @Size(max = 150)
    @Schema(description = "Sender's email address", example = "jane@example.com", maxLength = 150, requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "Message is required")
    @Size(max = 2000)
    @Column(length = 2000)
    @Schema(description = "Message body", example = "Hi Pratham, let's talk!", maxLength = 2000, requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Server-side creation timestamp")
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
