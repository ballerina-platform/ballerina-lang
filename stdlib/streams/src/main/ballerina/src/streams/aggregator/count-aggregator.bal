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

# Aggregator to count events in streams.
#
# + count - description
public type Count object {
    *Aggregator;
    *Snapshotable;
    public int count = 0;

    public function __init() {

    }

    # Updates the current count when a new event arrives and return the updated count. If the `eventType` is
    # `streams:CURRENT`, count is increase by 1. If the `eventType` is `streams:EXPIRED`, count is descreased by 1.
    # If the `eventType`is `streams:RESET`, count is reset, regardless the `value`.
    #
    # + value - In count aggregator the value is not used.
    # + eventType - Type of the incoming event `streams:CURRENT`, `streams:EXPIRED` or `streams:RESET`.
    #
    # + return - Updated count.
    public function process(anydata value, EventType eventType) returns anydata {
        if (eventType == "CURRENT") {
            self.count += 1;
        } else if (eventType == "EXPIRED"){
            self.count -= 1;
        } else if (eventType == "RESET"){
            self.count = 0;
        }
        return self.count;
    }

    # Returns a copy of the `Count` aggregator without its current state.
    #
    # + return - Returns `Count` aggregator.
    public function copy() returns Aggregator {
        Count countAggregator = new();
        return countAggregator;
    }

    # Return current state to be saved as a map of `any` typed values.
    #
    # + return - A map of `any` typed values.
    public function saveState() returns map<any> {
        return {
            "count": self.count
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
    }
};

# Returns a `Count` aggregator object. The aggregator function name which is used in a streaming query should have the
# same name as this function's name.
#
# + return - A `Aggregator` object which performs counting.
public function count() returns Aggregator {
    Count countAggregator = new();
    return countAggregator;
}
