import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

public class AbstractCommandTest {

    @Mock
    private Scanner scanner;
    @Mock
    private Logger logger;

    private AbstractCommand command;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        command = spy(new MyConcreteCommand());
        doReturn(logger).when(command).getLogger();
    }

    @Test
    public void testParse_InteractiveMode_WithInput() {
        Map<String, String> args = new HashMap<>();
        args.put("param1", "default1");
        doReturn("newInput").when(scanner).nextLine();

        command.parse(args);

        assertEquals("newInput", args.get("param1"));
        assertTrue(command.isInteractive());
        verifyLogMessages(false, true);
    }

    @Test
    public void testParse_InteractiveMode_WithoutInput() {
        Map<String, String> args = new HashMap<>();
        args.put("param1", "default1");
        doReturn("").when(scanner).nextLine();

        command.parse(args);

        assertEquals("default1", args.get("param1"));
        assertTrue(command.isInteractive());
        verifyLogMessages(true, false);
    }

    @Test
    public void testParse_NonInteractiveMode() {
        Map<String, String> args = new HashMap<>();
        command = spy(new MyNonInteractiveCommand());

        command.parse(args);

        assertEquals("default1", args.get("param1"));
        assertFalse(command.isInteractive());
        verifyLogMessages(false, false);
    }

    private void verifyLogMessages(boolean expectedUserKeptValue, boolean expectedUserChangedValue) {
        verify(logger).debug("User choose to keep previous value [param1]: default1");
        verify(logger).debug("User choose to change value [param1]: default1");
        if (expectedUserKeptValue) {
            verify(logger, times(1)).debug("User choose to keep previous value [param1]: default1");
            verify(logger, times(0)).debug("User choose to change value [param1]: default1");
        } else if (expectedUserChangedValue) {
            verify(logger, times(0)).debug("User choose to keep previous value [param1]: default1");
            verify(logger, times(1)).debug("User choose to change value [param1]: default1");
        }
    }

    private class MyConcreteCommand extends AbstractCommand {
        @Override
        protected Map<String, String> with(Map<String, String> args) {
            return args;
        }

        @Override
        protected String[] getParams() {
            return new String[]{"param1"};
        }

        @Override
        protected boolean isInteractive() {
            return true;
        }
    }

    private class MyNonInteractiveCommand extends AbstractCommand {
        @Override
        protected Map<String, String> with(Map<String, String> args) {
            return args;
        }

        @Override
        protected String[] getParams() {
            return new String[]{"param1"};
        }

        @Override
        protected boolean isInteractive() {
            return false;
        }
    }
}
