import com.example.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

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

    @ParameterizedTest
    @DisplayName("Booking in the past should throw exception")
    @CsvSource({
            "room1, 2025-01-29T11:59:59",
            "room2, 2024-12-25T10:00:00"
    })
    public void bookingInThePastThrowsException(String roomId, String pastTime) {
        LocalDateTime startTime = LocalDateTime.parse(pastTime);
        Room mockRoom = new Room(roomId, "Konferensrum");
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(mockRoom));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> bookingSystem.bookRoom(roomId, startTime, startTime.plusHours(1)));
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

    @ParameterizedTest
    @DisplayName("Incorrect booking time returns illegal exception in GetAvailableRoom")
    @CsvSource({
            "'2025-01-01T12:00:00', null",
            "null, '2025-01-01T12:00:01'"
    })
    public void incorrectBookingTimeReturnsIllegalExceptionInGetAvailableRoom(String start, String end) {
        LocalDateTime startTime = (start != null && !start.equals("null")) ? LocalDateTime.parse(start) : null;
        LocalDateTime endTime = (end != null && !end.equals("null")) ? LocalDateTime.parse(end) : null;

        // Kontrollera att ett undantag kastas om start eller sluttid är null
        Exception exception = assertThrows(IllegalArgumentException.class, () -> bookingSystem.getAvailableRooms(startTime, endTime));
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

    @ParameterizedTest
    @DisplayName("Returns a list of available rooms")
    @MethodSource("provideRoomsForAvailabilityTest")
    public void returnsAListOfAvailableRooms(List<Room> rooms, LocalDateTime startTime, LocalDateTime endTime) {
        when(roomRepository.findAll()).thenReturn(rooms);

        List<Room> availableRooms = bookingSystem.getAvailableRooms(startTime, endTime);

        for (Room room : rooms) {
            if (room.isAvailable(startTime, endTime)) {
                assertTrue(availableRooms.contains(room), "Expected room to be available: " + room);
            } else {
                assertFalse(availableRooms.contains(room), "Expected room to be booked: " + room);
            }
        }
    }

    private static Stream<Arguments> provideRoomsForAvailabilityTest() {
        Room availableRoom1 = mock(Room.class);
        Room availableRoom2 = mock(Room.class);
        Room bookedRoom1 = mock(Room.class);
        Room bookedRoom2 = mock(Room.class);

        LocalDateTime startTime = LocalDateTime.of(2025, 4, 1, 12, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2025, 4, 1, 12, 1, 0);

        when(availableRoom1.isAvailable(startTime, endTime)).thenReturn(true);
        when(availableRoom2.isAvailable(startTime, endTime)).thenReturn(true);
        when(bookedRoom1.isAvailable(startTime, endTime)).thenReturn(false);
        when(bookedRoom2.isAvailable(startTime, endTime)).thenReturn(false);

        return Stream.of(
                Arguments.of(Arrays.asList(availableRoom1, bookedRoom1), startTime, endTime),
                Arguments.of(Arrays.asList(availableRoom2, bookedRoom2), startTime, endTime),
                Arguments.of(Arrays.asList(availableRoom1, availableRoom2, bookedRoom1), startTime, endTime)
        );
    }

    @Test
    @DisplayName("Get available rooms should return empty list if no rooms are available")
    public void getAvailableRoomsShouldReturnEmptyListIfNoRoomsAvailable() {
        LocalDateTime startTime = LocalDateTime.of(2025, 4, 1, 12, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2025, 4, 1, 12, 1, 0);

        Room mockRoom = mock(Room.class);
        when(mockRoom.isAvailable(startTime, endTime)).thenReturn(false);

        List<Room> rooms = List.of(mockRoom);
        when(roomRepository.findAll()).thenReturn(rooms);

        List<Room> availableRooms = bookingSystem.getAvailableRooms(startTime, endTime);
        assertTrue(availableRooms.isEmpty(), "Available rooms should be empty if no rooms are available");
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

    @Test
    public void testCancelBookingHandlesNotificationException() throws NotificationException {
        String bookingId = "booking123";
        Booking mockBooking = mock(Booking.class);

        when(mockBooking.getStartTime()).thenReturn(LocalDateTime.of(2025, 4, 1, 12, 0, 0));

        doThrow(new NotificationException("Failed to send cancellation")).when(notificationService).sendCancellationConfirmation(mockBooking);

        Room mockRoom = mock(Room.class);
        when(mockRoom.hasBooking(bookingId)).thenReturn(true);
        when(mockRoom.getBooking(bookingId)).thenReturn(mockBooking);

        List<Room> rooms = Arrays.asList(mockRoom);
        when(roomRepository.findAll()).thenReturn(rooms);

        boolean result = bookingSystem.cancelBooking(bookingId);

        assertTrue(result, "Cancel booking should return true even if notification fails");
        verify(notificationService).sendCancellationConfirmation(mockBooking);
    }






}
