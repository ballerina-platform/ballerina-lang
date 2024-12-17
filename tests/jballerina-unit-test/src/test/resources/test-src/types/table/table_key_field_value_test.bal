// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type Row1 record {
    readonly int k;
    int value;
};
const int INT_VAL1 = 55;
const int INT_VAL2 = 22;

function testLiteralAsKeyValue() {
    table<Row1> key(k) tbl = table [
       {k: 12, value: 17},
       {k: INT_VAL1, value: 17}
    ];

    tbl.add({k: 13, value: 25});
    tbl.add({k: INT_VAL2, value: 25});
    var tbl2 = table key(k) [
        {k: 12, value: 17},
        {k: 55, value: 17},
        {k: 13, value: 25},
        {k: 22, value: 25}
    ];
    assertEqual(tbl2, tbl);

    Row1 row = {k: 13, value: 25};
    assertEqual(row, tbl.get(13));

    error? err = trap tbl.add({k: 12, value: 20});
    assertEqual(true, err is error);
    if (err is error) {
        map<string> msg = {"message":"a value found for key '12'"};
        assertEqual(msg, err.detail());
    }
}

type Row2 record {
    readonly string k;
    int value;
};

function testStringTemplateExprAsKeyValue() {
    table<Row2> key(k) tbl = table [
       {k: string `ABC`, value: 17},
       {k: string `ABC-${INT_VAL1}`, value: 17}
    ];

    tbl.add({k: string `DEF`, value: 25});
    tbl.add({k: string `DEF-${INT_VAL1}`, value: 25});
    var tbl2 = table key(k) [
        {k: string `ABC`, value: 17},
        {k: string `ABC-55`, value: 17},
        {k: string `DEF`, value: 25},
        {k: string `DEF-55`, value: 25}
    ];
    assertEqual(tbl2, tbl);

    Row2 row = {k: string `ABC`, value: 17};
    assertEqual(row, tbl.get(string `ABC`));

    error? err = trap tbl.add({k: string `ABC`, value: 20});
    assertEqual(true, err is error);
    if (err is error) {
        map<string> msg = {"message":"a value found for key 'ABC'"};
        assertEqual(msg, err.detail());
    }
}

type Row3 record {
    readonly xml k;
    int value;
};

function testXmlTemplateExprAsKeyValue() {
    table<Row3> key(k) tbl = table [
       {k: xml `ABC`, value: 17}
    // Blocked by #41985
    //    {k: xml `ABC-${INT_VAL1}`, value: 17}
    ];

    tbl.add({k: xml `DEF`, value: 25});
    // Blocked by #41981
    // tbl.add({k: xml `DEF-${INT_VAL1}`, value: 25});
    var tbl2 = table key(k) [
        {k: xml `ABC`, value: 17},
        // {k: xml `ABC-55`, value: 17},
        {k: xml `DEF`, value: 25}
        // {k: xml `DEF-55`, value: 25}
    ];
    assertEqual(tbl2, tbl);

    Row3 row = {k: xml `ABC`, value: 17};
    assertEqual(row, tbl.get(xml `ABC`));

    error? err = trap tbl.add({k: xml `ABC`, value: 20});
    assertEqual(true, err is error);
    if (err is error) {
        map<string> msg = {"message":"a value found for key 'ABC'"};
        assertEqual(msg, err.detail());
    }
}

type Row4 record {
    readonly int[] k;
    int value;
};
const int[] INT_ARR = [22 , 44];

function testListConstructorExprAsKeyValue() {
    table<Row4> key(k) tbl = table [
       {k: [12 , 13], value: 17},
       {k: [12 , INT_VAL1], value: 17}
    // Blocked by #41979
    //    {k: [12 , ...INT_ARR], value: 17}
    ];

    tbl.add({k: [13 , 14], value: 25});
    tbl.add({k: [13 , INT_VAL1], value: 25});
    tbl.add({k: [13 , ...INT_ARR], value: 25});
    var tbl2 = table key(k) [
        {k: [12, 13], value: 17},
        {k: [12, 55], value: 17},
        // {k: [12, 22, 44], value: 17},
        {k: [13, 14], value: 25},
        {k: [13, 55], value: 25},
        {k: [13, 22, 44], value: 25}
    ];
    assertEqual(tbl2, tbl);

    Row4 row = {k: [13 , 14], value: 25};
    assertEqual(row, tbl.get([13 , 14]));

    error? err = trap tbl.add({k: [13 , 14], value: 20});
    assertEqual(true, err is error);
    if (err is error) {
        map<string> msg = {"message":"a value found for key '[13,14]'"};
        assertEqual(msg, err.detail());
    }
}

