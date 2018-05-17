import ballerina/observe;

map<string> tags1 = { "method": "GET" };
map<string> tags2 = { "method": "POST" };

observe:Counter counter1 = check observe:getCounterInstance("requests_total", description = "Total requests.", tags = tags1);
observe:Counter counter2 = check observe:getCounterInstance("requests_total", description = "Total requests.", tags = tags2);
observe:Counter counter3 = check observe:getCounterInstance("new_requests_total");

function testCounterIncrementByOne() returns (int) {
    counter1.increment();
    return counter1.value();
}

function testCounterIncrement() returns (int) {
    counter2.increment(amount = 5);
    return counter2.value();
}

function testCounterWithoutTags() returns (int) {
    counter3.increment(amount = 2);
    counter3.increment();
    return counter3.value();
}
