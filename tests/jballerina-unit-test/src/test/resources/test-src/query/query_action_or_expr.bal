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

function testQueryActionOrExprWithStartAction() returns error? {
    future<int>[] a = from int i in 1...5
                      select start getInt(i);
    int[] res = check getIntsFromFutureInts(a);
    assertEquality([2, 3, 4, 5, 6], res);

    future<int>[] b = from int i in 1...5
                      let future<int> futureVal = start getInt(i)
                      let int intVal = check wait futureVal
                      select start getInt(intVal);
    res = check getIntsFromFutureInts(b);
    assertEquality([3, 4, 5, 6, 7], res);
}

function testQueryActionOrExprWithParenthesizedStartAction() returns error? {
    future<int>[] a = from int i in 1...5
                      select (start getInt(i));
    int[] res = check getIntsFromFutureInts(a);
    assertEquality([2, 3, 4, 5, 6], res);

    future<int>[] b = from int i in 1...5
                      let future<int> futureVal = (start getInt(i))
                      let int intVal = check wait futureVal
                      select (start getInt(intVal));
    res = check getIntsFromFutureInts(b);
    assertEquality([3, 4, 5, 6, 7], res);
}

function testQueryActionOrExprWithWaitAction() {
    (int|error)[] a = from int i in 1...5
                      let future<int> val = start getInt(i)
                      select wait val;
    assertEquality([2, 3, 4, 5, 6], getIntArr(a));

    int[] b = from int i in 1...5
              let future<int> val = start getInt(i)
              let int|error intVal = wait val
              where intVal is int
              select intVal;
    assertEquality([2, 3, 4, 5, 6], b);

    (int|error)[] c = from int i in 1...5
                      let future<int> val = start getInt(i)
                      let int|error intVal = wait val
                      where intVal is int
                      select wait getFutureInt(intVal);
    assertEquality([3, 4, 5, 6, 7], getIntArr(c));
}

function testQueryActionOrExprWithParenthesizedWaitAction() {
    (int|error)[] a = from int i in 1...5
                      let future<int> val = (start getInt(i))
                      select (wait val);
    assertEquality([2, 3, 4, 5, 6], getIntArr(a));

    int[] b = from int i in 1...5
              let future<int> val = (start getInt(i))
              let int|error intVal = (wait val)
              where intVal is int
              select intVal;
    assertEquality([2, 3, 4, 5, 6], b);

    (int|error)[] c = from int i in 1...5
                      let future<int> val = (start getInt(i))
                      let int|error intVal = (wait val)
                      where intVal is int
                      select (wait getFutureInt(intVal));
    assertEquality([3, 4, 5, 6, 7], getIntArr(c));
}

function getInt(int i) returns int {
    return i + 1;
}

function getIntsFromFutureInts(future<int>[] arr) returns int[]|error {
    int[] res = from future<int> i in arr
                select check wait i;
    return res;
}

function getIntArr((int|error)[] arr) returns int[] {
    int[] res = from var i in arr
                where i is int
                select i;
    return res;
}

function getFutureInt(int i) returns future<int> {
    future<int> res = start getInt(i);
    return res;
}

function testQueryActionOrExprWithClientRemoteMethodCall() {
    var obj = client object {
        remote function foo() returns int[] {
            return [1, 2, 3, 4, 5];
        }

        remote function bar() returns int {
            return 2;
        }

        remote function bam(int i, int k) returns int {
            return i * k;
        }
    };

    int[] a = from var i in obj->foo()
        select i * 2;
    assertEquality([2, 4, 6, 8, 10], a);

    int[] b = from var i in obj->foo()
        let int k = obj->bar()
        select i * k;
    assertEquality([2, 4, 6, 8, 10], b);

    int[] c = from var i in obj->foo()
        let int k = obj->bar()
        select obj->bam(i, k);
    assertEquality([2, 4, 6, 8, 10], c);
}

function testQueryActionOrExprWithParenthesizedClientRemoteMethodCall() {
    var obj = client object {
        remote function foo() returns int[] {
            return [1, 2, 3, 4, 5];
        }

        remote function bar() returns int {
            return 2;
        }

        remote function bam(int i, int k) returns int {
            return i * k;
        }
    };

    int[] a = from var i in (obj->foo())
        select i * 2;
    assertEquality([2, 4, 6, 8, 10], a);

    int[] b = from var i in (obj->foo())
        let int k = (obj->bar())
        select i * k;
    assertEquality([2, 4, 6, 8, 10], b);

    int[] c = from var i in (obj->foo())
        let int k = (obj->bar())
        select (obj->bam(i, k));
    assertEquality([2, 4, 6, 8, 10], c);
}