type Row5 record {
    readonly table<Row1> key(k) k;
    int value;
};

function testTableConstructorExprAsKeyValue() {
    table<Row5> key(k) tbl = table [
       {k: table key(k) [{k: 12, value: 17}], value: 17},
       {k: table key(k) [{k: INT_VAL1, value: 17}], value: 17}
    ];

    tbl.add({k: table key(k) [{k: 13, value: 25}], value: 25});
    tbl.add({k: table key(k) [{k: INT_VAL2, value: 25}], value: 25});
    var tbl2 = table key(k) [
        {k: table key(k) [{k: 12, value: 17}], value: 17},
        {k: table key(k) [{k: 55, value: 17}], value: 17},
        {k: table key(k) [{k: 13, value: 25}], value: 25},
        {k: table key(k) [{k: 22, value: 25}], value: 25}
    ];
    assertEqual(tbl2, tbl);

    Row5 row = {k: table key(k) [{k: 13, value: 25}], value: 25};
    readonly & table<Row1> key(k) keyVal = table [
       {k: 13, value: 25}
    ];
    assertEqual(row, tbl.get(keyVal));

    error? err = trap tbl.add({k: table key(k) [{k: 13, value: 25}], value: 25});
    assertEqual(true, err is error);
    if (err is error) {
        map<string> msg = {"message":"a value found for key '[{\"k\":13,\"value\":25}]'"};
        assertEqual(msg, err.detail());
    }
}

type Row6 record {
    readonly map<anydata> k;
    int value;
};
const string STR_VAL1 = "X";
const string STR_VAL2 = "Y";

function testMappingConstructorExprAsKeyValue() {
    table<Row6> key(k) tbl = table [
        {k: {"A": "a", "B": 12, "C": [13.5, 24.3]}, value: 17},
        {k: {"A": "a", "B": INT_VAL1, "C": [13.5, 24.3]}, value: 17},
        {k: {"A": "a", [STR_VAL1] : INT_VAL1, "C": [13.5, 24.3]}, value: 17},
        {k: {"A": "a", STR_VAL1, "C": [13.5, 24.3]}, value: 17}
    ];

    tbl.add({k: {"A": "z", "B": 12, "C": [23.5, 65.3]}, value: 25});
    tbl.add({k: {"A": "z", "B": INT_VAL2, "C": [23.5, 65.3]}, value: 25});
    tbl.add({k: {"A": "z", [STR_VAL2] : INT_VAL2, "C": [23.5, 65.3]}, value: 25});
    tbl.add({k: {"A": "z", STR_VAL2, "C": [23.5, 65.3]}, value: 25});
    var tbl2 = table key(k) [
        {k: {"A": "a", "B": 12, "C": [13.5, 24.3]}, value: 17},
        {k: {"A": "a", "B": 55, "C": [13.5, 24.3]}, value: 17},
        {k: {"A": "a", "X": 55, "C": [13.5, 24.3]}, value: 17},
        {k: {"A": "a", "STR_VAL1": "X", "C": [13.5, 24.3]}, value: 17},
        {k: {"A": "z", "B": 12, "C": [23.5, 65.3]}, value: 25},
        {k: {"A": "z", "B": 22, "C": [23.5, 65.3]}, value: 25},
        {k: {"A": "z", "Y": 22, "C": [23.5, 65.3]}, value: 25},
        {k: {"A": "z", "STR_VAL2": "Y", "C": [23.5, 65.3]}, value: 25}
    ];
    assertEqual(tbl2, tbl);

    Row6 row = {k: {"A": "z", "B": 12, "C": [23.5, 65.3]}, value: 25};
    assertEqual(row, tbl.get({"A": "z", "B": 12, "C": [23.5, 65.3]}));

    error? err = trap tbl.add({k: {"A": "z", "B": 12, "C": [23.5, 65.3]}, value: 25});
    assertEqual(true, err is error);
    if (err is error) {
        map<string> msg = {"message":"a value found for key '{\"A\":\"z\",\"B\":12,\"C\":[23.5,65.3]}'"};
        assertEqual(msg, err.detail());
    }
}

