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

function testQueryActionWithStartAction() returns error? {
    int sum = 0;
    error? a = from int i in 1...5
               let future<int> futureVal = start getInt(i)
               let int intVal = check wait futureVal
               do {
                   future<int> v1 = start getInt(intVal);
                   int v2 = check wait v1;
                   sum += v2;
               };
    assertEquality(true, a is ());
    assertEquality(25, sum);
}

function testQueryActionWithParenthesizedStartAction() returns error? {
    int sum = 0;
    error? a = from int i in 1...5
               let future<int> futureVal = (start getInt(i))
               let int intVal = check wait futureVal
               do {
                   future<int> v1 = start getInt(intVal);
                   int v2 = check wait v1;
                   sum += v2;
               };
    assertEquality(true, a is ());
    assertEquality(25, sum);
}

function testQueryActionWithWaitAction() {
    int sum = 0;
    error? a = from int i in 1...5
               let future<int> val = start getInt(i)
               let int|error intVal = wait val
               where intVal is int
               do {
                   sum += intVal;
               };
    assertEquality(true, a is ());
    assertEquality(20, sum);
}

function testQueryActionWithParenthesizedWaitAction() {
    int sum = 0;
    error? a = from int i in 1...5
               let future<int> val = start getInt(i)
               let int|error intVal = (wait val)
               where intVal is int
               do {
                   sum += intVal;
               };
    assertEquality(true, a is ());
    assertEquality(20, sum);
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

function testQueryActionWithClientRemoteMethodCall() {
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

    int sum = 0;
    error? a = from var i in obj->foo()
               do {
                   sum += i;
               };
    assertEquality(true, a is ());
    assertEquality(15, sum);

    error? b = from var i in obj->foo()
               let int k = obj->bar()
               do {
                   sum += i + k;
               };
    assertEquality(true, b is ());
    assertEquality(40, sum);

    error? c = from var i in obj->foo()
               let int k = obj->bar()
               do {
                   int v1 = obj->bam(i, k);
                   sum += v1;
               };
    assertEquality(true, c is ());
    assertEquality(70, sum);
}

function testQueryActionWithParenthesizedClientRemoteMethodCall() {
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

    int sum = 0;
    error? a = from var i in (obj->foo())
               do {
                   sum += i;
               };
    assertEquality(true, a is ());
    assertEquality(15, sum);

    error? b = from var i in (obj->foo())
               let int k = (obj->bar())
               do {
                   sum += i + k;
               };
    assertEquality(true, b is ());
    assertEquality(40, sum);

    error? c = from var i in (obj->foo())
               let int k = (obj->bar())
               do {
                   int v1 = obj->bam(i, k);
                   sum += v1;
               };
    assertEquality(true, c is ());
    assertEquality(70, sum);
}

function testQueryActionWithQueryAction() {
    assertTrue(checkNestedQueryAction() is ());
}

function checkNestedQueryAction() returns error? {
    int sum = 0;
    error? a = from int i in 1 ... 5
        let error? val = from var j in ["1", "2", "3"]
            do {
                sum += check int:fromString(j);
            }
        where val is ()
        do {
            sum += i;
        };
    assertEquality(true, a is ());
    assertEquality(45, sum);
}

function testQueryActionWithParenthesizedQueryAction() {
    assertTrue(checkQueryActionWithParenthesizedQueryAction() is ());
}

function checkQueryActionWithParenthesizedQueryAction() returns error? {
    int sum = 0;
    error? a = from int i in 1 ... 5
        let error? val = (from var j in ["1", "2", "3"]
            do {
                sum += check int:fromString(j);
            })
        where val is ()
        do {
            sum += i;
        };
    assertEquality(true, a is ());
    assertEquality(45, sum);
}


function testQueryActionWithTypeCastActionOrExpr() returns error? {
    int sum = 0;
    var obj1 = client object {
        remote function foo() returns int[]? {
            return [1, 2, 3];
        }

        remote function bam(int i, int k) returns int {
            return i * k;
        }
    };
    error? a = from var i in <int[]>(obj1->foo())
               let int val = 10
               do {
                   int v1 = <int>f1(i, val);
                   sum += v1 + i;
               };
    assertEquality(true, a is ());
    assertEquality(66, sum);

    sum = 0;
    error? b = from var i in <int[]>(obj1->foo())
               let int val = <int>(obj1->bam(i ,2))
               do {
                   int v1 = <int>f1(i, val);
                   sum += v1 + i;
               };
    assertEquality(true, b is ());
    assertEquality(34, sum);

    sum = 0;
    error? c = from int i in 1...5
               let future<int> futureVal = <future<int>>start getInt(i)
               let int intVal = <int>check wait futureVal
               do {
                   sum += i + intVal;
               };
    assertEquality(true, c is ());
    assertEquality(35, sum);
}

function testQueryActionWithParenthesizedTypeCastActionOrExpr() returns error? {
    int sum = 0;
    var obj1 = client object {
        remote function foo() returns int[]? {
            return [1, 2, 3];
        }

        remote function bam(int i, int k) returns int {
            return i * k;
        }
    };
    error? a = from var i in (<int[]>(obj1->foo()))
               let int val = 10
               do {
                   int v1 = <int>f1(i, val);
                   sum += v1 + i;
               };
    assertEquality(true, a is ());
    assertEquality(66, sum);

    sum = 0;
    error? b = from var i in (<int[]>(obj1->foo()))
               let int val = (<int>(obj1->bam(i ,2)))
               do {
                   int v1 = <int>f1(i, val);
                   sum += v1 + i;
               };
    assertEquality(true, b is ());
    assertEquality(34, sum);

    sum = 0;
    error? c = from int i in 1...5
               let future<int> futureVal = (<future<int>>start getInt(i))
               let int intVal = (<int>check wait futureVal)
               do {
                   sum += i + intVal;
               };
    assertEquality(true, c is ());
    assertEquality(35, sum);
}

function f1(int i, int j) returns int? {
    return i * j;
}

function testQueryActionWithCheckingActionOrExpr() returns error? {
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

    int sum = 0;
    error? a = from var i in check obj->foo()
               let int val = check obj->bar()
               do {
                   int v1 = check obj->bam(i, val);
                   sum += v1;
               };
    assertEquality(true, a is ());
    assertEquality(30, sum);

    sum = 0;
    error? b = from var i in check obj->foo()
               let () val = from var j in 1...5
                            do {
                                sum = sum + j;
                            }
               do {
                   int v1 = check f4(i, sum);
                   sum += v1;
               };
    assertEquality(true, b is ());
    assertEquality(18540, sum);
}

function testQueryActionWithParenthesizedCheckingActionOrExpr() returns error? {
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

    int sum = 0;
    error? a = from var i in (check obj->foo())
               let int val = (check obj->bar())
               do {
                   int v1 = check obj->bam(i, val);
                   sum += v1;
               };
    assertEquality(true, a is ());
    assertEquality(30, sum);

    sum = 0;
    error? b = from var i in (check obj->foo())
               let () val = (from var j in 1...5
                            do {
                                sum = sum + j;
                            })
               do {
                   int v1 = check f4(i, sum);
                   sum += v1;
               };
    assertEquality(true, b is ());
    assertEquality(18540, sum);
}

function f3() returns int[]|error {
    return [1, 2 ,3];
}

function f4(int i, int j) returns int|error {
    return i * j;
}

function testQueryActionWithTrapActionOrExpr() returns error? {
    int sum = 0;
    error? a = from var i in check f3()
               let int|error val = trap getInt(i)
               where val is int && i is int
               do {
                   sum += val + i;
               };
    assertEquality(true, a is ());
    assertEquality(15, sum);

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

    sum = 0;
    error? b = from var i in (obj->foo())
               let int|error val = trap obj->bar()
               where val is int
               let int|error val2 = trap obj->bam(i, val)
               where val2 is int
               do {
                   sum += val + val2;
               };
    assertEquality(true, b is ());
    assertEquality(40, sum);

    sum = 0;
    error? c = from var i in 1...5
               let int[]|error val = trap from var j in 1...5
                                     select j
               where val is int[]
               let int|error val2 = trap obj->bam(i, val[3])
               where val2 is int
               do {
                   sum += val2;
               };
    assertEquality(true, c is ());
    assertEquality(60, sum);
}

function testQueryActionWithParenthesizedTrapActionOrExpr() returns error? {
    int sum = 0;
    error? a = from var i in (check f3())
               let int|error val = (trap getInt(i))
               where val is int && i is int
               do {
                   sum += val + i;
               };
    assertEquality(true, a is ());
    assertEquality(15, sum);

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

    sum = 0;
    error? b = from var i in (obj->foo())
               let int|error val = (trap obj->bar())
               where val is int
               let int|error val2 = (trap obj->bam(i, val))
               where val2 is int
               do {
                   sum += val + val2;
               };
    assertEquality(true, b is ());
    assertEquality(40, sum);

    sum = 0;
    error? c = from var i in 1...5
               let int[]|error val = (trap from var j in 1...5
                                     select j)
               where val is int[]
               let int|error val2 = (trap obj->bam(i, val[3]))
               where val2 is int
               do {
                   sum += val2;
               };
    assertEquality(true, c is ());
    assertEquality(60, sum);
}

client class ClientClass {
    remote function foo() returns int[] {
        return [1, 2, 3, 4, 5];
    }

    remote function bar(int i, int j) returns int {
        return i * j;
    }
}

function testQueryActionWithQueryActionOrExpr() {
    ClientClass cls = new;
    int sum = 0;
    error? a = from int i in from int j in cls->foo() select cls->bar(j, 10)
               let int[] val = from int j in cls->foo() select cls->bar(j, 10)
               let int val2 = cls->bar(i, val[2])
               do {
                   sum += val2;
               };
    assertEquality(true, a is ());
    assertEquality(4500, sum);
}

function testQueryActionWithParenthesizedQueryActionOrExpr() {
    ClientClass cls = new;
    int sum = 0;
    error? a = from int i in (from int j in cls->foo() select cls->bar(j, 10))
               let int[] val = (from int j in cls->foo() select cls->bar(j, 10))
               let int val2 = cls->bar(i, val[2])
               do {
                   sum += val2;
               };
    assertEquality(true, a is ());
    assertEquality(4500, sum);
}

function testPrecedenceOfActionsWhenUsingActionOrExprWithQueryAction() returns error? {
    var obj = client object {
        remote function foo() returns int[]|error {
            return [1, 2, 3, 4, 5];
        }

        remote function bar() returns int[] {
            return [1, 2, 3, 4, 5];
        }
    };

    int sum = 0;
    error? a = from int i in check obj->foo()
               do {
                   sum += i;
               };
    assertEquality(true, a is ());
    assertEquality(15, sum);

    sum = 0;
    error? b = from int i in check (obj->foo())
               do {
                   sum += i;
               };
    assertEquality(true, b is ());
    assertEquality(15, sum);

    sum = 0;
    error? c = from int i in check <int[]>obj->bar()
               do {
                   sum += i;
               };
    assertEquality(true, c is ());
    assertEquality(15, sum);

    sum = 0;
    error? d = from int i in check (<int[]>(obj->bar()))
               do {
                   sum += i;
               };
    assertEquality(true, d is ());
    assertEquality(15, sum);
}

function testQueryActionWithAllQueryClauses() {
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
    int sum = 0;
    var a = from var i in obj->foo()
            let int val = 5
            where i is int
            let int val2 = obj->bar(i + val)
            let int val3 = obj->bam(i, x * val2)
            order by i descending
            limit 2
            do {
                sum = sum + val3;
            } ;
    assertEquality(true, a is ());
    assertEquality(620, sum);
}

function testQueryActionWithNestedQueryActionOrExpr() {
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

    int sum = 0;
    var a = from var i in from var j in (from var k in obj->foo() where k is int select k) select obj->bam(j, 2)
            from var l in from var m in (from var n in obj->foo() where n is int select n + 10) select obj->bam(m, 2)
            let (int|string)[] val = from var x in 1...3 select obj->bar(x)
            where val is int[]
            order by i descending
            limit 4
            do {
                sum = sum + i + l;
            };
    assertEquality(true, a is ());
    assertEquality(124, sum);

    var b = from var i in (from var j in (from var k in obj->foo() where k is int select k) select obj->bam(j, 2))
            join var l in (from var m in (from var n in [1, 2, "C", 4, "E"] where n is int select n) select m * 2)
            on i equals l
            let (int|string)[] val = from var x in 1...3 select obj->bar(x)
            where val is int[]
            order by i descending
            limit 4
            do {
                sum = sum + i + l;
            };
    assertEquality(true, b is ());
    assertEquality(152, sum);
}

type Book record {|
    int id;
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
}

function testQueryActionWithClientResourceAccessAction() {
    MyClient myClient = new;
    string[] arr = [];
    error? res = from var i in myClient->/books/names
            let string book = myClient->/.get
            where i == book
            do {
                string s = myClient->/books/[1];
                arr.push(s);
            };
    assertEquality(true, res is ());
    assertEquality(["book1"], arr);

    error? res2 = from var b in myClient->/books/names
            let Book book = myClient->/.bookDetails(1, b)
            where b == book.name
            do {
                arr.push(book.name);
            };
    assertEquality(true, res2 is ());
    assertEquality(["book1", "book1", "book2"], arr);

    "someLongPathSegment" path = "someLongPathSegment";
    var res3 = from var b in myClient->/books/names
            let string book = myClient->/books/someLongPathSegment.put(1)
            where b == book
            do {
                arr.push(book);
            };
    assertEquality(true, res3 is ());
    assertEquality(["book1", "book1", "book2", "book1"], arr);
}

function testQueryActionWithGroupedClientResourceAccessAction() {
    MyClient myClient = new;
    string[] arr = [];
    error? res = from var i in (myClient->/books/names)
            let string book = (myClient->/.get)
            where i == book
            do {
                string s = myClient->/books/[1];
                arr.push(s);
            };
    assertEquality(true, res is ());
    assertEquality(["book1"], arr);

    error? res2 = from var b in (myClient->/books/names)
            let Book book = (myClient->/.bookDetails(1, b))
            where b == book.name
            do {
                arr.push(book.name);
            };
    assertEquality(true, res2 is ());
    assertEquality(["book1", "book1", "book2"], arr);

    "someLongPathSegment" path = "someLongPathSegment";
    var res3 = from var b in (myClient->/books/names)
            let string book = (myClient->/books/someLongPathSegment.put(1))
            where b == book
            do {
                arr.push(book);
            };
    assertEquality(true, res3 is ());
    assertEquality(["book1", "book1", "book2", "book1"], arr);
}

function testNestedQueryActionWithClientResourceAccessAction() {
    MyClient myClient = new;
    anydata[] arr = [];

    error? res = from var i in (from string k in myClient->/books/names
                         let string book = myClient->/.get
                         where k == book
                         select myClient->/books/[1])
            do {
                Book bk = myClient->/.bookDetails(1, i);
                arr.push(bk);
            };
    assertEquality(true, res is ());
    assertEquality([{id: 1, name: "book1"}], arr);

    "someLongPathSegment" path = "someLongPathSegment";
    var res2 = from var b in myClient->/books/names
            from string c in myClient->/books/someLongPathSegment.someOtherMethod("book2")
            where b == c
            do {
                Book bk = myClient->/.bookDetails(1, b);
                arr.push(bk);
            };
    assertEquality(true, res2 is ());
    assertEquality([{id: 1, name: "book1"}, {id: 1, name: "book2"}], arr);
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
