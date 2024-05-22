// Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
//
// WSO2 LLC. licenses this file to you under the Apache License,
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
import ballerina/lang.runtime;
import ballerina/test;

@test:BeforeSuite
function setup() {
}

@test:BeforeEach
function beforeEachFunc() {
}

@test:BeforeGroups {value: ["g1"]}
function beforeGroups1() {
}

@test:Config {
    dataProvider: mapDataProvider,
    before: beforeFunc,
    after: afterFunc,
    groups: ["g1"]
}

function mapDataProviderTest(int value1, int value2, string fruit) returns error? {
    test:assertEquals(value1, value2, msg = "The provided values are not equal");
    runtime:sleep(0.1);
}

function beforeFunc() {
}

function mapDataProvider() returns map<[int, int, string]>|error {
    map<[int, int, string]> dataSet = {
        "banana": [10, 10, "banana"],
        "cherry": [5, 5, "cherry"],
        "apple": [5, 5, "apple"],
        "orange": [5, 5, "orange"],
        "carrot": [5, 5, "carrot"],
        "lemon": [5, 5, "lemon"],
        "tomatto": [5, 5, "tomatto"],
        "papaya": [5, 5, "papaya"],
        "grapes": [5, 5, "grapes"],
        "mango": [5, 5, "mango"],
        "pineapple": [5, 5, "pineapple"],
        "watermelon": [5, 5, "watermelon"],
        "strawberry": [5, 5, "strawberry"],
        "melon": [5, 5, "melon"],
        "guava": [5, 5, "guava"],
        "pomegranate": [5, 5, "pomegranate"],
        "jackfruit": [5, 5, "jackfruit"],
        "coconut": [5, 5, "coconut"],
        "peach": [5, 5, "peach"],
        "pear": [5, 5, "pear"],
        "plum": [5, 5, "plum"],
        "blueberry": [5, 5, "blueberry"],
        "raspberry": [5, 5, "raspberry"],
        "kiwi": [5, 5, "kiwi"],
        "avocado": [5, 5, "avocado"],
        "cucumber": [5, 5, "cucumber"],
        "pepper": [5, 5, "pepper"],
        "onion": [5, 5, "onion"],
        "potato": [5, 5, "potato"],
        "tomato": [5, 5, "tomato"],
        "garlic": [5, 5, "garlic"],
        "ginger": [5, 5, "ginger"],
        "spinach": [5, 5, "spinach"],
        "broccoli": [5, 5, "broccoli"],
        "cauliflower": [5, 5, "cauliflower"],
        "cabbage": [5, 5, "cabbage"],
        "beetroot": [5, 5, "beetroot"],
        "celery": [5, 5, "celery"],
        "corn": [5, 5, "corn"],
        "mushroom": [5, 5, "mushroom"]

    };
    return dataSet;
}

function afterFunc() {
}

@test:AfterGroups {value: ["g1"]}
function afterGroups1() {
}

@test:AfterEach
function afterEachFunc() {
}

@test:AfterSuite
function cleanup() {
}
