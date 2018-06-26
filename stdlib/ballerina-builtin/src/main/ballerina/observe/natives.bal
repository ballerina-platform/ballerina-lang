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
  @readonly map<string> tags,
  @readonly stream<CounterEvent> counterEvent;
 }

 public new(name, string? desc = "", map<string>? metricTags) {
  match desc {
   string strDesc => description = strDesc;
  }
  match metricTags {
   map<string> tagsMap => tags = tagsMap;
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
  json jsonTags = check <json>tags;
  string strTags = jsonTags.toString();
  CounterEvent event = { name: name, desc: description, value: currentValue, tags: strTags };
  counterEvent.publish(event);
 }

 native function nativeIncrement(int amount) returns int;

 public native function getValue() returns (int);

};

//public type Gauge object {
//
// public {
//  @readonly string name,
//  @readonly string description,
//  @readonly map<string> tags,
//  @readonly stream<GaugeEvent> gaugeStream,
//  @readonly future[] summaryQueries,
//  @readonly SummaryConfig[] summaryConfigs,
//  @readonly stream<WindowGaugeEvent>[] windowStreams,
//  @readonly map<Snapshot> snapshots;
// }
//
// new(name, string? desc = "", map<string>? gaugeTags = ()) {
//  match desc {
//   string strDesc => description = strDesc;
//  }
//  match gaugeTags {
//   map<string> tagMap => tags = tagMap;
//  }
//  gaugeStream.subscribe(createWindowGaugeStreamEvent);
// }
//
// public function subscribe(function (GaugeEvent) callBackFunction) {
//  gaugeStream.subscribe(callBackFunction);
// }
//
// public function addSummary(SummaryConfig config, function (GaugeSummaryEvent) subscribeCallbackFunction) {
//  summaryConfigs[lengthof summaryQueries] = config;
//  stream<WindowGaugeEvent> windowStream;
//  summaryQueries[lengthof summaryQueries] = start streamSummary(config, subscribeCallbackFunction);
//  windowStreams[lengthof windowStreams] = windowStream;
//  nativeAddSummary(config);
// }
//
// native function nativeAddSummary(SummaryConfig config);
//
// function createWindowGaugeStreamEvent(GaugeEvent gaugeEvent) {
//  int index = 0;
//  foreach summaryConfig in summaryConfigs {
//   WindowGaugeEvent windowEvent = { name: gaugeEvent.name, desc: gaugeEvent, timeExpiry: summaryConfig.expiry,
//    value: gaugeEvent.value, tags: gaugeEvent.tags };
//   windowStreams[index].publish(windowEvent);
//  }
// }
//
// function streamSummary(SummaryConfig config, stream<WindowGaugeEvent> windowStream,
//                        function (GaugeSummaryEvent) subscribeCallbackFunction) {
//  forever {
//   from windowStream
//   window timeBatch(timeExpiry)
//   select
//   name as name,
//   desc as desc,
//   value as currentValue,
//   avg(value) as averageValue,
//   max(value) as maxValue,
//   min(value) as minValue,
//   sum(value) as sumValue,
//   count(name) as totalCount,
//   percentile() as percentiles
//   => (GaugeSummaryEvent sumaryEvent) {
//    defaultSummaryEventSubscription(config, summaryEvent);
//    subscribeCallbackFunction(summaryEvent);
//   }
//  }
// }
//
// function defaultSummaryEventSubscription(SummaryConfig config, GaugeSummaryEvent summaryEvent) {
//  Snapshot currentSnapshot = { value: summaryEvent.currentValue, mean: summaryEvent.averageValue,
//   max: summaryEvent.maxValue, min: summaryEvent.minValue, sum: summaryEvent.sumValue,
//   count: summaryEvent.totalCount, percentileValues: summaryEvent.percentiles };
//  snapshots[config.name] = currentSnapshot;
// }
//
// public native function register() returns error?;
//
// public function increment(float amount) {
//  float currentVal = nativeIncrement(amount);
//  publishToGaugeStream(currentValue);
// }
//
// native function nativeIncrement(float amount) returns float;
//
// public function decrement(float amount) {
//  float currentVal = nativeDecrement(amount);
//  publishToGaugeStream(currentValue);
// }
// documentation {
//								Decrements the current recorded value.
//
//								P{{amount}} the amount by which the recorded value will be decreased.
//				}
// native function nativeDecrement(float amount) returns float;
//
//
// public function setValue(float amount) {
//  nativeSetValue(amount);
//  publishToGaugeStream(amount);
// }
//
// native function nativeSetValue(float amount);
//
//
// public function getSnapshot(string configName) returns (Snapshot) {
//  Snapshot returnSnapshot;
//  Snapshot? latestSnapShot = snapshots[configName];
//  match latestSnapShot {
//   Snapshot snapshotInstance => returnSnapshot = snapshotInstance;
//   () => returnSnapshot = { value: getValue() };
//  }
//  return returnSnapshot;
// }
//
// native function getValue() returns float;
//
// function publishToGaugeStream(float currentValue) {
//  string jsonTags = tags.toString();
//  GaugeEvent event = { name: name, desc: description, value: currentValue, tags: jsonTags };
//  gaugeStream.publish(event);
// }
//};
//
public type CounterEvent record {
 string name;
 string desc;
 int value;
 string tags;
};

//
//
//public type SummaryConfig object {
//
// public {
//  @readonly string name,
//  @readonly float[] percentiles,
//  @readonly int expiry,
//  @readonly int buckets;
// }
//
// new(name, percentiles, expiry, buckets) {}
//};
//
//type GaugeEvent {
// string name,
// string desc,
// float value,
// string tags;
//};
//
//type WindowGaugeEvent {
// string name,
// string desc,
// int timeExpiry,
// float value,
// string tags;
//};
//
//
//type GaugeSummaryEvent {
// string name,
// string desc,
// float currentValue,
// float averageValue,
// float minValue,
// float maxValue,
// float sumValue,
// int totalCount,
// string percentiles;
//};
//
//public type PercentileValue {
// float percentile;
// float value;
//};
//
//public type Snapshot {
// float value;
// float mean;
// float max;
// float min;
// float sum;
// int count;
// PercentileValue[] percentileValues;
//};