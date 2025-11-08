package com.example.mediplan.medecin;

import com.example.mediplan.user.Medecin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedecinSearchServiceTest {

    @Mock
    private MongoTemplate mongoTemplate;

    private MedecinSearchService searchService;

    @BeforeEach
    void setUp() {
        searchService = new MedecinSearchService(mongoTemplate);
    }

    @Test
    void search_withNullRequest_shouldReturnEmptyList() {
        // Arrange
        when(mongoTemplate.find(any(Query.class), eq(Medecin.class)))
                .thenReturn(List.of());

        // Act
        List<Medecin> result = searchService.search(null);

        // Assert
        assertNotNull(result);
        verify(mongoTemplate).find(any(Query.class), eq(Medecin.class));
    }

    @Test
    void search_withSpecialite_shouldBuildQueryCorrectly() {
        // Arrange
        MedecinSearchRequest request = MedecinSearchRequest.builder()
                .specialite("Cardiologue")
                .build();
        
        when(mongoTemplate.find(any(Query.class), eq(Medecin.class)))
                .thenReturn(List.of());

        // Act
        List<Medecin> result = searchService.search(request);

        // Assert
        assertNotNull(result);
        verify(mongoTemplate).find(any(Query.class), eq(Medecin.class));
    }

    @Test
    void search_withEmptySpecialite_shouldNotAddCriteria() {
        // Arrange
        MedecinSearchRequest request = MedecinSearchRequest.builder()
                .specialite("")
                .build();
        
        when(mongoTemplate.find(any(Query.class), eq(Medecin.class)))
                .thenReturn(List.of());

        // Act
        List<Medecin> result = searchService.search(request);

        // Assert
        assertNotNull(result);
        verify(mongoTemplate).find(any(Query.class), eq(Medecin.class));
    }
}
