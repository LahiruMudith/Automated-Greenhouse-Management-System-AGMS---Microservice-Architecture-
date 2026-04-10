package com.greenhouse.automation.repository;

import com.greenhouse.automation.model.AutomationLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AutomationLogRepository extends MongoRepository<AutomationLog, String> {
}
