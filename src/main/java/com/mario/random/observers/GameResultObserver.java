package com.mario.random.observers;

import com.mario.random.entity.GeneratedResultMessageResponse;

public interface GameResultObserver {

    void onResultGenerated(GeneratedResultMessageResponse response);

    void onError(Throwable error);
}
