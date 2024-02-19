import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StatusCodeTest {

    @Test
    public void testGetCode() {
        assertEquals(9101, StatusCode.STAR_FUNC_FAIL9101.getCode());
        assertEquals(100, StatusCode.SUCCESS.getCode());
        assertEquals(1604, StatusCode.ACTIION_FAILURE.getCode());
    }

    @Test
    public void testGetDesc() {
        assertNull(StatusCode.STAR_FUNC_FAIL9101.getDesc());
        assertNull(StatusCode.SUCCESS.getDesc());
        assertEquals("Action Failure", StatusCode.ACTIION_FAILURE.getDesc());
    }

    @Test
    public void testGetDescFormatted() {
        assertEquals("Func Fail9101", StatusCode.STAR_FUNC_FAIL9101.getDescFormatted());
        assertEquals("Success", StatusCode.SUCCESS.getDescFormatted());
        assertEquals("Action Failure", StatusCode.ACTIION_FAILURE.getDescFormatted());
    }
}
