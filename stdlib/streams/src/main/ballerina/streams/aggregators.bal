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

public type Aggregator abstract object {

    public function copy() returns Aggregator;

    public function process(anydata value, EventType eventType) returns anydata;

};

public type Sum object {
    *Aggregator;
    *Snapshotable;
    public int iSum = 0;
    public float fSum = 0.0;

    public function __init() {

    }

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
        } else {
            error e = error("Unsupported attribute type found");
            panic e;
        }
    }

    public function copy() returns Aggregator {
        Sum sumAggregator = new();
        return sumAggregator;
    }

    public function saveState() returns map<any> {
        return {
            "iSum": self.iSum,
            "fSum": self.fSum
        };
    }

    public function restoreState(map<any> state) {
        any? iSum = state["iSum"];
        if (iSum is int) {
            self.iSum = iSum;
        }
        any? fSum = state["fSum"];
        if (fSum is float) {
            self.fSum = fSum;
        }
    }
};

public function sum() returns Aggregator {
    Sum sumAggregator = new();
    return sumAggregator;
}

public type Average object {
    *Aggregator;
    *Snapshotable;
    public int count = 0;
    public float sum = 0.0;

    public function __init() {

    }

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
        } else {
            error e = error("Unsupported attribute type found");
            panic e;
        }
        return (self.count > 0) ? (self.sum / self.count) : 0.0;
    }

    public function copy() returns Aggregator {
        Average avgAggregator = new();
        return avgAggregator;
    }

    public function saveState() returns map<any> {
        return {
            "count": self.count,
            "sum": self.sum
        };
    }

    public function restoreState(map<any> state) {
        any? c = state["count"];
        if (c is int) {
            self.count = c;
        }
        any? s = state["sum"];
        if (s is float) {
            self.sum = s;
        }
    }
};

public function avg() returns Aggregator {
    Average avgAggregator = new();
    return avgAggregator;
}

public type Count object {
    *Aggregator;
    *Snapshotable;
    public int count = 0;

    public function __init() {

    }

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

    public function copy() returns Aggregator {
        Count countAggregator = new();
        return countAggregator;
    }

    public function saveState() returns map<any> {
        return {
            "count": self.count
        };
    }

    public function restoreState(map<any> state) {
        any? c = state["count"];
        if (c is int) {
            self.count = c;
        }
    }
};

public function count() returns Aggregator {
    Count countAggregator = new();
    return countAggregator;
}

public type DistinctCount object {
    *Aggregator;
    *Snapshotable;
    public map<int> distinctValues = {};

    public function __init() {

    }

    public function process(anydata value, EventType eventType) returns anydata {
        string key = crypto:crc32(value);
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

    public function copy() returns Aggregator {
        DistinctCount distinctCountAggregator = new();
        return distinctCountAggregator;
    }

    public function saveState() returns map<any> {
        return {
            "distinctValues": self.distinctValues
        };
    }

    public function restoreState(map<any> state) {
        any? distinctValues = state["distinctValues"];
        if (distinctValues is map<int>) {
            self.distinctValues = distinctValues;
        }
    }
};

public function distinctCount() returns Aggregator {
    DistinctCount distinctCountAggregator = new();
    return distinctCountAggregator;
}


public type Max object {
    *Aggregator;
    *Snapshotable;
    public LinkedList iMaxQueue = new;
    public LinkedList fMaxQueue = new;
    public int? iMax = ();
    public float? fMax = ();

    public function __init() {

    }

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
        } else if (value is float){
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
        } else {
            error e = error("Unsupported attribute type found");
            panic e;
        }
    }

    public function copy() returns Aggregator {
        Max maxAggregator = new();
        return maxAggregator;
    }

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

    public function restoreState(map<any> state) {
        any? iMaxQ = state["iMaxQ"];
        if (iMaxQ is any[]) {
            self.iMaxQueue = new;
            self.iMaxQueue.addAll(iMaxQ);
        }
        any? fMaxQ = state["fMaxQ"];
        if (fMaxQ is any[]) {
            self.fMaxQueue = new;
            self.fMaxQueue.addAll(fMaxQ);
        }
        any? iMax = state["iMax"];
        if (iMax is int) {
            self.iMax = iMax;
        }
        any? fMax = state["fMax"];
        if (fMax is float) {
            self.fMax = fMax;
        }
    }
};

