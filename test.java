import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.MapEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheEntryListenerTest {

    private CacheEntryListener listener;
    private EntryEvent<String, Object> mockEvent;
    private MapEvent mockMapEvent;

    @BeforeEach
    public void setup() {
        listener = new CacheEntryListener();
        mockEvent = mock(EntryEvent.class);
        mockMapEvent = mock(MapEvent.class);

        when(mockEvent.getKey()).thenReturn("sampleKey");
        when(mockEvent.getValue()).thenReturn("sampleValue");
        when(mockMapEvent.getName()).thenReturn("sampleMap");
    }

    @Test
    public void testEntryAdded() {
        listener.entryAdded(mockEvent);
    }

    @Test
    public void testEntryRemoved() {
        listener.entryRemoved(mockEvent);
    }

    @Test
    public void testEntryEvicted() {
        listener.entryEvicted(mockEvent);
    }

    @Test
    public void testEntryExpired() {
        listener.entryExpired(mockEvent);
    }

    @Test
    public void testMapEvicted() {
        listener.mapEvicted(mockMapEvent);
    }
}