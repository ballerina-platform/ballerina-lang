// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/jballerina.java;

public type Tag record {
  string key;
  string value;
};

public type MetricId record {
  string name;
  string description;
  Tag[] tags;
};

public type PercentileValue record {
  float percentile;
  float value;
};

public type TimeWindow record {
    int seconds;
    int nanos;
};

public type Snapshot record {
  TimeWindow timeWindow;
  float min;
  float max;
  float mean;
  float stdDev;
  PercentileValue[] percentileValues;
};

public type Counter record {
  MetricId id;
  int value;
};

public type Gauge record {
  MetricId id;
  float value;
  int count;
  float sum;
  Snapshot[] snapshots;
};

public type PolledGauge record {
  MetricId id;
  float value;
};

public type Metrics record {
  Counter[] counters;
  Gauge[] gauges;
  PolledGauge[] polledGauges;
};

# Get all the current metrics
#
# + return - Current metrics
public isolated function getMetrics() returns Metrics {
    json metricsJson = externGetMetrics();
    Metrics | error metrics = metricsJson.cloneWithType(Metrics);
    if (metrics is error) {
        panic error("cannot convert to Metrics record; json string: " + metricsJson.toJsonString(), metrics);
    } else {
        return metrics;
    }
}

# Get all the current metrics
#
# + return - The metrics currently in the metrics registry as a json
isolated function externGetMetrics() returns json = @java:Method {
    name: "getMetrics",
    'class: "org.ballerinalang.observe.mockextension.MetricsUtils"
} external;
