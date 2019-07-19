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

# Aggregator to find the maximum value in a stream.
#
# + iMaxQueue - description
# + fMaxQueue- description
# + iMax - description
# + fMax - description
public type Max object {
    *Aggregator;
    *Snapshotable;
    public LinkedList iMaxQueue = new;
    public LinkedList fMaxQueue = new;
    public int? iMax = ();
    public float? fMax = ();

    public function __init() {

    }

    # Updates the current maximum value and return the updated maximum value.
    #
    # + value - Value being checked whether it is greater than the current maximum value.
    # + eventType - Type of the incoming event `streams:CURRENT`, `streams:EXPIRED` or `streams:RESET`.
    #
    # + return - Updated max value.
    public function process(anydata value, EventType eventType) returns anydata {

        if (value is int) {
            if (eventType == "CURRENT") {
                self.iMaxQueue.resetToRear();
                while (self.iMaxQueue.hasPrevious()) {
                    int a = <int>self.iMaxQueue.previous();
                    if (a < value) {
                        self.iMaxQueue.removeCurrent();
                    } else {
                        break;
                    }
                }
                self.iMaxQueue.addLast(value);
                int? iMax = self.iMax;
                if (iMax is int) {
                    self.iMax = (iMax < value) ? value : iMax;
                } else {
                    self.iMax = value;
                }
                return self.iMax;
            } else if (eventType == "EXPIRED"){
                self.iMaxQueue.resetToFront();
                while (self.iMaxQueue.hasNext()) {
                    int a = <int>self.iMaxQueue.next();
                    if (a == value) {
                        self.iMaxQueue.removeCurrent();
                        break;
                    }
                }
                self.iMax = <int>self.iMaxQueue.getFirst();
                return self.iMax;
            } else if (eventType == "RESET"){
                self.iMaxQueue.clear();
                self.iMax = ();
            }
            return self.iMax;
        } else if (value is float) {
            if (eventType == "CURRENT") {
                self.fMaxQueue.resetToRear();
                while (self.fMaxQueue.hasPrevious()) {
                    float a = <float>self.fMaxQueue.previous();
                    if (a < value) {
                        self.fMaxQueue.removeCurrent();
                    } else {
                        break;
                    }
                }
                self.fMaxQueue.addLast(value);
                float? fMax = self.fMax;
                if (fMax is float) {
                    self.fMax = (fMax < value) ? value : fMax;
                }
                else {
                    self.fMax = value;
                }
                return self.fMax;
            } else if (eventType == "EXPIRED"){
                self.fMaxQueue.resetToFront();
                while (self.fMaxQueue.hasNext()) {
                    float a = <float>self.fMaxQueue.next();
                    if (a == value) {
                        self.fMaxQueue.removeCurrent();
                        break;
                    }
                }
                self.fMax = <float>self.fMaxQueue.getFirst();
                return self.fMax;
            } else if (eventType == "RESET") {
                self.fMaxQueue.clear();
                self.fMax = ();
            }
            return self.fMax;
        } else if (value is ()) {
            //do nothing
        } else {
            error e = error("Unsupported attribute type found");
            panic e;
        }
    }

    # Returns a copy of the `Max` aggregator without its current state.
    #
    # + return - Returns `Max` aggregator.
    public function copy() returns Aggregator {
        Max maxAggregator = new();
        return maxAggregator;
    }

    # Return current state to be saved as a map of `any` typed values.
    #
    # + return - A map of `any` typed values.
    public function saveState() returns map<any> {
        any[] iMaxQ = self.iMaxQueue.asArray();
        any[] fMaxQ = self.fMaxQueue.asArray();
        return {
            "iMaxQ": iMaxQ,
            "fMaxQ": fMaxQ,
            "iMax": self.iMax,
            "fMax": self.fMax
        };
    }

    # Restores the saved state which is passed as a map of `any` typed values.
    #
    # + state - A map of typed `any` values. This map contains the values to be restored from the persisted data.
    public function restoreState(map<any> state) {
        any iMaxQ = state["iMaxQ"];
        if (iMaxQ is any[]) {
            self.iMaxQueue = new;
            self.iMaxQueue.addAll(iMaxQ);
        }
        any fMaxQ = state["fMaxQ"];
        if (fMaxQ is any[]) {
            self.fMaxQueue = new;
            self.fMaxQueue.addAll(fMaxQ);
        }
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

# Returns a `Max` aggregator object. The aggregator function name which is used in a streaming query should
# have the same name as this function's name.
#
# + return - A `Aggregator` which represents `Max`.
public function max() returns Aggregator {
    Max maxAggregator = new();
    return maxAggregator;
}
