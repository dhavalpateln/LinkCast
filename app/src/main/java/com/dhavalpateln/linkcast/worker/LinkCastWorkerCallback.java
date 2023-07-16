package com.dhavalpateln.linkcast.worker;

import java.io.IOException;

public interface LinkCastWorkerCallback {
    void onFailure();
    void onComplete();
}
