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

import ballerina/task;
import ballerina/io;

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
    Counter metric, to track counts of events or running totals.
}
public type Counter object {

 public {
  @readonly string name,
  @readonly string description,
  @readonly map<string> metricTags,
  @readonly stream<CounterEvent> counterEvent;
 }

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

 public function subscribe(function (CounterEvent) callBackFunction) {
  counterEvent.subscribe(callBackFunction);
 }

 public function increment(int amount = 1) {
  int currentValue = nativeIncrement(amount);
  json jsonTags = check <json>metricTags;
  string strTags = jsonTags.toString();
  CounterEvent event = { name: name, value: currentValue, tags: strTags };
  counterEvent.publish(event);
 }

 native function nativeIncrement(int amount) returns int;

 public native function getValue() returns (int);

};

public type Gauge object {

 public {
  @readonly string name,
  @readonly string description,
  @readonly map<string> metricTags,
  @readonly StatisticConfig[] statisticConfigs,
  @readonly stream<GaugeEvent> gaugeStream,
  @readonly boolean isStatsPushEnabled,
  @readonly stream<GaugeStatisticsEvent> summaryStream,
 }

 private {
  task:Timer? timer,
 }

 public new(name, string? desc = "", map<string>? tags = (), StatisticConfig[]? statisticConfig = (),
            int pushInterval = -1) {
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
  }
  initialize();
  if (pushInterval != -1) {
   isStatsPushEnabled = true;
   timer = new task:Timer(self.onPushSummaryTrigger, self.onPushSummaryError, pushInterval, delay = 0);
   timer.start();
  }
 }

 native function initialize();

 function onPushSummaryTrigger() returns error? {
  Snapshot[] snapShots = self.getSnapshot();
  foreach snapShot in snapShots {
   json jsonPercentiles = check <json>snapShot.percentileValues;
   GaugeStatisticsEvent event = { expiry: snapShot.expiry, averageValue: snapShot.mean,
    maxValue: snapShot.max, minValue: snapShot.min, stdDev: snapShot.stdDev, percentiles: jsonPercentiles.toString() };
   summaryStream.publish(event);
  }
  return;
 }

 function onPushSummaryError(error e) {
  io:println("Error occured when pushing the statistics summary to stream");
  io:println(e);
 }

 public function subscribe(function (GaugeEvent) callBackFunction) {
  gaugeStream.subscribe(callBackFunction);
 }

 public function subscribeToStatistics(function (GaugeStatisticsEvent) callBackFunction) returns error? {
  if (isStatsPushEnabled){
   summaryStream.subscribe(callBackFunction);
   return ();
  } else {
   error e = { message: "Statistics push is disabled as this Gauge have been configured with pushInterval = -1.
   Therefore no subscription for statistics is permitted." };
   throw e;
  }
 }

 public native function register() returns error?;

 public function increment(float amount) {
  float currentVal = nativeIncrement(amount);
  publishToGaugeStream(currentVal);
 }

 native function nativeIncrement(float amount) returns float;

 public function decrement(float amount) {
  float currentVal = nativeDecrement(amount);
  publishToGaugeStream(currentVal);
 }
 native function nativeDecrement(float amount) returns float;

 public function setValue(float amount) {
  nativeSetValue(amount);
  publishToGaugeStream(amount);
 }

 native function nativeSetValue(float amount);

 public native function getSnapshot() returns (Snapshot[]);

 public native function getValue() returns float;

 function publishToGaugeStream(float currentValue) {
  json jsonTags = check <json>metricTags;
  string strTags = jsonTags.toString();
  GaugeEvent event = { name: name, value: currentValue, tags: strTags };
  gaugeStream.publish(event);
 }
};

public native function getAllMetrics() returns Metric[];

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
