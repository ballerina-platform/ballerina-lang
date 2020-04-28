import ballerina/runtime;

// Test field locking when a record is passed as a param
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
    runtime:sleep(10);
    incrementParam(param, conf);
}

function incrementParam(Student param, CircuitBreakerInferredConfig conf) {
   lock {
       foreach var i in 1 ... 1000 {
            conf.rollingWindow.requestVolumeThreshold = conf.rollingWindow.requestVolumeThreshold + i;
            conf.resetTimeMillis = i;
           param.score = param.score + i;
       }
    }
}

type Student record {
    int score = 0;
};

public type CircuitBreakerInferredConfig record {|
    float failureThreshold = 0.0;
    int resetTimeMillis = 0;
    boolean[] statusCodes = [];
    int noOfBuckets = 0;
    RollingWindow rollingWindow = {};
|};

public type RollingWindow record {|
    int requestVolumeThreshold = 10;
    int timeWindowMillis = 60000;
    int bucketSizeMillis = 10000;
|};

public type Bucket record {
    int id = 0;
    RollingWindow[] windows = [];
};

//--------------------------------------------------------------------------

// Test when there is a array access during the field traverse
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

    runtime:sleep(10);
    incrementParam2(buck);
}

function incrementParam2(Bucket buck) {
   lock {
       foreach var i in 1 ... 1000 {
            buck.windows[0].requestVolumeThreshold = buck.windows[0].requestVolumeThreshold + i;
       }
    }
}