function testQueryActionOrExprWithQueryAction() {
    assertTrue(checkQueryActionExpressionNested() is ());
}

function checkQueryActionExpressionNested() returns error? {
    int sum = 0;
    error?[] a = from int i in 1 ... 5
        select from var j in 1 ... 5
            do {
                sum = sum + j;
            };
    assertEquality(75, sum);

    sum = 0;
    error?[] b = from int i in 1 ... 5
        let error? val = from var j in ["1", "2", "3"]
            do {
                int _ = check int:fromString(j);
            }
        where val is ()
        select from var j in 1 ... 5
            do {
                sum = sum + j;
            };
    assertEquality(75, sum);
}

function testQueryActionOrExprWithParenthesizedQueryAction() {
    assertTrue(checkQueryActionOrExprWithParenthesizedQueryAction() is ());
}

function checkQueryActionOrExprWithParenthesizedQueryAction() returns error? {
    int sum = 0;
    error?[] a = from int i in 1 ... 5
        select (from var j in 1 ... 5
            do {
                sum = sum + j;
            });
    assertEquality(75, sum);

    sum = 0;
    error?[] b = from int i in 1 ... 5
        let error? val = (from var j in ["1", "2", "3"]
            do {
                int _ = check int:fromString(j);
            })
        where val is ()
        select (from var j in 1 ... 5
            do {
                sum = sum + j;
            });
    assertEquality(75, sum);
}

function testQueryActionOrExprWithTypeCastActionOrExpr() returns error? {
    int[] a = from var i in <int[]>f1()
              let int val = 10
              select <int>f2(i, val);
    assertEquality([10, 20, 30], a);

    int[] b = from var i in <int[]>f1()
              let int val = <int>f2(i, 20)
              select <int>f2(i, val);
    assertEquality([20, 80, 180], b);

    future<int>[] c = from int i in 1...5
                      let future<int> futureVal = <future<int>>start getInt(i)
                      let int intVal = <int>check wait futureVal
                      select <future<int>>start getInt(intVal);
    int[] res = check getIntsFromFutureInts(c);
    assertEquality([3, 4, 5, 6, 7], res);
}

function testQueryActionOrExprWithParenthesizedTypeCastActionOrExpr() returns error? {
    int[] a = from var i in (<int[]>f1())
              let int val = 10
              select (<int>f2(i, val));
    assertEquality([10, 20, 30], a);

    int[] b = from var i in (<int[]>f1())
              let int val = (<int>f2(i, 20))
              select (<int>f2(i, val));
    assertEquality([20, 80, 180], b);

    future<int>[] c = from int i in 1...5
                      let future<int> futureVal = (<future<int>>start getInt(i))
                      let int intVal = (<int>check wait futureVal)
                      select (<future<int>>start getInt(intVal));
    int[] res = check getIntsFromFutureInts(c);
    assertEquality([3, 4, 5, 6, 7], res);
}

function f1() returns int[]? {
    return [1, 2 ,3];
}

function f2(int i, int j) returns int? {
    return i * j;
}

function testQueryActionOrExprWithCheckingActionOrExpr() returns error? {
    int[] a = from var i in check f3()
              let int val = 10
              select check f4(i, val);
    assertEquality([10, 20, 30], a);

    var obj = client object {
        remote function foo() returns int[]|error {
            return [1, 2, 3, 4, 5];
        }

        remote function bar() returns int|error {
            return 2;
        }

        remote function bam(int i, int k) returns int|error {
            return i * k;
        }
    };

    int[] b = from var i in check obj->foo()
              let int val = check obj->bar()
              select check obj->bam(i, val);
    assertEquality([2, 4, 6, 8, 10], b);

    int sum = 0;
    int[] c = from var i in check obj->foo()
              let () val = from var j in 1...5
                           do {
                               sum = sum + j;
                           }
              select check f4(i, sum);
    assertEquality([15, 60, 135, 240, 375], c);
}

