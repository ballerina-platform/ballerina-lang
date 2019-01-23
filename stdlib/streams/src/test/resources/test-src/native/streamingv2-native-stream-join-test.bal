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
    string? symbol;
    string? tweet;
    float? price;
};

StockWithPrice[] globalEventsArray = [];
int index = 0;

stream<Stock> stockStream = new;
stream<Twitter> twitterStream = new;
stream<StockWithPrice> stockWithPriceStream = new;

public function startStreamJoinQuery() returns any {
    joinFunc();

    Stock s1 = { symbol: "WSO2", price: 55.6, volume: 100 };
    Stock s2 = { symbol: "MBI", price: 74.6, volume: 100 };
    Stock s3 = { symbol: "WSO2", price: 58.6, volume: 100 };

    Twitter t1 = { user: "User1", tweet: "Hello WSO2, happy to be a user.", company: "WSO2" };

    stockWithPriceStream.subscribe(function (StockWithPrice e) {printCompanyStockPrice(e);});

    twitterStream.publish(t1);
    runtime:sleep(1000);
    stockStream.publish(s1);
    runtime:sleep(1000);
    stockStream.publish(s2);
    runtime:sleep(1000);
    stockStream.publish(s3);
    runtime:sleep(1000);

    return globalEventsArray;
}


//  forever {
//      from stockStream window lengthWindow(1)
//      join twitterStream window lengthWindow(1)
//      on stockStream.symbol == twitterStream.company
//          select stockStream.symbol as symbol, twitterStream.tweet as tweet, stockStream.price as price
//          => (StockWithPrice[] emp) {
//                  foreach e in emp {
//                      stockWithPriceStream.publish(e);
//          }
//      }
//  }
//

function joinFunc() {

    function (map<anydata>[]) outputFunc = function (map<anydata>[] events) {
        foreach map<anydata> m in events {
            // just cast input map into the output type
            StockWithPrice o = <StockWithPrice>StockWithPrice.stamp(m.clone());
            stockWithPriceStream.publish(o);
        }
    };

    // Output processor
    streams:OutputProcess outputProcess = streams:createOutputProcess(outputFunc);

    // Selector
    streams:Select select =
    streams:createSelect(function (streams:StreamEvent?[] e) {outputProcess.process(e);},
        [], (),
        function (streams:StreamEvent e, streams:Aggregator[] aggregatorArr1) returns map<anydata> {
            return {
                "symbol": e.data["stockStream.symbol"],
                "tweet": e.data["twitterStream.tweet"],
                "price": e.data["stockStream.price"]
            };
        }
    );

    // On condition
    function (map<anydata>, map<anydata>) returns boolean conditionFunc =
    function (map<anydata> lhs, map<anydata> rhs) returns boolean {
        return <string>lhs["stockStream.symbol"] == <string>rhs["twitterStream.company"];
    };

    // Join processor
    streams:StreamJoinProcessor joinProcessor =
    streams:createStreamJoinProcessor(function (streams:StreamEvent?[] e) {select.process(e);}, "FULLOUTERJOIN",
        conditionFunc = conditionFunc);

    // Window processors
    streams:Window lengthWindowA = streams:length([1],
        nextProcessPointer = function (streams:StreamEvent?[] e) {joinProcessor.process(e);});
    streams:Window lengthWindowB = streams:length([1],
        nextProcessPointer = function (streams:StreamEvent?[] e) {joinProcessor.process(e);});

    // Set the window processors to the join processor
    joinProcessor.setLHS("stockStream", lengthWindowA);
    joinProcessor.setRHS("twitterStream", lengthWindowB);


    // Subscribe to input streams
    stockStream.subscribe(function (Stock i) {
            map<anydata> keyVal = <map<anydata>>map<anydata>.stamp
            (i.clone());
            streams:StreamEvent?[] eventArr = streams:buildStreamEvent(keyVal, "stockStream");
            lengthWindowA.process(eventArr);
        }
    );
    twitterStream.subscribe(function (Twitter i) {
            streams:StreamEvent?[] eventArr = streams:buildStreamEvent(i, "twitterStream");
            lengthWindowB.process(eventArr);
        }
    );
}

function printCompanyStockPrice(StockWithPrice s) {
    addToGlobalEventsArray(s);
}

function addToGlobalEventsArray(StockWithPrice s) {
    globalEventsArray[index] = s;
    index = index + 1;
}
