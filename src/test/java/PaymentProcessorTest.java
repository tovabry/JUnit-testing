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

    @Test
    @DisplayName("Successful payment should send email to payer")
    void successfulPaymentShouldSendEmailToPayer() throws SQLException {
        double amount = 55.50;
        when(paymentApi.charge("sk_test_123456", amount)).thenReturn(response);
        paymentProcessor.processPayment(amount);
        verify(emailService).sendPaymentConfirmation("user@example.com", amount);
    }

    @Test
    @DisplayName("Failed payment should not update database")
    void failedPaymentShouldNotUpdateDatabase() throws SQLException {
        PaymentApiResponse failedResponse = new PaymentApiResponse(false);
        when(paymentApi.charge("sk_test_123456", 200.0)).thenReturn(failedResponse);
        boolean result = paymentProcessor.processPayment(200.0);
        assertFalse(result);
        verify(databaseConnection, Mockito.never()).executeUpdate(Mockito.anyString());
    }

    @Test
    @DisplayName("Failed payment should not send email")
    void failedPaymentShouldNotSendEmail() throws SQLException {
        PaymentApiResponse failedResponse = new PaymentApiResponse(false);
        when(paymentApi.charge("sk_test_123456", 100.0)).thenReturn(failedResponse);
        boolean result = paymentProcessor.processPayment(100.0);
        assertFalse(result);
        verify(emailService, Mockito.never()).sendPaymentConfirmation(Mockito.anyString(), Mockito.anyDouble());
    }

}
