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
import ballerina/reflect;
import ballerina/crypto;
import ballerina/math;

public type Aggregator abstract object {

    public function copy() returns Aggregator;

    public function process(any value, EventType eventType) returns any|error;

};

public type Sum object {

    public int iSum = 0;
    public float fSum = 0.0;

    public new() {

    }

    public function process(any value, EventType eventType) returns any|error {
        match value {
            int i => {
                if (eventType == "CURRENT") {
                    self.iSum += i;
                } else if (eventType == "EXPIRED"){
                    self.iSum -= i;
                } else if (eventType == "RESET"){
                    self.iSum = 0;
                }
                return self.iSum;
            }
            float f => {
                if (eventType == "CURRENT") {
                    self.fSum += f;
                } else if (eventType == "EXPIRED"){
                    self.fSum -= f;
                } else if (eventType == "RESET"){
                    self.fSum = 0.0;
                }
                return self.fSum;
            }
            any a => {
                error e = error("Unsupported attribute type found");
                return e;
            }
        }
    }

    public function copy() returns Aggregator {
        Sum sumAggregator = new();
        return sumAggregator;
    }

};

public function sum() returns Aggregator {
    Sum sumAggregator = new();
    return sumAggregator;
}

public type Average object {

    public int count = 0;
    public float sum = 0.0;

    public new() {

    }

    public function process(any value, EventType eventType) returns any|error {
        match value {
            int i => {
                if (eventType == "CURRENT") {
                    self.sum += i;
                    self.count += 1;
                } else if (eventType == "EXPIRED"){
                    self.sum -= i;
                    self.count -= 1;
                } else if (eventType == "RESET"){
                    self.sum = 0.0;
                    self.count = 0;
                }
            }
            float f => {
                if (eventType == "CURRENT") {
                    self.sum += f;
                    self.count += 1;
                } else if (eventType == "EXPIRED"){
                    self.sum -= f;
                    self.count -= 1;
                } else if (eventType == "RESET"){
                    self.sum = 0.0;
                    self.count = 0;
                }
            }
            any a => {
                error e = error("Unsupported attribute type found");
                return e;
            }
        }
        return (self.count > 0) ? (self.sum / self.count) : 0.0;
    }

    public function copy() returns Aggregator {
        Average avgAggregator = new();
        return avgAggregator;
    }

};

public function avg() returns Aggregator {
    Average avgAggregator = new();
    return avgAggregator;
}

