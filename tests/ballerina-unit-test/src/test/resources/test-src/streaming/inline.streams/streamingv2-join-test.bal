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

import ballerina/runtime;
import ballerina/streams;

type Stock record {
    string symbol;
    float price;
    int volume;
};

type Twitter record {
    string user;
    string tweet;
    string company;
};

type StockWithPrice record {
    string symbol;
    string tweet;
    float price;
};

StockWithPrice[] globalEventsArray = [];
int index = 0;

stream<StockWithPrice> stockWithPriceStream;

function testJoinQuery(stream<Stock> ss, stream<Twitter> tt) {

    forever {
        from ss window lengthWindow([1])
        join tt window lengthWindow([1])
        on ss.symbol == tt.company
        select ss.symbol as symbol, tt.tweet as tweet, ss.price as price
        => (StockWithPrice[] emp) {
            foreach e in emp {
                stockWithPriceStream.publish(e);
            }
        }
    }
}

function startJoinQuery() returns (StockWithPrice[]) {

    stream<Stock> stockStream;
    stream<Twitter> twitterStream;

    testJoinQuery(stockStream, twitterStream);

    Stock s1 = {symbol:"WSO2", price:55.6, volume:100};
    Stock s2 = {symbol:"MBI", price:74.6, volume:100};
    Stock s3 = {symbol:"WSO2", price:58.6, volume:100};

    Twitter t1 = {user:"User1", tweet:"Hello WSO2, happy to be a user.", company:"WSO2"};

    stockWithPriceStream.subscribe(printCompanyStockPrice);

    stockStream.publish(s1);
    runtime:sleep(100);
    twitterStream.publish(t1);
    runtime:sleep(100);
    stockStream.publish(s2);
    runtime:sleep(100);
    stockStream.publish(s3);

    int count = 0;
    while(true) {
        runtime:sleep(500);
        count += 1;
        if((globalEventsArray.length()) == 2 || count == 10) {
            break;
        }
    }

    return globalEventsArray;
}

function printCompanyStockPrice(StockWithPrice s) {
    addToGlobalEventsArray(s);
}

function addToGlobalEventsArray(StockWithPrice s) {
    globalEventsArray[index] = s;
    index = index + 1;
}