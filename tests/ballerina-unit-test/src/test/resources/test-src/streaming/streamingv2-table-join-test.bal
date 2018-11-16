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

stream<Twitter> twitterStream;
stream<StockWithPrice> stockWithPriceStream;

table<Stock> stocksTable = table {
    { symbol, price, volume },
    [
        {"WSO2", 55.6, 100},
        {"IBM", 58.6, 70}
    ]
};

function testJoinQuery() {
    //forever {
    //    from twitterStream window lengthWindow([1]) as tw
    //    join queryStocksTable(tw.company, 1) as tb
    //    select tb.symbol, tw.tweet, tb.price
    //    => (StockWithPrice[] stocks) {
    //        foreach s in stocks {
    //            stockWithPriceStream.publish(s);
    //        }
    //    }
    //}
}

function testOuterJoinQuery() {
    //forever {
    //    from twitterStream window lengthWindow([1]) as tw
    //    full outer join queryStocksTable(tw.company, 1) as tb
    //    select tb.symbol, tw.tweet, tb.price
    //    => (StockWithPrice[] stocks) {
    //        foreach s in stocks {
    //            stockWithPriceStream.publish(s);
    //        }
    //    }
    //}
}

function startTableJoinQuery() returns (StockWithPrice[]) {
    clear();

    testJoinQuery();

    Twitter t1 = { user: "User1", tweet: "Hello WSO2, happy to be a user.", company: "WSO2" };
    Twitter t2 = { user: "User2", tweet: "Hello IBM, happy to be a user.", company: "IBM" };

    stockWithPriceStream.subscribe(printCompanyStockPrice);

    twitterStream.publish(t1);
    runtime:sleep(1000);
    twitterStream.publish(t2);
    runtime:sleep(1000);

    int count = 0;
    while(true) {
        runtime:sleep(500);
        count += 1;
        if((lengthof globalEventsArray) == 2 || count == 10) {
            break;
        }
    }
    return globalEventsArray;
}

function startTableOuterJoinQuery() returns (StockWithPrice[]) {
    clear();

    testOuterJoinQuery();

    Twitter t1 = { user: "User1", tweet: "Hello WSO2, happy to be a user.", company: "WSO2" };
    Twitter t2 = { user: "User3", tweet: "Hello BMW, happy to be a user.", company: "BMW" };

    stockWithPriceStream.subscribe(printCompanyStockPrice);

    twitterStream.publish(t1);
    runtime:sleep(1000);
    twitterStream.publish(t2);
    runtime:sleep(1000);

    int count = 0;
    while(true) {
        runtime:sleep(500);
        count += 1;
        if((lengthof globalEventsArray) == 2 || count == 10) {
            break;
        }
    }
    return globalEventsArray;
}

public function queryStocksTable(string symbol, int volume) returns table<Stock> {
    table<Stock> result = table {
        { symbol, price, volume }, []
    };
    foreach stock in stocksTable {
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

function clear() {
    globalEventsArray = [];
    index = 0;
}