function testQueryActionOrExprWithParenthesizedCheckingActionOrExpr() returns error? {
    int[] a = from var i in (check f3())
              let int val = 10
              select (check f4(i, val));
    assertEquality([10, 20, 30], a);

    var obj = client object {
        remote function foo() returns int[]|error {
            return [1, 2, 3, 4, 5];
        }

        remote function bar() returns int|error {
            return 2;
        }

        remote function bam(int i, int k) returns int|error {
            return i * k;
        }
    };

    int[] b = from var i in check obj->foo()
              let int val = check obj->bar()
              select check obj->bam(i, val);
    assertEquality([2, 4, 6, 8, 10], b);

    int sum = 0;
    int[] c = from var i in check obj->foo()
              let () val = from var j in 1...5
                           do {
                               sum = sum + j;
                           }
              select check f4(i, sum);
    assertEquality([15, 60, 135, 240, 375], c);
}

function f3() returns int[]|error {
    return [1, 2 ,3];
}

function f4(int i, int j) returns int|error {
    return i * j;
}

function testQueryActionOrExprWithTrapActionOrExpr() returns error? {
    (int|error?)[] a = from var i in check f3()
                       let int|error val = trap getInt(i)
                       where val is int && i is int
                       select trap f2(i, val);
    int[] res = from var i in a
                where i is int
                select i;
    assertEquality([2, 6, 12], res);

    var obj = client object {
        remote function foo() returns int[] {
            return [1, 2, 3, 4, 5];
        }

        remote function bar() returns int {
            return 2;
        }

        remote function bam(int i, int k) returns int {
            return i * k;
        }
    };

    (int|error)[] b = from var i in (obj->foo())
                      let int|error val = trap obj->bar()
                      where val is int
                      select trap obj->bam(i, val);
    assertEquality([2, 4, 6, 8, 10], getIntArr(b));

    (int|error)[] c = from var i in 1...5
                      let int[]|error val = trap from var j in 1...5
                                            select j
                      where val is int[]
                      select trap obj->bam(i, val[3]);
    assertEquality([4, 8, 12, 16, 20], getIntArr(c));
}

function testQueryActionOrExprWithParenthesizedTrapActionOrExpr() returns error? {
    (int|error?)[] a = from var i in (check f3())
                       let int|error val = (trap getInt(i))
                       where val is int && i is int
                       select (trap f2(i, val));
    int[] res = from var i in a
                where i is int
                select i;
    assertEquality([2, 6, 12], res);

    var obj = client object {
        remote function foo() returns int[] {
            return [1, 2, 3, 4, 5];
        }

        remote function bar() returns int {
            return 2;
        }

        remote function bam(int i, int k) returns int {
            return i * k;
        }
    };

    (int|error)[] b = from var i in obj->foo()
                      let int|error val = (trap obj->bar())
                      where val is int
                      select (trap obj->bam(i, val));
    assertEquality([2, 4, 6, 8, 10], getIntArr(b));

    (int|error)[] c = from var i in 1...5
                      let int[]|error val = trap from var j in 1...5
                                            select j
                      where val is int[]
                      select (trap obj->bam(i, val[3]));
    assertEquality([4, 8, 12, 16, 20], getIntArr(c));
}

client class ClientClass {
    remote function foo() returns int[] {
        return [1, 2, 3, 4, 5];
    }

    remote function bar(int i, int j) returns int {
        return i * j;
    }
}

function testQueryActionOrExprWithQueryActionOrExpr() {
    ClientClass cls = new;
    int[] a = from int i in from int j in cls->foo() select cls->bar(j, 10)
              let int[] val = from int j in cls->foo() select cls->bar(j, 10)
              select cls->bar(i, val[2]);
    assertEquality([300, 600, 900, 1200, 1500], a);

    int[][] b = from int i in from int j in cls->foo() select cls->bar(j, 10)
                select from int k in cls->foo() select cls->bar(k, 2);
    assertEquality([[2, 4, 6, 8, 10], [2, 4, 6, 8, 10], [2, 4, 6, 8, 10], [2, 4, 6, 8, 10], [2, 4, 6, 8, 10]], b);
}

