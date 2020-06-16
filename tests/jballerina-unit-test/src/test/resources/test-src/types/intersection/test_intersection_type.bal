// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function testIntersectionTypes() {
    testIntersectionWithMemberTypeDefinedAfter();
    testIntersectionInUnion();
    testComplexIntersectionsInUnions();
}

type ReadOnlyFoo readonly & Foo;

function testIntersectionWithMemberTypeDefinedAfter() {
    ReadOnlyFoo rf = {i: 100, "j": "hello world"};
    Foo f = rf;
    assertTrue(f is Foo & readonly);
    assertTrue(f is ReadOnlyFoo);
    assertEquality(<Foo> {i: 100, "j": "hello world"}, f);

    var fn = function () {
        f.i = 200;
    };
    error? res = trap fn();
    assertTrue(res is error);

    error err = <error> res;
    assertEquality("cannot update 'readonly' field 'i' in record of type '(Foo & readonly)'", err.detail()["message"]);
}

type Foo record {
    int i;
};

function testIntersectionInUnion() {
    (Foo & readonly)? a = ();
    Foo? & readonly b = {i: 100};

    (Foo & readonly)? c = b;
    Foo? & readonly d = a;

    anydata ad1 = c;
    anydata ad2 = d;

    assertTrue(c.isReadOnly());
    assertTrue(d.isReadOnly());
    assertTrue(ad1.isReadOnly());
    assertTrue(ad2.isReadOnly());
    assertEquality(<Foo> {i: 100}, c);
    assertEquality((), d);
}

type InboundHandler abstract object {
    public function canProcess(int req) returns boolean;
    readonly boolean enabled;
};

type IntermediateInboundHandler abstract object {
    *InboundHandler;
    readonly int allow;
};

type InboundHandlerImpl object {
    *IntermediateInboundHandler;
    readonly int count;

    public function init(int allow, boolean enabled = false) {
        self.enabled = enabled;
        self.allow = allow;
        self.count = allow * 2;
    }

    public function canProcess(int req) returns boolean {
        return self.enabled && req < self.allow;
    }
};

type InboundHandlers InboundHandler[]|InboundHandler[][];

type Handlers record {|
    (InboundHandlers & readonly)? handlers;
|};

type NonImmutableHandlers record {|
    InboundHandlers|int? handlers?;
|};

function testComplexIntersectionsInUnions() {
    InboundHandler[] & readonly arr = [new InboundHandlerImpl(10, true), new InboundHandlerImpl(20)];

    Handlers h1 = {handlers: arr};
    NonImmutableHandlers h2 = h1;

    Handlers & readonly h3 = {handlers: arr};
    NonImmutableHandlers & readonly h4 = h3;

    assertTrue(h2?.handlers is InboundHandlers & readonly);
    assertTrue(h2?.handlers is readonly);

    InboundHandlers hds = <InboundHandlers> h2?.handlers;
    InboundHandler[] hdArray = <InboundHandler[]> hds;

    assertTrue(hdArray[0].canProcess(5));
    assertFalse(hdArray[0].canProcess(12));
    assertFalse(hdArray[1].canProcess(10));
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertTrue(any|error actual) {
    assertEquality(true, actual);
}

function assertFalse(any|error actual) {
    assertEquality(false, actual);
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON, message = "expected '" + expected.toString() + "', found '" +
                                                        actual.toString () + "'");
}
