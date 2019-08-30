import ballerina/observe;


function getAllMetricsSize() returns (int) {
    observe:Metric?[] metrics = observe:getAllMetrics();
    return (metrics.length());
}

function registerAngGetMetrics() returns (int) {
    map<string> tags = { "method": "PUT" };
    observe:Counter counter1 = new("service_requests_total", "Total requests.", tags);
    checkpanic counter1.register();
    counter1.increment(5);
    return getAllMetricsSize();
}

function lookupRegisteredMetrics() returns (boolean) {
    string name = "service_requests_total";
    map<string> tags = { "method": "PUT" };
    observe:Counter|observe:Gauge|() metric = observe:lookupMetric(name, tags);

    if metric is observe:Counter {
        int value = metric.getValue();
        return (value == 5);
    }

    return false;
}

function lookupRegisteredNameOnlyMetrics() returns (boolean) {
    string name = "service_requests_total";
    observe:Counter|observe:Gauge|() metric = observe:lookupMetric(name);
    if metric is observe:Counter {
        return true;
    }

    if metric is observe:Gauge {
        return true;
    }

    return false;
}

function lookupRegisterAndIncrement() returns (boolean) {
    string name = "service_requests_total";
    map<string> tags = { "method": "PUT" };
    observe:Counter|observe:Gauge|() metric = observe:lookupMetric(name, tags);
    if metric is observe:Counter {
        metric.increment(10);
        observe:Counter|observe:Gauge|() nextLookupMetric = observe:lookupMetric(name, tags);
        if nextLookupMetric is observe:Counter {
            return (nextLookupMetric.getValue() == 15);
        }
        return false;
    }
    return false;
}
