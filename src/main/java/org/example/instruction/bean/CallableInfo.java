package org.example.instruction.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.concurrent.Callable;

@Getter
@AllArgsConstructor
public class CallableInfo implements Serializable {

    private Callable<ExecutionResult> callable;
    private long delaySec;

}
