import lombok.Getter;
import org.example.Main;
import org.example.Des;
import org.example.RainbowTable;
import org.example.ReductionFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Getter
public class RainbowTableTest {

    private Des desMock;
    private ReductionFunction reductionFunctionMock;
    private RainbowTable rainbowTable;
    private final int CHAIN_LENGTH = 2;
    private final String[] PASSWORDS = new String[]{"password", "12345678", "87654321", "drowssap"};
    private final String[] HASHED_VALUES = new String[]{"hashed_value_1", "hashed_value_2", "hashed_value_3", "hashed_value_4"};
    private final String PLAIN_TEXT = Main.PLAIN_TEXT;

    @BeforeEach
    public void setUp() {
        desMock = mock(Des.class);
        reductionFunctionMock = mock(ReductionFunction.class);
        rainbowTable = new RainbowTable(PLAIN_TEXT, CHAIN_LENGTH, PASSWORDS.length, desMock, reductionFunctionMock);
    }

    @Test
    public void givenRainbowTable_whenGenerate_thenRainbowTableGenerated() throws Exception {
        // Given
        String hashedValue = HASHED_VALUES[0];
        String beginningPassword = PASSWORDS[0];
        String finalPassword = PASSWORDS[1];
        when(desMock.cipherPassword(anyString(), anyString())).thenReturn(hashedValue);
        when(reductionFunctionMock.reduceHash(any(), anyInt())).thenReturn(finalPassword);

        // When
        rainbowTable.generateChain(beginningPassword);

        // Then
        assertEquals(beginningPassword, rainbowTable.getTable()[0][0]);
        assertEquals(hashedValue, rainbowTable.getTable()[0][1]);
    }

    @Test
    public void givenRainbowTable_whenGenerateMultipleChains_thenLastRowCorrect() throws Exception {
        // Given
        for (int i = 0; i < PASSWORDS.length; i++) {
            when(desMock.cipherPassword(PLAIN_TEXT, PASSWORDS[i])).thenReturn(HASHED_VALUES[i]);
            when(reductionFunctionMock.reduceHash(eq(HASHED_VALUES[i]), anyInt())).thenReturn(PASSWORDS[i]);
        }

        // When
        for (String password : PASSWORDS) {
            rainbowTable.generateChain(password);
        }

        // Then
        for (int i = 0; i < PASSWORDS.length; i++) {
            assertEquals(PASSWORDS[i], rainbowTable.getTable()[i][0]);
            assertEquals(HASHED_VALUES[i], rainbowTable.getTable()[i][1]);
        }
    }

    @Test
    void givenRainbowTable_whenCrackKey_thenKeyCrackedSuccessfully() throws Exception {
        // Given
        for (int i = 0; i < PASSWORDS.length; i++) {
            when(desMock.cipherPassword(eq(PLAIN_TEXT), eq(PASSWORDS[i]))).thenReturn(HASHED_VALUES[i]);
            when(reductionFunctionMock.reduceHash(eq(HASHED_VALUES[i]), anyInt())).thenReturn(PASSWORDS[i]);
        }

        for (String password : PASSWORDS) {
            rainbowTable.generateChain(password);
        }

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        // When
        rainbowTable.crackKeySequential(HASHED_VALUES[0]);

        // Then
        String output = outContent.toString();
        assertTrue(output.contains("Key cracked: " + PASSWORDS[0]));

        outContent.reset();
    }

    @Test
    void givenRainbowTable_whenCrackKeyThatNotExists_thenKeyCrackingFailed() throws Exception {
        // Given
        for (int i = 0; i < PASSWORDS.length; i++) {
            when(desMock.cipherPassword(eq(PLAIN_TEXT), eq(PASSWORDS[i]))).thenReturn(HASHED_VALUES[i]);
            when(reductionFunctionMock.reduceHash(eq(HASHED_VALUES[i]), anyInt())).thenReturn(PASSWORDS[i]);
        }
        when(desMock.cipherPassword(eq(PLAIN_TEXT), anyString())).thenReturn("unknownHash");
        when(reductionFunctionMock.reduceHash(any(), anyInt())).thenReturn("unknown_");

        for (String password : PASSWORDS) {
            rainbowTable.generateChain(password);
        }

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        // When
        rainbowTable.crackKeySequential("keyThatDoesNotExist");

        // Then
        String output = outContent.toString();
        assertTrue(output.contains("Key not found in the rainbow table chains"));

        outContent.reset();
    }

    @Test
    void abc() throws Exception {
        Des des1 = new Des();
//        Des des2 = new Des();

        String output1 = des1.cipherPassword(getPLAIN_TEXT(), "12345678");
        String output2 = des1.cipherPassword(getPLAIN_TEXT(), "12345678");

        assertEquals(output1, output2);
    }

    @Test
    void abc2() throws Exception {
        String hash = "asasdasdasd";

        String reduced1 = new ReductionFunction().reduceHash(hash, 1);
        String reduced2 = new ReductionFunction().reduceHash(hash, 1);

        assertEquals(reduced1, reduced2);
    }

}
