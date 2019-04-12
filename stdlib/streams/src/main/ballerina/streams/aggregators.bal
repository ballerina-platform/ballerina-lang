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

import ballerina/crypto;
import ballerina/math;

# Abstract object, which should be implemented in order to create a new aggregator.
public type Aggregator abstract object {

    # Returns a copy of self, but it does not contain the current state.
    #
    # + return - A `Aggregator` object.
    public function copy() returns Aggregator;

    # Updates the aggregated value and returns the final aggregated value.
    #
    # + value - value being aggregated.
    # + eventType - Type of the incoming event `streams:CURRENT`, `streams:EXPIRED` or `streams:RESET`. Based on the
    #               type of the event `value` will be added to the aggregation or removed from the aggregation.
    #
    # + return - Updated aggregated value after `value` being aggregated.
    public function process(anydata value, EventType eventType) returns anydata;

};

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
        string key = crypto:crc32b(value);
        if (eventType == "CURRENT") {
            int preVal = self.distinctValues[key] ?: 0;
            preVal += 1;
            self.distinctValues[key] = preVal;
        } else if (eventType == "EXPIRED"){
            int preVal = self.distinctValues[key] ?: 1;
            preVal -= 1;
            if (preVal <= 0) {
                _ = self.distinctValues.remove(key);
            } else {
                self.distinctValues[key] = preVal;
            }
        } else if (eventType == "RESET"){
            self.distinctValues.clear();
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
