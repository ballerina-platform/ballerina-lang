import ballerina/observe;

function testCountSummary() returns (int) {
    map<string> tags = { "method": "GET" };
    observe:Gauge gauge = new("response_size", desc = "Size of a response.", tags = tags);
    gauge.setValue(1.0);
    gauge.setValue(2.0);
    gauge.setValue(3.0);
    gauge.setValue(4.0);
    gauge.setValue(5.0);
    gauge.setValue(6.0);
    return gauge.getCount();
}

function testSumSummary() returns (float) {
    map<string> tags = { "method": "TRACE" };
    observe:Gauge gauge = new("response_size", desc = "Size of a response.",
        tags = tags);
    int i = 0;
    while (i < 100) {
        gauge.increment();
        i++;
    }
    return gauge.getSum();
}

function testMaxSummary() returns (float) {
    map<string> tags = { "method": "POST" };
    observe:Gauge gauge = new("response_size", desc = "Size of a response.", tags = tags);
    gauge.setValue(1.0);
    gauge.setValue(2.0);
    gauge.setValue(3.0);
    observe:Snapshot[]? summarySnapshot = gauge.getSnapshot();
    match summarySnapshot {
        observe:Snapshot[] stats => {
            return stats[0].max;
        }
        () => {
            return 0.0;
        }
    }
}

function testMeanSummary() returns (float) {
    map<string> tags = { "method": "UPDATE" };
    observe:Gauge gauge = new("response_size", desc = "Size of a response.", tags = tags);
    gauge.setValue(1.0);
    gauge.setValue(2.0);
    gauge.setValue(3.0);
    observe:Snapshot[]? summarySnapshot = gauge.getSnapshot();
    match summarySnapshot {
        observe:Snapshot[] stats => {
            return stats[0].mean;
        }
        () => {
            return 0.0;
        }
    }
}

function testPercentileSummary() returns (observe:PercentileValue[]?) {
    map<string> tags = { "method": "DELETE" };
    observe:Gauge gauge = new("response_size", desc = "Size of a response.", tags = tags);
    gauge.setValue(1.0);
    gauge.setValue(2.0);
    gauge.setValue(3.0);
    gauge.setValue(4.0);
    gauge.setValue(5.0);
    gauge.setValue(6.0);
    observe:Snapshot[]? summarySnapshot = gauge.getSnapshot();
    match summarySnapshot {
        observe:Snapshot[] stats => {
            return stats[0].percentileValues;
        }
        () => {
            return ();
        }
    }
}

function testValueSummary() returns (float) {
    map<string> tags = { "method": "DELETE" };
    observe:Gauge gauge = new("response_size", desc = "Size of a response.", tags = tags);
    gauge.increment();
    gauge.increment(amount = 1000.0);
    gauge.decrement();
    gauge.decrement(amount = 500.0);
    return gauge.getValue();
}

function testSummaryWithoutTags() returns (float) {
    observe:Gauge gauge = new("new_response_size");
    gauge.setValue(1.0);
    gauge.setValue(2.0);
    gauge.setValue(3.0);
    observe:Snapshot[]? summarySnapshot = gauge.getSnapshot();
    match summarySnapshot {
        observe:Snapshot[] stats => {
            return stats[0].mean;
        }
        () => {
            return 0.0;
        }
    }
}

function registerAndIncrement() returns (int) {
    observe:Gauge gauge = new("register_response_size");
    _ = gauge.register();
    gauge.increment();
    return gauge.getCount();
}
