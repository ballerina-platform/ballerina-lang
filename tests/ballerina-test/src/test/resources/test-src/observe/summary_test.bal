import ballerina/observe;

function testCountSummary() returns (int) {
    map<string> tags = { "method": "GET" };
    observe:Summary summary = check observe:getSummaryInstance("response_size", description = "Size of a response.",
        tags = tags);
    summary.record(1);
    summary.record(2);
    summary.record(3);
    summary.record(4);
    summary.record(5);
    summary.record(6);
    return summary.getCount();
}

function testSumSummary() returns (int) {
    map<string> tags = { "method": "TRACE" };
    observe:Summary summary = check observe:getSummaryInstance("response_size", description = "Size of a response.",
        tags = tags);
    foreach i in [1..100] {
        summary.record(i);
    }
    return summary.getSum();
}

function testSummaryError() returns (int) {
    map<string> tags = { "method": "HEAD" };
    observe:Summary summary = check observe:getSummaryInstance("response_size", description = "Size of a response.",
        tags = tags);
    summary.record(1);
    map<string> newTags = { "method": "HEAD", "extra_tag": "extra_tag" };
    observe:Summary newSummary = check observe:getSummaryInstance("response_size", description = "Size of a response.",
        tags = newTags);
    newSummary.record(1);
    return newSummary.getCount();
}

function testMaxSummary() returns (int) {
    map<string> tags = { "method": "POST" };
    observe:Summary summary = check observe:getSummaryInstance("response_size", description = "Size of a response.",
        tags = tags);
    summary.record(1);
    summary.record(2);
    summary.record(3);
    observe:Snapshot summarySnapshot = summary.getSnapshot();
    return summarySnapshot.max;
}

function testMeanSummary() returns (float) {
    map<string> tags = { "method": "UPDATE" };
    observe:Summary summary = check observe:getSummaryInstance("response_size", description = "Size of a response.",
        tags = tags);
    summary.record(1);
    summary.record(2);
    summary.record(3);
    observe:Snapshot summarySnapshot = summary.getSnapshot();
    return summarySnapshot.mean;
}

function testPercentileSummary() returns (observe:PercentileValue[]) {
    map<string> tags = { "method": "DELETE" };
    observe:Summary summary = check observe:getSummaryInstance("response_size", description = "Size of a response.",
        tags = tags);
    summary.record(1);
    summary.record(2);
    summary.record(3);
    summary.record(4);
    summary.record(5);
    summary.record(6);
    observe:Snapshot summarySnapshot = summary.getSnapshot();
    return summarySnapshot.percentileValues;
}

function testValueSummary() returns (int) {
    map<string> tags = { "method": "PUT" };
    observe:Summary summary = check observe:getSummaryInstance("response_size", description = "Size of a response.",
        tags = tags);
    summary.increment();
    summary.increment(amount = 1000);
    summary.decrement();
    summary.decrement(amount = 500);
    observe:Snapshot summarySnapshot = summary.getSnapshot();
    return summarySnapshot.value;
}

function testSummaryWithoutTags() returns (float) {
    observe:Summary summary = check observe:getSummaryInstance("new_response_size");
    summary.record(1);
    summary.record(3);
    summary.record(5);
    observe:Snapshot summarySnapshot = summary.getSnapshot();
    return summarySnapshot.mean;
}
