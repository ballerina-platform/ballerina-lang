// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ballerina/math;

# The aggregator object to calculate standard deviation.
#
# + mean - description
# + stdDeviation - description
# + sumValue - description
# + count - description
public type StdDev object {
    *Aggregator;
    *Snapshotable;
    public float mean = 0.0;
    public float stdDeviation = 0.0;
    public float sumValue = 0.0;
    public int count = 0;

    public function __init() {

    }

    # Updates the current standard deviation as the new values come into the aggregation.
    #
    # + value - Value being added or removed from aggregation in order to calculate the new standard deviation.
    # + eventType - Type of the incoming event `streams:CURRENT`, `streams:EXPIRED` or `streams:RESET`.
    #
    # + return - Updated standard deviation.
    public function process(anydata value, EventType eventType) returns anydata {
        float fVal;
        if (value is int) {
            fVal = <float>value;
        } else if (value is float) {
            fVal = value;
        } else if (value is ()) {
            fVal = 0.0;
        } else {
            error e = error("Unsupported attribute type found");
            panic e;
        }
        if (eventType == "CURRENT") {
            // See here for the algorithm: http://www.johndcook.com/blog/standard_deviation/
            self.count += 1;
            if (self.count == 0) {
                return ();
            } else if (self.count == 1) {
                self.sumValue = fVal;
                self.mean = fVal;
                self.stdDeviation = 0.0;
                return 0.0;
            } else {
                float oldMean = self.mean;
                self.sumValue += fVal;
                self.mean = self.sumValue / self.count;
                self.stdDeviation += (fVal - oldMean) * (fVal - self.mean);
                return math:sqrt(self.stdDeviation / self.count);
            }
        } else if (eventType == "EXPIRED") {
            self.count -= 1;
            if (self.count == 0) {
                self.sumValue = 0.0;
                self.mean = 0.0;
                self.stdDeviation = 0.0;
                return ();
            } else if (self.count == 1) {
                return 0.0;
            } else {
                float oldMean = self.mean;
                self.sumValue -= fVal;
                self.mean = self.stdDeviation / self.count;
                self.stdDeviation -= (fVal - oldMean) * (fVal - self.mean);
                return math:sqrt(self.stdDeviation / self.count);
            }
        } else if (eventType == "RESET") {
            self.mean = 0.0;
            self.stdDeviation = 0.0;
            self.sumValue = 0.0;
            self.count = 0;
            return 0.0;
        } else {
            return ();
        }
    }

    # Returns a copy of the `StdDev` aggregator.
    #
    # + return - A `Aggregator` object which represents `StdDev` aggregator.
    public function copy() returns Aggregator {
        StdDev stdDevAggregator = new();
        return stdDevAggregator;
    }

    # Return current state to be saved as a map of `any` typed values.
    #
    # + return - A map of `any` typed values.
    public function saveState() returns map<any> {
        return {
            "mean": self.mean,
            "stdDeviation": self.stdDeviation,
            "sumValue": self.sumValue,
            "count": self.count
        };
    }

    # Restores the saved state which is passed as a map of `any` typed values.
    #
    # + state - A map of typed `any` values. This map contains the values to be restored from the persisted data.
    public function restoreState(map<any> state) {
        any mean = state["mean"];
        if (mean is float) {
            self.mean = mean;
        }
        any stdDeviation = state["stdDeviation"];
        if (stdDeviation is float) {
            self.stdDeviation = stdDeviation;
        }
        any sumValue = state["sumValue"];
        if (sumValue is float) {
            self.sumValue = sumValue;
        }
        any c = state["count"];
        if (c is int) {
            self.count = c;
        }
    }
};

# Returns a `StdDev` aggregator object. The aggregator function name which is used in a streaming query should
# have the same name as this function's name.
#
# + return - A `Aggregator` which represents `StdDev`.
public function stdDev() returns Aggregator {
    StdDev stdDevAggregator = new();
    return stdDevAggregator;
}
