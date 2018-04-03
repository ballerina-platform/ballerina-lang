import ballerina/metrics;

map tags1 = {"method":"GET"};
map tags2 = {"method":"POST"};
map tags3 = {"method":"DELETE"};
map tags4 = {"method":"UPDATE"};

metrics:Gauge gauge1 = {name:"inprogress_requests",description:"Inprogress requests.", tags:tags1};
metrics:Gauge gauge2 = {name:"inprogress_requests",description:"Inprogress requests.", tags:tags2};
metrics:Gauge gauge3 = {name:"inprogress_requests",description:"Inprogress requests.", tags:tags3};
metrics:Gauge gauge4 = {name:"inprogress_requests",description:"Inprogress requests.", tags:tags4};
metrics:Gauge gauge5 = {name:"new_inprogress_requests",description:"Inprogress requests."};
metrics:Gauge gauge6 = {name:"active_events", tags:tags1};
metrics:Gauge gauge7 = {};

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

function testGaugeWithoutTags() returns (float) {
    gauge5.setValue(5);
    gauge5.incrementByOne();
    gauge5.increment(3);
    gauge5.decrementByOne();
    gauge5.decrement(2);
    return gauge5.value();
}

function testGaugeWithoutDescription() returns (float) {
    gauge6.setValue(7);
    return gauge6.value();
}

function testGaugeWithEmptyStruct() {
    gauge7.setValue(5);
}
