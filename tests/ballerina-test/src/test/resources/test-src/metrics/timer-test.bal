import ballerina/metrics;

map tagsMap = {"method":"GET"};
metrics:Timer timer = {name:"3rdPartyService", description:"Size of a response.", tags:tagsMap};

function testRegister() {
    timer.register();
}

function testMax() returns (float) {
    return timer.max(metrics:TimeUnit.NANOSECONDS);
}

function testMean() returns (float) {
    return timer.mean(metrics:TimeUnit.NANOSECONDS);
}

function testPercentile() returns (float) {
    return timer.percentile(0.5, metrics:TimeUnit.NANOSECONDS);
}

function testRecord() {
    timer.record(1000, metrics:TimeUnit.NANOSECONDS);
    timer.record(2000, metrics:TimeUnit.NANOSECONDS);
    timer.record(3000, metrics:TimeUnit.NANOSECONDS);
    timer.record(4000, metrics:TimeUnit.NANOSECONDS);
    timer.record(5000, metrics:TimeUnit.NANOSECONDS);

}

function testCount() returns (int){
    return timer.count();
}