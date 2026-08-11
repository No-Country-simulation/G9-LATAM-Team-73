package com.techmind.backend.translation;

import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.ailanguage.AIServiceLanguageClient;
import com.oracle.bmc.ailanguage.model.BatchLanguageTranslationDetails;
import com.oracle.bmc.ailanguage.model.TextDocument;
import com.oracle.bmc.ailanguage.requests.BatchLanguageTranslationRequest;
import com.oracle.bmc.ailanguage.responses.BatchLanguageTranslationResponse;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.techmind.backend.config.OciLanguageProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * Traducción EN → ES usando OCI Language (AI Service).
 * Activo solo cuando {@code techmind.oci.language.enabled=true}.
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "techmind.oci.language", name = "enabled", havingValue = "true")
public class OciLanguageTranslationService implements TranslationService {

    private final OciLanguageProperties properties;
    private final LanguageDetector languageDetector;
    private AIServiceLanguageClient client;

    public OciLanguageTranslationService(OciLanguageProperties properties, LanguageDetector languageDetector) {
        this.properties = properties;
        this.languageDetector = languageDetector;
    }

    @PostConstruct
    void init() throws IOException {
        if (properties.getCompartmentId() == null || properties.getCompartmentId().isBlank()) {
            throw new IllegalStateException(
                    "techmind.oci.language.compartment-id es obligatorio cuando OCI Language está habilitado."
            );
        }
        ConfigFileReader.ConfigFile configFile = ConfigFileReader.parse(
                properties.getConfigFile(),
                properties.getProfile()
        );
        ConfigFileAuthenticationDetailsProvider provider =
                new ConfigFileAuthenticationDetailsProvider(configFile);
        this.client = AIServiceLanguageClient.builder().build(provider);
        log.info("OCI Language translation client initialized (profile={})", properties.getProfile());
    }

    @PreDestroy
    void destroy() {
        if (client != null) {
            client.close();
        }
    }

    @Override
    public TranslationResult translateIfNeeded(String text) {
        String sourceLanguage = languageDetector.detect(text);
        if (!"en".equals(sourceLanguage)) {
            return new TranslationResult(text, text, sourceLanguage.equals("und") ? "es" : sourceLanguage, false);
        }

        try {
            TextDocument document = TextDocument.builder()
                    .key("content-1")
                    .text(text)
                    .languageCode("en")
                    .build();

            BatchLanguageTranslationDetails details = BatchLanguageTranslationDetails.builder()
                    .compartmentId(properties.getCompartmentId())
                    .targetLanguageCode("es")
                    .documents(List.of(document))
                    .build();

            BatchLanguageTranslationRequest request = BatchLanguageTranslationRequest.builder()
                    .batchLanguageTranslationDetails(details)
                    .build();

            BatchLanguageTranslationResponse response = client.batchLanguageTranslation(request);
            String translated = response.getBatchLanguageTranslationResult()
                    .getDocuments()
                    .stream()
                    .findFirst()
                    .map(doc -> doc.getTranslatedText())
                    .orElse(text);

            return new TranslationResult(text, translated, "en", true);
        } catch (Exception ex) {
            log.error("OCI Language translation failed, returning original English text", ex);
            throw new IllegalStateException("Fallo al traducir con OCI Language: " + ex.getMessage(), ex);
        }
    }
}
