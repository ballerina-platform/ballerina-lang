// Copyright (c) 2023 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

import bir/objs;

public type Foo distinct isolated client object {
    public isolated function execute() returns anydata|error;
};

public type Quxx isolated client object {
    public isolated function execute() returns anydata|error;
};

objs:Qux quxMod = isolated service object {
    public isolated function execute() returns anydata|error {
         return "qux";
    }
};

function testObjectTypeAssignabilityWithQualifiers() {
    objs:Foo foo = isolated client object {
         public isolated function execute() returns anydata|error {
           return "foo";
        }
    };
    assertTrue(foo is objs:Foo);
    assertTrue(<any>foo is objs:Foo);
    assertFalse(<any>foo is objs:Bar);
    assertFalse(<any>foo is Foo);
    
    Foo foo2 = isolated client object {
        public isolated function execute() returns anydata|error {
            return "foo";
        }
    };
    assertFalse(<any>foo2 is objs:Foo);
    
    objs:Baz baz = isolated client object {
        public isolated function execute() returns anydata|error {
            return "baz";
        }
    };
    assertTrue(<any>baz is objs:Baz);
    
    objs:Xyz xyz = isolated service object {
        public isolated function execute() returns anydata|error {
            return "xyz";
        }
    };
    assertTrue(xyz is objs:Xyz);
    assertTrue(<any>xyz is objs:Xyz);
    
    objs:Qux qux = isolated service object {
        public isolated function execute() returns anydata|error {
            return "qux";
        }
    };
    assertTrue(<any>qux is objs:Qux);
    assertTrue(<any>quxMod is objs:Qux);
    
    objs:Quxx quxx = isolated client object {
        public isolated function execute() returns anydata|error {
            return "quxx";
        }
    };
    
    objs:Muxx _ = quxx;
    Quxx _ = quxx;
    assertTrue(quxx is Quxx);
    
    Quxx _ = isolated client object {
        public isolated function execute() returns anydata|error {
            return "quxx";
        }
    };
    objs:Quxx _ = quxx;
    assertTrue(quxx is objs:Quxx);
}

function assertTrue(anydata actual) {
    assertEquality(actual, true);
}

function assertFalse(anydata actual) {
    assertEquality(actual, false);
}

function assertEquality(anydata actual, anydata expected) {
    if expected == actual {
        return;
    }

    string expectedValAsString = expected.toString();
    string actualValAsString = actual.toString();
    panic error("AssertionError",
            message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
