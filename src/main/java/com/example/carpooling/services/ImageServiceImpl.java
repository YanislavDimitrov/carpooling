package com.example.carpooling.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.Url;
import com.cloudinary.utils.ObjectUtils;
import com.example.carpooling.models.Image;
import com.example.carpooling.repositories.contracts.ImageRepository;
import com.example.carpooling.services.contracts.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class ImageServiceImpl implements ImageService {
    private final Cloudinary cloudinary;
    private final ImageRepository imageRepository;

    @Autowired
    public ImageServiceImpl(Cloudinary cloudinary, ImageRepository imageRepository) {
        this.cloudinary = cloudinary;
        this.imageRepository = imageRepository;
    }

    @Override
    public String uploadImage(byte[] imageDate, String username) throws IOException {

        Map uploadResult = cloudinary.uploader().upload(imageDate, ObjectUtils.asMap(
                "public_id", username
        ));

        return (String) uploadResult.get("secure_url");
    }

    @Override
    public void save(Image image) {
        this.imageRepository.save(image);
    }

    @Override
    public void delete(Image image) throws IOException {
        this.imageRepository.delete(image);
    }

    @Override
    public void destroyImage(Image image,String username) throws IOException {
        cloudinary.uploader().destroy(username, null);
    }
}
