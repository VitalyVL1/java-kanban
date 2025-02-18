package model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    private static Epic epic1;
    private static Epic epic2;

    @BeforeAll
    static void setUpBeforeClass(){
        epic1 = new Epic("Epic1", "Description Epic1");
        epic2 = new Epic("Epic2", "Description Epic2");
      }

    @Test
    void testEpicEquals() {
        epic1.setId(1);
        epic2.setId(1);
        assertEquals(epic1, epic2, "Epics не равны друг другу");
    }

}