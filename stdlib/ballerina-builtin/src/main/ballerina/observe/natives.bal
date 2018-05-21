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
    private {
        string name;
        string description;
        map? tags;
    }

    documentation {
        Increments the counter by one or by the given amount.

        P{{amount}} the amount by which the counter will be increased.
    }
    public native function increment(int amount = 1);

    documentation {
        Returns the counter's current value.

        R{{value}} the counter's current value.
    }
    public native function value() returns (int);
};

documentation {
    Get a Counter instance.

    P{{name}} The name of the counter.
    P{{tags}} Tags to be associated with the metric.
    P{{description}} The description of the metric.
    R{{Counter}} An instance of the counter.
}
public native function getCounterInstance(string name, map<string>? tags = (), string? description = ())
                           returns Counter|error;

documentation {
    Represents a span
    F{{spanId}} unique Id to identify a span
    F{{isFinished}} mark a span as finished
}
public type Span object {

    private {
        string spanId,
        boolean isFinished,
    }

    documentation {
        Add a key value pair as a tag to the span.

        P{{tagKey}} key of the tag
        P{{tagValue}} value of the tag
        R{{}} An error if an error occured while attaching tag to the span
    }
    public native function addTag(string tagKey, string tagValue) returns error?;

    documentation {
        Finish the current span.

        R{{}} An error if an error occured while attaching tag to the span
    }
    public native function finish();

};

documentation {
    Start a span.

    P{{serviceName}} Name of the service the span should belong to
    P{{spanName}} Name of the span
    P{{tags}} tags to be associated to the span
    R{{}} An instance of the started span
}
public native function startSpan(string serviceName, string spanName, map? tags = ()) returns Span {}

// Native implementation to avoid reading configuration file
//public native function isTraceEnabled() returns boolean {}

//public native function isMetricsEnabled() returns boolean {}