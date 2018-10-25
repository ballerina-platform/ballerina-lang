import ballerina/observe;


function testCounterIncrementByOne() returns (int) {
    map<string> tags = { "method": "GET" };
    observe:Counter counter = new("RequestCounter", tags = tags);
    counter.increment();
    return counter.getValue();
}

function testCounterIncrement() returns (int) {
    observe:Counter counter = new("RequestCounter");
    counter.increment(amount = 5);
    return counter.getValue();
}

function testCounterError() returns (float) {
    map<string> tags = { "method": "PUT" };
    observe:Counter counter1 = new("requests_total", desc = "Total requests.", tags = tags);
    _ = counter1.register();
    counter1.increment(amount = 5);
    observe:Gauge gauge = new("requests_total", desc = "Total requests.", tags = tags);
    error? err = gauge.register();
    match err {
        error e => panic e;
        () => {
            gauge.increment(amount = 5.0);
            return gauge.getValue();
        }
    }
}

function testCounterWithoutTags() returns (int) {
    observe:Counter counter1 = new("new_requests_total");
    counter1.increment(amount = 2);
    counter1.increment();
    return counter1.getValue();
}

function testReset() returns (boolean) {
    map<string> tags = { "method": "PUT" };
    observe:Counter counter1 = new("requests_total", desc = "Total requests.", tags = tags);
    _ = counter1.register();
    int value = counter1.getValue();
    counter1.reset();
    int newValue = counter1.getValue();
    return (value != newValue && newValue == 0);
}
