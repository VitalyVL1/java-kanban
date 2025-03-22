package exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ManagerSaveExceptionTest {
    @Test
    public void testManagerSaveException_ShouldCreateException() {
        Exception exception = new ManagerSaveException("Test");

        assertThrows(ManagerSaveException.class, () -> {
            throw exception;
        }, "Не выбросился Exception");

        assertEquals("Test", exception.getMessage(), "Не верно устанавливается сообщение для Exception");
    }
}
