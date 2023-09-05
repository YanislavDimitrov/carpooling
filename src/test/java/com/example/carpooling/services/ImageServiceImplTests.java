package com.example.carpooling.services;

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

@ExtendWith(MockitoExtension.class)
public class ImageServiceImplTests {
    @Mock
    private ImageRepository mockRepository;
    @InjectMocks
    private ImageServiceImpl imageService;

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
