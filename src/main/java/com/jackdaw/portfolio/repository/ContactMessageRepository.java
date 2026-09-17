package com.jackdaw.portfolio.repository;

import com.jackdaw.portfolio.model.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
}
