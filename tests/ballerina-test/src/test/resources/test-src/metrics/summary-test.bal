import ballerina/observe;

map tags1 = {"method":"GET"};
map tags2 = {"method":"POST"};
map tags3 = {"method":"UPDATE"};
map tags4 = {"method":"DELETE"};
observe:Summary summary1 = new("response_size", "Size of a response.", tags1);
observe:Summary summary2 = new("response_size", "Size of a response.", tags2);
observe:Summary summary3 = new("response_size", "Size of a response.", tags3);
observe:Summary summary4 = new("response_size", "Size of a response.", tags4);

function testCountSummary() returns (int) {
    summary1.record(1);
    summary1.record(2);
    summary1.record(3);
    summary1.record(4);
    summary1.record(5);
    summary1.record(6);
    return summary1.count();
}

function testMaxSummary() returns (float) {
    summary2.record(1);
    summary2.record(2);
    summary2.record(3);
    return summary2.max();
}

function testMeanSummary() returns (float) {
    summary3.record(1);
    summary3.record(2);
    summary3.record(3);
    return summary3.mean();
}

function testPercentileSummary() returns (float) {
    summary4.record(1);
    summary4.record(2);
    summary4.record(3);
    summary4.record(4);
    summary4.record(5);
    summary4.record(6);
    return summary4.percentile(0.5);
}