const ONE = 1;
const TWO = 2;

function testConstRefExprAsKeyValue() {
    table<Row1> key(k) tbl = table [
       {k: ONE, value: 17}
    ];

    tbl.add({k: TWO, value: 25});
    var tbl2 = table key(k) [{k: 1, value: 17}, {k: 2, value: 25}];
    assertEqual(tbl2, tbl);

    Row1 row = {k: 2, value: 25};
    assertEqual(row, tbl.get(2));

    error? err = trap tbl.add({k: ONE, value: 17});
    assertEqual(true, err is error);
    if (err is error) {
        map<string> msg = {"message":"a value found for key '1'"};
        assertEqual(msg, err.detail());
    }
}

const float FLOAT_VAL1 = 10.5;
const float FLOAT_VAL2 = 4.5;
function testTypeCastExprAsKeyValue() {
    table<Row1> key(k) tbl = table [
       {k: <int>1.2, value: 17},
       {k: <int>FLOAT_VAL1, value: 17}
    ];

    tbl.add({k: <int>2.5, value: 25});
    tbl.add({k: <int>FLOAT_VAL2, value: 25});
    var tbl2 = table key(k) [
        {k: 1, value: 17},
        {k: 10, value: 17},
        {k: 2, value: 25},
        {k: 4, value: 25}
    ];
    assertEqual(tbl2, tbl);

    Row1 row = {k: 2, value: 25};
    assertEqual(row, tbl.get(2));

    error? err = trap tbl.add({k: <int>2.5, value: 25});
    assertEqual(true, err is error);
    if (err is error) {
        map<string> msg = {"message":"a value found for key '2'"};
        assertEqual(msg, err.detail());
    }
}

function testUnaryExprAsKeyValue() {
    table<Row1> key(k) tbl = table [
       {k: +1, value: 17},
       {k: +INT_VAL1, value: 17}
    ];

    tbl.add({k: +2, value: 25});
    tbl.add({k: +INT_VAL2, value: 25});
    var tbl2 = table key(k) [
        {k: 1, value: 17},
        {k: 55, value: 17},
        {k: 2, value: 25},
        {k: 22, value: 25}
    ];
    assertEqual(tbl2, tbl);

    Row1 row = {k: 2, value: 25};
    assertEqual(row, tbl.get(2));

    error? err = trap tbl.add({k: +2, value: 25});
    assertEqual(true, err is error);
    if (err is error) {
        map<string> msg = {"message":"a value found for key '2'"};
        assertEqual(msg, err.detail());
    }
}

function testMultiplicativeExprAsKeyValue() {
    table<Row1> key(k) tbl = table [
       {k: 1 * 10, value: 17},
       {k: INT_VAL1 * 10, value: 17}
    ];

    tbl.add({k: 2 * 10, value: 25});
    tbl.add({k: INT_VAL2 * 10, value: 25});
    var tbl2 = table key(k) [
        {k: 10, value: 17},
        {k: 550, value: 17},
        {k: 20, value: 25},
        {k: 220, value: 25}
    ];
    assertEqual(tbl2, tbl);

    Row1 row = {k: 20, value: 25};
    assertEqual(row, tbl.get(20));

    error? err = trap tbl.add({k: 20, value: 25});
    assertEqual(true, err is error);
    if (err is error) {
        map<string> msg = {"message":"a value found for key '20'"};
        assertEqual(msg, err.detail());
    }
}