function testQueryActionOrExprWithParenthesizedQueryActionOrExpr() {
    ClientClass cls = new;
    int[] a = from int i in (from int j in cls->foo() select cls->bar(j, 10))
              let int[] val = (from int j in cls->foo() select cls->bar(j, 10))
              select cls->bar(i, val[2]);
    assertEquality([300, 600, 900, 1200, 1500], a);

    int[][] b = from int i in (from int j in cls->foo() select cls->bar(j, 10))
                let int[] val = (from int j in cls->foo() select cls->bar(j, 10))
                select (from int k in cls->foo() select cls->bar(k, 2));
    assertEquality([[2, 4, 6, 8, 10], [2, 4, 6, 8, 10], [2, 4, 6, 8, 10], [2, 4, 6, 8, 10], [2, 4, 6, 8, 10]], b);
}

function testPrecedenceOfActionsWithQueryActionOrExpr() returns error? {
    var obj = client object {
        remote function foo() returns int[]|error {
            return [1, 2, 3, 4, 5];
        }

        remote function bar() returns int[] {
            return [1, 2, 3, 4, 5];
        }
    };
    int[] a = from int i in check obj->foo()
              select i;
    assertEquality([1, 2, 3, 4, 5], a);

    int[] b = from int i in check (obj->foo())
              select i;
    assertEquality([1, 2, 3, 4, 5], b);

    int[] c = from int i in check <int[]>obj->bar()
              select i;
    assertEquality([1, 2, 3, 4, 5], c);

    int[] d = from int i in check (<int[]>(obj->bar()))
              select i;
    assertEquality([1, 2, 3, 4, 5], d);
}

function testQueryActionOrExprWithAllQueryClauses() {
    var obj = client object {
        remote function foo() returns (int|string)[] {
            return [1, 2, "C", 4, "E"];
        }

        remote function bar(int i) returns int {
            return i + 2;
        }

        remote function bam(int i, int j) returns int {
            return i * j;
        }
    };
    int x = 10;
    var a = from var i in obj->foo()
            let int val = 5
            where i is int
            let int val2 = obj->bar(i + val)
            order by i descending
            limit 2
            select obj->bam(i, x * val2);
    assertEquality([440, 180], a);
}

function testQueryActionOrExprWithNestedQueryActionOrExpr() {
    var obj = client object {
        remote function foo() returns (int|string)[] {
            return [1, 2, "C", 4, "E"];
        }

        remote function bar(int i) returns int {
            return i + 2;
        }

        remote function bam(int i, int j) returns int {
            return i * j;
        }
    };

    var a = from var i in from var j in (from var k in obj->foo() where k is int select k) select obj->bam(j, 2)
            from var l in from var m in (from var n in obj->foo() where n is int select n + 10) select obj->bam(m, 2)
            let (int|string)[] val = from var x in 1...3 select obj->bar(x)
            where val is int[]
            order by l
            limit 4
            select l * i * val[0];
    assertEquality([132, 264, 528, 144], a);

    var b = from var i in from var j in (from var k in obj->foo() where k is int select k) select obj->bam(j, 2)
            from var l in from var m in (from var n in obj->foo() where n is int select n + 10) select obj->bam(m, 2)
            let (int|string)[] val = from var x in 1...3 select obj->bar(x)
            where val is int[]
            order by i descending
            limit 4
            select obj->bam(l * i * val[0], 2);
    assertEquality([1056, 1152, 1344, 528], b);

    var c = from var i in (from var j in (from var k in obj->foo() where k is int select k) select obj->bam(j, 2))
            join var l in (from var m in (from var n in [1, 2, "C", 4, "E"] where n is int select n) select m * 2)
            on i equals l
            let (int|string)[] val = from var x in 1...3 select obj->bar(x)
            where val is int[]
            order by i descending
            limit 4
            select obj->bam(l * i * val[0], 2);
    assertEquality([384, 96, 24], c);
}

public type Token record {|
    readonly int idx;
    string val;
|};

type TokenTable table<Token> key(idx);

