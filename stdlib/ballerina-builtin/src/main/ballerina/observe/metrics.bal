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

package ballerina.observe;

@Description {value:"Counter metric, to track counts of events or running totals."}
@Field {value:"name: The name of the counter. Required."}
@Field {value:"description: The description string of the metric. Required."}
@Field {value:"tags: Key/value pairs used to classify counter. Optional."}
public type Counter object {
    public {
        string name;
        string description;
        map | () tags;
    }

    public new(name, description, tags) {}

    @Description {value:"Increment the counter by one."}
    public native function incrementByOne();

    @Description {value:"Increment the counter by the given amount."}
    @Param {value:"amount: float to be added with the counter value."}
    public native function increment(float amount);

    @Description {value:"Return the value of the counter."}
    @Return {value:"The value of a counter"}
    public native function count() returns (float);
};

@Description {value:"Gauge metric, to report instantaneous values. Gauges can go both up and down."}
@Field {value:"name: The name of the gauge. Required."}
@Field {value:"description: The description string of the metric. Required."}
@Field {value:"tags: Key/value pairs used to classify gauge. Optional."}
public type Gauge object {
    public {
        string name;
        string description;
        map | () tags;
    }

    public new(name, description, tags) {}

    @Description {value:"Increment the gauge by one."}
    public native function incrementByOne();

    @Description {value:"Increment the gauge by the given amount."}
    @Param {value:"amount: float to be added with the gauge value."}
    public native function increment(float amount);

    @Description {value:"Decrement the gauge by one."}
    public native function decrementByOne();

    @Description {value:"Decrement the gauge by the given amount."}
    @Param {value:"amount: float to be added with the gauge value."}
    public native function decrement(float amount);

    @Description {value:"Set the gauge to the given value."}
    @Param {value:"value: value to be set to the gauge."}
    public native function setValue(float value);

    @Description {value:"Return the value of the gauge."}
    @Return {value:"The value of a gauge"}
    public native function value() returns (float);
};

@Description {value:"Summary metric, to track the size of events."}
@Field {value:"name: The name of the summary. Required."}
@Field {value:"description: The description string of the metric. Required."}
@Field {value:"tags: Key/value pairs used to classify summary. Optional."}
public type Summary object {
    public {
        string name;
        string description;
        map | () tags;
    }

    public new(name, description, tags) {}

    @Description {value: "Record the specific amount and update the summary."}
    @Param {value: "amount: Amount for an event being measured."}
    public native function record(float amount);

    @Description {value:"Return the maximum value of an event."}
    @Return {value: "The maximum time of a single event."}
    public native function max() returns (float);

    @Description {value: "Return the mean value of all recorded events."}
    @Return {value: "The distribution average for all recorded events."}
    public native function mean() returns (float);

    @Description {value: "Return a map of percentile values."}
    @Return {value: "A map of values at specific percentiles."}
    public native function percentileValues() returns (map);

    @Description {value: "Return the number of values recorded in the summary."}
    @Return {value: "The number of times that record has been called since this timer was created."}
    public native function count() returns (int);
};

public type TimeUnit "NANOSECONDS" | "MICROSECONDS" | "MILLISECONDS" | "SECONDS" | "MINUTES" | "HOURS" | "DAYS";

@final public TimeUnit TIME_UNIT_NANOSECONDS = "NANOSECONDS";
@final public TimeUnit TIME_UNIT_MICROSECONDS = "MICROSECONDS";
@final public TimeUnit TIME_UNIT_MILLISECONDS = "MILLISECONDS";
@final public TimeUnit TIME_UNIT_SECONDS = "SECONDS";
@final public TimeUnit TIME_UNIT_MINUTES = "MINUTES";
@final public TimeUnit TIME_UNIT_HOURS = "HOURS";
@final public TimeUnit TIME_UNIT_DAYS = "DAYS";

@Description {value: "Timer metric, to track events."}
@Field {value: "name: The name of the timer. Required."}
@Field {value: "description: The description string of the metric. Required."}
@Field {value:"tags: Key/value pairs used to classify timer. Optional."}
public type Timer object {
    public {
        string name;
        string description;
        map | () tags;
    }

    public new(name, description, tags) {}

    @Description {value: "Record the specific amount with the time unit and update the timer."}
    @Param {value: "amount: Duration of a single event being measured by this timer."}
    @Param {value: "timeUnit: Time unit for the amount being recorded."}
    public native function record(int amount, TimeUnit timeUnit);

    @Description {value: "Return the maximum time of an event."}
    @Param {value: "timeUnit: The base unit of time to scale the max to."}
    @Return {value: "The maximum time of a single event."}
    public native function max(TimeUnit timeUnit) returns (float);

    @Description {value: "Return the mean time of all recorded events."}
    @Param {value: "timeUnit: The base unit of time to scale the mean to."}
    @Return {value: "The distribution average for all recorded events."}
    public native function mean(TimeUnit timeUnit) returns (float);

    @Description {value: "Return a map of latencies scaled with the given base unit of time at specific percentiles."}
    @Param {value: "timeUnit: The base unit of time to scale the percentile value to."}
    @Return {value: "A map of latencies at specific percentiles"}
    public native function percentileValues(TimeUnit timeUnit) returns (map);

    @Description {value: "Returns the number of times recorded in the timer."}
    @Return {value: "The number of times that stop has been called on this timer."}
    public native function count() returns (int);
};
