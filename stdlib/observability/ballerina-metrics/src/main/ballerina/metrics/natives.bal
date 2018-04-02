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
@Field {value:"tags: Key/value pairs used to classify counter. Optional."}
public struct Counter {
    string name;
    string description;
    map tags;
}

@Description {value:"Increment the counter by one."}
@Param {value:"counter: The counter instance to be incremented by one."}
public native function <Counter counter> incrementByOne();

@Description {value:"Increment the counter by the given amount."}
@Param {value:"counter: The counter instance to be incremented by 'amount'."}
@Param {value:"amount: float to be added with the counter value."}
public native function <Counter counter> increment(float amount);

@Description {value:"Return the value of the counter."}
@Param {value:"counter: The counter instance to be returned."}
@Return {value:"The value of a counter"}
public native function <Counter counter> count() returns (float);

@Description {value:"Gauge metric, to report instantaneous values. Gauges can go both up and down."}
@Field {value:"name: The name of the gauge. Required."}
@Field {value:"description: The description string of the metric. Required."}
@Field {value:"tags: Key/value pairs used to classify gauge. Optional."}
public struct Gauge {
    string name;
    string description;
    map tags;
}

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

@Description {value:"Return the value of the gauge."}
@Param {value:"gauge: The gauge instance to be returned."}
@Return {value:"The value of a gauge"}
public native function <Gauge gauge> value() returns (float);

@Description {value:"Summary metric, to track the size of events."}
@Field {value:"name: The name of the summary. Required."}
@Field {value:"description: The description string of the metric. Required."}
@Field {value:"tags: Key/value pairs used to classify summary. Optional."}
public struct Summary {
    string name;
    string description;
    map tags;
}

@Description {value: "Record the specific amount and update the summary."}
@Param {value: "summary: The summary instance."}
@Param {value: "amount: Amount for an event being measured."}
public native function <Summary summary> record(float amount);

@Description {value:"Return the maximum value of an event."}
@Param {value:"summary: The summary instance."}
@Return {value: "The maximum time of a single event."}
public native function <Summary summary> max() returns (float);

@Description {value: "Return the mean value of all recorded events."}
@Param {value: "summary: The summary instance."}
@Return {value: "The distribution average for all recorded events."}
public native function <Summary summary> mean() returns (float);

@Description {value: "Return the value at a specific percentile."}
@Param {value: "summary: The summary instance."}
@Param {value: "percentile: A percentile in the domain."}
@Return {value: "The value at a specific percentile."}
public native function <Summary summary> percentile(float percentile) returns (float);

@Description {value: "Return the number of values recorded in the summary."}
@Param {value: "summary: The summary instance."}
@Return {value: "The number of times that record has been called since this timer was created."}
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

@Description {value: "Timer metric, to track events."}
@Field {value: "name: The name of the timer. Required."}
@Field {value: "description: The description string of the metric. Required."}
@Field {value:"tags: Key/value pairs used to classify timer. Optional."}
public struct Timer {
    string name;
    string description;
    map tags;
}

@Description {value: "Record the specific amount with the time unit and update the timer."}
@Param {value: "timer: The timer instance."}
@Param {value: "amount: Duration of a single event being measured by this timer."}
@Param {value: "timeUnit: Time unit for the amount being recorded."}
public native function <Timer timer> record(int amount, TimeUnit timeUnit);

@Description {value: "Return the maximum time of an event."}
@Param {value: "timer: The timer instance."}
@Param {value: "timeUnit: The base unit of time to scale the max to."}
@Return {value: "The maximum time of a single event."}
public native function <Timer timer> max(TimeUnit timeUnit) returns (float);

@Description {value: "Return the mean time of all recorded events."}
@Param {value: "timer: The timer instance."}
@Param {value: "timeUnit: The base unit of time to scale the mean to."}
@Return {value: "The distribution average for all recorded events."}
public native function <Timer timer> mean(TimeUnit timeUnit) returns (float);

@Description {value: "Return the latency at a specific percentile."}
@Param {value: "timer: The timer instance."}
@Param {value: "percentiles: A percentile in the domain."}
@Param {value: "timeUnit: The base unit of time to scale the percentile value to."}
@Return {value: "The latency at a specific percentile."}
public native function <Timer timer> percentile(float percentiles, TimeUnit timeUnit) returns (float);

@Description {value: "Returns the number of times recorded in the timer."}
@Param {value: "timer: The timer instance."}
@Return {value: "The number of times that stop has been called on this timer."}
public native function <Timer timer> count() returns (int);
