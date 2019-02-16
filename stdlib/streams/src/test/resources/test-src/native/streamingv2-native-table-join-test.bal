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

public type Stock record {
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

stream<Twitter> twitterStream = new;
stream<StockWithPrice> stockWithPriceStream = new;

table<Stock> stocksTable = table {
    { symbol, price, volume },
    [
        {"WSO2", 55.6, 100},
        {"MBI", 58.6, 70}
    ]
};

public function startTableJoinQuery() returns any {
    tableJoinFunc();

    Twitter t1 = { user: "User1", tweet: "Hello WSO2, happy to be a user.", company: "WSO2" };
    Twitter t2 = { user: "User2", tweet: "Hello MBI, happy to be a user.", company: "MBI" };

    stockWithPriceStream.subscribe(printCompanyStockPrice);

    twitterStream.publish(t1);
    runtime:sleep(1000);
    twitterStream.publish(t2);
    runtime:sleep(1000);

    return globalEventsArray;
}


//    forever {
//        from twitterStream window lengthWindow(1)
//        join queryStocksTable(stockStream.symbol, 1) as tb
//        select tb.symbol, stockStream.tweet, tb.price
//        => (StockWithPrice[] stocks) {
//            foreach s in stocks {
//                stockWithPriceStream.publish(s)
//            }
//        }
//    }
function tableJoinFunc() {

    function (map<anydata>[]) outputFunc = function (map<anydata>[] events) {
        foreach var m in events {
            // just cast input map into the output type
            var o = <StockWithPrice>StockWithPrice.convert(m);
            stockWithPriceStream.publish(o);
        }
    };

    // Output processor
    streams:OutputProcess outputProcess = streams:createOutputProcess(outputFunc);

    // Selector
    streams:Select select = streams:createSelect(function (streams:StreamEvent?[] e) {outputProcess.process(e);},
        [], (),
        function (streams:StreamEvent e, streams:Aggregator[] aggregatorArr1) returns map<anydata> {
            return {
                "symbol": e.data["tb.symbol"],
                "tweet": e.data["twitterStream.tweet"],
                "price": e.data["tb.price"]
            };
        }
    );

    // Join processor
    streams:TableJoinProcessor tableJoinProcessor = streams:createTableJoinProcessor(
                                                        function (streams:StreamEvent?[] e) {select.process(e);},
                                                        "JOIN",
        function (streams:StreamEvent s) returns map<anydata>[] {
            map<anydata>[] result = [];
            int i = 0;
            foreach var r in queryStocksTable(<string>s.data["twitterStream.company"], 1) {
                result[i] = <map<anydata>>map<anydata>.convert(r);
                i += 1;
            }
            return result;
        });

    // Window processor
    streams:Window lengthWindow = streams:length([1], nextProcessPointer =
                                    function (streams:StreamEvent?[] e) {tableJoinProcessor.process(e);});

    // Set the tableName, streamName, windowProcessors to the table join processor
    tableJoinProcessor.setJoinProperties("tb", "twitterStream", lengthWindow);

    twitterStream.subscribe(function (Twitter i) {
            map<anydata> keyVal = <map<anydata>>map<anydata>.convert(i);
            streams:StreamEvent?[] eventArr = streams:buildStreamEvent(keyVal, "twitterStream");
            lengthWindow.process(eventArr);
        }
    );
}

public function queryStocksTable(string symbol, int volume) returns table<Stock> {
    table<Stock> result = table {
        { symbol, price, volume }, []
    };
    foreach var stock in stocksTable {
        if (stock.symbol == symbol && stock.volume > volume) {
            var ret = result.add(stock);
        }
    }
    return result;
}

function printCompanyStockPrice(StockWithPrice s) {
    addToGlobalEventsArray(s);
}

function addToGlobalEventsArray(StockWithPrice s) {
    globalEventsArray[index] = s;
    index = index + 1;
}
