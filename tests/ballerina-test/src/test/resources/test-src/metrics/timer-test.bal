import ballerina/metrics;

map tagsMap = {"method":"GET"};
metrics:Timer timer = {name:"3rdPartyService", description:"Size of a response.", tags:tagsMap};

function testTimer() returns (int){
    timer.register();
    timer.record(1000, metrics:TimeUnit.NANOSECONDS);
    timer.record(2000, metrics:TimeUnit.NANOSECONDS);
    timer.record(3000, metrics:TimeUnit.NANOSECONDS);
    timer.record(4000, metrics:TimeUnit.NANOSECONDS);
    timer.record(5000, metrics:TimeUnit.NANOSECONDS);
    return timer.count();
}