function testAdditiveExprAsKeyValue() {
    table<Row1> key(k) tbl = table [
       {k: 1 + 10, value: 17},
       {k: INT_VAL1 + 10, value: 17}
    ];

    tbl.add({k: 2 + 10, value: 25});
    tbl.add({k: INT_VAL2 + 10, value: 25});
    var tbl2 = table key(k) [
        {k: 11, value: 17},
        {k: 65, value: 17},
        {k: 12, value: 25},
        {k: 32, value: 25}
    ];
    assertEqual(tbl2, tbl);

    Row1 row = {k: 12, value: 25};
    assertEqual(row, tbl.get(12));

    error? err = trap tbl.add({k: 12, value: 25});
    assertEqual(true, err is error);
    if (err is error) {
        map<string> msg = {"message":"a value found for key '12'"};
        assertEqual(msg, err.detail());
    }
}

function testShiftExprAsKeyValue() {
    table<Row1> key(k) tbl = table [
       {k: 1 << 64, value: 17},
       {k: INT_VAL1 << 64, value: 17}
    ];

    tbl.add({k: 1 << 65, value: 25});
    tbl.add({k: INT_VAL2 << 65, value: 25});
    var tbl2 = table key(k) [
        {k: 1, value: 17},
        {k: 55, value: 17},
        {k: 2, value: 25},
        {k: 44, value: 25}
    ];
    assertEqual(tbl2, tbl);

    Row1 row = {k: 2, value: 25};
    assertEqual(row, tbl.get(0x2));

    error? err = trap tbl.add({k: 0x2, value: 25});
    assertEqual(true, err is error);
    if (err is error) {
        map<string> msg = {"message":"a value found for key '2'"};
        assertEqual(msg, err.detail());
    }
}

type Row7 record {
    readonly boolean k;
    readonly float m;
    int value;
};

function testRelationalExprAsKeyValue() {
    table<Row7> key(k, m) tbl = table [
       {k: 10 < 20, m: 20.5, value: 17},
       {k: INT_VAL1 < 20, m: 20.5, value: 17}
    ];

    tbl.add({k: 10 >= 20, m: 30.5, value: 25});
    tbl.add({k: INT_VAL2 >= 20, m: 30.5, value: 25});
    var tbl2 = table key(k, m) [
        {k: true, m: 20.5, value: 17},
        {k: false, m: 20.5, value: 17},
        {k: false, m: 30.5, value: 25},
        {k: true, m: 30.5, value: 25}
    ];
    assertEqual(tbl2, tbl);

    Row7 row = {k: true, m: 20.5, value: 17};
    assertEqual(row, tbl.get([true, 20.5]));

    error? err = trap tbl.add({k: 5 > 12, m: 30.5, value: 25});
    assertEqual(true, err is error);
    if (err is error) {
        map<string> msg = {"message":"a value found for key '[false,30.5]'"};
        assertEqual(msg, err.detail());
    }
}

function testIsExprAsKeyValue() {
    table<Row7> key(k, m) tbl = table [
       {k: INT_VAL1 is int, m: 20.5, value: 17}
    ];

    tbl.add({k: INT_VAL2 !is int, m: 30.5, value: 25});
    var tbl2 = table key(k) [{k: true, m: 20.5, value: 17}, {k: false, m: 30.5, value: 25}];
    assertEqual(tbl2, tbl);

    Row7 row = {k: true, m: 20.5, value: 17};
    assertEqual(row, tbl.get([true, 20.5]));

    error? err = trap tbl.add({k: false, m: 30.5, value: 25});
    assertEqual(true, err is error);
    if (err is error) {
        map<string> msg = {"message":"a value found for key '[false,30.5]'"};
        assertEqual(msg, err.detail());
    }
}

function testEqualityExprAsKeyValue() {
    table<Row7> key(k, m) tbl = table [
       {k: INT_VAL1 == 55, m: 20.5, value: 17}
    ];

    tbl.add({k: INT_VAL1 === INT_VAL2, m: 30.5, value: 25});
    var tbl2 = table key(k) [{k: true, m: 20.5, value: 17}, {k: false, m: 30.5, value: 25}];
    assertEqual(tbl2, tbl);

    Row7 row = {k: true, m: 20.5, value: 17};
    assertEqual(row, tbl.get([true, 20.5]));

    error? err = trap tbl.add({k: 20 != 20, m: 30.5, value: 25});
    assertEqual(true, err is error);
    if (err is error) {
        map<string> msg = {"message":"a value found for key '[false,30.5]'"};
        assertEqual(msg, err.detail());
    }
}

