package com.taixiu.unbalanced.core;

import com.mario.random.service.impl.RandomStrategy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@NoArgsConstructor
@Slf4j
@Builder
@AllArgsConstructor
public class Session {

    private long id;
    private long startTime;
    private long endTime;
    private RandomStrategy randomStrategy;

    public Session(int id) {
        this.id = id;
    }
}
