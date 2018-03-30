import ballerina/metrics;

map tagsMap = {"method":"GET"};
metrics:Summary summary = {name:"response_size",description:"Size of a response.", tags:tagsMap};

function testSummary() returns (int){
    summary.register();
    summary.record(1);
    summary.record(2);
    summary.record(3);
    summary.record(4);
    summary.record(5);
    summary.record(6);
    return summary.count();
}