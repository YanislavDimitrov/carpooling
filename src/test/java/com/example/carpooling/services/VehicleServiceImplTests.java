package com.example.carpooling.services;

import com.example.carpooling.exceptions.AuthorizationException;
import com.example.carpooling.exceptions.EntityNotFoundException;
import com.example.carpooling.models.User;
import com.example.carpooling.models.Vehicle;
import com.example.carpooling.models.dtos.VehicleUpdateDto;
import com.example.carpooling.repositories.contracts.VehicleRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static com.example.carpooling.Helpers.createMockUser;
import static com.example.carpooling.Helpers.createMockVehicle;

@ExtendWith(MockitoExtension.class)
public class VehicleServiceImplTests {
    private final ModelMapper modelMapper;
    @Mock
    private VehicleRepository mockRepository;
    @InjectMocks
    VehicleServiceImpl vehicleService;

    public VehicleServiceImplTests() {
        this.modelMapper = new ModelMapper();
    }

    @Test
    public void getById_Should_Throw_When_MatchDoesNotExists() {
        //Arrange
        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.empty());

        //Act, Assert
        Assertions.assertThrows(EntityNotFoundException.class, () -> vehicleService.getById(2L));
    }

    @Test
    public void getById_Should_ReturnVehicle_When_MatchExists() {
        //Arrange
        Vehicle mockVehicle = createMockVehicle();
        Mockito.when(mockRepository.findById(1L)).thenReturn(Optional.of(mockVehicle));

        //Act
        Vehicle vehicle = vehicleService.getById(1L);

        //Assert
        Assertions.assertEquals(1L, vehicle.getId());
    }

    @Test
    public void delete_Should_Throw_When_VehicleWithIdDoesNotExist() {
        //Arrange
        User mockUserLogged = createMockUser();
        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.empty());

        //Act, Assert
        Assertions.assertThrows(EntityNotFoundException.class, () -> vehicleService.delete(2L, mockUserLogged));
    }

    @Test
    public void delete_Should_Throw_When_DeletingUserIsNotAdminNorOwner() {
        //Arrange
        User mockUserLogged = createMockUser();
        mockUserLogged.setUserName("differentUsername");

        Vehicle mockVehicle = createMockVehicle();
        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(mockVehicle));

        //Act, Assert
        Assertions.assertThrows(AuthorizationException.class,
                () -> vehicleService.delete(mockVehicle.getId(), mockUserLogged));
    }

    @Test
    public void delete_Should_InvokeRepository_When_IdIsValidAndUserIsAuthorized() {
        //Arrange
        User mockUserLogged = createMockUser();
        Vehicle mockVehicle = createMockVehicle();
        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(mockVehicle));

        //Act
        vehicleService.delete(mockVehicle.getId(), mockUserLogged);

        //Assert
        Mockito.verify(mockRepository, Mockito.times(1))
                .delete(mockUserLogged.getId());
    }

    @Test
    public void update_Should_Throw_When_VehicleWithIdDoesNotExist() {
        //Arrange
        User mockUserLogged = createMockUser();
        VehicleUpdateDto dto = modelMapper.map(createMockVehicle(), VehicleUpdateDto.class);
        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.empty());

        //Act, Assert
        Assertions.assertThrows(EntityNotFoundException.class, () -> vehicleService.update(2L, dto, mockUserLogged));
    }

    @Test
    public void update_Should_Throw_When_UpdatingUserIsNotOwner() {
        //Arrange
        User mockUserLogged = createMockUser();
        mockUserLogged.setUserName("differentUsername");

        Vehicle mockVehicle = createMockVehicle();
        VehicleUpdateDto dto = modelMapper.map(mockVehicle, VehicleUpdateDto.class);

        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(mockVehicle));

        //Act, Assert
        Assertions.assertThrows(AuthorizationException.class,
                () -> vehicleService.update(mockVehicle.getId(), dto, mockUserLogged));
    }

    @Test
    public void update_Should_InvokeRepository_When_IdIsValidAndUserIsAuthorized() {
        //Arrange
        User mockUserLogged = createMockUser();

        Vehicle mockVehicle = createMockVehicle();
        mockVehicle.setOwner(mockUserLogged);

        VehicleUpdateDto dto = modelMapper.map(mockVehicle, VehicleUpdateDto.class);

        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(mockVehicle));

        //Act
        vehicleService.update(mockVehicle.getId(), dto, mockUserLogged);

        //Assert
        Mockito.verify(mockRepository, Mockito.times(1))
                .save(mockVehicle);
    }
}
