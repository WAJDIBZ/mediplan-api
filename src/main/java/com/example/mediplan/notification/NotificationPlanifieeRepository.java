package com.example.mediplan.notification;

import java.time.Instant;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationPlanifieeRepository extends MongoRepository<NotificationPlanifiee, String> {

    List<NotificationPlanifiee> findByDateEnvoiBeforeAndStatut(Instant date, StatutNotification statut);
}
