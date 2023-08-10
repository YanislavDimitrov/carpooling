package com.example.carpooling.repositories.contracts;

import com.example.carpooling.models.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback,Long> {
}
