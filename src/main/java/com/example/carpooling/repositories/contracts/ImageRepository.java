package com.example.carpooling.repositories.contracts;

import com.example.carpooling.models.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {

}
