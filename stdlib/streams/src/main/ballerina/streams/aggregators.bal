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

public type Aggregator object {

    public new() {

    }

    public function clone() returns Aggregator {
        Aggregator aggregator = new();
        return aggregator;
    }

    public function process(any value, EventType eventType) returns any {
        match value {
            int i => {
                return 0;
            }
            float f => {
                return 0.0;
            }
            any a => {
                error e = { message: "Unsupported attribute type found" };
                return e;
            }
        }
    }
};

public type Sum object {

    public int iSum = 0;
    public float fSum = 0.0;

    public new() {

    }

    public function process(any value, EventType eventType) returns any {
        match value {
            int i => {
                if (eventType == "CURRENT") {
                    iSum += i;
                } else if (eventType == "EXPIRED"){
                    iSum -= i;
                } else if (eventType == "RESET"){
                    iSum = 0;
                }
                return iSum;
            }
            float f => {
                if (eventType == "CURRENT") {
                    fSum += f;
                } else if (eventType == "EXPIRED"){
                    fSum -= f;
                } else if (eventType == "RESET"){
                    fSum = 0.0;
                }
                return fSum;
            }
            any a => {
                error e = { message: "Unsupported attribute type found" };
                return e;
            }
        }
    }

    public function clone() returns Aggregator {
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

    public function process(any value, EventType eventType) returns any {
        match value {
            int i => {
                if (eventType == "CURRENT") {
                    sum += i;
                    count++;
                } else if (eventType == "EXPIRED"){
                    sum -= i;
                    count--;
                } else if (eventType == "RESET"){
                    sum = 0.0;
                    count = 0;
                }
            }
            float f => {
                if (eventType == "CURRENT") {
                    sum += f;
                    count++;
                } else if (eventType == "EXPIRED"){
                    sum -= f;
                    count--;
                } else if (eventType == "RESET"){
                    sum = 0.0;
                    count = 0;
                }
            }
            any a => {
                error e = { message: "Unsupported attribute type found" };
                return e;
            }
        }
        return (count > 0) ? (sum / count) : 0.0;
    }

    public function clone() returns Aggregator {
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

    public function process(any value, EventType eventType) returns any {
        if (eventType == "CURRENT") {
            count++;
        } else if (eventType == "EXPIRED"){
            count--;
        } else if (eventType == "RESET"){
            count = 0;
        }
        return count;
    }

    public function clone() returns Aggregator {
        Count countAggregator = new();
        return countAggregator;
    }

};

public function count() returns Aggregator {
    Count countAggregator = new();
    return countAggregator;
}

public type DistinctCount object {

    public map<int> distinctValues;

    public new() {

    }

    public function process(any value, EventType eventType) returns any {
        string key = crypto:crc32(value);
        if (eventType == "CURRENT") {
            int preVal = distinctValues[key] ?: 0;
            preVal++;
            distinctValues[key] = preVal;
        } else if (eventType == "EXPIRED"){
            int preVal = distinctValues[key] ?: 1;
            preVal--;
            if (preVal <= 0) {
                _ = distinctValues.remove(key);
            } else {
                distinctValues[key] = preVal;
            }
        } else if (eventType == "RESET"){
            distinctValues.clear();
        }
        return lengthof distinctValues;
    }

    public function clone() returns Aggregator {
        DistinctCount distinctCountAggregator = new();
        return distinctCountAggregator;
    }
};

public function distinctCount() returns Aggregator {
    DistinctCount distinctCountAggregator = new();
    return distinctCountAggregator;
}


public type Max object {

    public LinkedList iMaxQueue;
    public LinkedList fMaxQueue;
    public int? iMax = ();
    public float? fMax = ();

    public new() {

    }

    public function process(any value, EventType eventType) returns any {
        match value {
            int i => {
                if (eventType == "CURRENT") {
                    iMaxQueue.resetToRear();
                    while (iMaxQueue.hasPrevious()) {
                        int a = check <int>iMaxQueue.previous();
                        if (a < i) {
                            iMaxQueue.removeCurrent();
                        } else {
                            break;
                        }
                    }
                    iMaxQueue.addLast(i);
                    match iMax {
                        int tempMax => {
                            iMax = (tempMax < i) ? i : tempMax;
                        }
                        () => {
                            iMax = i;
                        }
                    }
                    return iMax;
                } else if (eventType == "EXPIRED"){
                    iMaxQueue.resetToFront();
                    while (iMaxQueue.hasNext()) {
                        int a = check <int>iMaxQueue.next();
                        if (a == i) {
                            iMaxQueue.removeCurrent();
                            break;
                        }
                    }
                    iMax = check <int>iMaxQueue.getFirst();
                    return iMax;
                } else if (eventType == "RESET"){
                    iMaxQueue.clear();
                    iMax = ();
                }
                return iMax;
            }
            float f => {
                if (eventType == "CURRENT") {
                    fMaxQueue.resetToRear();
                    while (fMaxQueue.hasPrevious()) {
                        float a = check <float>fMaxQueue.previous();
                        if (a < f) {
                            fMaxQueue.removeCurrent();
                        } else {
                            break;
                        }
                    }
                    fMaxQueue.addLast(f);
                    match fMax {
                        float tempMax => {
                            fMax = (tempMax < f) ? f : tempMax;
                        }
                        () => {
                            fMax = f;
                        }
                    }
                    return fMax;
                } else if (eventType == "EXPIRED"){
                    fMaxQueue.resetToFront();
                    while (fMaxQueue.hasNext()) {
                        float a = check <float>fMaxQueue.next();
                        if (a == f) {
                            fMaxQueue.removeCurrent();
                            break;
                        }
                    }
                    fMax = check <float>fMaxQueue.getFirst();
                    return fMax;
                } else if (eventType == "RESET"){
                    fMaxQueue.clear();
                    fMax = ();
                }
                return fMax;
            }
            any a => {
                error e = { message: "Unsupported attribute type found" };
                return e;
            }
        }
    }

    public function clone() returns Aggregator {
        Max maxAggregator = new();
        return maxAggregator;
    }

};

public function max() returns Aggregator {
    Max maxAggregator = new();
    return maxAggregator;
}


public type Min object {

    public LinkedList iMinQueue;
    public LinkedList fMinQueue;
    public int? iMin = ();
    public float? fMin = ();

    public new() {

    }

    public function process(any value, EventType eventType) returns any {
        match value {
            int i => {
                if (eventType == "CURRENT") {
                    iMinQueue.resetToRear();
                    while (iMinQueue.hasPrevious()) {
                        int a = check <int>iMinQueue.previous();
                        if (a > i) {
                            iMinQueue.removeCurrent();
                        } else {
                            break;
                        }
                    }
                    iMinQueue.addLast(i);
                    match iMin {
                        int tempMin => {
                            iMin = (tempMin > i) ? i : tempMin;
                        }
                        () => {
                            iMin = i;
                        }
                    }
                    return iMin;
                } else if (eventType == "EXPIRED"){
                    iMinQueue.resetToFront();
                    while (iMinQueue.hasNext()) {
                        int a = check <int>iMinQueue.next();
                        if (a == i) {
                            iMinQueue.removeCurrent();
                            break;
                        }
                    }
                    iMin = check <int>iMinQueue.getFirst();
                    return iMin;
                } else if (eventType == "RESET"){
                    iMinQueue.clear();
                    iMin = ();
                }
                return iMin;
            }
            float f => {
                if (eventType == "CURRENT") {
                    fMinQueue.resetToRear();
                    while (fMinQueue.hasPrevious()) {
                        float a = check <float>fMinQueue.previous();
                        if (a > f) {
                            fMinQueue.removeCurrent();
                        } else {
                            break;
                        }
                    }
                    fMinQueue.addLast(f);
                    match fMin {
                        float tempMin => {
                            fMin = (tempMin > f) ? f : tempMin;
                        }
                        () => {
                            fMin = f;
                        }
                    }
                    return fMin;
                } else if (eventType == "EXPIRED"){
                    fMinQueue.resetToFront();
                    while (fMinQueue.hasNext()) {
                        float a = check <float>fMinQueue.next();
                        if (a == f) {
                            fMinQueue.removeCurrent();
                            break;
                        }
                    }
                    fMin = check <float>fMinQueue.getFirst();
                    return fMin;
                } else if (eventType == "RESET"){
                    fMinQueue.clear();
                    fMin = ();
                }
                return fMin;
            }
            any a => {
                error e = { message: "Unsupported attribute type found" };
                return e;
            }
        }
    }

    public function clone() returns Aggregator {
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

    public function process(any value, EventType eventType) returns any {
        float fVal;
        match value {
            int i => {
                fVal = <float>i;
            }
            float f => {
                fVal = f;
            }
            any a => {
                error e = { message: "Unsupported attribute type found" };
                return e;
            }
        }

        if (eventType == "CURRENT") {
            // See here for the algorithm: http://www.johndcook.com/blog/standard_deviation/
            count++;
            if (count == 0) {
                return ();
            } else if (count == 1) {
                sumValue = fVal;
                mean = fVal;
                stdDeviation = 0.0;
                return 0.0;
            } else {
                float oldMean = mean;
                sumValue += fVal;
                mean = sumValue / count;
                stdDeviation += (fVal - oldMean) * (fVal - mean);
                return math:sqrt(stdDeviation / count);
            }
        } else if (eventType == "EXPIRED") {
            count--;
            if (count == 0) {
                sumValue = 0.0;
                mean = 0.0;
                stdDeviation = 0.0;
                return ();
            } else if (count == 1) {
                return 0.0;
            } else {
                float oldMean = mean;
                sumValue -= fVal;
                mean = sumValue / count;
                stdDeviation -= (fVal - oldMean) * (fVal - mean);
                return math:sqrt(stdDeviation / count);
            }
        } else if (eventType == "RESET") {
            mean = 0.0;
            stdDeviation = 0.0;
            sumValue = 0.0;
            count = 0;
            return 0.0;
        } else {
            return ();
        }
    }

    public function clone() returns Aggregator {
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

    public function process(any value, EventType eventType) returns any {
        match value {
            int i => {
                if (eventType == "CURRENT" || eventType == "EXPIRED") {
                    match iMax {
                        int tempMax => {
                            iMax = (tempMax < i) ? i : tempMax;
                        }
                        () => {
                            iMax = i;
                        }
                    }
                }
                return iMax;
            }
            float f => {
                if (eventType == "CURRENT" || eventType == "EXPIRED") {
                    match fMax {
                        float tempMax => {
                            fMax = (tempMax < f) ? f : tempMax;
                        }
                        () => {
                            fMax = f;
                        }
                    }
                }
                return fMax;
            }
            any a => {
                error e = { message: "Unsupported attribute type found" };
                return e;
            }
        }
    }

    public function clone() returns Aggregator {
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

    public function process(any value, EventType eventType) returns any {
        match value {
            int i => {
                if (eventType == "CURRENT" || eventType == "EXPIRED") {
                    match iMin {
                        int tempMin => {
                            iMin = (tempMin > i) ? i : tempMin;
                        }
                        () => {
                            iMin = i;
                        }
                    }
                }
                return iMin;
            }
            float f => {
                if (eventType == "CURRENT" || eventType == "EXPIRED") {
                    match fMin {
                        float tempMin => {
                            fMin = (tempMin > f) ? f : tempMin;
                        }
                        () => {
                            fMin = f;
                        }
                    }
                }
                return fMin;
            }
            any a => {
                error e = { message: "Unsupported attribute type found" };
                return e;
            }
        }
    }

    public function clone() returns Aggregator {
        MinForever minForeverAggregator = new();
        return minForeverAggregator;
    }

};

public function minForever() returns Aggregator {
    MinForever minForeverAggregator = new();
    return minForeverAggregator;
}
