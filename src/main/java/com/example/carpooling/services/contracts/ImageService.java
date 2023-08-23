package com.example.carpooling.services.contracts;

import com.example.carpooling.models.Image;

import java.io.IOException;

public interface ImageService {
    String uploadImage(byte[] imageDate) throws IOException;

    void save(Image image);
}
