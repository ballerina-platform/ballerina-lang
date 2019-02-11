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

import ballerina/runtime;
import throttler;

public type StockRecord record {
    string symbol;
    float price;
    int volume;
    int expiryTimestamp?;
};

public type StockSumRecord record {
    string symbol;
    float sumPrice;
    int volume;
    int expiryTimestamp;
};

int index = 0;
stream<StockRecord> inputStream = new;
stream<StockSumRecord> outputStream = new;
StockSumRecord[] globalStockSumArray = [];

function startThrottleQuery() returns (StockSumRecord[]) {

    StockRecord s1  = { symbol: "IBM", price: 700.0, volume: 0 };
    StockRecord s2  = { symbol: "WSO2", price: 60.5, volume: 1 };
    StockRecord s3  = { symbol: "IBM", price: 700.0, volume: 0 };

    deployThrottleQuery();

    outputStream.subscribe(function(StockSumRecord e) {printStockDetails(e);});

    inputStream.publish(s1);
    inputStream.publish(s2);
    runtime:sleep(15000);
    inputStream.publish(s3);
    int count = 0;
    while(true) {
        runtime:sleep(500);
        count += 1;
        if((globalStockSumArray.length()) == 3 || count == 10) {
            break;
        }
    }
    return globalStockSumArray;
}

function deployThrottleQuery() {

    forever {
        from inputStream window throttler:timeBatch(10000, 0)
        select inputStream.symbol, sum(inputStream.price) as sumPrice, inputStream.volume, inputStream.expiryTimestamp
        => (StockSumRecord[] stockSum) {
            foreach var t in stockSum {
                outputStream.publish(t);
            }
        }
    }
}

function printStockDetails(StockSumRecord e) {
    addToGlobalStockSumArray(e);
}

function addToGlobalStockSumArray(StockSumRecord e) {
    globalStockSumArray[index] = e;
    index = index + 1;
}