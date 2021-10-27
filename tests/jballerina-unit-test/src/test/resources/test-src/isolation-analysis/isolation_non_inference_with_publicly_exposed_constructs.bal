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

////////////// Shouldn't result in warnings. //////////////

service "InferredServiceDecl" on new Listener() {
    final int i = 1;
    private int j = 0;

    resource function get foo() {
        lock {
            self.j += 1;
        }
    }

    remote function bar() {
    }
}

service class NonPubliclyExposedInferredClass {
    final int i = 1;
    private int j = 0;

    resource function get foo() {
        lock {
            self.j += 1;
        }
    }

    remote function bar() {
    }
}

NonPubliclyExposedInferredClassUsedInNonPublicVar nonPublicModuleLevelVar1 = new;
[int, NonPubliclyExposedInferredClassUsedInNonPublicVar] nonPublicModuleLevelVar2 = [1, new];

service class NonPubliclyExposedInferredClassUsedInNonPublicVar {
    final int i = 1;
    private int j = 0;

    resource function get foo() {
        lock {
            self.j += 1;
        }
    }

    remote function bar() {
    }
}

type RecordWithInferableClass record {|
    NonPubliclyExposedInferredClassUsedInNonPublicTypes x;
|};

type ArrayWithInferableClass NonPubliclyExposedInferredClassUsedInNonPublicTypes[];

type TupleWithInferableClass [NonPubliclyExposedInferredClassUsedInNonPublicTypes, string, PublicRecord];

type ErrorWithInferableClass error<record {| NonPubliclyExposedInferredReadOnlyClassUsedInNonPublicTypes cl; |}>;

type FunctionWithInferableClass function (NonPubliclyExposedInferredClassUsedInNonPublicTypes a,
                                          NonPubliclyExposedInferredClassUsedInNonPublicTypes b = new,
                                          NonPubliclyExposedInferredClassUsedInNonPublicTypes... c)
                                    returns NonPubliclyExposedInferredReadOnlyClassUsedInNonPublicTypes;

type MapWithInferableClass map<NonPubliclyExposedInferredReadOnlyClassUsedInNonPublicTypes>;

type StreamWithInferableClass stream<NonPubliclyExposedInferredReadOnlyClassUsedInNonPublicTypes,
                                     error<map<NonPubliclyExposedInferredReadOnlyClassUsedInNonPublicTypes>>?>;

type TypedescWithInferableClass typedesc<NonPubliclyExposedInferredReadOnlyClassUsedInNonPublicTypes>;

type UnionWithInferableClass NonPubliclyExposedInferredReadOnlyClassUsedInNonPublicTypes|int;

type IntersectionWithInferableClass NonPubliclyExposedInferredReadOnlyClassUsedInNonPublicTypes & readonly;

type ObjectWithInferableClass object {
    NonPubliclyExposedInferredReadOnlyClassUsedInNonPublicTypes x;

    public function func(NonPubliclyExposedInferredClassUsedInNonPublicTypes a,
                                          NonPubliclyExposedInferredClassUsedInNonPublicTypes b = new,
                                          NonPubliclyExposedInferredClassUsedInNonPublicTypes... c)
                                    returns NonPubliclyExposedInferredReadOnlyClassUsedInNonPublicTypes;
};

class ClassWithInferableClass {
    NonPubliclyExposedInferredReadOnlyClassUsedInNonPublicTypes x = new;

    function func(NonPubliclyExposedInferredClassUsedInNonPublicTypes a,
                                          NonPubliclyExposedInferredClassUsedInNonPublicTypes b = new,
                                          NonPubliclyExposedInferredClassUsedInNonPublicTypes... c)
                                    returns NonPubliclyExposedInferredReadOnlyClassUsedInNonPublicTypes => new;
}

type FutureWithInferableClass future<NonPubliclyExposedInferredReadOnlyClassUsedInNonPublicTypes>;

service class NonPubliclyExposedInferredClassUsedInNonPublicTypes {
    final int i = 1;
    private int j = 0;

    resource function get foo() {
        lock {
            self.j += 1;
        }
    }

    remote function bar() {
    }
}

readonly service class NonPubliclyExposedInferredReadOnlyClassUsedInNonPublicTypes {
    int i = 1;
    int[] j = [];

    resource function get foo() returns int[] => self.j;

    remote function bar() {
        _ = self.i;
    }
}

