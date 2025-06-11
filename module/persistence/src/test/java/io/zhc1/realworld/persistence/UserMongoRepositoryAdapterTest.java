package io.zhc1.realworld.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.zhc1.realworld.model.PasswordEncoder;
import io.zhc1.realworld.model.User;

@ExtendWith(MockitoExtension.class)
class UserMongoRepositoryAdapterTest {

    @Mock
    private UserMongoRepository userMongoRepository;

    @Mock
    private PasswordEncoder passwordEncoder; // Mock for updateUserDetails

    @InjectMocks
    private UserMongoRepositoryAdapter userMongoRepositoryAdapter;

    private User sampleUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        // Use a real User object to test its internal logic when setters are called
        sampleUser = new User("test@example.com", "testuser", "password123");
        // Simulate user having an ID as if it was persisted
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(sampleUser, userId);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("save should call repository save and return user")
    void save_shouldReturnSavedUser() {
        when(userMongoRepository.save(any(User.class))).thenReturn(sampleUser);

        User savedUser = userMongoRepositoryAdapter.save(sampleUser);

        assertNotNull(savedUser);
        assertEquals(sampleUser.getEmail(), savedUser.getEmail());
        verify(userMongoRepository).save(sampleUser);
    }

    @Nested
    @DisplayName("findById tests")
    class FindByIdTests {
        @Test
        @DisplayName("when user exists should return user")
        void findById_whenUserExists_shouldReturnUser() {
            when(userMongoRepository.findById(userId)).thenReturn(Optional.of(sampleUser));

            Optional<User> foundUser = userMongoRepositoryAdapter.findById(userId);

            assertTrue(foundUser.isPresent());
            assertEquals(sampleUser.getId(), foundUser.get().getId());
            verify(userMongoRepository).findById(userId);
        }

        @Test
        @DisplayName("when user does not exist should return empty optional")
        void findById_whenUserDoesNotExist_shouldReturnEmpty() {
            when(userMongoRepository.findById(userId)).thenReturn(Optional.empty());

            Optional<User> foundUser = userMongoRepositoryAdapter.findById(userId);

            assertFalse(foundUser.isPresent());
            verify(userMongoRepository).findById(userId);
        }
    }

    @Nested
    @DisplayName("findByEmail tests")
    class FindByEmailTests {
        @Test
        @DisplayName("when user with email exists should return user")
        void findByEmail_whenUserExists_shouldReturnUser() {
            String email = "test@example.com";
            when(userMongoRepository.findByEmail(email)).thenReturn(Optional.of(sampleUser));

            Optional<User> foundUser = userMongoRepositoryAdapter.findByEmail(email);

            assertTrue(foundUser.isPresent());
            assertEquals(email, foundUser.get().getEmail());
            verify(userMongoRepository).findByEmail(email);
        }

        @Test
        @DisplayName("when user with email does not exist should return empty optional")
        void findByEmail_whenUserDoesNotExist_shouldReturnEmpty() {
            String email = "nonexistent@example.com";
            when(userMongoRepository.findByEmail(email)).thenReturn(Optional.empty());

            Optional<User> foundUser = userMongoRepositoryAdapter.findByEmail(email);

            assertFalse(foundUser.isPresent());
            verify(userMongoRepository).findByEmail(email);
        }
    }

    @Nested
    @DisplayName("findByUsername tests")
    class FindByUsernameTests {
        @Test
        @DisplayName("when user with username exists should return user")
        void findByUsername_whenUserExists_shouldReturnUser() {
            String username = "testuser";
            when(userMongoRepository.findByUsername(username)).thenReturn(Optional.of(sampleUser));

            Optional<User> foundUser = userMongoRepositoryAdapter.findByUsername(username);

            assertTrue(foundUser.isPresent());
            assertEquals(username, foundUser.get().getUsername());
            verify(userMongoRepository).findByUsername(username);
        }

        @Test
        @DisplayName("when user with username does not exist should return empty optional")
        void findByUsername_whenUserDoesNotExist_shouldReturnEmpty() {
            String username = "nonexistentuser";
            when(userMongoRepository.findByUsername(username)).thenReturn(Optional.empty());

            Optional<User> foundUser = userMongoRepositoryAdapter.findByUsername(username);

            assertFalse(foundUser.isPresent());
            verify(userMongoRepository).findByUsername(username);
        }
    }

