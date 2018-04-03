import ballerina/metrics;

map tags1 = {"method":"GET"};
map tags2 = {"method":"POST"};
map tags3 = {"method":"UPDATE"};
map tags4 = {"method":"DELETE"};

metrics:Summary summary1 = {name:"response_size", description:"Size of a response.", tags:tags1};
metrics:Summary summary2 = {name:"response_size", description:"Size of a response.", tags:tags2};
metrics:Summary summary3 = {name:"response_size", description:"Size of a response.", tags:tags3};
metrics:Summary summary4 = {name:"response_size", description:"Size of a response.", tags:tags4};
metrics:Summary summary5 = {name:"new_response_size", description:"Size of a response."};
metrics:Summary summary6 = {name:"successful_response_size", tags:tags1};
metrics:Summary summary7 = {};

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

function testSummaryWithoutTags() returns (float) {
    summary5.record(1);
    summary5.record(3);
    summary5.record(5);
    return summary5.mean();
}

function testSummaryWithoutDescription() returns (float) {
    summary6.record(1);
    summary6.record(2);
    summary6.record(3);
    summary6.record(4);
    return summary6.mean();
}

function testSummaryWithEmptyStruct() {
    summary7.record(5);
}
