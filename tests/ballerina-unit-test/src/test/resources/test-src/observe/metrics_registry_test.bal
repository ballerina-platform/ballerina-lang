import ballerina/observe;


function getAllMetricsSize() returns (int) {
    observe:Metric[] metrics = observe:getAllMetrics();
    return (metrics.length());
}

function registerAngGetMetrics() returns (int) {
    map<string> tags = { "method": "PUT" };
    observe:Counter counter1 = new("service_requests_total", desc = "Total requests.", tags = tags);
    _ = counter1.register();
    counter1.increment(amount = 5);
    return getAllMetricsSize();
}

function lookupRegisteredMetrics() returns (boolean) {
    string name = "service_requests_total";
    map<string> tags = { "method": "PUT" };
    observe:Counter|observe:Gauge|() metric = observe:lookupMetric(name, tags = tags);
    match metric {
        observe:Counter counter => {
            int value = counter.getValue();
            return (value == 5);
        }
        observe:Gauge gauge => return false;
        () => return false;
    }
}

function lookupRegisteredNameOnlyMetrics() returns (boolean) {
    string name = "service_requests_total";
    observe:Counter|observe:Gauge|() metric = observe:lookupMetric(name);
    match metric {
        observe:Counter counter => return true;
        observe:Gauge gauge => return true;
        () => return false;
    }
}

function lookupRegisterAndIncrement() returns (boolean) {
    string name = "service_requests_total";
    map<string> tags = { "method": "PUT" };
    observe:Counter|observe:Gauge|() metric = observe:lookupMetric(name, tags = tags);
    match metric {
        observe:Counter counter => {
            counter.increment(amount=10);
            observe:Counter|observe:Gauge|() nextLookupMetric = observe:lookupMetric(name, tags = tags);
            match nextLookupMetric {
                observe:Counter nCounter => {
                    return (nCounter.getValue() == 15);
                }
                observe:Gauge gauge => return false;
                () => return false;
            }
        }
        observe:Gauge gauge => return false;
        () => return false;
    }
}
