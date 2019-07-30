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

import ballerina/crypto;

# Aggregator to get the distinct counts of values in streams.
#
# + distinctValues - description
public type DistinctCount object {
    *Aggregator;
    *Snapshotable;
    public map<int> distinctValues = {};

    public function __init() {

    }

    # Updates the current distinct count when a new event arrives and return the updated count. If the `eventType` is
    # `streams:CURRENT`, count is increased by 1. If the `eventType` is `streams:EXPIRED`, count is descreased by 1.
    # If the `eventType`is `streams:RESET`, count is reset, regardless of the `value`.
    #
    # + value - Value being counted uniquely.
    # + eventType - Type of the incoming event `streams:CURRENT`, `streams:EXPIRED` or `streams:RESET`.
    #
    # + return - Updated distinct count.
    public function process(anydata value, EventType eventType) returns anydata {
        string key = crypto:crc32b(value.toString().toBytes());
        if (eventType == "CURRENT") {
            int preVal = self.distinctValues[key] ?: 0;
            preVal += 1;
            self.distinctValues[key] = preVal;
        } else if (eventType == "EXPIRED"){
            int preVal = self.distinctValues[key] ?: 1;
            preVal -= 1;
            if (preVal <= 0) {
                var tempVar = self.distinctValues.remove(key);
            } else {
                self.distinctValues[key] = preVal;
            }
        } else if (eventType == "RESET"){
            self.distinctValues.removeAll();
        }
        return self.distinctValues.length();
    }

    # Returns a copy of the `DistinctCount` aggregator without its current state.
    #
    # + return - Returns `DistinctCount` aggregator.
    public function copy() returns Aggregator {
        DistinctCount distinctCountAggregator = new();
        return distinctCountAggregator;
    }

    # Return current state to be saved as a map of `any` typed values.
    #
    # + return - A map of `any` typed values.
    public function saveState() returns map<any> {
        return {
            "distinctValues": self.distinctValues
        };
    }

    # Restores the saved state which is passed as a map of `any` typed values.
    #
    # + state - A map of typed `any` values. This map contains the values to be restored from the persisted data.
    public function restoreState(map<any> state) {
        any distinctValues = state["distinctValues"];
        if (distinctValues is map<int>) {
            self.distinctValues = distinctValues;
        }
    }
};

# Returns a `DistinctCount` aggregator object. The aggregator function name which is used in a streaming query should
# have the same name as this function's name.
#
# + return - A `Aggregator` object which represents DistinctCount.
public function distinctCount() returns Aggregator {
    DistinctCount distinctCountAggregator = new();
    return distinctCountAggregator;
}

