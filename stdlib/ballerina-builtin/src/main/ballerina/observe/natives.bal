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

//@Description {value:"Counter metric, to track counts of events or running totals."}
//@Field {value:"name: The name of the counter. Required."}
//@Field {value:"description: The description string of the metric. Required."}
//@Field {value:"tags: Key/value pairs used to classify counter. Optional."}
//public type Counter object {
//    private {
//        string name;
//        string description;
//        map? tags;
//    }
//
//    @Description {value:"Increment the counter by the given amount."}
//    @Param {value:"amount: float to be added with the counter value."}
//    public native function increment(int amount = 1);
//
//    @Description {value:"Return the value of the counter."}
//    @Return {value:"The value of a counter."}
//    public native function value() returns (int);
//};
//
//public native function getOrCreateCounter(string name, string description = "", map<string>? tags = ())
//                           returns Counter | error;
//
//@Description {value:"Gauge metric, to report instantaneous values. Gauges can go both up and down."}
//@Field {value:"name: The name of the gauge. Required."}
//@Field {value:"description: The description string of the metric. Required."}
//@Field {value:"tags: Key/value pairs used to classify gauge. Optional."}
//public type Summary object {
//    private {
//        string name;
//        string description;
//        map? tags;
//    }
//
//    @Description {value:"Increment the gauge by the given amount."}
//    @Param {value:"amount: float to be added with the gauge value."}
//    public native function incrementCurrentValue(int amount = 1);
//
//    @Description {value:"Decrement the gauge by one."}
//    public native function decrementCurrentValue(int amount = 1);
//
//    @Description {value:"Set the gauge to the given value."}
//    @Param {value:"value: value to be set to the gauge."}
//    public native function setCurrentValue(int value);
//
//    @Description {value:"Return the value of the gauge."}
//    @Return {value:"The value of a gauge."}
//    public native function currentValue() returns (int);
//
//    @Description {value:"Return the maximum value of an event."}
//    @Return {value: "The maximum time of a single event."}
//    public native function max() returns (int);
//
//    public native function min() returns (int);
//
//    @Description {value: "Return the mean value of all recorded events."}
//    @Return {value: "The distribution average for all recorded events."}
//    public native function mean() returns (float);
//
//    @Description {value: "Return a map of percentile values."}
//    @Return {value: "A map of values at specific percentiles."}
//    public native function percentileValues() returns (float[]);
//
//};
//
//public native function getOrCreateSummary(string name, string description = "", map<string>? tags = (),
//                                        int duration = 2, float[] percentiles = [0.5, 0.75, 0.98, 0.99, 0.999])
//                           returns Summary | error;

//todo how to join this with OOTB impl

public type Span object {

    private {
        string spanId,
        boolean isFinished,
    }

    // todo check private constructors
    // todo Timer within startSpan and finish natives

    public native function addTag(string tagKey, string tagValue) returns error?;

    public native function finish();

};

public native function startSpan(string serviceName, string spanName, map? tags = ()) returns Span {}
