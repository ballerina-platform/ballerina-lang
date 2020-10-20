// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/java;

final StatisticConfig[] DEFAULT_GAUGE_STATS_CONFIG = [{ timeWindow: 600000, buckets: 5,
    percentiles: [0.33, 0.5, 0.66, 0.75, 0.95, 0.99, 0.999] }];

final map<string> DEFAULT_TAGS = {};


# Start a span with no parent span.
#
# + spanName - Name of the span
# + tags - Tags to be associated to the span
# + return - SpanId of the started span
public function startRootSpan(string spanName, map<string>? tags = ()) returns int = @java:Method {
    'class: "org.ballerinalang.observe.nativeimpl.StartRootSpan",
    name: "startRootSpan"
} external;

# Start a span and create child relationship to current active span or user specified span.
#
# + spanName - Name of the span
# + tags - Tags to be associated to the span
# + parentSpanId - Id of the parent span or -1 if parent span should be taken from system trace
# + return - SpanId of the started span
public function startSpan(string spanName, map<string>? tags = (), int parentSpanId = -1) returns int|error = @java:Method {
    'class: "org.ballerinalang.observe.nativeimpl.StartSpan",
    name: "startSpan"
} external;

# Add a key value pair as a tag to the span.
#
# + spanId - Id of span to which the tags should be added or -1 to add tags to the current active span
# + tagKey - Key of the tag
# + tagValue - Value of the tag
# + return - An error if an error occurred while attaching tag to the span
public isolated function addTagToSpan(string tagKey, string tagValue, int spanId = -1) returns error? = @java:Method {
    'class: "org.ballerinalang.observe.nativeimpl.AddTagToSpan",
    name: "addTagToSpan"
} external;

# Finish the current span.
#
# + spanId - Id of span to finish
# + return - An error if an error occurred while finishing the span
public function finishSpan(int spanId) returns error? = @java:Method {
    'class: "org.ballerinalang.observe.nativeimpl.FinishSpan",
    name: "finishSpan"
} external;

# Retrieve all registered metrics including default metrics from the ballerina runtime, and user defined metrics.
#
# + return - Array of all registered metrics.
public function getAllMetrics() returns Metric[] = @java:Method {
    'class: "org.ballerinalang.observe.nativeimpl.GetAllMetrics",
    name: "getAllMetrics"
} external;

# Retrieves the specific metric that is described by the given name and tags.
#
# + name - Name of the metric to lookup.
# + tags - The key/value pair tags that associated with the metric that should be looked up.
# + return - The metric instance.
public function lookupMetric(string name, map<string>? tags = ()) returns Counter|Gauge? = @java:Method {
    'class: "org.ballerinalang.observe.nativeimpl.LookupMetric",
    name: "lookupMetric"
} external;

# Checks of either metrics or tracing had been enabled.
#
# + return - True if observability had been enabled.
public isolated function isObservabilityEnabled() returns boolean = @java:Method {
    name: "isObservabilityEnabled",
    'class: "io.ballerina.runtime.observability.ObserveUtils"
} external;

# This represents the metric type - counter, that can be only increased by an integer number.
#
# + name - Name of the counter metric.
# + description - Description of the counter metric.
# + metricTags - Tags associated with the counter metric.
public  class Counter {

    public string name;
    public string description;
    public map<string> metricTags;

    # This instantiates the Counter object. Name field is mandatory, and description and tags fields
    # are optional and have its own default values when no params are passed.
    #
    # + name - Name of the Counter instance.
    # + desc - Description of the Counter instance. If no description is provided, the the default empty string
    #          will be used.
    # + tags - The key/value pair of Tags. If no tags are provided, the default nil value will be used.
    public function init(string name, string? desc = "", map<string>? tags = ()) {
        self.name = name;
        if (desc is string) {
            self.description = desc;
        } else {
            self.description = "";
        }
        if (tags is map<string>) {
            self.metricTags = tags;
        } else {
            self.metricTags = DEFAULT_TAGS;
        }
        externCounterInit(self);
    }

    //# Performs the necessary native operations during the initialization of the counter.
    //function initialize() = external;

