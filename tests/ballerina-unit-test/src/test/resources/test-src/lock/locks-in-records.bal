type Student record {
    int score;
};

function fieldLock() returns int {
        Student stParam = {};
        CircuitBreakerInferredConfig conf = {};
        workerFuncParam(stParam, conf);
        return stParam.score;
}

function workerFuncParam(Student param, CircuitBreakerInferredConfig conf) {

    worker w1 {
        incrementParam(param, conf);
    }

    worker w2 {
        incrementParam(param, conf);
    }

}

function incrementParam(Student param, CircuitBreakerInferredConfig conf) {
   lock {
       foreach i in 1 ... 1000 {
            conf.rollingWindow.requestVolumeThreshold = conf.rollingWindow.requestVolumeThreshold + i;
           param.score = param.score + i;
       }
    }
}

public type CircuitBreakerInferredConfig record {
    float failureThreshold;
    int resetTimeMillis;
    boolean[] statusCodes;
    int noOfBuckets;
    RollingWindow rollingWindow;
    !...
};

public type RollingWindow record {
    int requestVolumeThreshold = 10;
    int timeWindowMillis = 60000;
    int bucketSizeMillis = 10000;
    !...
};

public type Bucket record {
    int id;
    RollingWindow[] windows;
};

//--------------------------------------------------------------------------

function arrayFieldLock() returns int {
        RollingWindow w = {};
        w.requestVolumeThreshold = 0;
        Bucket bucket = {};
        bucket.windows[0] = w;
        buckWorkerFuncParam(bucket);
        return bucket.windows[0].requestVolumeThreshold;
}

function buckWorkerFuncParam(Bucket buck) {

    worker w1 {
        incrementParam2(buck);
    }

    worker w2 {
        incrementParam2(buck);
    }
}

function incrementParam2(Bucket buck) {
   lock {
       foreach i in 1 ... 1000 {
            buck.windows[0].requestVolumeThreshold = buck.windows[0].requestVolumeThreshold + i;
       }
    }
}