function testQueryActionOrExprWithQueryConstructingTable() {
    var obj = client object {
        remote function foo() returns (int|string)[] {
            return [1, 2, "C", 4, "E"];
        }

        remote function bar(int i) returns int {
            return i + 2;
        }

        remote function bam(int i, int j) returns int {
            return i * j;
        }

        remote function getToken(int i, string j) returns Token {
            return {idx: i, val: j};
        }
    };

    TokenTable|error a = table key(idx) from var i in obj->foo()
            where i is int
            let int val = obj->bar(i)
            order by i
            limit 2
            select obj->getToken(i * val, "A")
            on conflict error("Duplicate Keys Error");

    TokenTable expectedTbl1 = table [
            {"idx":3,"val":"A"},
            {"idx":8,"val":"A"}
        ];

    assertEquality(true, a is TokenTable);
    if (a is TokenTable) {
        assertEquality(expectedTbl1, a);
    }

    TokenTable|error b = table key(idx) from var i in [1, 2, 1]
            where i is int
            let int val = obj->bar(i)
            order by i
            limit 2
            select obj->getToken(i * val, "A")
            on conflict error("Duplicate Keys Error");

    assertEquality(true, b is error);
    if (b is error) {
        assertEquality("Duplicate Keys Error", b.message());
    }

    TokenTable|error c = table key(idx) from var i in from var j in (from var k in obj->foo() where k is int select k) select obj->bam(j, 2)
            from var l in from var m in (from var n in obj->foo() where n is int select n + 10) select obj->bam(m, 2)
            let (int|string)[] val = from var x in 1...3 select obj->bar(x)
            where val is int[]
            order by l
            limit 4
            select {
                idx: l * i * val[0],
                val: "A"
            };

    TokenTable expectedTbl2 = table [
            {"idx": 132, "val": "A"},
            {"idx": 264, "val": "A"},
            {"idx": 528, "val": "A"},
            {"idx": 144, "val": "A"}
        ];

    assertEquality(true, c is TokenTable);
    if (c is TokenTable) {
        assertEquality(expectedTbl2, c);
    }
}

function testQueryActionOrExprWithQueryConstructingStream() {
    var obj = client object {
        remote function foo() returns (int|string)[] {
            return [1, 2, "C", 4, "E"];
        }

        remote function bar(int i) returns int {
            return i + 2;
        }

        remote function bam(int i, int j) returns int {
            return i * j;
        }

        remote function getToken(int i, string j) returns Token {
            return {idx: i, val: j};
        }
    };

    stream<Token> a = stream from var i in obj->foo()
            where i is int
            let int val = obj->bar(i)
            order by i
            limit 2
            select obj->getToken(i * val, "A");

    record {| Token value; |}? token = getTokenValue(a.next());
    assertEquality({idx: 3, val: "A"}, token?.value);

    token = getTokenValue(a.next());
    assertEquality({idx: 8, val: "A"}, token?.value);


    stream<Token> b = stream from var i in from var j in (from var k in obj->foo() where k is int select k) select obj->bam(j, 2)
            from var l in from var m in (from var n in obj->foo() where n is int select n + 10) select obj->bam(m, 2)
            let (int|string)[] val = from var x in 1...3 select obj->bar(x)
            where val is int[]
            order by l
            limit 4
            select {
                idx: l * i * val[0],
                val: "A"
            };

    token = getTokenValue(b.next());
    assertEquality({idx: 132, val: "A"}, token?.value);

    token = getTokenValue(b.next());
    assertEquality({idx: 264, val: "A"}, token?.value);

    token = getTokenValue(b.next());
    assertEquality({idx: 528, val: "A"}, token?.value);

    token = getTokenValue(b.next());
    assertEquality({idx: 144, val: "A"}, token?.value);
}

type TokenValue record {|
    Token value;
|};

function getTokenValue((record {| Token value; |}|error?)|(record {| Token value; |}?) returnedVal) returns TokenValue? {
    var result = returnedVal;
    if (result is TokenValue) {
        return result;
    } else {
        return ();
    }
}

function testQueryingEmptyTuple() {
    var a = [];
    int count = 0;
    foreach var item in a {
        count += 1;
    }
    assertEquality(0, count);
    count = 0;

    foreach var item in [] {
        count += 1;
    }
    assertEquality(0, count);
}

type Book record {|
    readonly int id;
    string name;
|};

const PATH = "someLongPathSegment";

client class MyClient {
    resource function get .() returns string {
        return "book1";
    }

    resource function get books/names() returns string[2] {
        return ["book1", "book2"];
    }

    resource function get books/[int id]() returns string {
        return "book" + id.toString();
    }

    resource function get [int number]() returns string {
        return string`data${number}`;
    }

    resource function bookDetails .(int no, string bookName) returns Book {
            Book b = {id: no, name: bookName};
            return b;
    }

    resource function put books/[PATH](int a) returns string {
        return "book1";
    }

    resource function someOtherMethod books/[PATH...](string a) returns string[] {
        return [a, "book4"];
    }

    resource function post game/[string name]/path/[int players]() returns string {
        return name + ": " + players.toString();
    }
}

