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

@final StatisticConfig[] DEFAULT_GAUGE_STATS_CONFIG = [{ timeWindow: 600000, buckets: 5,
    percentiles: [0.33, 0.5, 0.66, 0.99] }];

@final map<string> DEFAULT_TAGS;


documentation {
    Start a span with no parent span.

    P{{spanName}} Name of the span
    P{{tags}} Tags to be associated to the span
    R{{spanId}} SpanId of the started span
}
public extern function startRootSpan(string spanName, map<string>? tags = ()) returns int;

documentation {
    Start a span and create child relationship to current active span or user specified span.

    P{{spanName}} Name of the span
    P{{tags}} Tags to be associated to the span
    P{{parentSpanId}} Id of the parent span or -1 if parent span should be taken from system trace
    R{{spanId}} SpanId of the started span
}
public extern function startSpan(string spanName, map<string>? tags = (), int parentSpanId = -1) returns int|error;

documentation {
   Add a key value pair as a tag to the span.

    P{{spanId}} Id of span to which the tags should be added
    P{{tagKey}} Key of the tag
    P{{tagValue}} Value of the tag
    R{{error}} An error if an error occured while attaching tag to the span
}
public extern function addTagToSpan(int spanId, string tagKey, string tagValue) returns error?;

documentation {
        Finish the current span.

        P{{spanId}} Id of span to finish
    }
public extern function finishSpan(int spanId) returns error?;

documentation {
    Retrieve all registered metrics including default metrics from the ballerina runtime, and user defined metrics.

    R{{metrics}} Array of all registered metrics.
}
public extern function getAllMetrics() returns Metric[];

documentation {
    Retrieves the specific metric that is described by the given name and tags.

    P{{name}} Name of the metric to lookup.
    P{{tags}} The key/value pair tags that associated with the metric that should be looked up.
    R{{metric}} The metric instance.
}
public extern function lookupMetric(string name, map<string>? tags=()) returns Counter|Gauge|();

documentation {
    This represents the metric type - counter, that can be only increased by an integer number.

    F{{name}} Name of the counter metric.
    F{{decription}} Description of the counter metric.
    F{{metricTags}} Tags associated with the counter metric.
}
public type Counter object {

    @readonly public string name;
    @readonly public string description;
    @readonly public map<string> metricTags;

    documentation{
        This instantiates the Counter object. Name field is mandatory, and description and tags fields
         are optional and have its own default values when no params are passed.

         F{{name}} Name of the Counter instance.
         F{{desc}} Description of the Counter instance. If no description is provided, the the default empty string
                   will be used.
         F{{tags}} The key/value pair of Tags. If no tags are provided, the default nil value will be used.
    }
    public new(name, string? desc = "", map<string>? tags=()) {
        description = desc but {
            () => ""
        };
        metricTags = tags but {
            () =>  DEFAULT_TAGS
        };
        initialize();
    }

    documentation {
        Performs the necessary native operations during the initialization of the counter.
    }
    extern function initialize();

    documentation {
        Register the counter metric instance with the Metric Registry.

        R{{error}} Returns error if there is any metric registered already with the same name
                   but different parameters or in a different kind.
    }
    public extern function register() returns error?;

    documentation {
        Unregister the counter metric instance with the Metric Registry.
    }
    public extern function unregister();

    documentation{
        Increment the counter's value by an amount.

        P{{amount}} The amount by which the value needs to be increased. The amount is defaulted as 1 and will be
                    used if there is no amount passed in.
    }
    public extern function increment(int amount = 1);

    documentation{
        Resets the counter's value to zero.

    }
    public extern function reset();

    documentation{
        Retrieves the counter's current value.

        R{{value}} The current value of the counter.
    }
    public extern function getValue() returns (int);

};

