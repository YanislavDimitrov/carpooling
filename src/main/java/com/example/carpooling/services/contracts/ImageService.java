package com.example.carpooling.services.contracts;

import com.example.carpooling.models.Image;

import java.io.IOException;

public interface ImageService {
    String uploadImage(byte[] imageDate, String username) throws IOException;

    void save(Image image);

    void delete(Image image) throws IOException;

    void destroyImage(Image image, String username) throws IOException;
}
