import ballerina/metrics;

map tagsMap = {"method":"GET"};
metrics:Gauge gauge = {name:"inprogress_requests",description:"Inprogress requests.", tags:tagsMap};

function testRegisterGauge() {
    gauge.register();
}

function testIncrementGaugeByOne() {
    gauge.incrementByOne();
}

function testIncrementGauge() {
    gauge.increment(5);
}

function testDecrementGaugeByOne() {
    gauge.decrementByOne();
}

function testDecrementGauge() {
    gauge.decrement(2);
}

function testSetGauge() {
    gauge.setValue(12);
}

function testGetGauge() returns (float){
    return gauge.value();
}
