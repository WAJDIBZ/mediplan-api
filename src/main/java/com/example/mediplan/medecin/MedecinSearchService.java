package com.example.mediplan.medecin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.example.mediplan.user.Medecin;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class MedecinSearchService {

    private final MongoTemplate mongoTemplate;

    public List<Medecin> search(MedecinSearchRequest request) {
        Query query = buildQuery(request);
        return mongoTemplate.find(query, Medecin.class);
    }

    private Query buildQuery(MedecinSearchRequest request) {
        Query query = new Query();
        if (request == null) {
            return query;
        }

        List<Criteria> criteres = new ArrayList<>();

        // Apply the fix: use Pattern.compile() instead of passing int directly
        if (StringUtils.hasText(request.getSpecialite())) {
            criteres.add(
                Criteria.where("specialty").regex(
                    Pattern.compile("^" + Pattern.quote(request.getSpecialite()) + "$", Pattern.CASE_INSENSITIVE)
                )
            );
        }

        if (!criteres.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteres.toArray(new Criteria[0])));
        }

        return query;
    }
}
