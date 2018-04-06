import ballerina/observe;

map tags1 = {"method":"GET"};
map tags2 = {"method":"POST"};
observe:Counter counter1 = new("requests_total", "Total requests.", tags1);
observe:Counter counter2 = new("requests_total", "Total requests.", tags2);

function testCounterIncrementByOne() returns (float) {
    counter1.incrementByOne();
    return counter1.count();
}

function testCounterIncrement() returns (float) {
    counter2.increment(5);
    return counter2.count();
}
