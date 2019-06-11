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

# The aggregator to keep the minimum value received so far. It is similar to `Min` aggregator, but it keeps the minimum
# value it received so far, forever.
#
# + iMin - description
# + fMin - description
public type MinForever object {
    *Aggregator;
    *Snapshotable;
    public int? iMin = ();
    public float? fMin = ();

    public function __init() {

    }

    # Updates the current minimum value and return the updated minimum value.
    #
    # + value - Value being checked whether it is lesser than the current minimum value.
    # + eventType - Type of the incoming event `streams:CURRENT`, `streams:EXPIRED` or `streams:RESET`.
    #
    # + return - Updated minimum value.
    public function process(anydata value, EventType eventType) returns anydata {

        if (value is int) {
            if (eventType == "CURRENT" || eventType == "EXPIRED") {
                int? iMin = self.iMin;
                if (iMin is int) {
                    self.iMin = (iMin > value) ? value : iMin;
                } else {
                    self.iMin = value;
                }
            }
            return self.iMin;
        } else if (value is float) {
            if (eventType == "CURRENT" || eventType == "EXPIRED") {
                float? fMin = self.fMin;
                if (fMin is float) {
                    self.fMin = (fMin > value) ? value : fMin;
                } else {
                    self.fMin = value;
                }

            }
            return self.fMin;
        } else if (value is ()) {
            //do nothing
        } else {
            error e = error("Unsupported attribute type found");
            panic e;
        }
    }

    # Returns a copy of the `MinForever` aggregator.
    #
    # + return - A `Aggregator` object which represents `MinForever` aggregator.
    public function copy() returns Aggregator {
        MinForever minForeverAggregator = new();
        return minForeverAggregator;
    }

    # Return current state to be saved as a map of `any` typed values.
    #
    # + return - A map of `any` typed values.
    public function saveState() returns map<any> {
        return {
            "iMin": self.iMin,
            "fMin": self.fMin
        };
    }

    # Restores the saved state which is passed as a map of `any` typed values.
    #
    # + state - A map of typed `any` values. This map contains the values to be restored from the persisted data.
    public function restoreState(map<any> state) {
        any iMin = state["iMin"];
        if (iMin is int) {
            self.iMin = iMin;
        }
        any fMin = state["fMin"];
        if (fMin is float) {
            self.fMin = fMin;
        }
    }
};

# Returns a `MinForever` aggregator object. The aggregator function name which is used in a streaming query should
# have the same name as this function's name.
#
# + return - A `Aggregator` which represents `MainForever`.
public function minForever() returns Aggregator {
    MinForever minForeverAggregator = new();
    return minForeverAggregator;
}
