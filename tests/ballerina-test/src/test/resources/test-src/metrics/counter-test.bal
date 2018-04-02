import ballerina/metrics;

map tags1 = {"method":"GET"};
map tags2 = {"method":"POST"};

metrics:Counter counter1 = {name:"requests_total", description:"Total requests.", tags:tags1};
metrics:Counter counter2 = {name:"requests_total", description:"Total requests.", tags:tags2};
metrics:Counter counter3 = {name:"new_requests_total", description:"Total requests."};
metrics:Counter counter4 = {name:"requests_failed_total", tags:tags1};
metrics:Counter counter5 = {};

function testCounterIncrementByOne() returns (float) {
    counter1.incrementByOne();
    return counter1.count();
}

function testCounterIncrement() returns (float) {
    counter2.increment(5);
    return counter2.count();
}

function testCounterWithoutTags() returns (float) {
    counter3.increment(2);
    counter3.incrementByOne();
    return counter3.count();
}

function testCounterWithoutDescription() returns (float) {
    counter4.increment(4);
    return counter4.count();
}

function testCounterWithEmptyStruct() {
    counter5.increment(5);
}
