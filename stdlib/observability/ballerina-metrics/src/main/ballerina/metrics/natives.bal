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

package ballerina.metrics;

@Description {value:"Counter metric, to track counts of events or running totals."}
@Field {value:"name: The name of the counter. Required."}
@Field {value:"description: The description string of the metric. Required."}
public struct Counter {
    string name;
    string description;
    map tags;
}

@Description {value:"Create and register the counter."}
@Param {value:"counter: The counter instance to be registered."}
public native function <Counter counter> register();

@Description {value:"Increment the counter by one."}
@Param {value:"counter: The counter instance to be incremented by one."}
public native function <Counter counter> incrementByOne();

@Description {value:"Increment the counter by the given amount."}
@Param {value:"counter: The counter instance to be incremented by 'amount'."}
@Param {value:"amount: float to be added with the counter value."}
public native function <Counter counter> increment(float amount);

@Description {value:"Get the value of the counter."}
@Param {value:"counter: The counter instance to be returned."}
@Return {value:"The value of a counter"}
public native function <Counter counter> count() returns (float);

@Description {value:"Gauge metric, to report instantaneous values. Gauges can go both up and down."}
@Field {value:"name: The name of the gauge. Required."}
@Field {value:"description: The description string of the metric. Required."}
public struct Gauge {
    string name;
    string description;
    map tags;
}

@Description {value:"Create and register the gauge."}
@Param {value:"gauge: The gauge instance to be registered."}
public native function <Gauge gauge> register();

@Description {value:"Increment the gauge by one."}
@Param {value:"gauge: The gauge instance to be incremented by one."}
public native function <Gauge gauge> incrementByOne();

@Description {value:"Increment the gauge by the given amount."}
@Param {value:"gauge: The gauge instance to be incremented by 'amount'."}
@Param {value:"amount: float to be added with the gauge value."}
public native function <Gauge gauge> increment(float amount);

@Description {value:"Decrement the gauge by one."}
@Param {value:"gauge: The gauge instance to be decremented by one."}
public native function <Gauge gauge> decrementByOne();

@Description {value:"Decrement the gauge by the given amount."}
@Param {value:"gauge: The gauge instance to be decremented by 'amount'."}
@Param {value:"amount: float to be added with the gauge value."}
public native function <Gauge gauge> decrement(float amount);

@Description {value:"Set the gauge to the given value."}
@Param {value:"gauge: The gauge instance to be set."}
@Param {value:"value: value to be set to the gauge."}
public native function <Gauge gauge> setValue(float value);

@Description {value:"Get the value of the gauge."}
@Param {value:"gauge: The gauge instance to be returned."}
@Return {value:"The value of a gauge"}
public native function <Gauge gauge> value() returns (float);

@Description {value:"Summary metric, to track the size of events."}
@Field {value:"name: The name of the summary. Required."}
@Field {value:"description: The description string of the metric. Required."}
public struct Summary {
    string name;
    string description;
    map tags;
}

@Description {value:"Create and register the summary."}
@Param {value:"summary: The summary instance to be registered."}
public native function <Summary summary> register();

@Description {value:""}
@Param {value:"summary:"}
public native function <Summary summary> max() returns (float);

@Description {value: "Get the current values in Summary"}
@Param {value: "summary: "}
@Return {value: ""}
public native function <Summary summary> mean() returns (float);

@Description {value: ""}
@Param {value: "summary: "}
@Param {value: "percentile "}
@Return {value: ""}
public native function <Summary summary> percentile(float percentile) returns (float);

@Description {value: ""}
@Param {value: "summary: "}
@Param {value: "amount "}
public native function <Summary summary> record(float amount);

@Description {value: ""}
@Param {value: "summary: "}
@Return {value: ""}
public native function <Summary summary> count() returns (int);

@Description {value: ""}
@Field {value: "NANOSECONDS: "}
@Field {value: "MICROSECONDS: "}
@Field {value: "MILLISECONDS: "}
@Field {value: "SECONDS: "}
@Field {value: "MINUTES: "}
@Field {value: "HOURS: "}
@Field {value: "DAYS: "}
public enum TimeUnit {
    NANOSECONDS,
    MICROSECONDS,
    MILLISECONDS,
    SECONDS,
    MINUTES,
    HOURS,
    DAYS
}

@Description {value: ""}
@Field {value: "name: "}
@Field {value: "description: "}
@Field {value: "baseTimeUnit: "}
public struct Timer {
    string name;
    string description;
    map tags;
}

@Description {value: ""}
@Param {value: "timer: "}
public native function <Timer timer> register();

@Description {value: ""}
@Param {value: "timer: "}
@Param {value: "timeUnit: "}
@Return {value: ""}
public native function <Timer timer> max(TimeUnit timeUnit) returns (float);

@Description {value: ""}
@Param {value: "timer: "}
@Param {value: "timeUnit: "}
@Return {value: ""}
public native function <Timer timer> mean(TimeUnit timeUnit) returns (float);

@Description {value: ""}
@Param {value: "timer: "}
@Param {value: "percentiles: "}
@Param {value: "timeUnit: "}
@Return {value: ""}
public native function <Timer timer> percentile(float percentiles, TimeUnit timeUnit) returns (float);

@Description {value: ""}
@Param {value: "timer: "}
@Param {value: "amount: "}
@Param {value: "timeUnit: "}
@Param {value: ""}
public native function <Timer timer> record(float amount, TimeUnit timeUnit);

@Description {value: ""}
@Param {value: "timer: "}
@Return {value: ""}
public native function <Timer timer> count() returns (int);
