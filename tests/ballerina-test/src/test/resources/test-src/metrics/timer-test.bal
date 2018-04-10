import ballerina/observe;

map tags1 = {"method":"GET"};
map tags2 = {"method":"POST"};
map tags3 = {"method":"UPDATE"};
map tags4 = {"method":"DELETE"};

observe:Timer timer1 = new("3rdPartyService", "Size of a response.", tags1);
observe:Timer timer2 = new("3rdPartyService", "Size of a response.", tags2);
observe:Timer timer3 = new("3rdPartyService", "Size of a response.", tags3);
observe:Timer timer4 = new("3rdPartyService", "Size of a response.", tags4);
observe:Timer timer5 = new("new_3rdPartyService", "Size of a response.", ());

function testCountTimer() returns (int) {
    timer1.record(1000, observe:TIME_UNIT_NANOSECONDS);
    timer1.record(2000, observe:TIME_UNIT_NANOSECONDS);
    timer1.record(3000, observe:TIME_UNIT_NANOSECONDS);
    timer1.record(4000, observe:TIME_UNIT_NANOSECONDS);
    timer1.record(5000, observe:TIME_UNIT_NANOSECONDS);
    return timer1.count();
}

function testMaxTimer() returns (float) {
    timer2.record(1, observe:TIME_UNIT_SECONDS);
    timer2.record(2, observe:TIME_UNIT_SECONDS);
    timer2.record(3, observe:TIME_UNIT_SECONDS);
    timer2.record(4000, observe:TIME_UNIT_MILLISECONDS);
    timer2.record(5, observe:TIME_UNIT_SECONDS);
    return timer2.max(observe:TIME_UNIT_SECONDS);
}

function testMeanTimer() returns (float) {
    timer3.record(1, observe:TIME_UNIT_MICROSECONDS);
    timer3.record(2, observe:TIME_UNIT_MICROSECONDS);
    timer3.record(3, observe:TIME_UNIT_MICROSECONDS);
    timer3.record(4, observe:TIME_UNIT_MICROSECONDS);
    timer3.record(5, observe:TIME_UNIT_MICROSECONDS);
    return timer3.mean(observe:TIME_UNIT_NANOSECONDS);
}

function testPercentileTimer() returns (float) {
    timer4.record(1000, observe:TIME_UNIT_NANOSECONDS);
    timer4.record(2000, observe:TIME_UNIT_NANOSECONDS);
    timer4.record(3000, observe:TIME_UNIT_NANOSECONDS);
    timer4.record(4000, observe:TIME_UNIT_NANOSECONDS);
    timer4.record(5000, observe:TIME_UNIT_NANOSECONDS);
    return timer4.percentile(0.5, observe:TIME_UNIT_NANOSECONDS);
}

function testTimerWithoutTags() returns (float) {
     timer5.record(1000, observe:TIME_UNIT_NANOSECONDS);
     timer5.record(2000, observe:TIME_UNIT_NANOSECONDS);
     timer5.record(3000, observe:TIME_UNIT_NANOSECONDS);
     timer5.record(4000, observe:TIME_UNIT_NANOSECONDS);
     return timer5.mean(observe:TIME_UNIT_NANOSECONDS);
}
