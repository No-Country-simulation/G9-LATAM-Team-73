package com.techmind.backend.repository;

import com.techmind.backend.model.ContentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ContentRepository extends JpaRepository<ContentEntity, Long> {

    List<ContentEntity> findByCategory(String category);

    List<ContentEntity> findAllByOrderByProcessedAtDesc();

    List<ContentEntity> findByProcessedAtBetweenOrderByProcessedAtDesc(
            LocalDateTime from, LocalDateTime to);
}
