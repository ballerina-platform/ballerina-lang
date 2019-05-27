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

# The aggregator to keep the maximum value received so far. It is similar to `Max` aggregator, but it keeps the maximum
# value it received so far, forever.
#
# + iMax - description
# + fMax - description
public type MaxForever object {
    *Aggregator;
    *Snapshotable;
    public int? iMax = ();
    public float? fMax = ();

    public function __init() {

    }

    # Updates the current maximum value and return the updated maximum value.
    #
    # + value - Value being checked whether it is greater than the current maximum value.
    # + eventType - Type of the incoming event `streams:CURRENT`, `streams:EXPIRED` or `streams:RESET`.
    #
    # + return - Updated maximum value.
    public function process(anydata value, EventType eventType) returns anydata {

        if (value is int) {
            if (eventType == "CURRENT" || eventType == "EXPIRED") {
                int? iMax = self.iMax;
                if (iMax is int) {
                    self.iMax = (iMax < value) ? value : iMax;
                } else {
                    self.iMax = value;
                }
            }
            return self.iMax;
        }
        else if (value is float) {
            if (eventType == "CURRENT" || eventType == "EXPIRED") {
                float? fMax = self.fMax;
                if (fMax is float) {
                    self.fMax = (fMax < value) ? value : fMax;
                } else {
                    self.fMax = value;
                }
            }
            return self.fMax;
        } else if (value is ()) {
            //do nothing
        } else {
            error e = error("Unsupported attribute type found");
            panic e;
        }
    }

    # Returns a copy of the `MaxForever` aggregator.
    #
    # + return - A `Aggregator` object which represents `MaxForever` aggregator.
    public function copy() returns Aggregator {
        MaxForever maxForeverAggregator = new();
        return maxForeverAggregator;
    }

    # Return current state to be saved as a map of `any` typed values.
    #
    # + return - A map of `any` typed values.
    public function saveState() returns map<any> {
        return {
            "iMax": self.iMax,
            "fMax": self.fMax
        };
    }

    # Restores the saved state which is passed as a map of `any` typed values.
    #
    # + state - A map of typed `any` values. This map contains the values to be restored from the persisted data.
    public function restoreState(map<any> state) {
        any iMax = state["iMax"];
        if (iMax is int) {
            self.iMax = iMax;
        }
        any fMax = state["fMax"];
        if (fMax is float) {
            self.fMax = fMax;
        }
    }
};

# Returns a `MaxForever` aggregator object. The aggregator function name which is used in a streaming query should
# have the same name as this function's name.
#
# + return - A `Aggregator` which represents `MaxForever`.
public function maxForever() returns Aggregator {
    MaxForever maxForeverAggregator = new();
    return maxForeverAggregator;
}
