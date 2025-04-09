package exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TaskOverlappingExceptionTest {
    @Test
    public void testManagerSaveException_ShouldCreateException() {
        Exception exception = new TaskOverlappingException("Test");

        assertThrows(TaskOverlappingException.class, () -> {
            throw exception;
        }, "Не выбросился Exception");

        assertEquals("Test", exception.getMessage(), "Не верно устанавливается сообщение для Exception");
    }
}