function testBinaryBitwiseExprAsKeyValue() {
    table<Row1> key(k) tbl = table [
       {k: 100 | 10, value: 17},
       {k: INT_VAL1 | 10, value: 17}
    ];

    tbl.add({k: 100 & 1000, value: 25});
    tbl.add({k: 100 & INT_VAL2, value: 25});
    var tbl2 = table key(k) [
        {k: 110, value: 17},
        {k: 63, value: 17},
        {k: 96, value: 25},
        {k: 4, value: 25}
    ];
    assertEqual(tbl2, tbl);

    Row1 row = {k: 96, value: 25};
    assertEqual(row, tbl.get(96));

    error? err = trap tbl.add({k: 10 | 100, value: 17});
    assertEqual(true, err is error);
    if (err is error) {
        map<string> msg = {"message":"a value found for key '110'"};
        assertEqual(msg, err.detail());
    }
}

const boolean BOOL_VAL1 = true;
const boolean BOOL_VAL2 = false;
function testLogicalExprAsKeyValue() {
    table<Row7> key(k, m) tbl = table [
       {k: BOOL_VAL1 || BOOL_VAL2, m: 20.5, value: 17}
    ];

    tbl.add({k: BOOL_VAL1 && BOOL_VAL2, m: 30.5, value: 25});
    var tbl2 = table key(k) [{k: true, m: 20.5, value: 17}, {k: false, m: 30.5, value: 25}];
    assertEqual(tbl2, tbl);

    Row7 row = {k: true, m: 20.5, value: 17};
    assertEqual(row, tbl.get([true, 20.5]));

    error? err = trap tbl.add({k: BOOL_VAL1 && BOOL_VAL2, m: 30.5, value: 25});
    assertEqual(true, err is error);
    if (err is error) {
        map<string> msg = {"message":"a value found for key '[false,30.5]'"};
        assertEqual(msg, err.detail());
    }
}

function testConditionalExprAsKeyValue() {
    table<Row1> key(k) tbl = table [
       {k: INT_VAL1 is int ? 10 : 20, value: 17}
    ];

    tbl.add({k: INT_VAL1 !is int ? 30 : 40, value: 25});
    var tbl2 = table key(k) [{k: 10, value: 17}, {k: 40, value: 25}];
    assertEqual(tbl2, tbl);

    Row1 row = {k: 40, value: 25};
    assertEqual(row, tbl.get(40));

    error? err = trap tbl.add({k: INT_VAL1 !is int ? 20 : 40, value: 17});
    assertEqual(true, err is error);
    if (err is error) {
        map<string> msg = {"message":"a value found for key '40'"};
        assertEqual(msg, err.detail());
    }
}

function testGroupExprAsKeyValue() {
    table<Row1> key(k) tbl = table [
       {k: (INT_VAL1 is int ? (<int> 10.5) : 20), value: 17}
    ];

    tbl.add({k: (INT_VAL1 !is int ? 30 : (<int> 40.5)), value: 25});
    var tbl2 = table key(k) [{k: 10, value: 17}, {k: 40, value: 25}];
    assertEqual(tbl2, tbl);

    Row1 row = {k: 40, value: 25};
    assertEqual(row, tbl.get(40));

    error? err = trap tbl.add({k: (INT_VAL1 !is int ? 20 : 40), value: 17});
    assertEqual(true, err is error);
    if (err is error) {
        map<string> msg = {"message":"a value found for key '40'"};
        assertEqual(msg, err.detail());
    }
}

type A record {
    readonly int a;
};

type Row8 record {
    readonly table<A>|A k;
    int value;
};