    # Register the counter metric instance with the Metric Registry.
    #
    # + return - Returns error if there is any metric registered already with the same name
    #            but different parameters or in a different kind.
    public function register() returns error? {
        return externCounterRegister(self);
    }

    # Unregister the counter metric instance with the Metric Registry.
    public function unregister() {
        externCounterUnRegister(self);
    }

    # Increment the counter's value by an amount.
    #
    # + amount - The amount by which the value needs to be increased. The amount is defaulted as 1 and will be
    #            used if there is no amount passed in.
    public function increment(int amount = 1) {
        externCounterIncrement(self, amount);
    }

    # Resets the counter's value to zero.
    public function reset() {
        externCounterReset(self);
    }

    # Retrieves the counter's current value.
    #
    # + return - The current value of the counter.
    public function getValue() returns int {
        return externCounterGetValue(self);
    }
}

function externCounterInit(Counter counter) = @java:Method {
    'class: "org.ballerinalang.observe.nativeimpl.CounterInitialize",
    name: "initialize"
} external;

function externCounterRegister(Counter counter) returns error? = @java:Method {
    'class: "org.ballerinalang.observe.nativeimpl.CounterRegister",
    name: "register"
} external;

function externCounterUnRegister(Counter counter) = @java:Method {
    'class: "org.ballerinalang.observe.nativeimpl.CounterUnregister",
    name: "unregister"
} external;

function externCounterIncrement(Counter counter, int amount) = @java:Method {
    'class: "org.ballerinalang.observe.nativeimpl.CounterIncrement",
    name: "increment"
} external;

function externCounterReset(Counter counter) = @java:Method {
    'class: "org.ballerinalang.observe.nativeimpl.CounterReset",
    name: "reset"
} external;

function externCounterGetValue(Counter counter) returns int = @java:Method {
    'class: "org.ballerinalang.observe.nativeimpl.CounterGetValue",
    name: "getValue"
} external;

# This represents the metric type - gauge, that can hold instantaneous, increased or decreased value
# during the usage.
#
# + name - Name of the counter metric.
# + description - Description of the counter metric.
# + metricTags - Tags associated with the counter metric.
# + statisticConfigs - Array of StatisticConfig objects which defines about the statistical calculation
#                      of the gauge during its usage.
public class Gauge {

    public string name;
    public string description;
    public map<string> metricTags;
    public StatisticConfig[] statisticConfigs;

    # This instantiates the Gauge object. Name field is mandatory, and description, tags, and statitics config fields
    # are optional and have its own default values when no params are passed.
    #
    # + name - Name of the Gauge instance.
    # + desc - Description of the Gauge instance. If no description is provided, the the default empty string
    #          will be used.
    # + tags - The key/value pair of Tags. If no tags are provided, the default nil value will be used.
    # + statisticConfig - Statistics configurations array is used for statistics calculation. In case if empty
    #                     statistics configurations array is passed, then statistics calculation will be disabled.
    #                     If nil () is passed, then default statistics configs will be used for the statitics
    #                     calculation.
    public function init(string name, string? desc = "", map<string>? tags = (),
               StatisticConfig[]? statisticConfig = ()) {
        self.name = name;
        self.description = desc ?: "";
        self.metricTags = tags ?: DEFAULT_TAGS;
        self.statisticConfigs = statisticConfig ?: DEFAULT_GAUGE_STATS_CONFIG;
        externGaugeInit(self);
    }

    //# Performs the necessary native operations during the initialization of the gauge.
    //function initialize() = external;

    # Register the gauge metric instance with the Metric Registry.
    #
    # + return - Returns error if there is any metric registered already with the same name
    #            but different parameters or in a different kind.
    public function register() returns error? {
        return externGaugeRegister(self);
    }

    # Unregister the counter metric instance with the Metric Registry.
    public function unregister() {
        externGaugeUnRegister(self);
    }

    # Increment the gauge's value by an amount.
    #
    # + amount - The amount by which the value of gauge needs to be increased.
    #            The amount is defaulted as 1.0 and will be used if there is no amount passed in.
    public function increment(float amount = 1.0) {
        externGaugeIncrement(self, amount);
    }

