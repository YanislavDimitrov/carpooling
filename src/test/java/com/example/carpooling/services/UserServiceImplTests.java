package com.example.carpooling.services;


import com.example.carpooling.exceptions.*;
import com.example.carpooling.exceptions.duplicate.DuplicateEmailException;
import com.example.carpooling.exceptions.duplicate.DuplicatePhoneNumberException;
import com.example.carpooling.exceptions.duplicate.DuplicateUsernameException;
import com.example.carpooling.models.Feedback;
import com.example.carpooling.models.Travel;
import com.example.carpooling.models.User;
import com.example.carpooling.models.Vehicle;
import com.example.carpooling.models.dtos.UserChangePasswordDto;
import com.example.carpooling.models.dtos.UserUpdateDto;
import com.example.carpooling.models.enums.TravelStatus;
import com.example.carpooling.models.enums.UserRole;
import com.example.carpooling.models.enums.UserStatus;
import com.example.carpooling.repositories.contracts.UserRepository;
import com.example.carpooling.repositories.contracts.VehicleRepository;
import com.example.carpooling.services.contracts.ValidationService;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.example.carpooling.Helpers.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTests {

    @Mock
    private UserRepository mockRepository;
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private ValidationService validationService;
    private ModelMapper modelMapper;
    @InjectMocks
    UserServiceImpl userService;

    public UserServiceImplTests() {
        this.modelMapper = new ModelMapper();
    }

    @Test
    public void getAll_Should_CallRepository() {
        //Act
        userService.getAll();

        //Assert
        Mockito.verify(mockRepository, Mockito.times(1))
                .findAll();
    }

    @Test
    public void findAll_Should_CallRepository() {
        //Act
        userService.findAll(Sort.by(Sort.Direction.DESC, "firstName"));

        //Assert
        Mockito.verify(mockRepository, Mockito.times(1))
                .findAll(Mockito.any(Sort.class));
    }

    @Test
    public void findByCriteria_Should_CallRepository() {
        //Act
        userService.findAll(
                "firstName",
                "lastName",
                "userName",
                "email",
                "1234567890",
                "ADMIN",
                "ACTIVE",
                Sort.by(Sort.Direction.DESC, "firstName"));

        //Assert
        Mockito.verify(mockRepository, Mockito.times(1))
                .findByCriteria("firstName",
                        "lastName",
                        "userName",
                        "email",
                        "1234567890",
                        "ADMIN",
                        "ACTIVE",
                        Sort.by(Sort.Direction.DESC, "firstName"));
    }

    @Test
    public void getById_Should_ReturnUser_When_MatchExists() {
        //Arrange
        User mockUser = createMockUser();
        Mockito.when(mockRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        //Act
        User user = userService.getById(1L);

        //Assert
        Assertions.assertEquals(1L, user.getId());
    }

    @Test
    public void getById_Should_Throw_When_MatchDoesNotExists() {
        //Arrange
        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.empty());

        //Act, Assert
        Assertions.assertThrows(EntityNotFoundException.class, () -> userService.getById(2L));
    }

    @Test
    public void getByUsername_Should_ReturnUser_When_MatchExists() {
        //Arrange
        User mockUser = createMockUser();
        Mockito.when(mockRepository.findByUserName(Mockito.any(String.class))).thenReturn(mockUser);

        //Act
        User user = userService.getByUsername(mockUser.getUserName());

        //Assert
        Assertions.assertEquals(mockUser.getUserName(), user.getUserName());
    }

    @Test
    public void getByUsername_Should_Throw_When_MatchDoesNotExists() {
        //Arrange
        User mockUser = createMockUser();
        Mockito.when(mockRepository.findByUserName(Mockito.any(String.class))).thenReturn(null);

        //Act, Assert
        Assertions.assertThrows(EntityNotFoundException.class, () -> userService.getByUsername(mockUser.getUserName()));
    }

    @Test
    public void create_Should_Throw_When_DuplicateUsername() throws MessagingException, IOException {
        //Arrange
        User mockUserToCreate = createMockUser();
        User mockUserExisting = createMockUser();
        mockUserExisting.setId(2L);
        Mockito.when(mockRepository.findByUserName(Mockito.any(String.class))).thenReturn(mockUserExisting);

        try {
            userService.create(mockUserToCreate);
        } catch (DuplicateUsernameException e) {
            Assertions.assertEquals(String.format("User with username %s already exists.", mockUserToCreate.getUserName()), e.getMessage());
        }

        //Act, Assert
        Assertions.assertThrows(DuplicateUsernameException.class, () -> userService.create(mockUserToCreate));
    }

    @Test
    public void create_Should_Throw_When_DuplicateEmail() throws MessagingException, IOException {
        //Arrange
        User mockUserToCreate = createMockUser();
        User mockUserExisting = createMockUser();
        mockUserExisting.setId(2L);
        Mockito.when(mockRepository.findByEmail(Mockito.any(String.class))).thenReturn(mockUserExisting);

        try {
            userService.create(mockUserToCreate);
        } catch (DuplicateEmailException e) {
            Assertions.assertEquals(String.format("User with email %s already exists.", mockUserToCreate.getEmail()), e.getMessage());
        }

        //Act, Assert
        Assertions.assertThrows(DuplicateEmailException.class, () -> userService.create(mockUserToCreate));
    }

    @Test
    public void create_Should_Throw_When_DuplicatePhoneNumber() throws MessagingException, IOException {
        //Arrange
        User mockUserToCreate = createMockUser();
        User mockUserExisting = createMockUser();
        mockUserExisting.setId(2L);
        Mockito.when(mockRepository.findByPhoneNumber(Mockito.any(String.class))).thenReturn(mockUserExisting);

        try {
            userService.create(mockUserToCreate);
        } catch (DuplicatePhoneNumberException e) {
            Assertions.assertEquals(String.format("User with phone number %s already exists.", mockUserToCreate.getPhoneNumber()), e.getMessage());
        }

        //Act, Assert
        Assertions.assertThrows(DuplicatePhoneNumberException.class, () -> userService.create(mockUserToCreate));
    }

    @Test
    public void create_Should_InvokeSave_When_Called() throws MessagingException, IOException {
        //Arrange
        User mockUser = createMockUser();

        //Act
        userService.create(mockUser);

        //Assert
        Mockito.verify(mockRepository, Mockito.times(1))
                .save(mockUser);
    }

    @Test
    public void create_Should_InvokeValidate_When_Called() throws MessagingException, IOException {
        //Arrange
        User mockUser = createMockUser();

        //Act
        userService.create(mockUser);

        //Assert
        Mockito.verify(validationService, Mockito.times(1))
                .validate(mockUser);
    }

    @Test
    public void update_Should_Throw_When_UserWithIdDoesNotExist() {
        //Arrange
        User mockUserLogged = createMockUser();
        UserUpdateDto payload = this.modelMapper.map(mockUserLogged, UserUpdateDto.class);
        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.empty());

        //Act, Assert
        Assertions.assertThrows(EntityNotFoundException.class, () -> userService.update(2L, payload, mockUserLogged));
    }

    @Test
    public void update_Should_Throw_When_EditorIsNotAdminNorSameUser() {
        //Arrange
        User mockUserLogged = createMockUser();
        User mockUserTarget = createMockUser();
        mockUserTarget.setId(2L);
        UserUpdateDto payload = this.modelMapper.map(mockUserTarget, UserUpdateDto.class);
        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(mockUserTarget));

        //Act, Assert
        Assertions.assertThrows(AuthorizationException.class, () -> userService.update(1L, payload, mockUserLogged));
    }

    @Test
    public void update_Should_InvokeSave_When_SameUser() {
        //Arrange
        User mockUserLogged = createMockUser();
        UserUpdateDto payload = this.modelMapper.map(mockUserLogged, UserUpdateDto.class);
        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(mockUserLogged));

        //Act
        userService.update(mockUserLogged.getId(), payload, mockUserLogged);

        //Assert
        Mockito.verify(mockRepository, Mockito.times(1))
                .save(mockUserLogged);
    }

    @Test
    public void update_Should_InvokeSave_When_Admin() {
        //Arrange
        User mockUserLogged = createMockUser();
        User mockUserAdmin = createMockUser();
        mockUserAdmin.setId(2L);
        mockUserAdmin.setRole(UserRole.ADMIN);
        UserUpdateDto payload = this.modelMapper.map(mockUserLogged, UserUpdateDto.class);
        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(mockUserLogged));

        //Act
        userService.update(mockUserLogged.getId(), payload, mockUserAdmin);

        //Assert
        Mockito.verify(mockRepository, Mockito.times(1))
                .save(mockUserLogged);
    }

    @Test
    public void delete_Should_Throw_When_UserWithIdDoesNotExist() {
        //Arrange
        User mockUserLogged = createMockUser();
        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.empty());

        //Act, Assert
        Assertions.assertThrows(EntityNotFoundException.class, () -> userService.delete(2L, mockUserLogged));
    }

    @Test
    public void delete_Should_Throw_When_DeletingUserIsNotSame() {
        //Arrange
        User mockUserLogged = createMockUser();
        User mockUserToDelete = createMockUser();
        mockUserToDelete.setId(2L);
        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(mockUserToDelete));

        //Act, Assert
        Assertions.assertThrows(AuthorizationException.class,
                () -> userService.delete(mockUserToDelete.getId(), mockUserLogged));
    }

    @Test
    public void delete_Should_InvokeDelete_When_DeletingUserIsTheSame() {
        //Arrange
        User mockUserLogged = createMockUser();
        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(mockUserLogged));

        //Act
        userService.delete(mockUserLogged.getId(), mockUserLogged);

        //Assert
        Mockito.verify(mockRepository, Mockito.times(1))
                .delete(mockUserLogged.getId());
    }

    @Test
    public void delete_Should_DeleteAllUserFeedbacks_When_DeletingUserIsTheSame() {
        //Arrange
        User mockUserLogged = createMockUser();
        mockUserLogged.setFeedbacks(List.of(
                createMockFeedback(),
                createMockFeedback(),
                createMockFeedback()
        ));
        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(mockUserLogged));

        //Act
        userService.delete(mockUserLogged.getId(), mockUserLogged);

        //Assert
        for (Feedback feedback : mockUserLogged.getFeedbacks()) {
            Assertions.assertTrue(feedback.isDeleted());
        }
    }

    @Test
    public void delete_Should_Throw_When_ActiveTravels() {
        //Arrange
        User mockUserLogged = createMockUser();
        mockUserLogged.setTravelsAsDriver(List.of(
                createMockActiveTravel()
        ));
        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(mockUserLogged));

        //Act, Assert
        Assertions.assertThrows(ActiveTravelException.class, () -> userService.delete(1L, mockUserLogged));
    }

    @Test
    public void delete_Should_DeleteAllPlannedTravels_When_DeletingUserIsTheSame() {
        //Arrange
        User mockUserLogged = createMockUser();
        Travel mockPlannedTravel = createMockActiveTravel();
        mockPlannedTravel.setStatus(TravelStatus.PLANNED);
        mockUserLogged.setTravelsAsDriver(List.of(
                mockPlannedTravel,
                mockPlannedTravel,
                mockPlannedTravel
        ));

        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(mockUserLogged));

        //Act
        userService.delete(mockUserLogged.getId(), mockUserLogged);

        //Assert
        for (Travel travel : mockUserLogged.getTravelsAsDriver()) {
            Assertions.assertTrue(travel.isDeleted());
            Assertions.assertEquals(travel.getStatus(), (TravelStatus.CANCELED));
        }
    }

    @Test
    public void restore_Should_Throw_When_UserWithIdDoesNotExist() {
        //Arrange
        User mockUserLogged = createMockUser();
        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.empty());

        //Act, Assert
        Assertions.assertThrows(EntityNotFoundException.class, () -> userService.restore(2L, mockUserLogged));
    }

    @Test
    public void restore_Should_Throw_When_RestoringUserIsNotSame() {
        //Arrange
        User mockUserLogged = createMockUser();
        User mockUserToRestore = createMockUser();
        mockUserToRestore.setId(2L);
        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(mockUserToRestore));

        //Act, Assert
        Assertions.assertThrows(AuthorizationException.class,
                () -> userService.restore(mockUserToRestore.getId(), mockUserLogged));
    }

    @Test
    public void restore_Should_InvokeRestore_When_RestoringUserIsTheSame() {
        //Arrange
        User mockUserLogged = createMockUser();
        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(mockUserLogged));

        //Act
        userService.restore(mockUserLogged.getId(), mockUserLogged);

        //Assert
        Mockito.verify(mockRepository, Mockito.times(1))
                .restore(mockUserLogged.getId());
    }

    @Test
    public void restore_Should_RestoreAllUserFeedbacks_When_RestoringUserIsTheSame() {
        //Arrange
        User mockUserLogged = createMockUser();
        Feedback mockDeletedFeedback = createMockFeedback();
        mockDeletedFeedback.setDeleted(true);
        mockUserLogged.setFeedbacks(List.of(
                mockDeletedFeedback,
                mockDeletedFeedback,
                mockDeletedFeedback
        ));
        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(mockUserLogged));

        //Act
        userService.restore(mockUserLogged.getId(), mockUserLogged);

        //Assert
        for (Feedback feedback : mockUserLogged.getFeedbacks()) {
            Assertions.assertFalse(feedback.isDeleted());
        }
    }

    @Test
    public void restore_Should_RestoreAllTravels_When_RestoringUserIsTheSame() {
        //Arrange
        User mockUserLogged = createMockUser();
        Travel mockPlannedTravel = createMockActiveTravel();
        mockPlannedTravel.setDeleted(true);
        mockUserLogged.setTravelsAsDriver(List.of(
                mockPlannedTravel,
                mockPlannedTravel,
                mockPlannedTravel
        ));

        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(mockUserLogged));

        //Act
        userService.restore(mockUserLogged.getId(), mockUserLogged);

        //Assert
        for (Travel travel : mockUserLogged.getTravelsAsDriver()) {
            Assertions.assertFalse(travel.isDeleted());
        }
    }

    @Test
    public void block_Should_Throw_When_UserWithIdDoesNotExist() {
        //Arrange
        User mockUserLogged = createMockUser();
        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.empty());

        //Act, Assert
        Assertions.assertThrows(EntityNotFoundException.class, () -> userService.block(2L, mockUserLogged));
    }

    @Test
    public void block_Should_Throw_When_BlockingUserIsNotAdmin() {
        //Arrange
        User mockUserLogged = createMockUser();
        User mockUserToBlock = createMockUser();
        mockUserToBlock.setId(2L);
        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(mockUserToBlock));

        //Act, Assert
        Assertions.assertThrows(AuthorizationException.class,
                () -> userService.block(mockUserToBlock.getId(), mockUserLogged));
    }

    @Test
    public void block_Should_InvokeBlock_When_BlockingUserIsAdmin() {
        //Arrange
        User mockUserLogged = createMockUser();
        mockUserLogged.setRole(UserRole.ADMIN);
        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(mockUserLogged));

        //Act
        userService.block(mockUserLogged.getId(), mockUserLogged);

        //Assert
        Mockito.verify(mockRepository, Mockito.times(1))
                .block(mockUserLogged.getId());
    }

    @Test
    public void block_Should_DeleteAllUserFeedbacks_When_BlockingUserIsAdmin() {
        //Arrange
        User mockUserLogged = createMockUser();
        mockUserLogged.setRole(UserRole.ADMIN);
        mockUserLogged.setFeedbacks(List.of(
                createMockFeedback(),
                createMockFeedback(),
                createMockFeedback()
        ));
        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(mockUserLogged));

        //Act
        userService.block(mockUserLogged.getId(), mockUserLogged);

        //Assert
        for (Feedback feedback : mockUserLogged.getFeedbacks()) {
            Assertions.assertTrue(feedback.isDeleted());
        }
    }

    @Test
    public void block_Should_Throw_When_ActiveTravels() {
        //Arrange
        User mockUserLogged = createMockUser();
        mockUserLogged.setRole(UserRole.ADMIN);
        mockUserLogged.setTravelsAsDriver(List.of(
                createMockActiveTravel()
        ));
        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(mockUserLogged));

        //Act, Assert
        Assertions.assertThrows(ActiveTravelException.class, () -> userService.block(1L, mockUserLogged));
    }

    @Test
    public void block_Should_DeleteAllPlannedTravels_When_BlockingUserIsAdmin() {
        //Arrange
        User mockUserLogged = createMockUser();
        mockUserLogged.setRole(UserRole.ADMIN);
        Travel mockPlannedTravel = createMockActiveTravel();
        mockPlannedTravel.setStatus(TravelStatus.PLANNED);
        mockUserLogged.setTravelsAsDriver(List.of(
                mockPlannedTravel,
                mockPlannedTravel,
                mockPlannedTravel
        ));

        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(mockUserLogged));

        //Act
        userService.block(mockUserLogged.getId(), mockUserLogged);

        //Assert
        for (Travel travel : mockUserLogged.getTravelsAsDriver()) {
            Assertions.assertTrue(travel.isDeleted());
            Assertions.assertEquals(travel.getStatus(), (TravelStatus.CANCELED));
        }
    }

    @Test
    public void unblock_Should_Throw_When_UserWithIdDoesNotExist() {
        //Arrange
        User mockUserLogged = createMockUser();
        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.empty());

        //Act, Assert
        Assertions.assertThrows(EntityNotFoundException.class, () -> userService.unblock(2L, mockUserLogged));
    }

    @Test
    public void unblock_Should_Throw_When_UnblockingUserIsNotAdmin() {
        //Arrange
        User mockUserLogged = createMockUser();
        User mockUserToUnblock = createMockUser();
        mockUserToUnblock.setId(2L);
        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(mockUserToUnblock));

        //Act, Assert
        Assertions.assertThrows(AuthorizationException.class,
                () -> userService.unblock(mockUserToUnblock.getId(), mockUserLogged));
    }

    @Test
    public void unblock_Should_InvokeRestore_When_UnblockingUserIsAdmin() {
        //Arrange
        User mockUserLogged = createMockUser();
        mockUserLogged.setRole(UserRole.ADMIN);
        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(mockUserLogged));

        //Act
        userService.unblock(mockUserLogged.getId(), mockUserLogged);

        //Assert
        Mockito.verify(mockRepository, Mockito.times(1))
                .restore(mockUserLogged.getId());
    }

    @Test
    public void unblock_Should_RestoreAllUserFeedbacks_When_UnblockingUserIsAdmin() {
        //Arrange
        User mockUserLogged = createMockUser();
        mockUserLogged.setRole(UserRole.ADMIN);
        Feedback mockDeletedFeedback = createMockFeedback();
        mockDeletedFeedback.setDeleted(true);
        mockUserLogged.setFeedbacks(List.of(
                mockDeletedFeedback,
                mockDeletedFeedback,
                mockDeletedFeedback
        ));
        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(mockUserLogged));

        //Act
        userService.unblock(mockUserLogged.getId(), mockUserLogged);

        //Assert
        for (Feedback feedback : mockUserLogged.getFeedbacks()) {
            Assertions.assertFalse(feedback.isDeleted());
        }
    }

    @Test
    public void unblock_Should_RestoreAllTravels_When_UnblockingUserIsAdmin() {
        //Arrange
        User mockUserLogged = createMockUser();
        mockUserLogged.setRole(UserRole.ADMIN);
        Travel mockPlannedTravel = createMockActiveTravel();
        mockPlannedTravel.setDeleted(true);
        mockUserLogged.setTravelsAsDriver(List.of(
                mockPlannedTravel,
                mockPlannedTravel,
                mockPlannedTravel
        ));

        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(mockUserLogged));

        //Act
        userService.unblock(mockUserLogged.getId(), mockUserLogged);

        //Assert
        for (Travel travel : mockUserLogged.getTravelsAsDriver()) {
            Assertions.assertFalse(travel.isDeleted());
        }
    }

    @Test
    public void upgrade_Should_Throw_When_UpgradingUserIsNotAdmin() {
        //Arrange
        User mockUserLogged = createMockUser();
        User mockUserToUpgrade = createMockUser();
        mockUserToUpgrade.setId(2L);

        //Act, Assert
        Assertions.assertThrows(AuthorizationException.class,
                () -> userService.upgrade(mockUserToUpgrade.getId(), mockUserLogged));
    }

    @Test
    public void upgrade_Should_Throw_When_UserWithIdDoesNotExist() {
        //Arrange
        User mockUserLogged = createMockUser();
        mockUserLogged.setRole(UserRole.ADMIN);
        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.empty());

        //Act, Assert
        Assertions.assertThrows(EntityNotFoundException.class, () -> userService.upgrade(2L, mockUserLogged));
    }

    @Test
    public void upgrade_Should_InvokeUpgrade_When_UpgradingUserIsAdminAndTargetUserExists() {
        //Arrange
        User mockUserLogged = createMockUser();
        mockUserLogged.setRole(UserRole.ADMIN);
        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(mockUserLogged));

        //Act
        userService.upgrade(mockUserLogged.getId(), mockUserLogged);

        //Assert
        Mockito.verify(mockRepository, Mockito.times(1))
                .upgrade(mockUserLogged.getId());
    }

    @Test
    public void downgrade_Should_Throw_When_DowngradingUserIsNotAdmin() {
        //Arrange
        User mockUserLogged = createMockUser();
        User mockUserToUpgrade = createMockUser();
        mockUserToUpgrade.setId(2L);

        //Act, Assert
        Assertions.assertThrows(AuthorizationException.class,
                () -> userService.downgrade(mockUserToUpgrade.getId(), mockUserLogged));
    }

    @Test
    public void downGrade_Should_Throw_When_UserWithIdDoesNotExist() {
        //Arrange
        User mockUserLogged = createMockUser();
        mockUserLogged.setRole(UserRole.ADMIN);
        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.empty());

        //Act, Assert
        Assertions.assertThrows(EntityNotFoundException.class, () -> userService.downgrade(2L, mockUserLogged));
    }

    @Test
    public void downgrade_Should_InvokeDowngrade_When_DowngradingUserIsAdminAndTargetUserExists() {
        //Arrange
        User mockUserLogged = createMockUser();
        mockUserLogged.setRole(UserRole.ADMIN);
        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(mockUserLogged));

        //Act
        userService.downgrade(mockUserLogged.getId(), mockUserLogged);

        //Assert
        Mockito.verify(mockRepository, Mockito.times(1))
                .downgrade(mockUserLogged.getId());
    }

    @Test
    public void verify_Should_Throw_When_UserWithIdDoesNotExist() {
        //Arrange
        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.empty());

        //Act, Assert
        Assertions.assertThrows(EntityNotFoundException.class, () -> userService.verify(2L));
    }

    @Test
    public void verify_Should_InvokeValidate_When_TargetUserExists() {
        //Arrange
        User mockUserToValidate = createMockUser();
        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(mockUserToValidate));

        //Act
        userService.verify(mockUserToValidate.getId());

        //Assert
        Mockito.verify(mockRepository, Mockito.times(1))
                .validate(mockUserToValidate.getId());
    }

    @Test
    public void changePassword_Should_Throw_When_EditorIsNotAdminNorSameUser() {
        //Arrange
        UserChangePasswordDto changePasswordDto = new UserChangePasswordDto();
        changePasswordDto.setOldPassword("MockOldPassword");
        changePasswordDto.setNewPassword("MockNewPassword");
        changePasswordDto.setConfirmNewPassword("MockConfirmNewPassword");

        User mockUserLogged = createMockUser();

        User mockUserTarget = createMockUser();
        mockUserTarget.setId(2L);

        //Act, Assert
        Assertions.assertThrows(AuthorizationException.class, () -> userService.changePassword(mockUserTarget, changePasswordDto, mockUserLogged));
    }

    @Test
    public void changePassword_Should_Throw_When_CurrentPasswordIsIncorrect() {
        //Arrange
        UserChangePasswordDto changePasswordDto = new UserChangePasswordDto();
        changePasswordDto.setOldPassword("MockOldPassword");
        changePasswordDto.setNewPassword("MockNewPassword");
        changePasswordDto.setConfirmNewPassword("MockConfirmNewPassword");

        User mockUserLogged = createMockUser();

        //Act, Assert
        Assertions.assertThrows(WrongPasswordException.class, () -> userService.changePassword(mockUserLogged, changePasswordDto, mockUserLogged));
    }

    @Test
    public void changePassword_Should_Throw_When_NewPasswordDoesNotMatchConfirmPassword() {
        //Arrange
        User mockUserLogged = createMockUser();
        mockUserLogged.setPassword("MockOldPassword");

        UserChangePasswordDto changePasswordDto = new UserChangePasswordDto();
        changePasswordDto.setOldPassword("MockOldPassword");
        changePasswordDto.setNewPassword("MockNewPassword");
        changePasswordDto.setConfirmNewPassword("MockConfirmNewPassword");


        //Act, Assert
        Assertions.assertThrows(PasswordMismatchException.class, () -> userService.changePassword(mockUserLogged, changePasswordDto, mockUserLogged));
    }

    @Test
    public void changePassword_Should_InvokeChangePassword_When_UserIsAuthorizedOldPasswordIsCorrectAndNewPasswordMatchesConfirmPassword() {
        //Arrange
        User mockUserLogged = createMockUser();
        mockUserLogged.setPassword("MockOldPassword");

        UserChangePasswordDto changePasswordDto = new UserChangePasswordDto();
        changePasswordDto.setOldPassword("MockOldPassword");
        changePasswordDto.setNewPassword("MockNewPassword");
        changePasswordDto.setConfirmNewPassword("MockNewPassword");

        //Act
        userService.changePassword(mockUserLogged, changePasswordDto, mockUserLogged);

        //Assert
        Mockito.verify(mockRepository, Mockito.times(1))
                .changePassword(mockUserLogged.getId(), changePasswordDto.getNewPassword());
    }

    @Test
    public void findAllPaginated_Should_InvokeFindAllPaginated() {
        //Act
        int page = 1;
        int size = 5;
        String role = "ADMIN";
        String status = "ACTIVE";
        userService.findAllPaginated(
                page,
                size,
                "firstName",
                "lastName",
                "userName",
                "email",
                "1234567890",
                role,
                status,
                Sort.by(Sort.Direction.DESC, "firstName"));

        //Assert
        Mockito.verify(mockRepository, Mockito.times(1))
                .findAllPaginated(
                        PageRequest.of(page, size),
                        "firstName",
                        "lastName",
                        "userName",
                        "email",
                        "1234567890",
                        UserRole.valueOf(role),
                        UserStatus.valueOf(status),
                        Sort.by(Sort.Direction.DESC, "firstName"));
    }

    @Test
    public void findAllPaginated_ShouldConsiderNullForRoleAndStatus_When_RoleAndStatusAreEmpty() {
        //Act
        int page = 1;
        int size = 5;
        String role = "";
        String status = "";
        userService.findAllPaginated(
                page,
                size,
                "firstName",
                "lastName",
                "userName",
                "email",
                "1234567890",
                role,
                status,
                Sort.by(Sort.Direction.DESC, "firstName"));

        //Assert
        Mockito.verify(mockRepository, Mockito.times(1))
                .findAllPaginated(
                        PageRequest.of(page, size),
                        "firstName",
                        "lastName",
                        "userName",
                        "email",
                        "1234567890",
                        null,
                        null,
                        Sort.by(Sort.Direction.DESC, "firstName"));
    }

    @Test
    public void findAllPaginated_ShouldConsiderNullForRoleAndStatus_When_RoleAndStatusAreNull() {
        //Act
        int page = 1;
        int size = 5;
        String role = null;
        String status = null;
        userService.findAllPaginated(
                page,
                size,
                "firstName",
                "lastName",
                "userName",
                "email",
                "1234567890",
                role,
                status,
                Sort.by(Sort.Direction.DESC, "firstName"));

        //Assert
        Mockito.verify(mockRepository, Mockito.times(1))
                .findAllPaginated(
                        PageRequest.of(page, size),
                        "firstName",
                        "lastName",
                        "userName",
                        "email",
                        "1234567890",
                        null,
                        null,
                        Sort.by(Sort.Direction.DESC, "firstName"));
    }

    @Test
    public void unverify_Should_Throw_When_UserWithIdDoesNotExist() {
        //Arrange
        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.empty());

        //Act, Assert
        Assertions.assertThrows(EntityNotFoundException.class, () -> userService.unverify(2L));
    }

    @Test
    public void unverify_Should_InvokeInvalidate_When_TargetUserExists() {
        //Arrange
        User mockUserToValidate = createMockUser();
        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(mockUserToValidate));

        //Act
        userService.unverify(mockUserToValidate.getId());

        //Assert
        Mockito.verify(mockRepository, Mockito.times(1))
                .invalidate(mockUserToValidate.getId());
    }

    @Test
    public void getVehiclesByUserId_Should_Throw_When_UserWithIdDoesNotExist() {
        //Arrange
        User mockUserLogged = createMockUser();
        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.empty());

        //Act, Assert
        Assertions.assertThrows(EntityNotFoundException.class, () -> userService.getVehiclesByUserId(2L, mockUserLogged));
    }

    @Test
    public void getVehiclesByUserId_Should_Throw_When_EditorIsNotAdminNorSameUser() {
        //Arrange
        User mockUserLogged = createMockUser();

        User mockUserTarget = createMockUser();
        mockUserTarget.setId(2L);

        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(mockUserTarget));

        //Act, Assert
        Assertions.assertThrows(AuthorizationException.class, () -> userService.getVehiclesByUserId(1L, mockUserLogged));
    }

    @Test
    public void getVehiclesByUserId_Should_InvokeGetVehiclesByOwnerId_When_EditorIsAdminOrSameUser() {
        //Arrange
        User mockUserLogged = createMockUser();
        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(mockUserLogged));

        //Act
        userService.getVehiclesByUserId(mockUserLogged.getId(), mockUserLogged);

        //Assert
        Mockito.verify(vehicleRepository, Mockito.times(1))
                .findAllByOwnerId(mockUserLogged.getId());
    }

    @Test
    public void count_Should_InvokeCount() {
        //Act
        userService.count();

        //Assert
        Mockito.verify(mockRepository, Mockito.times(1))
                .count();
    }

    @Test
    public void addVehicle_Should_Throw_When_UserWithIdDoesNotExist() {
        //Arrange
        User mockUserLogged = createMockUser();
        Vehicle mockVehicle = createMockVehicle();

        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.empty());

        //Act, Assert
        Assertions.assertThrows(EntityNotFoundException.class, () -> userService.addVehicle(2L, mockVehicle, mockUserLogged));
    }

    @Test
    public void addVehicle_Should_Throw_When_EditorIsNotAdminNorSameUser() {
        //Arrange
        User mockUserLogged = createMockUser();
        Vehicle mockVehicle = createMockVehicle();

        User mockUserTarget = createMockUser();
        mockUserTarget.setId(2L);

        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(mockUserTarget));

        //Act, Assert
        Assertions.assertThrows(AuthorizationException.class,
                () -> userService.addVehicle(mockUserTarget.getId(), mockVehicle, mockUserLogged));
    }

    @Test
    public void addVehicle_Should_AddVehicle_When_EditorIsAdminOrSameUser() {
        //Arrange
        User mockUserLogged = createMockUser();
        Vehicle mockVehicle = createMockVehicle();

        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(mockUserLogged));

        //Act
        userService.addVehicle(mockUserLogged.getId(), mockVehicle, mockUserLogged);

        //Assert
        Assertions.assertEquals(1, mockUserLogged.getVehicles().size());
        Assertions.assertEquals(mockVehicle, mockUserLogged.getVehicles().get(0));
    }

    @Test
    public void addVehicle_Should_SetVehicleOwner_When_EditorIsAdminOrSameUser() {
        //Arrange
        User mockUserLogged = createMockUser();
        Vehicle mockVehicle = createMockVehicle();

        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(mockUserLogged));

        //Act
        userService.addVehicle(mockUserLogged.getId(), mockVehicle, mockUserLogged);

        //Assert
        Assertions.assertEquals(mockUserLogged, mockVehicle.getOwner());
    }

    @Test
    public void addVehicle_Should_InvokeSave_When_EditorIsAdminOrSameUser() {
        //Arrange
        User mockUserLogged = createMockUser();
        Vehicle mockVehicle = createMockVehicle();

        Mockito.when(mockRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(mockUserLogged));

        //Act
        userService.addVehicle(mockUserLogged.getId(), mockVehicle, mockUserLogged);

        //Assert
        Mockito.verify(mockRepository, Mockito.times(1))
                .save(mockUserLogged);
    }
    @Test
    public void findTopTenDrivers_Should_Invoke_Repository(){
        //Act
        userService.findTopTenDrivers();

        //Assert
        Mockito.verify(mockRepository,Mockito.times(1))
                .findTopTenDrivers();
    }
    @Test
    public void findTopTenPassengers_Should_Invoke_Repository(){
        //Act
        userService.findTopTenPassengers();

        //Assert
        Mockito.verify(mockRepository,Mockito.times(1))
                .findTopTenPassengers();
    }
}
