package com.techmind.backend.service;

import com.techmind.backend.dto.ContenidoResponse;
import com.techmind.backend.ml.ContentAnalysisService;
import com.techmind.backend.model.ContentEntity;
import com.techmind.backend.repository.ContentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContenidoServiceTest {

    @Mock
    private ContentAnalysisService contentAnalysisService;

    @Mock
    private ContentRepository contentRepository;

    @InjectMocks
    private ContenidoService contenidoService;

    @Test
    void procesar_probabilidadBajoUmbral_devuelveIndeterminadoPeroGuardaCategoriaReal() {
        when(contentAnalysisService.analyze(anyString(), anyString()))
                .thenReturn(new ContentAnalysisService.AnalyzedContent(
                        "Receta de cocina",
                        "Receta de cocina",
                        null,
                        "es",
                        false,
                        "Backend",
                        0.2,
                        List.of(),
                        "onnx"
                ));

        ContenidoResponse response = contenidoService.procesar("Receta de cocina", "Arroz con pollo");

        assertThat(response.getCategoria()).isEqualTo("Indeterminado");
        assertThat(response.getProbabilidad()).isEqualTo(0.2);

        ArgumentCaptor<ContentEntity> captor = ArgumentCaptor.forClass(ContentEntity.class);
        verify(contentRepository).save(captor.capture());
        assertThat(captor.getValue().getCategory()).isEqualTo("Backend");
    }

    @Test
    void procesar_probabilidadSobreUmbral_devuelveCategoriaTalCual() {
        when(contentAnalysisService.analyze(anyString(), anyString()))
                .thenReturn(new ContentAnalysisService.AnalyzedContent(
                        "Spring Boot",
                        "Spring Boot",
                        null,
                        "es",
                        false,
                        "Backend",
                        0.89,
                        List.of("Java", "Spring Boot"),
                        "onnx"
                ));

        ContenidoResponse response = contenidoService.procesar("Spring Boot", "API REST con Java");

        assertThat(response.getCategoria()).isEqualTo("Backend");
        assertThat(response.getProbabilidad()).isEqualTo(0.89);
        assertThat(response.getInformacionAdicional()).containsExactly("Java", "Spring Boot");
    }
}
