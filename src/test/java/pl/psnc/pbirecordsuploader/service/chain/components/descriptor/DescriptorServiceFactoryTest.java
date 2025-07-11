package pl.psnc.pbirecordsuploader.service.chain.components.descriptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.psnc.pbirecordsuploader.service.chain.components.descriptor.bn.BNDescriptorService;
import pl.psnc.pbirecordsuploader.service.chain.components.descriptor.semantic.SemanticDescriptorService;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class DescriptorServiceFactoryTest {

    @Mock
    private BNDescriptorService bnService;

    @Mock
    private SemanticDescriptorService semanticService;

    private DescriptorServiceFactory factory;

    @BeforeEach
    void setUp() {
        factory = new DescriptorServiceFactory(bnService, semanticService);
    }

    @Test
    void shouldReturnBNDescriptorService() {
        DescriptorService result = factory.getDescriptorService(DescriptorServiceFactory.DescriptorSourceType.BN);
        assertThat(result).isSameAs(bnService);
    }

    @Test
    void shouldReturnSemanticDescriptorService() {
        DescriptorService result = factory.getDescriptorService(DescriptorServiceFactory.DescriptorSourceType.SEMANTIC);
        assertThat(result).isSameAs(semanticService);
    }
}