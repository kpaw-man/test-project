package pl.psnc.pbirecordsuploader.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.psnc.pbirecordsuploader.service.chain.ChainManager;
import pl.psnc.pbirecordsuploader.service.chain.components.*;

@Configuration
public class ChainConfig {

    @Bean
    public ChainManager chainManager(
            FetchDataHandler fetchDataHandler,
            XsltTransformationHandler xsltTransformationHandler,
            ExtractedXmlContentHandler extractedXmlContentHandler,
            ConvertToResearchObjectHandler convertToResearchObjectHandler,
            ValidateResearchObjectHandler validateResearchObjectHandler,
            SendForwardHandler sendForwardHandler,
            UploadAnnotationsHandler uploadAnnotationsHandler,
            SuccessHandler successHandler) {

        return ChainManager.createChain(
                fetchDataHandler,
                xsltTransformationHandler,
                extractedXmlContentHandler,
                convertToResearchObjectHandler,
                validateResearchObjectHandler,
                sendForwardHandler,
                uploadAnnotationsHandler,
                successHandler
        );
    }
}