import ballerina/metrics;

map tags1 = {"method":"GET"};
map tags2 = {"method":"POST"};
map tags3 = {"method":"DELETE"};
map tags4 = {"method":"UPDATE"};

metrics:Gauge gauge1 = {name:"inprogress_requests",description:"Inprogress requests.", tags:tags1};
metrics:Gauge gauge2 = {name:"inprogress_requests",description:"Inprogress requests.", tags:tags2};
metrics:Gauge gauge3 = {name:"inprogress_requests",description:"Inprogress requests.", tags:tags3};
metrics:Gauge gauge4 = {name:"inprogress_requests",description:"Inprogress requests.", tags:tags4};

function testIncrementGaugeByOne() returns (float) {
    gauge1.incrementByOne();
    return gauge1.value();
}

function testIncrementGauge() returns (float) {
    gauge2.increment(5);
    return gauge2.value();
}

function testDecrementGaugeByOne() returns (float) {
    gauge3.setValue(10);
    gauge3.decrementByOne();
    return gauge3.value();
}

function testDecrementGauge() returns (float) {
    gauge4.setValue(10);
    gauge4.decrement(2);
    return gauge4.value();
}
