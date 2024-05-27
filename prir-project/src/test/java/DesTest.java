import org.example.config.AppConfig;
import org.example.crypto.Des;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DesTest {
    private final String PLAIN_TEXT = AppConfig.PLAIN_TEXT;

    @Test
    void givenDes_whenCipherPassword_thenOutputsAreEqual() throws Exception {
        // Given
        Des des = new Des();

        // When
        String output1 = des.cipherPassword(PLAIN_TEXT, "12345678");
        String output2 = des.cipherPassword(PLAIN_TEXT, "12345678");

        // Then
        assertEquals(output1, output2);
    }
}
