import ballerina/observe;

map tags1 = {"method":"GET"};
map tags2 = {"method":"POST"};
map tags3 = {"method":"DELETE"};
map tags4 = {"method":"UPDATE"};

observe:Gauge gauge1 = new("inprogress_requests", "Inprogress requests.", tags1);
observe:Gauge gauge2 = new("inprogress_requests", "Inprogress requests.", tags2);
observe:Gauge gauge3 = new("inprogress_requests", "Inprogress requests.", tags3);
observe:Gauge gauge4 = new("inprogress_requests", "Inprogress requests.", tags4);
observe:Gauge gauge5 = new("new_inprogress_requests","Inprogress requests.", ());

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
