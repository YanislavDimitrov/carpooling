package com.example.carpooling.repositories.contracts;

import com.example.carpooling.models.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback,Long> {
}