function testKeyCollision() {
    table<Row8> key(k) tbl = table [
       {k: table [{a: 1}], value: 17},
       {k: {a: 1}, value: 17}
    ];

    error? err = trap tbl.add({k: table [{a: 1}], value: 17});
    assertEqual(true, err is error);
    if (err is error) {
        map<string> msg = {"message":"a value found for key '[{\"a\":1}]'"};
        assertEqual(msg, err.detail());
    }
}

type Row9 record {
    readonly string:RegExp k;
    int value;
};

function testRegExpAsKeyValue() {
    table<Row9> key(k) tbl = table [
       {k: re `AB*[^abc-efg](?:A|B|[ab-fgh]+(?im-x:[cdeg-k]??)|)|^|PQ?`, value: 17}
    ];

    tbl.add({k: re `AB*[^abc-efg](?:A|B|[ab-fgh]+(?i-x:[cdeg-k]??)|)|^|PR?`, value: 25});
    var tbl2 = table key(k) [{ k: re `AB*[^abc-efg](?:A|B|[ab-fgh]+(?im-x:[cdeg-k]??)|)|^|PQ?`, value: 17 },
                             {k: re `AB*[^abc-efg](?:A|B|[ab-fgh]+(?i-x:[cdeg-k]??)|)|^|PR?`, value: 25}];
    assertEqual(tbl2, tbl);

    Row9 row = {k: re `AB*[^abc-efg](?:A|B|[ab-fgh]+(?im-x:[cdeg-k]??)|)|^|PQ?`, value: 17};
    assertEqual(row, tbl.get(re `AB*[^abc-efg](?:A|B|[ab-fgh]+(?im-x:[cdeg-k]??)|)|^|PQ?`));

    error? err = trap tbl.add({k: re `AB*[^abc-efg](?:A|B|[ab-fgh]+(?im-x:[cdeg-k]??)|)|^|PQ?`, value: 20});
    assertEqual(true, err is error);
    if (err is error) {
        map<string> msg = {"message":"a value found for key 'AB*[^abc-efg](?:A|B|[ab-fgh]+(?im-x:[cdeg-k]??)|)|^|PQ?'"};
        assertEqual(msg, err.detail());
    }

    table<Row9> key(k)|error err2 = trap getTable();
    assertEqual(true, err2 is error);
    if (err2 is error) {
        map<string> msg = {"message":"a value found for key 'AB*[^abc-efg](?:A|B|[ab-fgh]+(?im-x:[cdeg-k]??)|)|^|PQ?'"};
        assertEqual(msg, err2.detail());
    }

    table<Row9> key(k) tbl3 = table [
       {k: re `[a-z]??|(AB)*`, value: 17}
    ];

    tbl3.add({k: re `[a-z]?|(AC){1}`, value: 25});
    var tbl4 = table key(k) [{ k: re `[a-z]??|(AB)*`, value: 17 },
                             {k: re `[a-z]?|(AC){1}`, value: 25}];
    assertEqual(tbl4, tbl3);

    Row9 row2 = {k: re `[a-z]?|(AC){1}`, value: 25};
    assertEqual(row2, tbl3.get(re `[a-z]?|(AC){1}`));

    error? err3 = trap tbl3.add({k: re `[a-z]??|(AB)*`, value: 20});
    assertEqual(true, err3 is error);
    if (err3 is error) {
        map<string> msg = {"message":"a value found for key '[a-z]??|(AB)*'"};
        assertEqual(msg, err3.detail());
    }

    string a = "ABC";
    table<Row9> key(k) tbl5 = table [
       {k: re `[^a-g]??|(${a}DEF(${a+"FGH"})+)*`, value: 17}
    ];

    tbl5.add({k: re `^${a}*(?i-m:${123*10}{1,5})$${30.toString()}+`, value: 25});
    var tbl6 = table key(k) [{ k: re `[^a-g]??|(${a}DEF(${a+"FGH"})+)*`, value: 17 },
                             {k: re `^${a}*(?i-m:${123*10}{1,5})$${30.toString()}+`, value: 25}];
    assertEqual(tbl6, tbl5);

    Row9 row3 = {k: re `^${a}*(?i-m:${123*10}{1,5})$${30.toString()}+`, value: 25};
    assertEqual(row3, tbl5.get(re `^${a}*(?i-m:${123*10}{1,5})$${30.toString()}+`));

    error? err4 = trap tbl5.add({k: re `[^a-g]??|(${a}DEF(${a+"FGH"})+)*`, value: 20});
    assertEqual(true, err4 is error);
    if (err4 is error) {
        map<string> msg = {"message":"a value found for key '[^a-g]??|(ABCDEF(ABCFGH)+)*'"};
        assertEqual(msg, err4.detail());
    }
}

