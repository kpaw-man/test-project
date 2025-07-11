package pl.psnc.pbirecordsuploader.service.chain.components.rocrate.properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.psnc.pbirecordsuploader.model.metadata.DCTerms;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PropertyHandlerRegistryTest {

    @Mock
    private PropertyHandler handler1;
    @Mock
    private PropertyHandler handler2;

    private PropertyHandlerRegistry registry;

    @BeforeEach
    void setUp() {
        when(handler1.getSupportedTerms()).thenReturn(Set.of(DCTerms.TITLE));
        when(handler2.getSupportedTerms()).thenReturn(Set.of(DCTerms.CREATOR));

        registry = new PropertyHandlerRegistry(List.of(handler1, handler2));
    }

    @Test
    void shouldReturnCorrectHandlerForDCTerm() {
        Optional<PropertyHandler> handlerOpt = registry.getHandler(DCTerms.TITLE);
        assertTrue(handlerOpt.isPresent());
        assertEquals(handler1, handlerOpt.get());
    }

    @Test
    void shouldReturnEmptyWhenNoHandlerForDCTerm() {
        Optional<PropertyHandler> handlerOpt = registry.getHandler(DCTerms.FORMAT);
        assertTrue(handlerOpt.isEmpty());
    }

    @Test
    void shouldNotThrowWhenHandlerListIsEmpty() {
        PropertyHandlerRegistry emptyRegistry = new PropertyHandlerRegistry(List.of());
        assertTrue(emptyRegistry.getHandler(DCTerms.TITLE).isEmpty());
    }
}