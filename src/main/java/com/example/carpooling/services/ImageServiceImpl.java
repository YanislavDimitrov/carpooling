package com.example.carpooling.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.utils.ObjectUtils;
import com.example.carpooling.models.Image;
import com.example.carpooling.repositories.contracts.ImageRepository;
import com.example.carpooling.services.contracts.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

/**
 * The {@code ImageServiceImpl} class is responsible for managing and interacting with images in the carpooling application.
 * It utilizes the Cloudinary service for image storage and manipulation. This service provides methods to upload, save, delete,
 * and destroy images associated with user profiles.
 *
 * <p>This class is annotated with {@code @Service} to indicate that it is a Spring service component, making it eligible
 * for automatic dependency injection.</p>
 *
 * @author Yanislav Dimitrov & Ivan Boev
 * @version 1.0
 * @since 06.09.23
 */
@Service
public class ImageServiceImpl implements ImageService {
    private final Cloudinary cloudinary;
    private final ImageRepository imageRepository;

    /**
     * Constructs an {@code ImageServiceImpl} instance with the necessary dependencies.
     *
     * @param cloudinary      The Cloudinary instance used for image uploads and destruction.
     * @param imageRepository The repository for storing and managing image-related data.
     */
    @Autowired
    public ImageServiceImpl(Cloudinary cloudinary, ImageRepository imageRepository) {
        this.cloudinary = cloudinary;
        this.imageRepository = imageRepository;
    }

    /**
     * Uploads an image to the Cloudinary service associated with a specified username. The image data is provided
     * as a byte array.
     *
     * @param imageDate The image data to be uploaded as a byte array.
     * @param username  The username associated with the uploaded image.
     * @return A map containing upload result information from Cloudinary.
     * @throws IOException If there is an error during the image upload process.
     */
    @Override
    public Map uploadImage(byte[] imageDate, String username) throws IOException {

        Uploader uploader = cloudinary.uploader();
        Map uploadResult = uploader.upload(imageDate, ObjectUtils.asMap(
                "public_id", username
        ));

        return uploadResult;
    }

    /**
     * Saves an {@code Image} entity to the repository.
     *
     * @param image The image entity to be saved.
     */
    @Override
    public void save(Image image) {
        this.imageRepository.save(image);
    }

    /**
     * Deletes an {@code Image} entity from the repository.
     *
     * @param image The image entity to be deleted.
     * @throws IOException If there is an error during the image deletion process.
     */
    @Override
    public void delete(Image image) throws IOException {
        this.imageRepository.delete(image);
    }

    /**
     * Destroys (deletes) an image from the Cloudinary service associated with a specified username.
     *
     * @param image    The image entity to be destroyed.
     * @param username The username associated with the image to be destroyed.
     * @throws IOException If there is an error during the image destruction process.
     */
    @Override
    public void destroyImage(Image image, String username) throws IOException {
        Uploader uploader = cloudinary.uploader();
        uploader.destroy(username, null);
    }
}
