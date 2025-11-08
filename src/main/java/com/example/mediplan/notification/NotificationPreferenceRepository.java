package com.example.mediplan.notification;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationPreferenceRepository extends MongoRepository<NotificationPreference, String> {

    Optional<NotificationPreference> findByUserId(String userId);
}
