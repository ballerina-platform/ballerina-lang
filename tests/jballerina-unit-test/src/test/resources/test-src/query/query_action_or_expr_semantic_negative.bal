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

function testQueryActionOrExprWithStartActionInSelect() {
    _ = from var i in start f1()
        select i;

    _ = from var i in (start f1())
        select i;
}

function testQueryActionOrExprWithWaitActionInSelect() {
    future<int[]> v1 = start f1();
    _ = from var i in wait v1
        select i;

    _ = from var i in (wait v1)
        select i;
}

function f1() returns int[] {
    return [1, 2, 3];
}

function testQueryActionOrExprWithQueryActionInSelect() {
    _ = from var k in from int i in [1, 2, 3] do { int _ = 10; }
        select k;

    _ = from var k in (from int i in [1, 2, 3] do { int _ = 10; })
        select k;
}

function testQueryActionOrExprWithAsyncSendAction() {
    worker w1 {
        int a = 5;
        int b = 0;

        _ = from var i in a -> w6
            let any v = a -> w6
            select a -> w6;
        b = <- w6;
    }

    worker w2 {
        int c = 5;
        int d = 0;

        _ = from var i in c -> w6
            let any v = c -> w6, int x = 1
            select c -> w6;
        d = <- w6;
    }

    worker w3 {
        int e = 5;
        int f = 0;

        _ = from int i in e -> w6
            let any v = e -> w6
            order by i
            select e -> w6;
        f = <- w6;
    }

    worker w4 {
        int g = 5;
        int h = 0;

        _ = from var i in g -> w6
            let any v = g -> w6
            limit 1
            select g -> w6;
        h = <- w6;
    }

    worker w5 {
        int i = 5;
        int j = 0;

        _ = from var m in i -> w6
            let any v = i -> w6
            let int x = 10
            select i -> w6;
        j = <- w6;
    }

    worker w6 {
        int a = 0;
        int b = 15;
        a = <- w1;
        b -> w1;

        int c = 0;
        int d = 15;
        c = <- w2;
        d -> w2;

        int e = 0;
        int f = 15;
        e = <- w3;
        f -> w3;

        int g = 0;
        int h = 15;
        g = <- w4;
        h -> w4;

        int i = 0;
        int j = 15;
        i = <- w5;
        j -> w5;
    }
}

function testQueryActionOrExprWithSyncSendAction() {
    worker w1 {
        int a = 5;
        int b = 0;

        _ = from var i in a ->> w2
            select i;
        b = <- w2;
    }

    worker w2 {
        int a = 0;
        int b = 15;
        a = <- w1;
        b -> w1;
    }
}

function testQueryActionOrExprWithSingleReceiveAction() {
    worker w1 {
        int a = 5;

        a -> w2;
        _ = from var i in <- w2
            select i;
    }

    worker w2 {
        int b = 15;
        _ = <- w1;
        b -> w1;
    }
}

function testQueryActionOrExprWithMultipleReceiveAction() {
    worker w1 {
        int a = 5;
        int b = 0;

        a -> w6;
        _ = from var i in <- {w6, w7}
            let any v = <- {w6, w7}
            select <- {w6, w7};
    }

    worker w2 {
        int c = 5;
        int d = 0;

        c -> w6;
        _ = from var i in 1...3
            let any v = <- {w6, w7}, int x = 1
            select <- {w6, w7};
    }

    worker w3 {
        int e = 5;
        int f = 0;

        e -> w6;
        _ = from int i in 1...3
            let any v = <- {w6, w7}
            order by i
            select <- {w6, w7};
    }

    worker w4 {
        int g = 5;
        int h = 0;

        g -> w6;
        _ = from var i in 1...3
            let any v = <- {w6, w7}
            limit 1
            select <- {w6, w7};
    }

    worker w5 {
        int i = 5;
        int j = 0;

        i -> w6;
        _ = from var m in 1...3
            let any v = <- {w6, w7}
            let int x = 10
            select <- {w6, w7};
    }

    worker w6 {
        int a = 0;
        int b = 15;
        a = <- w1;
        b -> w1;

        int c = 0;
        int d = 15;
        c = <- w2;
        d -> w2;

        int e = 0;
        int f = 15;
        e = <- w3;
        f -> w3;

        int g = 0;
        int h = 15;
        g = <- w4;
        h -> w4;

        int i = 0;
        int j = 15;
        i = <- w5;
        j -> w5;
    }

    worker w7 {
        int a = 0;
        int b = 15;
        a = <- w1;
        b -> w1;

        int c = 0;
        int d = 15;
        c = <- w2;
        d -> w2;

        int e = 0;
        int f = 15;
        e = <- w3;
        f -> w3;

        int g = 0;
        int h = 15;
        g = <- w4;
        h -> w4;

        int i = 0;
        int j = 15;
        i = <- w5;
        j -> w5;
    }
}

function testQueryActionOrExprWithActionsInWhereClause() {
    var obj = client object {
        remote function foo() returns boolean {
            return true;
        }
    };

    _ = from var i in 1...3
        where obj->foo()
        select i;
}

function testQueryActionOrExprWithActionsInOrderByClause() {
    var obj = client object {
        remote function foo() returns int {
            return 1;
        }
    };

    _ = from var i in 1...3
        order by obj->foo()
        select i;
}

function testQueryActionOrExprWithActionsInLimitClause() {
    var obj = client object {
        remote function foo() returns int {
            return 1;
        }
    };

    _ = from var i in 1...3
        limit obj->foo()
        select i;
}

function testQueryActionOrExprWithActionsInJoinClause() {
    var obj = client object {
        remote function foo() returns int[] {
            return [1];
        }
    };

    _ = from var i in 1...3
        join var j in obj->foo()
        on i equals j
        select i;
}

int[][] globalQuery = from var i in 1...3 select start f1();
