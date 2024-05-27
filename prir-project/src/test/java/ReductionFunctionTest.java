import org.example.core.ReductionFunction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReductionFunctionTest {

    @Test
    void givenHash_whenReduceHash_thenReducedHashesAreEqual() {
        // Given
        String hash = "asasdasdasd";

        // When
        String reduced1 = new ReductionFunction().reduceHash(hash, 1);
        String reduced2 = new ReductionFunction().reduceHash(hash, 1);

        // Then
        assertEquals(reduced1, reduced2);
    }
}
