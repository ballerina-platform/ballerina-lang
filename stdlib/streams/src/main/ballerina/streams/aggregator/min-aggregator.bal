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

# Aggregator to find the minimum value in a stream.
#
# + iMinQueue - description
# + fMinQueue - description
# + iMin - description
# + fMin - description
public type Min object {
    *Aggregator;
    *Snapshotable;
    public LinkedList iMinQueue = new;
    public LinkedList fMinQueue = new;
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
            if (eventType == "CURRENT") {
                self.iMinQueue.resetToRear();
                while (self.iMinQueue.hasPrevious()) {
                    int a = <int>self.iMinQueue.previous();
                    if (a > value) {
                        self.iMinQueue.removeCurrent();
                    } else {
                        break;
                    }
                }
                self.iMinQueue.addLast(value);
                int? iMin = self.iMin;
                if (iMin is int) {
                    self.iMin = (iMin > value) ? value : iMin;
                } else {
                    self.iMin = value;
                }
                return self.iMin;
            } else if (eventType == "EXPIRED"){
                self.iMinQueue.resetToFront();
                while (self.iMinQueue.hasNext()) {
                    int a = <int>self.iMinQueue.next();
                    if (a == value) {
                        self.iMinQueue.removeCurrent();
                        break;
                    }
                }
                self.iMin = <int>self.iMinQueue.getFirst();
                return self.iMin;
            } else if (eventType == "RESET") {
                self.iMinQueue.clear();
                self.iMin = ();
            }
            return self.iMin;
        } else if (value is float) {
            if (eventType == "CURRENT") {
                self.fMinQueue.resetToRear();
                while (self.fMinQueue.hasPrevious()) {
                    float a = <float>self.fMinQueue.previous();
                    if (a > value) {
                        self.fMinQueue.removeCurrent();
                    } else {
                        break;
                    }
                }
                self.fMinQueue.addLast(value);
                float? fMin = self.fMin;
                if (fMin is float) {
                    self.fMin = (fMin > value) ? value : fMin;
                } else {
                    self.fMin = value;
                }
                return self.fMin;
            } else if (eventType == "EXPIRED") {
                self.fMinQueue.resetToFront();
                while (self.fMinQueue.hasNext()) {
                    float a = <float>self.fMinQueue.next();
                    if (a == value) {
                        self.fMinQueue.removeCurrent();
                        break;
                    }
                }
                self.fMin = <float>self.fMinQueue.getFirst();
                return self.fMin;
            } else if (eventType == "RESET") {
                self.fMinQueue.clear();
                self.fMin = ();
            }
            return self.fMin;
        } else if (value is ()) {
            //do nothing
        } else {
            error e = error("Unsupported attribute type found");
            panic e;
        }
    }

    # Returns a copy of the `Min` aggregator.
    #
    # + return - A `Aggregator` object which represents `Min` aggregator.
    public function copy() returns Aggregator {
        Min minAggregator = new();
        return minAggregator;
    }

    # Return current state to be saved as a map of `any` typed values.
    #
    # + return - A map of `any` typed values.
    public function saveState() returns map<any> {
        any[] iMinQ = self.iMinQueue.asArray();
        any[] fMinQ = self.fMinQueue.asArray();
        return {
            "iMinQ": iMinQ,
            "fMinQ": fMinQ,
            "iMin": self.iMin,
            "fMin": self.fMin
        };
    }

    # Restores the saved state which is passed as a map of `any` typed values.
    #
    # + state - A map of typed `any` values. This map contains the values to be restored from the persisted data.
    public function restoreState(map<any> state) {
        any iMinQ = state["iMinQ"];
        if (iMinQ is any[]) {
            self.iMinQueue = new;
            self.iMinQueue.addAll(iMinQ);
        }
        any fMinQ = state["fMinQ"];
        if (fMinQ is any[]) {
            self.fMinQueue = new;
            self.fMinQueue.addAll(fMinQ);
        }
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

# Returns a `Min` aggregator object. The aggregator function name which is used in a streaming query should
# have the same name as this function's name.
#
# + return - A `Aggregator` which represents `Min`.
public function min() returns Aggregator {
    Min minAggregator = new();
    return minAggregator;
}
