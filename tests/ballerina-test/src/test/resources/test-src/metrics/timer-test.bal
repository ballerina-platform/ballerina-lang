import ballerina/metrics;

map tags1 = {"method":"GET"};
map tags2 = {"method":"POST"};
map tags3 = {"method":"UPDATE"};
map tags4 = {"method":"DELETE"};

metrics:Timer timer1 = {name:"3rdPartyService", description:"Size of a response.", tags:tags1};
metrics:Timer timer2 = {name:"3rdPartyService", description:"Size of a response.", tags:tags2};
metrics:Timer timer3 = {name:"3rdPartyService", description:"Size of a response.", tags:tags3};
metrics:Timer timer4 = {name:"3rdPartyService", description:"Size of a response.", tags:tags4};
metrics:Timer timer5 = {name:"3rdPartyService", description:"Size of a response."};
metrics:Timer timer6 = {name:"new_3rdPartyService", tags:tags1};
metrics:Timer timer7 = {};

function testCountTimer() returns (int) {
    timer1.record(1000, metrics:TimeUnit.NANOSECONDS);
    timer1.record(2000, metrics:TimeUnit.NANOSECONDS);
    timer1.record(3000, metrics:TimeUnit.NANOSECONDS);
    timer1.record(4000, metrics:TimeUnit.NANOSECONDS);
    timer1.record(5000, metrics:TimeUnit.NANOSECONDS);
    return timer1.count();
}

function testMaxTimer() returns (float) {
    timer2.record(1, metrics:TimeUnit.SECONDS);
    timer2.record(2, metrics:TimeUnit.SECONDS);
    timer2.record(3, metrics:TimeUnit.SECONDS);
    timer2.record(4000, metrics:TimeUnit.MILLISECONDS);
    timer2.record(5, metrics:TimeUnit.SECONDS);
    return timer2.max(metrics:TimeUnit.SECONDS);
}

function testMeanTimer() returns (float) {
    timer3.record(1, metrics:TimeUnit.MICROSECONDS);
    timer3.record(2, metrics:TimeUnit.MICROSECONDS);
    timer3.record(3, metrics:TimeUnit.MICROSECONDS);
    timer3.record(4, metrics:TimeUnit.MICROSECONDS);
    timer3.record(5, metrics:TimeUnit.MICROSECONDS);
    return timer3.mean(metrics:TimeUnit.NANOSECONDS);
}

function testPercentileTimer() returns (float) {
    timer4.record(1000, metrics:TimeUnit.NANOSECONDS);
    timer4.record(2000, metrics:TimeUnit.NANOSECONDS);
    timer4.record(3000, metrics:TimeUnit.NANOSECONDS);
    timer4.record(4000, metrics:TimeUnit.NANOSECONDS);
    timer4.record(5000, metrics:TimeUnit.NANOSECONDS);
    return timer4.percentile(0.5, metrics:TimeUnit.NANOSECONDS);
}

function testTimerWithoutTags() returns (float) {
    timer5.record(1000, metrics:TimeUnit.NANOSECONDS);
    timer5.record(2000, metrics:TimeUnit.NANOSECONDS);
    timer5.record(3000, metrics:TimeUnit.NANOSECONDS);
    timer5.record(4000, metrics:TimeUnit.NANOSECONDS);
    return timer5.mean(metrics:TimeUnit.NANOSECONDS);
}

function testTimerWithoutDescription() returns (float) {
    timer6.record(100, metrics:TimeUnit.NANOSECONDS);
    timer6.record(200, metrics:TimeUnit.NANOSECONDS);
    timer6.record(300, metrics:TimeUnit.NANOSECONDS);
    return timer6.mean(metrics:TimeUnit.NANOSECONDS);
}

function testTimerWithEmptyStruct() {
    timer7.record(5, metrics:TimeUnit.SECONDS);
}
