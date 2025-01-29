import com.example.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    }

    @Test
    @DisplayName("Booking time is incorrect returns illegal Exceptions")
    public void BookingTimeIsIncorrectReturnsIllegalExceptions() {
        Exception validTimeException = assertThrows(IllegalArgumentException.class, () -> bookingSystem.bookRoom(null, null, null));
        assertEquals("Bokning kräver giltiga start- och sluttider samt rum-id", validTimeException.getMessage());

    }


}
