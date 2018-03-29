import ballerina/metrics;

map tagsMap = {"method":"GET"};
metrics:Summary summary = {name:"response_size",description:"Size of a response.", tags:tagsMap};

function testRegister() {
    summary.register();
}

function testMax() returns (float) {
    return summary.max();
}

function testMean() returns (float) {
    return summary.mean();
}

function testPercentile() returns (float) {
    return summary.percentile(0.5);
}

function testRecord() {
    summary.record(1);
    summary.record(2);
    summary.record(3);
    summary.record(4);
    summary.record(5);
    summary.record(6);
}

function testCount() returns (int){
    return summary.count();
}