documentation {
    This represents the metric type - gauge, that can hold instantaneous, increased or decreased value
    during the usage.

    F{{name}} Name of the counter metric.
    F{{decription}} Description of the counter metric.
    F{{metricTags}} Tags associated with the counter metric.
    F{{statisticConfigs}} Array of StatisticConfig objects which defines about the statistical calculation
                          of the gauge during its usage.
}
public type Gauge object {

    @readonly public string name;
    @readonly public string description;
    @readonly public map<string> metricTags;
    @readonly public StatisticConfig[] statisticConfigs;

    documentation{
        This instantiates the Gauge object. Name field is mandatory, and description, tags, and statitics config fields
         are optional and have its own default values when no params are passed.

         F{{name}} Name of the Gauge instance.
         F{{desc}} Description of the Gauge instance. If no description is provided, the the default empty string
                   will be used.
         F{{tags}} The key/value pair of Tags. If no tags are provided, the default nil value will be used.
         F{{statisticConfig}} Statistics configurations array is used for statistics calculation. In case if empty
                              statistics configurations array is passed, then statistics calculation will be disabled.
                              If nil () is passed, then default statistics configs will be used for the statitics
                              calculation.
    }
    public new(name, string? desc = "", map<string>? tags = (),
               StatisticConfig[]? statisticConfig = ()) {
        description = desc but {
            () => ""
        };
        metricTags = tags but {
            () =>  DEFAULT_TAGS
        };
        statisticConfigs = statisticConfig but {
            () => DEFAULT_GAUGE_STATS_CONFIG
        };
        initialize();
    }

    documentation {
        Performs the necessary native operations during the initialization of the gauge.
    }
    extern function initialize();

    documentation {
        Register the gauge metric instance with the Metric Registry.

        R{{error}} Returns error if there is any metric registered already with the same name
        but different parameters or in a different kind.
    }
    public extern function register() returns error?;

    documentation {
        Unregister the counter metric instance with the Metric Registry.
    }
    public extern function unregister();

    documentation{
        Increment the gauge's value by an amount.

        P{{amount}} The amount by which the value of gauge needs to be increased.
        The amount is defaulted as 1.0 and will be used if there is no amount passed in.
    }
    public extern function increment(float amount = 1.0);

    documentation{
        Decrement the gauge's value by an amount.

        P{{amount}} The amount by which the value of gauge needs to be decreased.
        The amount is defaulted as 1.0 and will be used if there is no amount passed in.
    }
    public extern function decrement(float amount = 1.0);

    documentation{
        Sets the instantaneous value for gauge.

        P{{amount}} The instantaneous value that needs to be set as gauge value.
    }
    public extern function setValue(float amount);

    documentation{
        Retrieves the gauge's current value.

        R{{value}} The current value of the gauge.
    }
    public extern function getValue() returns float;

    documentation{
        Retrieves statistics snapshots based on the statistics configs of the gauge.

        R{{snapshots}} Array of the statistics snapshots.
        If there is no statisticsConfigs provided, then it will be nil.
    }
    public extern function getSnapshot() returns (Snapshot[]?);

};

documentation {
    This represents the generic metric record that can represent both counter and gauge.

    F{{name}} Name of the metric.
    F{{desc}} Description of the metric.
    F{{tags}} Tags associated with the metric.
    F{{metricType}} Type of the metric.
    F{{value}} Current value the metric.
    F{{summary}} If the metric is configured with statistics config, then the calculated statistics of the metric.
}
public type Metric record {
    @readonly string name;
    @readonly string desc;
    @readonly map<string> tags;
    @readonly string metricType;
    @readonly int|float value;
    @readonly Snapshot[]? summary;
};

documentation {
    This represents the statistic configuration that can be used to instatiate gauge metric.

    F{{percentiles}} The percentiles that needs to be calculated.
    F{{timeWindow}} The time window (in milli seconds) in which variation of the values are considered.
    F{{buckets}} The number of buckets used in the sliding time window.
}
public type StatisticConfig record {
    float[] percentiles;
    int timeWindow;
    int buckets;
};

documentation {
    This represents the percentile value record.

    F{{percentile}} The percentile of the reported value.
    F{{value}} The value of the percentile.
}
public type PercentileValue record {
    @readonly float percentile;
    @readonly float value;
};

documentation {
    This represents the snapshot of the statistics calculation of the gauge.

    F{{timeWindow}} The time window in which variation of the values are considered.
    F{{mean}} The average value within the time window.
    F{{max}} The max value within the time window.
    F{{min}} The min value within the time window.
    F{{stdDev}} The standard deviation value within the time window.
    F{{percentileValues}} The percentiles values calculated wihtin the time window.
}
public type Snapshot record {
    @readonly int timeWindow;
    @readonly float mean;
    @readonly float max;
    @readonly float min;
    @readonly float stdDev;
    @readonly PercentileValue[] percentileValues;
};
