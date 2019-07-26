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

# Aggregator to perform summation of values in a stream.
#
# + iSum - description
# + fSum - description
public type Sum object {
    *Aggregator;
    *Snapshotable;
    public int iSum = 0;
    public float fSum = 0.0;

    public function __init() {

    }

    # Updates the current sum of numeric values based on the `eventType`. If the `eventType` is `streams:CURRENT`,
    # `value` is added to the current sum. If the `eventType` is `streams:EXPIRED`, `value` is subtracted from the
    # current sum. If the `eventType` is `streams:RESET`, Current summation will be reset, regardless the `value`.
    #
    # + value - The numeric value being aggregated to the current sum.
    # + eventType - Type of the incoming event `streams:CURRENT`, `streams:EXPIRED` or `streams:RESET`.
    #
    # + return - aggregated summation after the given `value`.
    public function process(anydata value, EventType eventType) returns anydata {

        if (value is int) {
            if (eventType == "CURRENT") {
                self.iSum += value;
            } else if (eventType == "EXPIRED"){
                self.iSum -= value;
            } else if (eventType == "RESET"){
                self.iSum = 0;
            }
            return self.iSum;
        } else if (value is float) {
            if (eventType == "CURRENT") {
                self.fSum += value;
            } else if (eventType == "EXPIRED"){
                self.fSum -= value;
            } else if (eventType == "RESET"){
                self.fSum = 0.0;
            }
            return self.fSum;
        } else if (value is ()) {
            //do nothing
        } else {
            error e = error("Unsupported attribute type found");
            panic e;
        }

    }

    # Returns a copy of the `Sum` aggregator without its current state.
    #
    # + return - Returns `Sum` aggregator.
    public function copy() returns Aggregator {
        Sum sumAggregator = new();
        return sumAggregator;
    }

    # Return current state to be saved as a map of `any` typed values.
    #
    # + return - A map of `any` typed values.
    public function saveState() returns map<any> {
        return {
            "iSum": self.iSum,
            "fSum": self.fSum
        };
    }

    # Restores the saved state which is passed as a map of `any` typed values.
    #
    # + state - A map of typed `any` values. This map contains the values to be restored from the persisted data.
    public function restoreState(map<any> state) {
        any iSum = state["iSum"];
        if (iSum is int) {
            self.iSum = iSum;
        }
        any fSum = state["fSum"];
        if (fSum is float) {
            self.fSum = fSum;
        }
    }
};

# Returns a `Sum` aggregator object. The aggregator function name which is used in a streaming query should have the
# same name as this function's name.
#
# + return - A `Aggregator` which perform addition/summation.
public function sum() returns Aggregator {
    Sum sumAggregator = new();
    return sumAggregator;
}