client class MyClient2 {
    resource function get [string... a](string... b) returns string[][] {
        return [a, b];
    }

    resource function get1 [string](int a, int b) returns int? {
        return a;
    }

    resource function get path/[int a]/path2/[string](string b, int... c) returns [int, string, int[]] {
        return [a, b, c];
    }

    resource function put [int](int a, int b = 5) returns int[2] {
        return [a, b];
    }
}

type Rec record {|
    int a;
    string b;
|};

client class MyClient3 {
    resource function get [string]() returns Rec[] {
        return [{a: 1, b: "A"}, {a: 2, b: "B"}];
    }

    resource function put path/[string](Rec r) returns int {
        return r.a;
    }
}

function testQueryActionOrExprWithClientResourceAccessAction() {
    MyClient myClient = new;

    string[] res = from var i in myClient->/books/names
            let string book = myClient->/.get
            where i == book
            select myClient->/books/[1];
    assertEquality(["book1"], res);

    table<Book> key(id) res2 = table key(id) from var b in myClient->/books/names
            let string book = myClient->/.get
            where b == book
            select myClient->/.bookDetails(1, b);
    table<Book> key(id) tbl = table [{id: 1, name: "book1"}];
    assertEquality(tbl, res2);

    "someLongPathSegment" path = "someLongPathSegment";
    var res3 = from var b in myClient->/books/names
            let string book = myClient->/books/someLongPathSegment.put(1)
            where b == book
            select myClient->/.bookDetails(1, b);
    assertEquality([{id: 1, name: "book1"}], res3);

    var res4 = from var b in myClient->/books/names
            select myClient->/books/someLongPathSegment.someOtherMethod(b);
    assertEquality([["book1", "book4"], ["book2", "book4"]], res4);

    var res5 = from var b in [1, 2]
            select myClient->/[b];
    assertEquality(["data1", "data2"], res5);

    var res6 = from var b in [1, 2]
            let [string, "path", int] gameDetails2 = ["Carrom", "path", 4]
            select myClient->/game/[...gameDetails2].post;
    assertEquality(["Carrom: 4", "Carrom: 4"], res6);

    MyClient2 myClient2 = new;

    var res7 = from string[] b in myClient2->/books/books2
            where b.length() > 1
            select myClient2->/(b[0], "pen");
    assertEquality([[[], ["books", "pen"]]], res7);

    var res8 = from int b in myClient2->/[5].put(1, 6)
            let int[] arr = myClient2->/[5].put(b)
            select myClient2->/path/[arr[0]]/path2/path3("book", arr[1]);
    assertEquality([[1, "book", [5]], [6, "book", [5]]], res8);

    MyClient3 myClient3 = new;

    string a = "a";
    var res9 = from Rec r in myClient3->/a.get()
            where r.a == 1
            select myClient3->/path/[a].put(r);
    assertEquality([1], res9);
}

function testQueryActionOrExprWithGroupedClientResourceAccessAction() {
    MyClient myClient = new;

    string[] res = from var i in (myClient->/books/names)
            let string book = (myClient->/.get)
            where i == book
            select (myClient->/books/[1]);
    assertEquality(["book1"], res);

    table<Book> key(id) res2 = table key(id) from var b in (myClient->/books/names)
            let string book = myClient->/.get
            where b == book
            select (myClient->/.bookDetails(1, b));
    table<Book> key(id) tbl = table [{id: 1, name: "book1"}];
    assertEquality(tbl, res2);

    "someLongPathSegment" path = "someLongPathSegment";
    var res3 = from var b in myClient->/books/names
            let string book = (myClient->/books/someLongPathSegment.put(1))
            where b == book
            select (myClient->/.bookDetails(1, b));
    assertEquality([{id: 1, name: "book1"}], res3);
}

