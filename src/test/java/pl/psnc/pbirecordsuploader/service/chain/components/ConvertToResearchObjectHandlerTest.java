//package pl.psnc.pbirecordsuploader.service.chain.components;
//
//import edu.kit.datamanager.ro_crate.RoCrate;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.junit.jupiter.MockitoExtension;
//import pl.psnc.pbirecordsuploader.domain.ChainJobEntity;
//import pl.psnc.pbirecordsuploader.exceptions.ConvertException;
//import pl.psnc.pbirecordsuploader.exceptions.PbiUploaderException;
//import pl.psnc.pbirecordsuploader.service.chain.components.rocrate.RocrateService;
//
//import static org.junit.Assert.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class ConvertToResearchObjectHandlerTest {
//    @InjectMocks
//    private ConvertToResearchObjectHandler handler;
//
//    @Mock
//    private RocrateService rocrateService;
//
//    @Mock
//    private ChainJobEntity chainJobEntity;
//
//    private final byte[] dummyBytes = "fake-zip".getBytes();
//    private final RoCrate dummyRoCrate = new RoCrate();
//
//
//    @Test
//    void testProcess_successfulConversion() throws Exception {
//        String daceBody = "validDaceBody";
//
//        when(chainJobEntity.getDaceBody()).thenReturn(daceBody);
////        when(rocrateService.createFromDace(daceBody)).thenReturn(dummyRoCrate);
//        when(rocrateService.validateCrate(dummyRoCrate)).thenReturn(true);
//        when(rocrateService.serializeToZip(dummyRoCrate)).thenReturn(dummyBytes);
//
//        ConvertToResearchObjectHandler spyHandler = Mockito.spy(handler);
//
//        boolean result = spyHandler.process(chainJobEntity);
//        Assertions.assertTrue(result);
//        verify(chainJobEntity).setRoCrate(dummyBytes);
//    }
//
//    @Test
//    void testProcess_daceBodyIsNull_shouldThrowException() {
//        when(chainJobEntity.getDaceBody()).thenReturn(null);
//
//        PbiUploaderException exception = assertThrows(PbiUploaderException.class, () -> handler.process(chainJobEntity));
//
//        Assertions.assertTrue(exception.getMessage().contains("Upload terminated"));
//    }
//
//    @Test
//    void testProcess_daceBodyIsBlank_shouldThrowException() {
//        when(chainJobEntity.getDaceBody()).thenReturn(" ");
//
//        PbiUploaderException exception = assertThrows(PbiUploaderException.class, () -> handler.process(chainJobEntity));
//
//        Assertions.assertTrue(exception.getMessage().contains("Upload terminated"));
//    }
//
//    @Test
//    void testProcess_rocrateServiceThrowsException_shouldThrowWrappedException() throws ConvertException {
//        when(chainJobEntity.getDaceBody()).thenReturn("valid");
////        when(rocrateService.createFromDace(anyString())).thenThrow(new RuntimeException("Service failed"));
//
//        PbiUploaderException exception = assertThrows(PbiUploaderException.class, () -> handler.process(chainJobEntity));
//
//        Assertions.assertTrue(exception.getMessage().contains("Upload terminated"));
//        Assertions.assertNotNull(exception.getCause());
//        Assertions.assertEquals("Service failed", exception.getCause().getMessage());
//    }
//}