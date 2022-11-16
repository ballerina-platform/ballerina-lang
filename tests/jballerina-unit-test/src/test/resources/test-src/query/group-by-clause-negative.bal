// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/lang.array;

type Order record {|
    int cusId;
    int price1;
    int price2;
    string name;
    int year;
|};

function getOrders() returns Order[] {
    Order res1 = {cusId: 1, price1: 200, price2: 300, name: "John", year: 2001};
    Order res2 = {cusId: 2, price1: 300, price2: 200, name: "Monica", year: 2022};
    Order res3 = {cusId: 3, price1: 20, price2: 50, name: "Tom", year: 2010};
    Order res4 = {cusId: 2, price1: 10, price2: 490, name: "Monica", year: 2001};

    return [res1, res2, res3, res4];
}

function testGroupByClausWithInvalidGroupingKey1() {
    Order[] orderList = getOrders();

    var _ = from var {cusId, price1, name} in orderList
        group by year
        select name;
}

function testGroupByClausWithInvalidGroupingKey2() {
    Order[] orderList = getOrders();

    var _ = from var {cusId, price1, price2, name} in orderList
        group by string totPrice = price1 + price2
        select name;
}

function testGroupByClausWithInvalidGroupingKey3() {
    Order[] orderList = getOrders();

    var _ = from var {cusId, price1, price2, name} in orderList
        group by string totPrice = price1 + name
        select name;
}

function testNonGroupingKeyInLangLibFunctionContext() {
    Order[] orderList = getOrders();

    int[] _ = from var {cusId, price1, price2, name} in orderList
        group by price1
        select sum(price2, 1);

    int[] _ = from var {cusId, price1, price2, name} in orderList
        group by price1
        select sum((price2));

    int[] _ = from var {cusId, price1, price2, name} in orderList
        group by price1
        select sum(1, 2);

    int[] _ = from var {cusId, price1, price2, name} in orderList
        group by price1
        select sum(price2, cusId);

    int[] _ = from var {cusId, price1, price2, name} in orderList
        group by price1
        select sum(name);

    int[] _ = from var {cusId, price1, price2, name} in orderList
        group by price1
        select sum();

    int[] _ = from var {cusId, price1, price2, name} in orderList
        group by price1
        let int[] a = [1, 2]
        select sum(...a);

    int[] _ = from var {cusId, price1, price2, name} in orderList
        group by price1
        let int b = 1
        select sum(b);

    int[] _ = from var {cusId, price1, price2, name} in orderList
        group by price1
        select sum(price2 + price1);

    string[] _ = from var {price1, price2, name} in orderList
        group by price1
        select startsWith(",", name);

    string[] _ = from var {price1, price2, name} in orderList
        group by price1
        select toHexString(price2);
}

function testNonGroupingKeyInUserDefinedFunctionContext() {
    Order[] orderList = getOrders();

    int[] _ = from var {price1, price2, name} in orderList
        group by price1
        let int b = userDefinedFunc1(price2)
        select price1;

    int[] _ = from var {price1, price2, name} in orderList
        group by price1
        let int b = undefinedFunc(price2)
        select price1;

    int[] _ = from var {price1, price2, name} in orderList
        group by price1
        let int b = undefinedFunc(price2, 1)
        select price1;

    int[] _ = from var {price1, price2, name} in orderList
        group by price1
        let int b = undefinedFunc(1, price2)
        select price1;

    int[] _ = from var {price1, price2, name} in orderList
        group by price1
        let int b = userDefinedFunc2(1, price2)
        select price1;
}

function userDefinedFunc1 (int... arg1) returns int {
    return int:sum(...arg1);
}

function userDefinedFunc2 (int arg1, int... arg2) returns int {
    return int:sum(...arg2);
}

function testNonGroupingKeysUsedByItself() {
    Order[] orderList = getOrders();

    int[] _ = from var {price1, price2, name} in orderList
        group by price1
        select price2;

    int[] _ = from var {price1, price2, name} in orderList
        group by price1
        let int[] a = price2
        select price1;
}

function testNonGroupingKeyInLangLibFunctionContextWithPrefix() {
    Order[] orderList = getOrders();

    int[] _ = from var {cusId, price1, price2, name} in orderList
        group by price1
        select int:sum(price2, 1);

    int[] _ = from var {cusId, price1, price2, name} in orderList
        group by price1
        select int:sum((price2));

    int[] _ = from var {cusId, price1, price2, name} in orderList
        group by price1
        select int:sum(price2, cusId);

    int[] _ = from var {cusId, price1, price2, name} in orderList
        group by price1
        select int:sum(name);

    int[] _ = from var {cusId, price1, price2, name} in orderList
        group by price1
        select int:sum(price2 + price1);

    string[] _ = from var {price1, price2, name} in orderList
        group by price1
        select string:startsWith(",", name);

    string[] _ = from var {price1, price2, name} in orderList
        group by price1
        select int:toHexString(price2);

    int[] _ = from var {price1, price2, name} in orderList
        group by price1
        select array:length(price2);

    string[] _ = from var {price1, price2, name} in orderList
        group by price1
        select undefined:sum(price2);
}

function testNonGroupingKeyInLangLibFunctionContextWithExpression() {
    Order[] orderList = getOrders();

    int[] _ = from var {price1, price2, name} in orderList
        group by price1
        select price2.sum();

    string[] _ = from var {price1, price2, name} in orderList
        group by price1
        select price2.toHexString();

    string[] _ = from var {price1, price2, name} in orderList
        group by price1
        select price2.concat();
}

function testNonGroupingKeyInListConstructorContext() {
    Order[] orderList = getOrders();

    int[*][*] _ = from var {price1, price2, name} in orderList
        group by price1
        select [price2];

    [int[]] a = from var {price1, price2, name} in orderList
        group by price1
        select [price2];

    var _ = from var {price1, price2, name} in orderList
        group by price1
        select [price2];

    readonly[] _ = from var {price1, price2, name} in orderList
        group by price1
        select [price2];
}

//TODO
function testNonGroupingKeyWithoutTypebinding() {
    //int[] _ = from var item in orderList
      //  group by int a = item.price1
        //select (item.price2);
}

//TODO
function testNonGroupingKeyInLangLibFunctionContextWhenNested() {

}