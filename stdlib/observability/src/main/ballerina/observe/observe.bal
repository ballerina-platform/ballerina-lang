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

@final StatisticConfig[] DEFAULT_GAUGE_STATS_CONFIG = [{ expiry: 600000, buckets: 5,
    percentiles: [0.33, 0.5, 0.66, 0.99] }];

documentation {
    Start a span with no parent span.

    P{{spanName}} name of the span
    P{{tags}} tags to be associated to the span
    R{{spanId}} spanId of the started span
}
public native function startRootSpan(string spanName, map<string>? tags = ()) returns int;

documentation {
    Start a span and create child relationship to current active span or user specified span.

    P{{spanName}} name of the span
    P{{tags}} tags to be associated to the span
    P{{parentSpanId}} id of the parent span or -1 if parent span should be taken from system trace
    R{{spanId}} spanId of the started span
}
public native function startSpan(string spanName, map<string>? tags = (), int parentSpanId = -1) returns int|error;

documentation {
        Add a key value pair as a tag to the span.

        P{{spanId}} id of span to which the tags should be added
        P{{tagKey}} key of the tag
        P{{tagValue}} value of the tag
        R{{error}} An error if an error occured while attaching tag to the span
}
public native function addTagToSpan(int spanId, string tagKey, string tagValue) returns error?;

documentation {
        Finish the current span.

        P{{spanId}} id of span to finish
    }
public native function finishSpan(int spanId) returns error?;

documentation {

}
public native function getAllMetrics() returns Metric[];

documentation {
    Counter metric, to track counts of events or running totals.
}
public type Counter object {

    @readonly public string name;
    @readonly public string description;
    @readonly public map<string> metricTags;

    public new(name, string? desc = "", map<string>? tags) {
        match desc {
            string strDesc => description = strDesc;
            () => description = "";
        }
        match tags {
            map<string> tagsMap => metricTags = tagsMap;
        }
        initialize();
    }

    native function initialize();

    public native function register() returns error?;

    public native function increment(int amount);

    public native function getValue() returns (int);

};

public type Gauge object {

    @readonly public string name;
    @readonly public string description;
    @readonly public map<string> metricTags;
    @readonly public StatisticConfig[] statisticConfigs;

    public new(name, string? desc = "", map<string>? tags = (),
               StatisticConfig[]?|boolean statisticConfig = true) {
        match desc {
            string strDesc => description = strDesc;
        }
        match tags {
            map<string> tagMap => metricTags = tagMap;
        }
        match statisticConfig {
            StatisticConfig[] configs => {
                statisticConfigs = configs;
            }
            () => {
                statisticConfigs = [];
            }
            boolean configs => {
                if (configs) {
                    statisticConfigs = DEFAULT_GAUGE_STATS_CONFIG;
                } else {
                    statisticConfigs = [];
                }
            }
        }
        initialize();
    }

    native function initialize();

    public native function register() returns error?;

    public native function increment(float amount);

    public native function decrement(float amount);

    public native function setValue(float amount);

    public native function getValue() returns float;

    public native function getSnapshot() returns (Snapshot[]);

};

public type Metric record {
    string name;
    string desc;
    map<string> tags;
    string metricType;
    int|float value;
    Snapshot[]? summary;
};

public type CounterEvent record {
    string name;
    int value;
    string tags;
};

public type GaugeEvent record {
    string name;
    float value;
    string tags;
};

public type GaugeStatisticsEvent record {
    int expiry;
    float averageValue;
    float minValue;
    float maxValue;
    float stdDev;
    string percentiles;
};

public type StatisticConfig record {
    float[] percentiles;
    int expiry;
    int buckets;
};

public type PercentileValue record {
    float percentile;
    float value;
};

public type Snapshot record {
    int expiry;
    float mean;
    float max;
    float min;
    float stdDev;
    PercentileValue[] percentileValues;
};
