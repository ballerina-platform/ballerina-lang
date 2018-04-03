import ballerina/metrics;

map tags1 = {"method":"GET"};
map tags2 = {"method":"POST"};
metrics:Counter counter1 = {name:"requests_total", description:"Total requests.", tags:tags1};
metrics:Counter counter2 = {name:"requests_total", description:"Total requests.", tags:tags2};

function testCounterIncrementByOne() returns (float) {
    counter1.incrementByOne();
    return counter1.count();
}

function testCounterIncrement() returns (float) {
    counter2.increment(5);
    return counter2.count();
}
