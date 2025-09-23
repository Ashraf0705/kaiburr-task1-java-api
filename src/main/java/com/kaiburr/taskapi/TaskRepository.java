package com.kaiburr.taskapi;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface TaskRepository extends MongoRepository<Task, String> {

    List<Task> findByNameContaining(String name);

}