    @Nested
    @DisplayName("existsByEmail tests")
    class ExistsByEmailTests {
        @Test
        @DisplayName("when email exists should return true")
        void existsByEmail_whenEmailExists_shouldReturnTrue() {
            String email = "test@example.com";
            when(userMongoRepository.existsByEmail(email)).thenReturn(true);

            assertTrue(userMongoRepositoryAdapter.existsByEmail(email));
            verify(userMongoRepository).existsByEmail(email);
        }

        @Test
        @DisplayName("when email does not exist should return false")
        void existsByEmail_whenEmailDoesNotExist_shouldReturnFalse() {
            String email = "nonexistent@example.com";
            when(userMongoRepository.existsByEmail(email)).thenReturn(false);

            assertFalse(userMongoRepositoryAdapter.existsByEmail(email));
            verify(userMongoRepository).existsByEmail(email);
        }
    }

    @Nested
    @DisplayName("existsByUsername tests")
    class ExistsByUsernameTests {
        @Test
        @DisplayName("when username exists should return true")
        void existsByUsername_whenUsernameExists_shouldReturnTrue() {
            String username = "testuser";
            when(userMongoRepository.existsByUsername(username)).thenReturn(true);

            assertTrue(userMongoRepositoryAdapter.existsByUsername(username));
            verify(userMongoRepository).existsByUsername(username);
        }

        @Test
        @DisplayName("when username does not exist should return false")
        void existsByUsername_whenUsernameDoesNotExist_shouldReturnFalse() {
            String username = "nonexistentuser";
            when(userMongoRepository.existsByUsername(username)).thenReturn(false);

            assertFalse(userMongoRepositoryAdapter.existsByUsername(username));
            verify(userMongoRepository).existsByUsername(username);
        }
    }

    @Nested
    @DisplayName("existsBy (email or username) tests")
    class ExistsByTests {
        @Test
        @DisplayName("when email or username exists should return true")
        void existsBy_whenEmailOrUsernameExists_shouldReturnTrue() {
            String email = "test@example.com";
            String username = "testuser";
            when(userMongoRepository.existsByEmailOrUsername(email, username)).thenReturn(true);

            assertTrue(userMongoRepositoryAdapter.existsBy(email, username));
            verify(userMongoRepository).existsByEmailOrUsername(email, username);
        }

        @Test
        @DisplayName("when neither email nor username exists should return false")
        void existsBy_whenNeitherEmailNorUsernameExists_shouldReturnFalse() {
            String email = "nonexistent@example.com";
            String username = "nonexistentuser";
            when(userMongoRepository.existsByEmailOrUsername(email, username)).thenReturn(false);

            assertFalse(userMongoRepositoryAdapter.existsBy(email, username));
            verify(userMongoRepository).existsByEmailOrUsername(email, username);
        }
    }

    @Nested
    @DisplayName("updateUserDetails tests")
    class UpdateUserDetailsTests {
        private String newEmail = "new@example.com";
        private String newUsername = "newuser";
        private String newPassword = "newPassword123";
        private String newBio = "New bio";
        private String newImageUrl = "http://new.image.url";