public type PublicRecord record {
    int i = 0;
};

////////////// Should result in warnings. //////////////

public ClassPubliclyExposedViaVariable moduleLevelVar = new;

service class ClassPubliclyExposedViaVariable {
    final int i = 1;
    private int j = 0;

    resource function get foo() {
        lock {
            self.j += 1;
        }
    }

    remote function bar() {
    }
}

public type PublicRecordWithInferableClass record {|
    PubliclyExposedInferableClassUsedInRecord x;
|};

service class PubliclyExposedInferableClassUsedInRecord {
    final int i = 1;
    private int j = 0;

    resource function get foo() {
        lock {
            self.j += 1;
        }
    }

    remote function bar() {
    }

    function baz() {
    }
}

public type PublicArrayWithInferableClass PubliclyExposedInferableClassUsedInArray[];

service class PubliclyExposedInferableClassUsedInArray {
    final int i = 1;
    private int j = 0;

    resource function get foo() {
        lock {
            self.j += 1;
        }
    }

    remote function bar() {
    }
}

public type PublicTupleWithInferableClass [PubliclyExposedInferableClassUsedInTuple, string, PublicRecord];

service class PubliclyExposedInferableClassUsedInTuple {
    final int i = 1;
    private int j = 0;

    resource function get foo() {
        lock {
            self.j += 1;
        }
    }

    remote function bar() {
    }
}

service readonly class PubliclyExposedInferableClassUsedInError {
    final int i = 1;
    private int j = 0;

    resource function get foo() {
        lock {
            _ = 1;
        }
    }

    remote function bar() {
    }
}

public type PublicErrorWithInferableClass error<record {| PubliclyExposedInferableClassUsedInError cl; |}>;

public type PublicFunctionWithInferableClass function (PubliclyExposedInferableClassUsedInFunctionTypeRequiredParam a,
                                          PubliclyExposedInferableClassUsedInFunctionTypeDefaultableParam b = new,
                                          PubliclyExposedInferableClassUsedInFunctionTypeRestParam... c)
                                    returns PubliclyExposedInferableClassUsedInFunctionTypeReturnType;

service class PubliclyExposedInferableClassUsedInFunctionTypeRequiredParam {
    final int i = 1;
    private int j = 0;

    resource function get foo() {
        lock {
            self.j += 1;
        }
    }

    remote function bar() {
    }
}

service class PubliclyExposedInferableClassUsedInFunctionTypeDefaultableParam {
    final int i = 1;
    private int j = 0;

    resource function get foo() {
        lock {
            self.j += 1;
        }
    }

    remote function bar() {
    }
}

service class PubliclyExposedInferableClassUsedInFunctionTypeRestParam {
    final int i = 1;
    private int j = 0;

    resource function get foo() {
        lock {
            self.j += 1;
        }
    }

    remote function bar() {
    }
}

service class PubliclyExposedInferableClassUsedInFunctionTypeReturnType {
    final int i = 1;
    private int j = 0;

    resource function get foo() {
        lock {
            self.j += 1;
        }
    }

    remote function bar() {
    }
}

public type PublicMapWithInferableClass map<PubliclyExposedInferableClassUsedInMap>;

service class PubliclyExposedInferableClassUsedInMap {
    final int i = 1;
    private int j = 0;

    resource function get foo() {
        lock {
            self.j += 1;
        }
    }

    remote function bar() {
    }
}

public type PublicStreamWithInferableClass stream<PubliclyExposedInferableClassUsedInStream,
                                     error<map<PubliclyExposedInferableClassUsedInError>>?>;

service class PubliclyExposedInferableClassUsedInStream {
    final int i = 1;
    private int j = 0;

    resource function get foo() {
        lock {
            self.j += 1;
        }
    }

    remote function bar() {
    }
}

public type PublicTypedescWithInferableClass typedesc<PubliclyExposedInferableClassUsedInTypedesc>;

service class PubliclyExposedInferableClassUsedInTypedesc {
    final int i = 1;
    private int j = 0;

    resource function get foo() {
        lock {
            self.j += 1;
        }
    }

    remote function bar() {
    }
}

public type PublicUnionWithInferableClass string|PubliclyExposedInferableClassUsedInUnion?;

