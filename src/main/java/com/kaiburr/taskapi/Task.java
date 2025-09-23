package com.kaiburr.taskapi;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@Document(collection = "tasks")
public class Task {

    @Id
    private String id;

    private String name;
    private String owner;
    private String command;
    private List<TaskExecution> taskExecutions;

}

// We will define the TaskExecution class in the next step.
// For now, the IDE might show an error on "TaskExecution". This is expected.