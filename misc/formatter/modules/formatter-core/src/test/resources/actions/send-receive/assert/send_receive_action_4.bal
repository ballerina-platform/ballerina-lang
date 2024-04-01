function foo() {
    worker w1 {
        1 -> function;
    }
    worker w2 {
        2 -> function;
    }
    map<int> x = <- {a: w1, b: w2};
}

function bar() {
    worker w1 {
        1 -> function;
    }
    worker w2 {
        2 -> function;
    }
    map<int> x = <- {w1, w2};
}

function baz() {
    worker w1 {
        1 -> function;
    }
    worker w2 {
        2 -> function;
    }
    map<int> x = <- {
        a: w1,
        b: w2
    };

}

function foz() {
    worker lengthyWorkerName {
        1 -> resultWorker;
    }
    worker tooLengthyWorkerName {
        2 -> resultWorker;
    }
    worker short1 {
        true -> resultWorker;
    }
    worker short2 {
        "s" -> resultWorker;
    }

    worker resultWorker {
        map<any> y = <- {
            short1: short1,
            short2,
            lengthyWorkerName,
            tooLengthyWorkerName
        };
    }
}

function foooz() {
    do {
        _ = <-
        {
            a: w1,
            b: w2
        };
    }
}
