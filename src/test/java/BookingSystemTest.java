import com.example.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class BookingSystemTest {
    @Mock TimeProvider timeProvider;
    @Mock RoomRepository roomRepository;
    @Mock NotificationService notificationService;
    @InjectMocks
    BookingSystem bookingSystem;

    //Initierar mocksen
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        //setup för att få ett mockat localdatetime
        LocalDateTime currentTime = LocalDateTime.of(2025, 1, 29, 12, 0, 0);
        when(timeProvider.getCurrentTime()).thenReturn(currentTime);
    }

    @Test
    @DisplayName("Booking time is incorrect returns illegal Exceptions")
    public void BookingTimeIsIncorrectReturnsIllegalExceptions() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> bookingSystem.bookRoom(null, null, null));
        assertEquals("Bokning kräver giltiga start- och sluttider samt rum-id", exception.getMessage());

    }

    @Test
    @DisplayName("Booking a room in the past should throw exception")
    public void bookingInThePastThrowsException() {
        // Mocka att ett rum med id "room1" existerar
        Room mockRoom = new Room("room1", "Konferensrum");
        when(roomRepository.findById("room1")).thenReturn(Optional.of(mockRoom));

        // Testa att boka en tid i dåtid
        LocalDateTime pastTime = LocalDateTime.of(2023, 1, 1, 12, 0, 0);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> bookingSystem.bookRoom("room1", pastTime, pastTime.plusHours(1)));

        assertEquals("Kan inte boka tid i dåtid", exception.getMessage());
    }

    @Test
    @DisplayName("Booking non existing room should throw exception")
    public void bookingNonExistingRoomThrowsException() {

        LocalDateTime startTime = LocalDateTime.of(2026, 1, 1, 12, 0, 0);

        // Försök att boka ett rum som inte finns (room2)
        String nonExistentRoomId = "room2";
        // Mocka att rum med id "room1" finns men inte "room2"
        Room mockRoom = new Room("room1", "Konferensrum");
        when(roomRepository.findById("room1")).thenReturn(Optional.of(mockRoom));
        when(roomRepository.findById(nonExistentRoomId)).thenReturn(Optional.empty());  // Mocka att "room2" inte finns

        // Test: Förvänta att en exception kastas när man försöker boka ett icke-existerande rum
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingSystem.bookRoom(nonExistentRoomId, startTime, startTime.plusHours(1));
        });
        assertEquals("Rummet existerar inte", exception.getMessage());
    }


}
