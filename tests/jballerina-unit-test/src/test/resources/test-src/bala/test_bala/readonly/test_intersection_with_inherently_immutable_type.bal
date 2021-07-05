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

import testorg/immutable;

function testEnumIntersectionWithReadOnly() {
    any a = immutable:evictionPolicy;
    assertTrue(a is readonly & any);
    assertTrue(a is immutable:EvictionPolicy);
    assertTrue(a is immutable:LRU);
    assertFalse(a is int);

    anydata b = immutable:ep;
    assertTrue(b is readonly & anydata);
    assertTrue(b is immutable:EvictionPolicies);
    assertTrue(b is immutable:A);
    assertFalse(b is immutable:B);

    anydata[] c = immutable:epArr;
    anydata[] d = c.cloneReadOnly();
    assertTrue(d is readonly & anydata[]);
    assertTrue(d is immutable:EvictionPolicies[] & readonly);
    assertTrue(c is immutable:EvictionPolicies[]);
    assertFalse(c is immutable:EvictionPolicies[] & readonly);
    assertEquality(c, d);
    assertEquality(<immutable:EvictionPolicies[]> [immutable:A, immutable:B], d);
}

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

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error("expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}