        @BeforeEach
        void updateSetup() {
            // Default stub for findById, can be overridden by specific tests.
            when(userMongoRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        }

        @Test
        @DisplayName("when user exists and all data is valid, should update and return user")
        void updateUserDetails_whenUserExistsAndDataIsValid_shouldUpdateAndReturnUser() {
            when(userMongoRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");
            // Make passwordEncoder.matches return false so password gets updated
            when(passwordEncoder.matches(newPassword, sampleUser.getPassword())).thenReturn(false);


            User updatedUser = userMongoRepositoryAdapter.updateUserDetails(
                    userId, passwordEncoder, newEmail, newUsername, newPassword, newBio, newImageUrl);

            assertEquals(newEmail, updatedUser.getEmail());
            assertEquals(newUsername, updatedUser.getUsername());
            assertEquals("encodedNewPassword", updatedUser.getPassword());
            assertEquals(newBio, updatedUser.getBio());
            assertEquals(newImageUrl, updatedUser.getImageUrl());
            verify(userMongoRepository).save(sampleUser);
            verify(passwordEncoder).encode(newPassword);
        }

        @Test
        @DisplayName("when user does not exist, should throw IllegalArgumentException")
        void updateUserDetails_whenUserDoesNotExist_shouldThrowException() {
            // Override the findById from updateSetup
            when(userMongoRepository.findById(userId)).thenReturn(Optional.empty());

            Exception exception = assertThrows(IllegalArgumentException.class, () ->
                    userMongoRepositoryAdapter.updateUserDetails(
                            userId, passwordEncoder, newEmail, newUsername, newPassword, newBio, newImageUrl));
            assertEquals("user not found.", exception.getMessage());
            verify(userMongoRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("when new email already exists for another user, should throw IllegalArgumentException")
        void updateUserDetails_whenNewEmailExists_shouldThrowException() {
            // findById is stubbed in updateSetup to return sampleUser
            when(userMongoRepository.existsByEmail(newEmail)).thenReturn(true);

            Exception exception = assertThrows(IllegalArgumentException.class, () ->
                    userMongoRepositoryAdapter.updateUserDetails(
                            userId, passwordEncoder, newEmail, sampleUser.getUsername(), // old username
                            sampleUser.getPassword(), newBio, newImageUrl));
            assertEquals("email is already exists.", exception.getMessage());
            verify(userMongoRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("when new username already exists for another user, should throw IllegalArgumentException")
        void updateUserDetails_whenNewUsernameExists_shouldThrowException() {
            // findById is stubbed in updateSetup
            // Email check will pass because sampleUser.getEmail() is used, and !user.equalsEmail(email) will be false.
            // So, existsByEmail will not be called.
            when(userMongoRepository.existsByUsername(newUsername)).thenReturn(true);


            Exception exception = assertThrows(IllegalArgumentException.class, () ->
                    userMongoRepositoryAdapter.updateUserDetails(
                            userId, passwordEncoder, sampleUser.getEmail(), // old email, so no existsByEmail check
                            newUsername, sampleUser.getPassword(), newBio, newImageUrl));
            assertEquals("username is already exists.", exception.getMessage());
            verify(userMongoRepository, never()).existsByEmail(anyString()); // Ensure existsByEmail is not called
            verify(userMongoRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("when password is null, should not update password")
        void updateUserDetails_withNullPassword_shouldNotUpdatePassword() {
            when(userMongoRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
            String originalPassword = sampleUser.getPassword();
            userMongoRepositoryAdapter.updateUserDetails(
                    userId, passwordEncoder, newEmail, newUsername, null, newBio, newImageUrl);

            assertEquals(originalPassword, sampleUser.getPassword());
            verify(passwordEncoder, never()).encode(anyString());
            verify(userMongoRepository).save(sampleUser);
        }

        @Test
        @DisplayName("when password is blank, should not update password")
        void updateUserDetails_withBlankPassword_shouldNotUpdatePassword() {
            when(userMongoRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
            String originalPassword = sampleUser.getPassword();
            userMongoRepositoryAdapter.updateUserDetails(
                    userId, passwordEncoder, newEmail, newUsername, " ", newBio, newImageUrl);

            assertEquals(originalPassword, sampleUser.getPassword());
            verify(passwordEncoder, never()).encode(anyString());
            verify(userMongoRepository).save(sampleUser);
        }
        
        @Test
        @DisplayName("when new password is same as old password (after encoding check), should not re-encrypt")
        void updateUserDetails_withSamePassword_shouldNotReEncrypt() {
            when(userMongoRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
            String currentPassword = sampleUser.getPassword();
            // Simulate that PasswordEncoder.matches returns true, meaning plain password matches current
            when(passwordEncoder.matches(newPassword, currentPassword)).thenReturn(true);

            User updatedUser = userMongoRepositoryAdapter.updateUserDetails(
                userId, passwordEncoder, newEmail, newUsername, newPassword, newBio, newImageUrl);
            
            assertEquals(currentPassword, updatedUser.getPassword()); // Password should remain the original encoded one
            verify(passwordEncoder, never()).encode(newPassword); // encode should not be called
            verify(passwordEncoder).matches(newPassword, currentPassword); // matches should be called
            verify(userMongoRepository).save(sampleUser);
        }


        @Test
        @DisplayName("when new password is provided and different, should encrypt and update")
        void updateUserDetails_withNewPassword_shouldEncryptAndUpdate() {
            when(userMongoRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(passwordEncoder.matches(newPassword, sampleUser.getPassword())).thenReturn(false);
            when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");

            User updatedUser = userMongoRepositoryAdapter.updateUserDetails(
                    userId, passwordEncoder, newEmail, newUsername, newPassword, newBio, newImageUrl);

            assertEquals("encodedNewPassword", updatedUser.getPassword());
            verify(passwordEncoder).encode(newPassword);
            verify(userMongoRepository).save(sampleUser);
        }


        @Test
        @DisplayName("when bio is null, should set bio to null")
        void updateUserDetails_withNullBio_shouldSetBioToNull() {
            when(userMongoRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
            userMongoRepositoryAdapter.updateUserDetails(
                    userId, passwordEncoder, newEmail, newUsername, newPassword, null, newImageUrl);
            assertNull(sampleUser.getBio());
            verify(userMongoRepository).save(sampleUser);
        }

        @Test
        @DisplayName("when bio is blank, should not update bio (User model handles this)")
        void updateUserDetails_withBlankBio_shouldNotUpdateBio() {
            when(userMongoRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
            sampleUser.setBio("Initial Bio"); // Set an initial bio
             String originalBio = sampleUser.getBio();
            userMongoRepositoryAdapter.updateUserDetails(
                    userId, passwordEncoder, newEmail, newUsername, newPassword, " ", newImageUrl);
            // User.setBio logs a warning and doesn't change if blank.
            assertEquals(originalBio, sampleUser.getBio());
            verify(userMongoRepository).save(sampleUser);
        }


        @Test
        @DisplayName("when image URL is null, should set image URL to null")
        void updateUserDetails_withNullImage_shouldSetImageToNull() {
            when(userMongoRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
            userMongoRepositoryAdapter.updateUserDetails(
                    userId, passwordEncoder, newEmail, newUsername, newPassword, newBio, null);
            assertNull(sampleUser.getImageUrl());
            verify(userMongoRepository).save(sampleUser);
        }

        @Test
        @DisplayName("when image URL is blank, should not update image URL (User model handles this)")
        void updateUserDetails_withBlankImage_shouldNotUpdateImage() {
            when(userMongoRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
            sampleUser.setImageUrl("http://initial.url"); // Set an initial image
            String originalImage = sampleUser.getImageUrl();

            userMongoRepositoryAdapter.updateUserDetails(
                    userId, passwordEncoder, newEmail, newUsername, newPassword, newBio, " ");
            // User.setImageUrl logs a warning and doesn't change if blank.
            assertEquals(originalImage, sampleUser.getImageUrl());
            verify(userMongoRepository).save(sampleUser);
        }

        @Test
        @DisplayName("when email is not changed, should not check for email existence")
        void updateUserDetails_whenEmailNotChanged_shouldNotCheckExistence() {
            when(userMongoRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
            userMongoRepositoryAdapter.updateUserDetails(
                userId, passwordEncoder, sampleUser.getEmail(), newUsername, newPassword, newBio, newImageUrl);
            
            verify(userMongoRepository, never()).existsByEmail(sampleUser.getEmail());
            verify(userMongoRepository).save(sampleUser);
        }

        @Test
        @DisplayName("when username is not changed, should not check for username existence")
        void updateUserDetails_whenUsernameNotChanged_shouldNotCheckExistence() {
            when(userMongoRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
            userMongoRepositoryAdapter.updateUserDetails(
                userId, passwordEncoder, newEmail, sampleUser.getUsername(), newPassword, newBio, newImageUrl);
            
            verify(userMongoRepository, never()).existsByUsername(sampleUser.getUsername());
            verify(userMongoRepository).save(sampleUser);
        }
    }
}
