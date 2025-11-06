package com.example.mediplan.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<User> search(AdminUserFilter filter, Pageable pageable) {
        Query query = buildQuery(filter);
        long total = mongoTemplate.count(query, User.class);
        query.with(pageable);
        List<User> users = mongoTemplate.find(query, User.class);
        return new PageImpl<>(users, pageable, total);
    }

    @Override
    public List<User> findAll(AdminUserFilter filter, Sort sort) {
        Query query = buildQuery(filter);
        if (sort != null) {
            query.with(sort);
        }
        return mongoTemplate.find(query, User.class);
    }

    private Query buildQuery(AdminUserFilter filter) {
        Query query = new Query();
        if (filter == null) {
            return query;
        }
        List<Criteria> criteriaList = new ArrayList<>();
        if (StringUtils.hasText(filter.getQuery())) {
            String q = filter.getQuery().trim();
            Pattern pattern = Pattern.compile(".*" + Pattern.quote(q) + ".*", Pattern.CASE_INSENSITIVE);
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("fullName").regex(pattern),
                    Criteria.where("email").regex(pattern)
            ));
        }
        if (filter.getRole() != null) {
            criteriaList.add(Criteria.where("role").is(filter.getRole()));
        }
        if (filter.getActive() != null) {
            criteriaList.add(Criteria.where("active").is(filter.getActive()));
        }
        if (StringUtils.hasText(filter.getProvider())) {
            criteriaList.add(Criteria.where("provider").is(filter.getProvider().trim().toUpperCase()));
        }
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }
        return query;
    }
}