public type Count object {

    public int count = 0;

    public new() {

    }

    public function process(any value, EventType eventType) returns any|error {
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

};

public function count() returns Aggregator {
    Count countAggregator = new();
    return countAggregator;
}

public type DistinctCount object {

    public map<int> distinctValues = {};

    public new() {

    }

    public function process(any value, EventType eventType) returns any|error {
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
};

public function distinctCount() returns Aggregator {
    DistinctCount distinctCountAggregator = new();
    return distinctCountAggregator;
}


public type Max object {

    public LinkedList iMaxQueue = new;
    public LinkedList fMaxQueue = new;
    public int? iMax = ();
    public float? fMax = ();

    public new() {

    }

    public function process(any value, EventType eventType) returns any|error {
        match value {
            int i => {
                if (eventType == "CURRENT") {
                    self.iMaxQueue.resetToRear();
                    while (self.iMaxQueue.hasPrevious()) {
                        int a = check <int>self.iMaxQueue.previous();
                        if (a < i) {
                            self.iMaxQueue.removeCurrent();
                        } else {
                            break;
                        }
                    }
                    self.iMaxQueue.addLast(i);
                    match self.iMax {
                        int tempMax => {
                            self.iMax = (tempMax < i) ? i : tempMax;
                        }
                        () => {
                            self.iMax = i;
                        }
                    }
                    return self.iMax;
                } else if (eventType == "EXPIRED"){
                    self.iMaxQueue.resetToFront();
                    while (self.iMaxQueue.hasNext()) {
                        int a = check <int>self.iMaxQueue.next();
                        if (a == i) {
                            self.iMaxQueue.removeCurrent();
                            break;
                        }
                    }
                    self.iMax = check <int>self.iMaxQueue.getFirst();
                    return self.iMax;
                } else if (eventType == "RESET"){
                    self.iMaxQueue.clear();
                    self.iMax = ();
                }
                return self.iMax;
            }
            float f => {
                if (eventType == "CURRENT") {
                    self.fMaxQueue.resetToRear();
                    while (self.fMaxQueue.hasPrevious()) {
                        float a = check <float>self.fMaxQueue.previous();
                        if (a < f) {
                            self.fMaxQueue.removeCurrent();
                        } else {
                            break;
                        }
                    }
                    self.fMaxQueue.addLast(f);
                    match self.fMax {
                        float tempMax => {
                            self.fMax = (tempMax < f) ? f : tempMax;
                        }
                        () => {
                            self.fMax = f;
                        }
                    }
                    return self.fMax;
                } else if (eventType == "EXPIRED"){
                    self.fMaxQueue.resetToFront();
                    while (self.fMaxQueue.hasNext()) {
                        float a = check <float>self.fMaxQueue.next();
                        if (a == f) {
                            self.fMaxQueue.removeCurrent();
                            break;
                        }
                    }
                    self.fMax = check <float>self.fMaxQueue.getFirst();
                    return self.fMax;
                } else if (eventType == "RESET"){
                    self.fMaxQueue.clear();
                    self.fMax = ();
                }
                return self.fMax;
            }
            any a => {
                error e = error("Unsupported attribute type found");
                return e;
            }
        }
    }

    public function copy() returns Aggregator {
        Max maxAggregator = new();
        return maxAggregator;
    }

};

public function max() returns Aggregator {
    Max maxAggregator = new();
    return maxAggregator;
}


public type Min object {

    public LinkedList iMinQueue = new;
    public LinkedList fMinQueue = new;
    public int? iMin = ();
    public float? fMin = ();

    public new() {

    }

    public function process(any value, EventType eventType) returns any|error {
        match value {
            int i => {
                if (eventType == "CURRENT") {
                    self.iMinQueue.resetToRear();
                    while (self.iMinQueue.hasPrevious()) {
                        int a = check <int>self.iMinQueue.previous();
                        if (a > i) {
                            self.iMinQueue.removeCurrent();
                        } else {
                            break;
                        }
                    }
                    self.iMinQueue.addLast(i);
                    match self.iMin {
                        int tempMin => {
                            self.iMin = (tempMin > i) ? i : tempMin;
                        }
                        () => {
                            self.iMin = i;
                        }
                    }
                    return self.iMin;
                } else if (eventType == "EXPIRED"){
                    self.iMinQueue.resetToFront();
                    while (self.iMinQueue.hasNext()) {
                        int a = check <int>self.iMinQueue.next();
                        if (a == i) {
                            self.iMinQueue.removeCurrent();
                            break;
                        }
                    }
                    self.iMin = check <int>self.iMinQueue.getFirst();
                    return self.iMin;
                } else if (eventType == "RESET"){
                    self.iMinQueue.clear();
                    self.iMin = ();
                }
                return self.iMin;
            }
            float f => {
                if (eventType == "CURRENT") {
                    self.fMinQueue.resetToRear();
                    while (self.fMinQueue.hasPrevious()) {
                        float a = check <float>self.fMinQueue.previous();
                        if (a > f) {
                            self.fMinQueue.removeCurrent();
                        } else {
                            break;
                        }
                    }
                    self.fMinQueue.addLast(f);
                    match self.fMin {
                        float tempMin => {
                            self.fMin = (tempMin > f) ? f : tempMin;
                        }
                        () => {
                            self.fMin = f;
                        }
                    }
                    return self.fMin;
                } else if (eventType == "EXPIRED"){
                    self.fMinQueue.resetToFront();
                    while (self.fMinQueue.hasNext()) {
                        float a = check <float>self.fMinQueue.next();
                        if (a == f) {
                            self.fMinQueue.removeCurrent();
                            break;
                        }
                    }
                    self.fMin = check <float>self.fMinQueue.getFirst();
                    return self.fMin;
                } else if (eventType == "RESET"){
                    self.fMinQueue.clear();
                    self.fMin = ();
                }
                return self.fMin;
            }
            any a => {
                error e = error("Unsupported attribute type found");
                return e;
            }
        }
    }

    public function copy() returns Aggregator {
        Min minAggregator = new();
        return minAggregator;
    }

};

public function min() returns Aggregator {
    Min minAggregator = new();
    return minAggregator;
}



public type StdDev object {

    public float mean = 0.0;
    public float stdDeviation = 0.0;
    public float sumValue = 0.0;
    public int count = 0;

    public new() {

    }

    public function process(any value, EventType eventType) returns any|error {
        float fVal;
        match value {
            int i => {
                fVal = <float>i;
            }
            float f => {
                fVal = f;
            }
            any a => {
                error e = error("Unsupported attribute type found");
                return e;
            }
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
                self.mean = self.stdDeviation / self.count;
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

};

public function stdDev() returns Aggregator {
    StdDev stdDevAggregator = new();
    return stdDevAggregator;
}

public type MaxForever object {

    public int? iMax = ();
    public float? fMax = ();

    public new() {

    }

    public function process(any value, EventType eventType) returns any|error {
        match value {
            int i => {
                if (eventType == "CURRENT" || eventType == "EXPIRED") {
                    match self.iMax {
                        int tempMax => {
                            self.iMax = (tempMax < i) ? i : tempMax;
                        }
                        () => {
                            self.iMax = i;
                        }
                    }
                }
                return self.iMax;
            }
            float f => {
                if (eventType == "CURRENT" || eventType == "EXPIRED") {
                    match self.fMax {
                        float tempMax => {
                            self.fMax = (tempMax < f) ? f : tempMax;
                        }
                        () => {
                            self.fMax = f;
                        }
                    }
                }
                return self.fMax;
            }
            any a => {
                error e = error("Unsupported attribute type found");
                return e;
            }
        }
    }

    public function copy() returns Aggregator {
        MaxForever maxForeverAggregator = new();
        return maxForeverAggregator;
    }

};

public function maxForever() returns Aggregator {
    MaxForever maxForeverAggregator = new();
    return maxForeverAggregator;
}

public type MinForever object {

    public int? iMin = ();
    public float? fMin = ();

    public new() {

    }

    public function process(any value, EventType eventType) returns any|error {
        match value {
            int i => {
                if (eventType == "CURRENT" || eventType == "EXPIRED") {
                    match self.iMin {
                        int tempMin => {
                            self.iMin = (tempMin > i) ? i : tempMin;
                        }
                        () => {
                            self.iMin = i;
                        }
                    }
                }
                return self.iMin;
            }
            float f => {
                if (eventType == "CURRENT" || eventType == "EXPIRED") {
                    match self.fMin {
                        float tempMin => {
                            self.fMin = (tempMin > f) ? f : tempMin;
                        }
                        () => {
                            self.fMin = f;
                        }
                    }
                }
                return self.fMin;
            }
            any a => {
                error e = error("Unsupported attribute type found");
                return e;
            }
        }
    }

    public function copy() returns Aggregator {
        MinForever minForeverAggregator = new();
        return minForeverAggregator;
    }

};

public function minForever() returns Aggregator {
    MinForever minForeverAggregator = new();
    return minForeverAggregator;
}
