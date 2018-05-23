import ballerina/observe;

function testCounterIncrementByOne() returns (int) {
    map<string> tags = { "method": "GET" };
    observe:Counter counter = check observe:getCounterInstance("requests_total", description = "Total requests.",
        tags = tags);
    counter.increment();
    return counter.getValue();
}

function testCounterIncrement() returns (int) {
    map<string> tags = { "method": "POST" };
    observe:Counter counter = check observe:getCounterInstance("requests_total", description = "Total requests.",
        tags = tags);
    counter.increment(amount = 5);
    return counter.getValue();
}

function testCounterError() returns (int) {
    map<string> tags = { "method": "PUT" };
    observe:Counter counter = check observe:getCounterInstance("requests_total", description = "Total requests.",
        tags = tags);
    counter.increment(amount = 5);
    map<string> newTags = { "method": "PUT", "extra_tag": "extra_tag" };
    observe:Counter newCounter = check observe:getCounterInstance("requests_total", description = "Total requests.",
        tags = newTags);
    newCounter.increment(amount = 5);
    return newCounter.getValue();
}

function testCounterWithoutTags() returns (int) {
    observe:Counter counter = check observe:getCounterInstance("new_requests_total");
    counter.increment(amount = 2);
    counter.increment();
    return counter.getValue();
}
