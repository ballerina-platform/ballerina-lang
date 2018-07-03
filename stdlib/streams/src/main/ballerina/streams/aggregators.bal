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

public type Sum object {
    public {
        int sumValue = 0;
    }

    public new() {

    }

    public function process(int value, EventType eventType) returns int {
        if (eventType == "CURRENT") {
            sumValue += value;
        } else if (eventType == "EXPIRED"){
            sumValue -= value;
        } else if (eventType == "RESET"){
            sumValue = 0;
        }
        return sumValue;
    }

    public function clone() returns Aggregator {
        Sum sumAggregator = new();
        return sumAggregator;
    }

};

public function createSumAggregator() returns Sum {
    Sum sumAggregator = new();
    return sumAggregator;
}

public type Aggregator object {

    public new() {

    }

    public function clone() returns Aggregator {
        Aggregator aggregator = new();
        return aggregator;
    }

    public function process(int value, EventType eventType) returns int {
        return 10;
    }

};

