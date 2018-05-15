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
    Counter metric, to track counts of events or running totals.
}
public type Counter object {
    documentation {
        Increments the counter by one or by the given amount.

        P{{amount}} the amount by which the counter will be increased.
    }
    public native function increment(int amount = 1);

    documentation {
        Returns the counter's current value.

        R{{value}} the counter's current value.
    }
    public native function getValue() returns (int);
};

documentation {
    Get a Counter instance.

    P{{name}} The name of the counter.
    P{{tags}} Tags to be associated with the metric.
    P{{description}} The description of the metric.
    R{{Counter}} An instance of the counter.
}
public native function getCounterInstance(string name, string? description = (), map<string>? tags = ())
                           returns Counter|error;

documentation {
    A precomputed percentile of a distribution.
}
public type PercentileValue object {
    public {
        @readonly float percentile;
        @readonly float value;
    }
};

documentation {
    Snapshot of all distribution statistics at a point in time.
}
public type Snapshot object {
    public {
        @readonly int value;
        @readonly float mean;
        @readonly int max;
        @readonly PercentileValue[] percentileValues;
    }
};

documentation {
    Track the sample distribution of events.
}
public type Summary object {
    documentation {
        Increments the current recorded value.

        P{{amount}} the amount by which the recorded value will be increased.
    }
    public native function increment(int amount = 1);

    documentation {
        Decrements the current recorded value.

        P{{amount}} the amount by which the recorded value will be decreased.
    }
    public native function decrement(int amount = 1);

    documentation {
        Updates the statistics kept by the summary with the specified amount.

        P{{amount}} amount for an event being measured.
    }
    public native function record(int amount);

    documentation {
        Returns the number of times that record has been called since this summary was created.

        R{{count}} The number of values recorded.
    }
    public native function getCount() returns (int);

    documentation {
        Returns the total amount of all recorded events.

        R{{sum}} The sum of values recorded.
    }
    public native function getSum() returns (int);

    documentation {
        Get a snapshot of all distribution statistics.
        R{{Snapshot}} snapshot with all distribution statistics.
    }
    public native function getSnapshot() returns (Snapshot);
};

documentation {
    Get a Summary instance.

    P{{name}} The name of the summary.
    P{{tags}} Tags to be associated with the metric.
    P{{description}} The description of the metric.
    P{{expiry}} The amount of time in minutes to keep statistics like max and percentiles.
    P{{percentiles}} Percentiles to compute and publish
    R{{Summary}} An instance of the summary.
}
public native function getSummaryInstance(string name, string? description = (), map<string>? tags = (),
                                          int? expiry = (), float[]? percentiles = ())
                           returns Summary|error;

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
