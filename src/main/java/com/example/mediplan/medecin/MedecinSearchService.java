package com.example.mediplan.medecin;

import com.example.mediplan.medecin.dto.MedecinSearchRequest;
import com.example.mediplan.medecin.dto.MedecinSearchResponse;
import com.example.mediplan.user.Medecin;
import com.example.mediplan.user.Role;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class MedecinSearchService {

    private final MongoTemplate mongoTemplate;

    public Page<MedecinSearchResponse> rechercher(@Valid MedecinSearchRequest request, Pageable pageable) {
        Query query = new Query();
        List<Criteria> criteres = new ArrayList<>();
        criteres.add(Criteria.where("role").is(Role.MEDECIN));
        criteres.add(Criteria.where("active").is(true));
        if (StringUtils.hasText(request.getQ())) {
            String q = request.getQ().trim();
            Pattern pattern = Pattern.compile(".*" + Pattern.quote(q) + ".*", Pattern.CASE_INSENSITIVE);
            criteres.add(new Criteria().orOperator(
                    Criteria.where("fullName").regex(pattern),
                    Criteria.where("specialty").regex(pattern),
                    Criteria.where("clinicName").regex(pattern)
            ));
        }
        if (StringUtils.hasText(request.getSpecialite())) {
            criteres.add(Criteria.where("specialty").regex("^" + Pattern.quote(request.getSpecialite()) + "$", "i"));
        }
        if (StringUtils.hasText(request.getVille())) {
            criteres.add(Criteria.where("clinicAddress.city").regex(
                    Pattern.compile(".*" + Pattern.quote(request.getVille().trim()) + ".*", Pattern.CASE_INSENSITIVE)));
        }
        query.addCriteria(new Criteria().andOperator(criteres.toArray(new Criteria[0])));
        long total = mongoTemplate.count(query, Medecin.class);
        query.with(pageable);
        List<Medecin> medecins = mongoTemplate.find(query, Medecin.class);
        List<MedecinSearchResponse> responses = medecins.stream()
                .map(this::toResponse)
                .toList();
        return new PageImpl<>(responses, pageable, total);
    }

    private MedecinSearchResponse toResponse(Medecin medecin) {
        return MedecinSearchResponse.builder()
                .id(medecin.getId())
                .fullName(medecin.getFullName())
                .email(medecin.getEmail())
                .specialty(medecin.getSpecialty())
                .phone(medecin.getPhone())
                .clinicAddress(medecin.getClinicAddress())
                .yearsOfExperience(medecin.getYearsOfExperience())
                .active(medecin.isActive())
                .build();
    }
}
