package com.kaiburr.taskapi;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class TaskExecution {

    private Date startTime;
    private Date endTime;
    private String output;

}