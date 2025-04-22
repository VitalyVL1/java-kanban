package exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NotFoundExceptionTest {
    @Test
    public void testNotFoundExceptionTest_ShouldCreateException() {
        Exception exception = new NotFoundException("Test");

        assertThrows(NotFoundException.class, () -> {
            throw exception;
        }, "Не выбросился Exception");

        assertEquals("Test", exception.getMessage(), "Не верно устанавливается сообщение для Exception");
    }
}
