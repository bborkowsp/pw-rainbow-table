import lombok.Getter;
import org.example.Des;
import org.example.RainbowTable;
import org.example.ReductionFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Getter
public class RainbowTableTest {

    private Des desMock;
    private ReductionFunction reductionFunctionMock;
    private RainbowTable rainbowTable;
    private final int CHAIN_LENGTH = 100;
    private final int NUMBER_OF_CHAINS = 100;

    @BeforeEach
    public void setUp() {
        desMock = mock(Des.class);
        reductionFunctionMock = mock(ReductionFunction.class);
        rainbowTable = new RainbowTable(CHAIN_LENGTH, NUMBER_OF_CHAINS);

        rainbowTable.setDes(desMock);
        rainbowTable.setReductionFunction(reductionFunctionMock);
    }

    @Test
    public void givenRainbowTable_whenGenerate_thenRainbowTableGenerated() throws Exception {
        //Given
        when(desMock.cipherPassword(anyString(), anyString())).thenReturn("hashedValue".getBytes());
        when(reductionFunctionMock.reduceHash(any())).thenReturn("reducedValue");

        //When
        rainbowTable.generateChain("password");

        //Then
        assertEquals("password", rainbowTable.getTable()[0][0]);
        assertEquals("hashedValue", rainbowTable.getTable()[0][1]);
    }

    @Test
    public void givenRainbowTable_whenGenerateMultipleChains_thenLastRowCorrect() throws Exception {
        // Given
        when(desMock.cipherPassword(anyString(), anyString())).thenReturn("hashedValue".getBytes());
        when(reductionFunctionMock.reduceHash(any())).thenReturn("reducedValue");

        //When
        for (int i = 0; i < NUMBER_OF_CHAINS; i++) {
            rainbowTable.generateChain("qwerty" + i);
        }

        //Then
        assertEquals("qwerty99", rainbowTable.getTable()[99][0]);
        assertEquals("hashedValue", rainbowTable.getTable()[99][1]);
    }
}