function testNestedQueryActionOrExprWithClientResourceAccessAction() {
    MyClient myClient = new;

    Book[] res = from var i in (from string k in myClient->/books/names
                         let string book = myClient->/.get
                         where k == book
                         select myClient->/books/[1])
            select myClient->/.bookDetails(1, i);
    assertEquality([{id: 1, name: "book1"}], res);

    "someLongPathSegment" path = "someLongPathSegment";
    var res2 = from var b in myClient->/books/names
            from string c in myClient->/books/someLongPathSegment.someOtherMethod("book2")
            where b == c
            select myClient->/.bookDetails(1, b);
    assertEquality([{id: 1, name: "book2"}], res2);

    string b1 = "book1";
    var res3 = from var b in myClient->/books/names
            from string c in myClient->/books/someLongPathSegment.someOtherMethod(b1)
            where b == c
            select myClient->/books/someLongPathSegment.someOtherMethod(b);
    assertEquality([["book1", "book4"]], res3);
}

function testQueryActionWithQueryExpression() {
    string[] res = [];
    int[] res2 = [];

    from var item in from string letter in ["a", "b", "c"] select letter
    do {
        res.push(item);
    };

    from var x in from int num in [2, -3, -4, 5] where num > 0 select num * num
    do {
        res2.push(x);
    };

    assertEquality([4, 25], res2);
}

function testQueryActionWithRegexpLangLibs() {
    string[] res = [];

    from var item in ["a", "aab", "bc", "ac"] 
    do {
        if re `a.*`.isFullMatch(item) {
            res.push(item);
        }
    };
    
    assertEquality(["a", "aab", "ac"], res);
}

function testQueryExprWithRegExpLangLibs() {
    string[] res = from var item in ["a", "aab", "bc", "ac"] 
    where re `a.*`.isFullMatch(item)
    select item;
    
    assertEquality(["a", "aab", "ac"], res);
}

function testQueryActionWithInterpolationRegexpLangLibs() {
    string[] res = [];
    string pattern = "a.*";
    from var item in ["aa", "aaab", "bc", "aac"] 
    do {
        if re `a${pattern}`.isFullMatch(item) {
            res.push(item);
        }
    };

    assertEquality(["aa", "aaab", "aac"], res);
}

type T1 record {
    T3[] t3s;
};

type T2 record {
    T3[]|T4[] t3OrT4;
};

type T3 record {
    string str;
};

type T4 record {
    boolean foo;
};

function transform(T1 t1) returns T2 => {
    t3OrT4: from var t3sItem in t1.t3s
        select {
           str: "transformed_" + t3sItem.str
        }
};

function testQueryActionOrExpressionWithUnionRecordResultType() {
    T1 t1 = {t3s: [{str: "str1"}, {str: "str2"}]};
    T2 t2 = transform(t1);
    assertEquality([{str: "transformed_str1"}, {str: "transformed_str2"}], t2.t3OrT4);
}

type Student record {
    string firstName;
    string lastName;
    int intakeYear;
    float gpa;
};

function calGraduationYear(int year) returns int => year + 5;

function getBestStudents() returns any|error {
    Student[] studentList = getStudents();

    return from var student in studentList
        where student.gpa >= 2.0
        let string degreeName = "Bachelor of Medicine", int graduationYear = calGraduationYear(student.intakeYear)
        order by student.gpa descending
        limit 2
        select {name: student.firstName + " " + student.lastName, degree: degreeName, graduationYear};
}

function testQueryActionOrExprWithAnyOrErrResultType() {
    assertTrue(getBestStudents() is record {|string name; string degree; int graduationYear;|}[]);
}

function getStudents() returns Student[] {
    return [
        {firstName: "Martin", lastName: "Sadler", intakeYear: 1990, gpa: 3.5},
        {firstName: "Ranjan", lastName: "Fonseka", intakeYear: 2001, gpa: 1.9},
        {firstName: "Michelle", lastName: "Guthrie", intakeYear: 2002, gpa: 3.7},
        {firstName: "George", lastName: "Fernando", intakeYear: 2005, gpa: 4.0}
    ];
}

function testQueryActionWithLetExpression() {
    Student[] studentList = getStudents();
    float[] actualGpaList = [];
    _ = from var {gpa} in studentList
        do {
            float gpaVal = let var sGpa = gpa in sGpa;
            actualGpaList.push(gpaVal);
        };
    assertEquality([3.5, 1.9, 3.7, 4.0], actualGpaList);

    int[] actualValues = [];
    int weight = 2;
    _ = from var i in [1, 2, 3, 4]
        do {
            int gpaVal = let var sGpa = i in weight * i;
            actualValues.push(gpaVal);
        };
    assertEquality([2, 4, 6, 8], actualValues);
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}

function assertTrue(anydata actual) {
    return assertEquality(true, actual);
}