service class PubliclyExposedInferableClassUsedInUnion {
    final int i = 1;
    private int j = 0;

    resource function get foo() {
        lock {
            self.j += 1;
        }
    }

    remote function bar() {
    }
}

public type PublicIntersectionWithInferableClass PubliclyExposedInferableClassUsedInIntersection & readonly;

service class PubliclyExposedInferableClassUsedInIntersection {
    final int i = 1;
    private int j = 0;

    resource function get foo() {
        lock {
            self.j += 1;
        }
    }

    remote function bar() {
    }
}

public type PublicObjectWithInferableClass object {
    PubliclyExposedInferableClassUsedInObjectField x;

    function func(PubliclyExposedInferableClassUsedInObjectMethodParam a,
                                          PubliclyExposedInferableClassUsedInObjectMethodParam b = new,
                                          PubliclyExposedInferableClassUsedInObjectMethodParam... c)
                                    returns PubliclyExposedInferableClassUsedInObjectMethodReturnType;
};

service class PubliclyExposedInferableClassUsedInObjectField {
    final int i = 1;
    private int j = 0;

    resource function get foo() {
        lock {
            self.j += 1;
        }
    }

    remote function bar() {
    }
}

service class PubliclyExposedInferableClassUsedInObjectMethodParam {
    final int i = 1;
    private int j = 0;

    resource function get foo() {
        lock {
            self.j += 1;
        }
    }

    remote function bar() {
    }
}

service class PubliclyExposedInferableClassUsedInObjectMethodReturnType {
    final int i = 1;
    private int j = 0;

    resource function get foo() {
        lock {
            self.j += 1;
        }
    }

    remote function bar() {
    }
}

service class PubliclyExposedInferableClassUsedInClassField {
    final int i = 1;
    private int j = 0;

    resource function get foo() {
        lock {
            self.j += 1;
        }
    }

    remote function bar() {
    }
}

service class PubliclyExposedInferableClassUsedInClassMethodParam {
    final int i = 1;
    private int j = 0;

    resource function get foo() {
        lock {
            self.j += 1;
        }
    }

    remote function bar() {
    }
}

service class PubliclyExposedInferableClassUsedInClassMethodReturnType {
    final int i = 1;
    private int j = 0;

    resource function get foo() {
        lock {
            self.j += 1;
        }
    }

    remote function bar() {
    }
}

public class PublicClassWithInferableClass {
    PubliclyExposedInferableClassUsedInClassField x = new;

    public function func(PubliclyExposedInferableClassUsedInClassMethodParam a,
                                          PubliclyExposedInferableClassUsedInClassMethodParam b = new,
                                          PubliclyExposedInferableClassUsedInClassMethodParam... c)
                                    returns PubliclyExposedInferableClassUsedInClassMethodReturnType => new;
}

service class PubliclyExposedInferableClassUsedInFunctionParam {
    final int i = 1;
    private int j = 0;

    resource function get foo() {
        lock {
            self.j += 1;
        }
    }

    remote function bar() {
    }
}

service class PubliclyExposedInferableClassUsedInFunctionReturnType {
    final int i = 1;
    private int j = 0;

    resource function get foo() {
        lock {
            self.j += 1;
        }
    }

    remote function bar() {
    }
}


public type PublicFutureWithInferableClass future<PubliclyExposedInferableClassUsedInFuture>;

service class PubliclyExposedInferableClassUsedInFuture {
    final int i = 1;
    private int j = 0;

    resource function get foo() {
        lock {
            self.j += 1;
        }
    }

    remote function bar() {
    }
}

public function func(PubliclyExposedInferableClassUsedInFunctionParam a,
                                      PubliclyExposedInferableClassUsedInFunctionParam b = new,
                                      PubliclyExposedInferableClassUsedInFunctionParam... c)
                                returns PubliclyExposedInferableClassUsedInFunctionReturnType => new;


// Listener for service declaration.
class Listener {
    public function attach(service object {} s, string|string[]? name = ()) returns error? {
        return;
    }

    public function detach(service object {} s) returns error? {
        return;
    }

    public function 'start() returns error? {
        return;
    }

    public function gracefulStop() returns error? {
        return;
    }

    public function immediateStop() returns error? {
        return;
    }
}