public function max() returns Aggregator {
    Max maxAggregator = new();
    return maxAggregator;
}


public type Min object {
    *Aggregator;
    *Snapshotable;
    public LinkedList iMinQueue = new;
    public LinkedList fMinQueue = new;
    public int? iMin = ();
    public float? fMin = ();

    public function __init() {

    }

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
        } else {
            error e = error("Unsupported attribute type found");
            panic e;
        }
    }

    public function copy() returns Aggregator {
        Min minAggregator = new();
        return minAggregator;
    }

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

    public function restoreState(map<any> state) {
        any? iMinQ = state["iMinQ"];
        if (iMinQ is any[]) {
            self.iMinQueue = new;
            self.iMinQueue.addAll(iMinQ);
        }
        any? fMinQ = state["fMinQ"];
        if (fMinQ is any[]) {
            self.fMinQueue = new;
            self.fMinQueue.addAll(fMinQ);
        }
        any? iMin = state["iMin"];
        if (iMin is int) {
            self.iMin = iMin;
        }
        any? fMin = state["fMin"];
        if (fMin is float) {
            self.fMin = fMin;
        }
    }
};

public function min() returns Aggregator {
    Min minAggregator = new();
    return minAggregator;
}


public type StdDev object {
    *Aggregator;
    *Snapshotable;
    public float mean = 0.0;
    public float stdDeviation = 0.0;
    public float sumValue = 0.0;
    public int count = 0;

    public function __init() {

    }

    public function process(anydata value, EventType eventType) returns anydata {
        float fVal;
        if (value is int) {
            fVal = <float>value;
        } else if (value is float) {
            fVal = value;
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

    public function copy() returns Aggregator {
        StdDev stdDevAggregator = new();
        return stdDevAggregator;
    }

    public function saveState() returns map<any> {
        return {
            "mean": self.mean,
            "stdDeviation": self.stdDeviation,
            "sumValue": self.sumValue,
            "count": self.count
        };
    }

    public function restoreState(map<any> state) {
        any? mean = state["mean"];
        if (mean is float) {
            self.mean = mean;
        }
        any? stdDeviation = state["stdDeviation"];
        if (stdDeviation is float) {
            self.stdDeviation = stdDeviation;
        }
        any? sumValue = state["sumValue"];
        if (sumValue is float) {
            self.sumValue = sumValue;
        }
        any? c = state["count"];
        if (c is int) {
            self.count = c;
        }
    }
};

public function stdDev() returns Aggregator {
    StdDev stdDevAggregator = new();
    return stdDevAggregator;
}

public type MaxForever object {
    *Aggregator;
    *Snapshotable;
    public int? iMax = ();
    public float? fMax = ();

    public function __init() {

    }

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
        } else {
            error e = error("Unsupported attribute type found");
            panic e;
        }
    }

    public function copy() returns Aggregator {
        MaxForever maxForeverAggregator = new();
        return maxForeverAggregator;
    }

    public function saveState() returns map<any> {
        return {
            "iMax": self.iMax,
            "fMax": self.fMax
        };
    }

    public function restoreState(map<any> state) {
        any? iMax = state["iMax"];
        if (iMax is int) {
            self.iMax = iMax;
        }
        any? fMax = state["fMax"];
        if (fMax is float) {
            self.fMax = fMax;
        }
    }
};

public function maxForever() returns Aggregator {
    MaxForever maxForeverAggregator = new();
    return maxForeverAggregator;
}

public type MinForever object {
    *Aggregator;
    *Snapshotable;
    public int? iMin = ();
    public float? fMin = ();

    public function __init() {

    }

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
        } else {
            error e = error("Unsupported attribute type found");
            panic e;
        }
    }

    public function copy() returns Aggregator {
        MinForever minForeverAggregator = new();
        return minForeverAggregator;
    }

    public function saveState() returns map<any> {
        return {
            "iMin": self.iMin,
            "fMin": self.fMin
        };
    }

    public function restoreState(map<any> state) {
        any? iMin = state["iMin"];
        if (iMin is int) {
            self.iMin = iMin;
        }
        any? fMin = state["fMin"];
        if (fMin is float) {
            self.fMin = fMin;
        }
    }
};

public function minForever() returns Aggregator {
    MinForever minForeverAggregator = new();
    return minForeverAggregator;
}
