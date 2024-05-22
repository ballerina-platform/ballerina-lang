// Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

function testSelectClauseWithBindingVar() {
    record {|int nR; int[] valsR;|} a = {nR: 2, valsR: [1, 2, 4, 5]};
    var {nR, valsR} = a;
    int[] times = from int i in valsR
        select nR * i;
    assertEquality(times, [2, 4, 8, 10]);

    [int, int[]] [nT, valsT] = [3, [1, 2, 3, 4, 5]];
    times = from int i in valsT
        select nT * i;
    assertEquality(times, [3, 6, 9, 12, 15]);
}

function testCollectClauseWithBindingVar() {
    record {|int n; int[] vals;|} a = {n: 2, vals: [1, 2, 4, 5]};
    var {n, vals} = a;
    int[] times = from int i in vals
        collect [i].map(j => j * n);
    assertEquality(times, [2, 4, 8, 10]);
}

function testFunctionPointerInvocationWithBindingVar() {
    [int] [t] = [21];
    record {|int r;|} rec = {r: 2};
    var {r} = rec;
    int[] x = from int i in [1, 2, 3]
        let function () returns int func = function() returns int => i * r * t
        select func();
    assertEquality(x, [42, 84, 126]);
}

function testQueryActionWithBindingVar() {
    [int] [t] = [21];
    record {|int r;|} rec = {r: 2};
    var {r} = rec;
    int count = 0;
    int[] intList = [1, 2, 3];

    from var value in intList
    do {
        count += r * value + t;
    };
    assertEquality(count, 75);
}

function testClosureInQueryActionInDoWithBindingVar() {
    record {|int[] lst;|} rec = {lst: [5, 6]};
    [int] [offset] = [1];
    var {lst} = rec;
    int[] arr = [];
    from var j in 1 ... 5
    do {
        from var k in lst
        do {
            arr.push(k + j + offset);
        };
    };
    assertEquality(arr, [7, 8, 8, 9, 9, 10, 10, 11, 11, 12]);
}

type Student record {
    string name;
    int age;
    float[] grades;
};

type Result record {|
    string name;
    float grade;
|};

function testNestedQueryInSelectWithBindingVar() returns () {
    record {|float mx;|} rec = {mx: 80.0};
    [int, float] [ageLimit, bonus] = [18, 5.0];
    var {mx} = rec;

    Student[] students = [
        {name: "Alice", age: 22, grades: [75.2, 88.4, 92.9]},
        {name: "Bob", age: 20, grades: [60.2, 70.3, 85.1]},
        {name: "Charlie", age: 21, grades: [90.7, 95.4, 55.5]}
    ];

    Result[][] results = from var {name, age, grades} in students
        where age > ageLimit
        select from var grade in grades
            where grade < mx
            select {
                name: name,
                grade: grade + bonus
            };

    assertEquality(results,
                   [[{"name": "Alice", "grade": 80.2}],
                    [{"name": "Bob", "grade": 65.2}, {"name": "Bob", "grade": 75.3}],
                    [{"name": "Charlie", "grade": 60.5}]]);
}

function testLimitClauseAndQueryExprWithBindingVar() {
    record {|int lmt; float mx;|} rec = {lmt: 2, mx: 80.0};
    [int, int] [curOutOf, outOf] = [40, 100];
    var {lmt, mx} = rec;

    Result r1 = {name: "Alex", grade: 34};
    Result r2 = {name: "Ranjan", grade: 38};
    Result r3 = {name: "John", grade: 39};
    Result r4 = {name: "Max", grade: 33};
    Result[] resultList = [r1, r2, r3, r4];

    Result[] topList =
            from var result in resultList
    let float newMarks = (result.grade / curOutOf) * outOf
    where newMarks > mx
    limit lmt
    select {
        name: result.name,
        grade: newMarks
    };

    assertEquality(topList, [{"name": "Alex", "grade": 85.0}, {"name": "Ranjan", "grade": 95.0}]);
}

function assertEquality(anydata actual, anydata expected) {
    if expected != actual {
        panic error(string `expected '${expected.toBalString()}', found '${actual.toBalString()}'`);
    }
}