    # Decrement the gauge's value by an amount.
    #
    # + amount - The amount by which the value of gauge needs to be decreased.
    #            The amount is defaulted as 1.0 and will be used if there is no amount passed in.
    public function decrement(float amount = 1.0) {
        externGaugeDecrement(self, amount);
    }

    # Sets the instantaneous value for gauge.
    #
    # + amount - The instantaneous value that needs to be set as gauge value.
    public function setValue(float amount) {
        return externGaugeSetValue(self, amount);
    }

    # Retrieves the gauge's current value.
    #
    # + return - The current value of the gauge.
    public function getValue() returns float {
        return externGaugeGetValue(self);
    }

    # Retrieves statistics snapshots based on the statistics configs of the gauge.
    #
    # + return - Array of the statistics snapshots.
    #            If there is no statisticsConfigs provided, then it will be nil.
    public function getSnapshot() returns Snapshot[]? {
        return externGaugeGetSnapshot(self);
    }
}

function externGaugeInit(Gauge gauge) = @java:Method {
    'class: "org.ballerinalang.observe.nativeimpl.GaugeInitialize",
    name: "initialize"
} external;

function externGaugeRegister(Gauge gauge) returns error? = @java:Method {
    'class: "org.ballerinalang.observe.nativeimpl.GaugeRegister",
    name: "register"
} external;

function externGaugeUnRegister(Gauge gauge) = @java:Method {
    'class: "org.ballerinalang.observe.nativeimpl.GaugeUnregister",
    name: "unregister"
} external;

function externGaugeIncrement(Gauge gauge, float amount) = @java:Method {
    'class: "org.ballerinalang.observe.nativeimpl.GaugeIncrement",
    name: "increment"
} external;

function externGaugeDecrement(Gauge gauge, float amount) = @java:Method {
    'class: "org.ballerinalang.observe.nativeimpl.GaugeDecrement",
    name: "decrement"
} external;

function externGaugeGetValue(Gauge gauge) returns float = @java:Method {
    'class: "org.ballerinalang.observe.nativeimpl.GaugeGetValue",
    name: "getValue"
} external;

function externGaugeSetValue(Gauge gauge, float amount) = @java:Method {
    'class: "org.ballerinalang.observe.nativeimpl.GaugeSetValue",
    name: "setValue"
} external;

function externGaugeGetSnapshot(Gauge gauge) returns Snapshot[]? = @java:Method {
    'class: "org.ballerinalang.observe.nativeimpl.GaugeGetSnapshot",
    name: "getSnapshot"
} external;

# This represents the generic metric record that can represent both counter and gauge.
#
# + name - Name of the metric.
# + desc - Description of the metric.
# + tags - Tags associated with the metric.
# + metricType - Type of the metric.
# + value - Current value the metric.
# + summary - If the metric is configured with statistics config, then the calculated statistics of the metric.
public type Metric record {
    string name;
    string desc;
    map<string> tags;
    string metricType;
    int|float value;
    Snapshot[]? summary;
};

# This represents the statistic configuration that can be used to instatiate gauge metric.
#
# + percentiles - The percentiles that needs to be calculated.
# + timeWindow - The time window (in milli seconds) in which variation of the values are considered.
# + buckets - The number of buckets used in the sliding time window.
public type StatisticConfig record {
    float[] percentiles;
    int timeWindow;
    int buckets;
};

# This represents the percentile value record.
#
# + percentile - The percentile of the reported value.
# + value - The value of the percentile.
public type PercentileValue record {
    float percentile;
    float value;
};

# This represents the snapshot of the statistics calculation of the gauge.
#
# + timeWindow - The time window in which variation of the values are considered.
# + mean - The average value within the time window.
# + max - The max value within the time window.
# + min - The min value within the time window.
# + stdDev - The standard deviation value within the time window.
# + percentileValues - The percentiles values calculated wihtin the time window.
public type Snapshot record {
    int timeWindow;
    float mean;
    float max;
    float min;
    float stdDev;
    PercentileValue[] percentileValues;
};
