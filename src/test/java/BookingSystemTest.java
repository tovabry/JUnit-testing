import com.example.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    @DisplayName("Trying to book with end time before start time returns illegal exception in getAvailableRoom")
    public void tryingToBookWithEndTimeBeforeStartTimeReturnsIllegalException() {
        LocalDateTime startTime = LocalDateTime.of(2025, 4, 1, 12, 0, 1);
        LocalDateTime endTime = LocalDateTime.of(2025, 4, 1, 12, 0, 0);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> bookingSystem.bookRoom("room1", startTime, endTime));
        assertEquals("Sluttid måste vara efter starttid", exception.getMessage());
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

    @Test
    @DisplayName("Booking allowed room and date should save room and return confirmation")
    public void bookingAllowedRoomAndDateShouldSaveRoomAndReturnConfirmation() throws NotificationException {
        LocalDateTime startTime = LocalDateTime.of(2025, 4, 1, 12, 0, 0);
        Room mockRoom = new Room("room1", "Konferensrum");

        //simulerar att rummet finns i databasen
        when(roomRepository.findById("room1")).thenReturn(Optional.of(mockRoom));
        doThrow(new NotificationException("Notification failed")).when(notificationService).sendBookingConfirmation(any(Booking.class));

        boolean result = bookingSystem.bookRoom("room1", startTime, startTime.plusHours(1));

        assertTrue(result);
        //kontrollerar att save-metoden anropas
        verify(roomRepository).save(any(Room.class));
        //kontrollerar att sendBookingConfirmation anropas
        verify(notificationService).sendBookingConfirmation(any(Booking.class)); //Kontrollera att notifieringen försöktes skickas
    }

    @Test
    @DisplayName("Booking an unavailable date should return false")
    public void bookingNonAvailableDateShouldReturnFalse() {
        LocalDateTime startTime = LocalDateTime.of(2025, 4, 1, 12, 0, 0);
        Room mockRoom = mock(Room.class); // Mock av rummet

        // Simulerar att rummet inte är tillgängligt
        when(mockRoom.isAvailable(startTime, startTime.plusHours(1))).thenReturn(false);

        // Mockar roomRepository för att returnera mockRoom
        when(roomRepository.findById("room1")).thenReturn(Optional.of(mockRoom));

        boolean result = bookingSystem.bookRoom("room1", startTime, startTime.plusHours(1));
        assertFalse(result);
    }

    @Test
    @DisplayName("Incorrect booking time returns illegal exception in GetAvaliableRoom")
    public void incorrectBookingTimeReturnsIllegalExceptionInGetAvaliableRoom() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> bookingSystem.getAvailableRooms(null, null));
        assertEquals("Måste ange både start- och sluttid", exception.getMessage());
    }

    @Test
    @DisplayName("End time before start time returns illegal exception in getAvailableRoom")
    public void endTimeBeforeStartTimeReturnsIllegalExceptionInGetAvailableRoom() {
        LocalDateTime startTime = LocalDateTime.of(2025, 4, 1, 12, 0, 1);
        LocalDateTime endTime = LocalDateTime.of(2025, 4, 1, 12, 0, 0);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> bookingSystem.getAvailableRooms(startTime, endTime));
        assertEquals("Sluttid måste vara efter starttid", exception.getMessage());
    }

    @Test
    @DisplayName("Returns a list of available rooms")
    public void returnsAListOfAvailableRooms() {

        LocalDateTime startTime = LocalDateTime.of(2025, 4, 1, 12, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2025, 4, 1, 12, 0, 1);
        Room availableRoom = mock(Room.class);
        Room bookedRoom = mock(Room.class);
        when(availableRoom.isAvailable(startTime, endTime)).thenReturn(true);
        when(bookedRoom.isAvailable(startTime, endTime)).thenReturn(false);

        List<Room> rooms = List.of(availableRoom, bookedRoom);
        when(roomRepository.findAll()).thenReturn(rooms);
        List<Room> availableRooms = bookingSystem.getAvailableRooms(startTime, endTime);

        assertTrue(availableRooms.contains(availableRoom));
        assertFalse(availableRooms.contains(bookedRoom));
    }

    @Test
    @DisplayName("Cancel booking input can't be null")
    public void cancelBookingInputCanNotBeNull() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> bookingSystem.cancelBooking(null));
        assertEquals("Boknings-id kan inte vara null", exception.getMessage());
    }

    @Test
    @DisplayName("Cancel booking should return true if booking exists")
    public void cancelBookingSuccessfully() {
        String bookingId = "booking123";

        // Mockar ett rum som har en bokning
        Room mockRoom = mock(Room.class);
        Booking mockBooking = mock(Booking.class);

        // Mockar att getStartTime() returnerar ett specifikt starttid för bokningen
        LocalDateTime startTime = LocalDateTime.of(2025, 4, 1, 12, 0, 0);
        when(mockBooking.getStartTime()).thenReturn(startTime);

        // Simulerar att rummet har den specifika bokningen
        when(mockRoom.hasBooking(bookingId)).thenReturn(true);
        when(mockRoom.getBooking(bookingId)).thenReturn(mockBooking);

        List<Room> rooms = Arrays.asList(mockRoom);
        when(roomRepository.findAll()).thenReturn(rooms);

        boolean result = bookingSystem.cancelBooking(bookingId);
        assertTrue(result);
        verify(mockRoom).getBooking(bookingId);
    }

    @Test
    @DisplayName("Cancel booking should return false if no room with the booking ID exists")
    public void cancelBookingRoomNotFound() {
        String bookingId = "booking123";
        when(roomRepository.findAll()).thenReturn(Collections.emptyList());
        boolean result = bookingSystem.cancelBooking(bookingId);

        assertFalse(result, "Result should be false if no room with the bookingId exists");

        // Verifierar att findAll() anropades för att få listan på alla rum
        verify(roomRepository).findAll();
    }

    @Test
    @DisplayName("roomWithBooking should return illegalStateEx if trying to cancel a current or terminated booking")
    public void roomWithBookingShouldReturnIllegalStateExIfTryingToCancelACurrentOrTerminatedBooking() {
        String bookingId = "booking123";

        Room mockRoom = mock(Room.class);
        Booking mockBooking = mock(Booking.class);

        LocalDateTime startTime = LocalDateTime.of(2024, 4, 1, 12, 0, 0);
        when(mockBooking.getStartTime()).thenReturn(startTime);

        when(mockRoom.hasBooking(bookingId)).thenReturn(true);
        when(mockRoom.getBooking(bookingId)).thenReturn(mockBooking);

        List<Room> rooms = Arrays.asList(mockRoom);
        when(roomRepository.findAll()).thenReturn(rooms);

        Exception exception = assertThrows(IllegalStateException.class, () -> bookingSystem.cancelBooking(bookingId));
        assertEquals("Kan inte avboka påbörjad eller avslutad bokning", exception.getMessage());

    }





}