function getTable() returns table<Row9> key(k) {
    return table [{k: re `AB*[^abc-efg](?:A|B|[ab-fgh]+(?im-x:[cdeg-k]??)|)|^|PQ?`, value: 17},
                  {k: re `AB*[^abc-efg](?:A|B|[ab-fgh]+(?im-x:[cdeg-k]??)|)|^|PQ?`, value: 17}];
}

type Row10 record {
    readonly string:RegExp|string k;
    int value;
};

type RegExpTable table<Row10> key(k);

function testKeyCollisionWithStringAndRegExpAsKeyValues() {
    table<Row10> key(k) tbl = table [
       {k: re `AB*(?:[a-f])`, value: 17}
    ];

    tbl.add({k: string `AB*(?:[a-f])`, value: 17});
    var tbl2 = table key(k) [{ k: re `AB*(?:[a-f])`, value: 17 },
                             {k: string `AB*(?:[a-f])`, value: 17}];
    assertEqual(tbl, tbl2);

    Row10 row = {k: re `AB*(?:[a-f])`, value: 17};
    assertEqual(row, tbl.get(re `AB*(?:[a-f])`));

    RegExpTable tbl3 = table [
       {k: re `AB*(?:[a-f])`, value: 17},
       {k: string `AB*(?:[a-f])`, value: 17}
    ];
    assertEqual(tbl, tbl3);

    table<Row10> key(k) tbl4 = table [
       {k: re `AB*(?:[a-f])`, value: 17},
       {k: "AB*(?:[a-f])", value: 17}
    ];
    assertEqual(tbl, tbl4);

    RegExpTable tbl5 = table [
       {k: re `AB*(?:[a-f])`, value: 17}
    ];
    tbl5.add({k: "AB*(?:[a-f])", value: 17});
    assertEqual(tbl, tbl5);
}

type Row12 record {|
    readonly string key1;
    readonly string key2;
    string a;
|};

function testStringAsKeyValue() {
    Row12 expectedRec = {key1: "k1", key2: "k2", a: "n2"};

    table<Row12> key(key1) tbl = table [
        {key1: "k1", key2: "k2", a: "n1"}
    ];
    table<Row12> tbl2 = table [
        {key1: "k1", key2: "k2", a: "n2"}
    ];

    // Test the put method of the table
    tbl.put(expectedRec.clone());

    table<Row12> tbl1 = from Row12 r in tbl
        where r.key1 == "k1" && r.key2 == "k2"
        select r;
    assertEqual(tbl2, tbl1);

    // Test the get method of the table
    readonly & string keyString = "k1";
    Row12 outputRec = tbl.get(keyString);
    assertEqual(expectedRec, outputRec);

    // Test the add method of the table
    error? err = trap tbl.add(expectedRec);
    assertEqual(err is error, true);
    assertEqual((<error>err).message(), "{ballerina/lang.table}KeyConstraintViolation");

    // Test the remove method of the table
    Row12 removedRec = tbl.remove(keyString);
    assertEqual(expectedRec, removedRec);
    assertEqual(tbl.length(), 0);
}

