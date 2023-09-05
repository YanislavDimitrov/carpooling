package com.example.carpooling.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.example.carpooling.models.Image;
import com.example.carpooling.repositories.contracts.ImageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static com.example.carpooling.Helpers.createMockImage;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ImageServiceImplTests {
    @Mock
    private ImageRepository mockRepository;
    @Mock
    private Cloudinary mockCloudinary;
    @InjectMocks
    private ImageServiceImpl imageService;

    @Test
    public void upload_Should_Invoke_Uploader() throws IOException {
        //Arrange
        Uploader uploader = mock(Uploader.class);
        Mockito.when(mockCloudinary.uploader()).thenReturn(uploader);

        //Act
        imageService.uploadImage(new byte[5], "username");

        //Assert
        Mockito.verify(uploader, Mockito.times(1))
                .upload(any(), any());
    }
    @Test
    public void destroy_Should_Invoke_Ddestroy() throws IOException {
        //Arrange
        Uploader uploader = mock(Uploader.class);
        Mockito.when(mockCloudinary.uploader()).thenReturn(uploader);

        //Act
        imageService.uploadImage(new byte[5], "username");

        //Assert
        Mockito.verify(uploader, Mockito.times(1))
                .upload(any(), any());
    }

    @Test
    public void save_Should_Invoke_Save() {
        //Arrange
        Image mockImage = createMockImage();

        //Act
        imageService.save(mockImage);

        //Assert
        Mockito.verify(mockRepository, Mockito.times(1))
                .save(mockImage);
    }

    @Test
    public void delete_Should_Invoke_Delete() throws IOException {
        //Arrange
        Image mockImage = createMockImage();

        //Act
        imageService.delete(mockImage);

        //Assert
        Mockito.verify(mockRepository, Mockito.times(1))
                .delete(mockImage);
    }
}
