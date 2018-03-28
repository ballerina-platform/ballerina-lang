import ballerina/metrics;

map tagsMap = {"method":"GET"};
metrics:Counter counter = {name:"requests_total",description:"Total requests.", tags:tagsMap};

function testRegisterCounter() {
    counter.register();
}

function testCounterIncrementByOne() {
    counter.incrementByOne();
}

function testCounterIncrement() {
    counter.increment(5.0f);
}

function testCountCounter() returns (float){
    return counter.count();
}