function testStringAsCompositeKeyValue() {
    Row12 expectedRec = {key1: "k1", key2: "k2", a: "n2"};

    table<Row12> key(key1, key2) tbl = table [
        {key1: "k1", key2: "k2", a: "n1"}
    ];
    table<Row12> tbl2 = table [
        {key1: "k1", key2: "k2", a: "n2"}
    ];

    // Test the put method of the table
    tbl.put(expectedRec.clone());

    table<Row12> tbl1 = from Row12 r in tbl
        where r.key1 == "k1" && r.key2 == "k2"
        select r;
    assertEqual(tbl2, tbl1);

    // Test the get method of the table
    [string & readonly, string & readonly] keyTuple = ["k1", "k2"];
    Row12 outputRec = tbl.get(keyTuple);
    assertEqual(expectedRec, outputRec);

    // Test the add method of the table
    error? err = trap tbl.add(expectedRec);
    assertEqual(err is error, true);
    assertEqual((<error>err).message(), "{ballerina/lang.table}KeyConstraintViolation");

    // Test the remove method of the table
    Row12 removedRec = tbl.remove(keyTuple);
    assertEqual(expectedRec, removedRec);
    assertEqual(tbl.length(), 0);
}

type Row13 record {|
    readonly map<string> keys;
    readonly map<int> values;
    string a;
|};

function testMapAsCompositeKeyValue() {
    Row13 expectedRec = {keys: {"k1": "v1", "k2": "v2"}, values: {"k1": 1, "k2": 2}, a: "n2"};

    table<Row13> key(keys, values) tbl = table [
        {keys: {"k1": "v1", "k2": "v2"}, values: {"k1": 1, "k2": 2}, a: "n1"}
    ];
    table<Row13> tbl2 = table [
        {keys: {"k1": "v1", "k2": "v2"}, values: {"k1": 1, "k2": 2}, a: "n2"}
    ];

    // Test the put method of the table
    tbl.put(expectedRec.clone());

    table<Row13> tbl1 = from Row13 r in tbl
        where r.keys == {"k1": "v1", "k2": "v2"} && r.values == {"k1": 1, "k2": 2}
        select r;
    assertEqual(tbl2, tbl1);

    // Test the get method of the table
    [map<string> & readonly, map<int> & readonly] keyTuple = [{"k1": "v1", "k2": "v2"}, {"k1": 1, "k2": 2}];
    Row13 outputRec = tbl.get(keyTuple);
    assertEqual(expectedRec, outputRec);

    // Test the add method of the table
    error? err = trap tbl.add(expectedRec);
    assertEqual(err is error, true);
    assertEqual((<error>err).message(), "{ballerina/lang.table}KeyConstraintViolation");

    // Test the remove method of the table
    Row13 removedRec = tbl.remove(keyTuple);
    assertEqual(expectedRec, removedRec);
    assertEqual(tbl.length(), 0);
}

type Row14 record {|
    readonly string[] keys;
    readonly int[] values;
    string a;
|};

function testArrayAsCompositeKeyValue() {
    Row14 expectedRec = {keys: ["k1", "k2"], values: [1, 2], a: "n2"};

    table<Row14> key(keys, values) tbl = table [
        {keys: ["k1", "k2"], values: [1, 2], a: "n1"}
    ];
    table<Row14> tbl2 = table [
        {keys: ["k1", "k2"], values: [1, 2], a: "n2"}
    ];

    // Test the put method of the table
    tbl.put(expectedRec.clone());

    table<Row14> tbl1 = from Row14 r in tbl
        where r.keys == ["k1", "k2"] && r.values == [1, 2]
        select r;
    assertEqual(tbl2, tbl1);

    // Test the get method of the table
    [string[] & readonly, int[] & readonly] keyTuple = [["k1", "k2"], [1, 2]];
    Row14 outputRec = tbl.get(keyTuple);
    assertEqual(expectedRec, outputRec);

    // Test the add method of the table
    error? err = trap tbl.add(expectedRec);
    assertEqual(err is error, true);
    assertEqual((<error>err).message(), "{ballerina/lang.table}KeyConstraintViolation");

    // Test the remove method of the table
    Row14 removedRec = tbl.remove(keyTuple);
    assertEqual(expectedRec, removedRec);
    assertEqual(tbl.length(), 0);
}

function assertEqual(any expected, any actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected.toString();
    string actualValAsString = actual.toString();
    panic error(string `Assertion error: expected ${expectedValAsString} found ${actualValAsString}`);
}
