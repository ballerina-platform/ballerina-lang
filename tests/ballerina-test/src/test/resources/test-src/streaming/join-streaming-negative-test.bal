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
import ballerina/io;

type Stock {
    string symbol;
    float price;
    int volume;
};

type Twitter {
    string user;
    string tweet;
    string company;
};

type StockWithPrice {
    string symbol;
    string tweet;
    float price;
};

StockWithPrice[] globalEventsArray = [];
int index = 0;

function startJoinQuery() returns (StockWithPrice[]) {

    stream<Stock> stockStream;
    stream<Twitter> twitterStream;
    stream<StockWithPrice> stockWithPriceStream;

    forever {
        from stockStream window time(1000) as s
        join twitterStream window time(1000)
        on stockStream.symbol == twitterStream.company
        select stockStream.symbol as symbol, twitterStream.tweet as tweet, stockStream.price as price
        => (StockWithPrice[] emp) {
            stockWithPriceStream.publish(emp);
        }
    }

    Stock s1 = {symbol:"WSO2", price:55.6, volume:100};
    Stock s2 = {symbol:"MBI", price:74.6, volume:100};
    Stock s3 = {symbol:"WSO2", price:58.6, volume:100};

    Twitter t1 = {user:"User1", tweet:"Hello WSO2, happy to be a user.", company:"WSO2"};

    stockWithPriceStream.subscribe(printCompanyStockPrice);

    stockStream.publish(s1);
    twitterStream.publish(t1);
    stockStream.publish(s2);
    stockStream.publish(s3);

    runtime:sleep(3000);
    return globalEventsArray;
}

function printCompanyStockPrice(StockWithPrice s) {
    io:println("printCompanyStock function invoked for company:" + s.symbol + " and price:" + s.price);
    addToGlobalEventsArray(s);
}

function addToGlobalEventsArray(StockWithPrice s) {
    globalEventsArray[index] = s;
    index = index + 1;
}