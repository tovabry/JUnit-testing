import com.example.payment.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PaymentProcessorTest {

    @Mock
    private EmailService emailService;

    @Mock
    private DatabaseConnection databaseConnection;

    @Mock
    private PaymentApi paymentApi;

    @Mock
    private PreparedStatement preparedStatement;

    @InjectMocks
    private PaymentProcessor paymentProcessor;

    private PaymentApiResponse response;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        response = new PaymentApiResponse(true);
        when(databaseConnection.getInstance()).thenReturn(preparedStatement);
    }

    @Test
    @DisplayName("Successful payment should update database")
    void successfulPaymentShouldUpdateDatabase() throws SQLException {
        double amount = 100.0;
        when(paymentApi.charge("sk_test_123456", amount)).thenReturn(response);
        boolean result = paymentProcessor.processPayment(amount);
        assertTrue(result);
        verify(preparedStatement).executeUpdate(Mockito.anyString());
    }
}
