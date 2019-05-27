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

# Aggregator to calculate average in streams.
#
# + count - description
# + sum - description
public type Average object {
    *Aggregator;
    *Snapshotable;
    public int count = 0;
    public float sum = 0.0;

    public function __init() {

    }

    # Returns the calculated average after `value` being aggregated into current average. If the `eventType` is
    # `streams:CURRENT`,`value` is added to the current sum and count is increase by 1. If the `eventType` is
    # `streams:EXPIRED`,  `value` is subtracted from the current sum and count is descreased by 1. If the `eventType`
    # is `streams:RESET`, Current  summation and count is reset, regardless the `value`. Then by dividing the sum by
    # count, the average is calculated.
    #
    # + value - The numeric value being aggregated in order to calculate the new average.
    # + eventType - Type of the incoming event `streams:CURRENT`, `streams:EXPIRED` or `streams:RESET`.
    #
    # + return - Updated average value after `value` being aggregated.
    public function process(anydata value, EventType eventType) returns anydata {
        if (value is int) {
            if (eventType == "CURRENT") {
                self.sum += value;
                self.count += 1;
            } else if (eventType == "EXPIRED"){
                self.sum -= value;
                self.count -= 1;
            } else if (eventType == "RESET"){
                self.sum = 0.0;
                self.count = 0;
            }
        } else if (value is float) {
            if (eventType == "CURRENT") {
                self.sum += value;
                self.count += 1;
            } else if (eventType == "EXPIRED"){
                self.sum -= value;
                self.count -= 1;
            } else if (eventType == "RESET"){
                self.sum = 0.0;
                self.count = 0;
            }
        } else if (value is ()) {
            //do nothing
        } else {
            error e = error("Unsupported attribute type found");
            panic e;
        }
        return (self.count > 0) ? (self.sum / self.count) : 0.0;
    }

    # Returns a copy of the `Average` aggregator without its current state.
    #
    # + return - Returns `Average` aggregator.
    public function copy() returns Aggregator {
        Average avgAggregator = new();
        return avgAggregator;
    }

    # Return current state to be saved as a map of `any` typed values.
    #
    # + return - A map of `any` typed values.
    public function saveState() returns map<any> {
        return {
            "count": self.count,
            "sum": self.sum
        };
    }

    # Restores the saved state which is passed as a map of `any` typed values.
    #
    # + state - A map of typed `any` values. This map contains the values to be restored from the persisted data.
    public function restoreState(map<any> state) {
        any c = state["count"];
        if (c is int) {
            self.count = c;
        }
        any s = state["sum"];
        if (s is float) {
            self.sum = s;
        }
    }
};

# Returns a `Average` aggregator object. The aggregator function name which is used in a streaming query should have the
# same name as this function's name.
#
# + return - A `Aggregator` object which performs averaging.
public function avg() returns Aggregator {
    Average avgAggregator = new();
    return avgAggregator;
}
