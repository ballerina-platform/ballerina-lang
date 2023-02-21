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

function testLiteralAsKeyValue() {
    table<Row1> key(k) tbl = table [
       {k: 12, value: 17}
    ];

    tbl.add({k: 13, value: 25});
    var tbl2 = table key(k) [{ k: 12, value: 17 }, {k: 13, value: 25}];
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
       {k: string `ABC`, value: 17}
    ];

    tbl.add({k: string `DEF`, value: 25});
    var tbl2 = table key(k) [{ k: string `ABC`, value: 17 }, {k: string `DEF`, value: 25}];
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
    ];

    tbl.add({k: xml `DEF`, value: 25});
    var tbl2 = table key(k) [{ k: xml `ABC`, value: 17 }, {k: xml `DEF`, value: 25}];
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

function testListConstructorExprAsKeyValue() {
    table<Row4> key(k) tbl = table [
       {k: [12 , 13], value: 17}
    ];

    tbl.add({k: [13 , 14], value: 25});
    var tbl2 = table key(k) [{ k: [12 , 13], value: 17 }, {k: [13 , 14], value: 25}];
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
       {k: table key(k) [{k: 12, value: 17}], value: 17}
    ];

    tbl.add({k: table key(k) [{k: 13, value: 25}], value: 25});
    var tbl2 = table key(k) [{k: table key(k) [{k: 12, value: 17}], value: 17}, {k: table key(k) [{k: 13, value: 25}], value: 25}];
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

function testMappingConstructorExprAsKeyValue() {
    table<Row6> key(k) tbl = table [
       {k: {"A": "a", "B": 12, "C": [13.5, 24.3]}, value: 17}
    ];

    tbl.add({k: {"A": "z", "B": 12, "C": [23.5, 65.3]}, value: 25});
    var tbl2 = table key(k) [{k: {"A": "a", "B": 12, "C": [13.5, 24.3]}, value: 17},
                               {k: {"A": "z", "B": 12, "C": [23.5, 65.3]}, value: 25}];
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

function testTypeCastExprAsKeyValue() {
    table<Row1> key(k) tbl = table [
       {k: <int>1.2, value: 17}
    ];

    tbl.add({k: <int>2.5, value: 25});
    var tbl2 = table key(k) [{k: 1, value: 17}, {k: 2, value: 25}];
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
       {k: +1, value: 17}
    ];

    tbl.add({k: +2, value: 25});
    var tbl2 = table key(k) [{k: 1, value: 17}, {k: 2, value: 25}];
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
       {k: 1 * 10, value: 17}
    ];

    tbl.add({k: 2 * 10, value: 25});
    var tbl2 = table key(k) [{k: 10, value: 17}, {k: 20, value: 25}];
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
       {k: 1 + 10, value: 17}
    ];

    tbl.add({k: 2 + 10, value: 25});
    var tbl2 = table key(k) [{k: 11, value: 17}, {k: 12, value: 25}];
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
       {k: 1 << 64, value: 17}
    ];

    tbl.add({k: 1 << 65, value: 25});
    var tbl2 = table key(k) [{k: 1, value: 17}, {k: 2, value: 25}];
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
       {k: 10 < 20, m: 20.5, value: 17}
    ];

    tbl.add({k: 10 >= 20, m: 30.5, value: 25});
    var tbl2 = table key(k) [{k: true, m: 20.5, value: 17}, {k: false, m: 30.5, value: 25}];
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
    anydata a = 20;
    table<Row7> key(k, m) tbl = table [
       {k: a is int, m: 20.5, value: 17}
    ];

    tbl.add({k: a is map<int>, m: 30.5, value: 25});
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
    anydata a = 20;
    any b = 10;
    table<Row7> key(k, m) tbl = table [
       {k: a == 20, m: 20.5, value: 17}
    ];

    tbl.add({k: a === b, m: 30.5, value: 25});
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
       {k: 100 | 10, value: 17}
    ];

    tbl.add({k: 100 & 1000, value: 25});
    var tbl2 = table key(k) [{k: 110, value: 17}, {k: 96, value: 25}];
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

function testLogicalExprAsKeyValue() {
    boolean a = true;
    boolean b = false;
    table<Row7> key(k, m) tbl = table [
       {k: a || b, m: 20.5, value: 17}
    ];

    tbl.add({k: a && b, m: 30.5, value: 25});
    var tbl2 = table key(k) [{k: true, m: 20.5, value: 17}, {k: false, m: 30.5, value: 25}];
    assertEqual(tbl2, tbl);

    Row7 row = {k: true, m: 20.5, value: 17};
    assertEqual(row, tbl.get([true, 20.5]));

    error? err = trap tbl.add({k: a && b, m: 30.5, value: 25});
    assertEqual(true, err is error);
    if (err is error) {
        map<string> msg = {"message":"a value found for key '[false,30.5]'"};
        assertEqual(msg, err.detail());
    }
}

function testConditionalExprAsKeyValue() {
    anydata a = 10;
    table<Row1> key(k) tbl = table [
       {k: a is int ? 10 : 20, value: 17}
    ];

    tbl.add({k: a is string ? 30 : 40, value: 25});
    var tbl2 = table key(k) [{k: 10, value: 17}, {k: 40, value: 25}];
    assertEqual(tbl2, tbl);

    Row1 row = {k: 40, value: 25};
    assertEqual(row, tbl.get(40));

    error? err = trap tbl.add({k: a is float ? 20 : 40, value: 17});
    assertEqual(true, err is error);
    if (err is error) {
        map<string> msg = {"message":"a value found for key '40'"};
        assertEqual(msg, err.detail());
    }
}

function testGroupExprAsKeyValue() {
    anydata a = 10;
    table<Row1> key(k) tbl = table [
       {k: (a is int ? (<int> 10.5) : 20), value: 17}
    ];

    tbl.add({k: (a is string ? 30 : (<int> 40.5)), value: 25});
    var tbl2 = table key(k) [{k: 10, value: 17}, {k: 40, value: 25}];
    assertEqual(tbl2, tbl);

    Row1 row = {k: 40, value: 25};
    assertEqual(row, tbl.get(40));

    error? err = trap tbl.add({k: (a is float ? 20 : 40), value: 17});
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
       {k: re `[]??|(AB)*`, value: 17}
    ];

    tbl3.add({k: re `[]?|(AC){1}`, value: 25});
    var tbl4 = table key(k) [{ k: re `[]??|(AB)*`, value: 17 },
                             {k: re `[]?|(AC){1}`, value: 25}];
    assertEqual(tbl4, tbl3);

    Row9 row2 = {k: re `[]?|(AC){1}`, value: 25};
    assertEqual(row2, tbl3.get(re `[]?|(AC){1}`));

    error? err3 = trap tbl3.add({k: re `[]??|(AB)*`, value: 20});
    assertEqual(true, err3 is error);
    if (err3 is error) {
        map<string> msg = {"message":"a value found for key '[]??|(AB)*'"};
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
