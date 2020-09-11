// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/lang.'object as lang;

type Annot record {
    string val;
};

public annotation Annot v1 on type;
annotation Annot v2 on class;
public annotation Annot v3 on function;
annotation map<int> v4 on object function;
public annotation Annot v5 on resource function;
annotation Annot v6 on parameter;
public annotation v7 on return;
annotation Annot v8 on service;

public const annotation map<string> v9 on source listener;
const annotation map<string> v10 on source annotation;
const annotation map<int> v11 on source var;
public const annotation map<string> v12 on source const;
const annotation map<string> v13 on source external;
const annotation map<boolean> v15 on source worker;

@v2 {
    val: "v2"
}
@v3 {
    val: "v3"
}
@v4 {
    val: 4
}
@v5 {
    val: "v5"
}
@v6 {
    val: "v6"
}
@v7
@v8 {
    val: "v8"
}
@v9 {
    val: "v9"
}
@v10 {
    val: "v10"
}
@v11 {
    val: 11
}
@v12 {
    val: "v12"
}
@v13 {
    val: "v13"
}
@v15 {
    val: false
}
public type T1 record {
    string name;
};

@v3 {
    val: "v3"
}
@v4 {
    val: 4
}
@v5 {
    val: "v5"
}
@v6 {
    val: "v6"
}
@v7
@v8 {
    val: "v8"
}
@v9 {
    val: "v9"
}
@v10 {
    val: "v10"
}
@v11 {
    val: 11
}
@v12 {
    val: "v12"
}
@v13 {
    val: "v13"
}
@v15 {
    val: false
}
class T2 {
    string name = "ballerina";

    @v1 {
        val: "v1"
    }
    @v2 {
        val: "v2"
    }
    @v5 {
        val: "v5"
    }
    @v6 {
        val: "v6"
    }
    @v7
    @v8 {
        val: "v8"
    }
    @v9 {
        val: "v9"
    }
    @v10 {
        val: "v10"
    }
    @v11 {
        val: 11
    }
    @v12 {
        val: "v12"
    }
    @v13 {
        val: "v13"
    }
    @v15 {
        val: false
    }
    public function setName(string name) {
        self.name = name;
    }

    @v1 {
        val: "v1"
    }
    @v2 {
        val: "v2"
    }
    @v5 {
        val: "v5"
    }
    @v6 {
        val: "v6"
    }
    @v7
    @v8 {
        val: "v8"
    }
    @v9 {
        val: "v9"
    }
    @v10 {
        val: "v10"
    }
    @v11 {
        val: 11
    }
    @v12 {
        val: "v12"
    }
    @v13 {
        val: "v13"
    }
    @v15 {
        val: false
    }
    public function getName() returns string { return self.name; }
}

//@v1 {
//    val: "v1"
//}
//@v2 {
//    val: "v2"
//}
//@v5 {
//    val: "v5"
//}
//@v6 {
//    val: "v6"
//}
//@v7
//@v8 {
//    val: "v8"
//}
//@v9 {
//    val: "v9"
//}
//@v10 {
//    val: "v10"
//}
//@v11 {
//    val: 11
//}
//@v12 {
//    val: "v12"
//}
//@v13 {
//    val: "v13"
//}
//@v15 {
//    val: false
//}
//public function T2.getName() returns string {
//    return self.name;
//}

@v1 {
    val: "v1"
}
@v2 {
    val: "v2"
}
@v4 {
    val: 4
}
@v5 {
    val: "v5"
}
@v6 {
    val: "v6"
}
@v7
@v8 {
    val: "v8"
}
@v9 {
    val: "v9"
}
@v10 {
    val: "v10"
}
@v11 {
    val: 11
}
@v12 {
    val: "v12"
}
@v13 {
    val: "v13"
}
@v15 {
    val: false
}
public function func() returns int {
    return 1;
}

public function funcWithParam(@v1 {
                                val: "v1"
                            }
                            @v2 {
                                val: "v2"
                            }
                            @v3 {
                                val: "v2"
                            }
                            @v4 {
                                val: 4
                            }
                            @v5 {
                                val: "v5"
                            }
                            @v7
                            @v8 {
                                val: "v8"
                            }
                            @v9 {
                                val: "v9"
                            }
                            @v10 {
                                val: "v10"
                            }
                            @v11 {
                                val: 11
                            }
                            @v12 {
                                val: "v12"
                            }
                            @v13 {
                                val: "v13"
                            }
                            @v15 {
                                val: false
                            } string param) returns @v1 {
                                                        val: "v1"
                                                    }
                                                    @v2 {
                                                        val: "v2"
                                                    }
                                                    @v3 {
                                                        val: "v2"
                                                    }
                                                    @v4 {
                                                        val: 4
                                                    }
                                                    @v5 {
                                                        val: "v5"
                                                    }
                                                    @v6 {
                                                        val: "v6"
                                                    }
                                                    @v8 {
                                                        val: "v8"
                                                    }
                                                    @v9 {
                                                        val: "v9"
                                                    }
                                                    @v10 {
                                                        val: "v10"
                                                    }
                                                    @v11 {
                                                        val: 11
                                                    }
                                                    @v12 {
                                                        val: "v12"
                                                    }
                                                    @v13 {
                                                        val: "v13"
                                                    }
                                                    @v15 {
                                                        val: false
                                                    } int {
    return 1;
}

@v1 {
    val: "v1"
}
@v2 {
    val: "v2"
}
@v3 {
    val: "v3"
}
@v4 {
    val: 4
}
@v5 {
    val: "v5"
}
@v6 {
    val: "v6"
}
@v7
@v8 {
    val: "v8"
}
@v10 {
    val: "v10"
}
@v11 {
    val: 11
}
@v12 {
    val: "v12"
}
@v13 {
    val: "v13"
}
@v15 {
    val: false
}
listener Listener lis = new;

@v1 {
    val: "v1"
}
@v2 {
    val: "v2"
}
@v3 {
    val: "v3"
}
@v4 {
    val: 4
}
@v5 {
    val: "v5"
}
@v6 {
    val: "v6"
}
@v7
@v9 {
    val: "v9"
}
@v10 {
    val: "v10"
}
@v11 {
    val: 11
}
@v12 {
    val: "v12"
}
@v13 {
    val: "v13"
}
@v15 {
    val: false
}
service ser on lis {

    @v1 {
        val: "v1"
    }
    @v2 {
        val: "v2"
    }
    @v4 {
        val: 4
    }
    @v6 {
        val: "v6"
    }
    @v7
    @v8 {
        val: "v8"
    }
    @v9 {
        val: "v9"
    }
    @v10 {
        val: "v10"
    }
    @v11 {
        val: 11
    }
    @v12 {
        val: "v12"
    }
    @v13 {
        val: "v13"
    }
    @v15 {
        val: false
    }
    resource function res() {

    }
}

class Listener {
    *lang:Listener;

    public function init() {
    }

    public function __attach(service s, string? name = ()) returns error? {
    }

    public function __detach(service s) returns error? {
    }

    public function __start() returns error? {
    }

    public function __gracefulStop() returns error? {
        return ();
    }

    public function __immediateStop() returns error? {
        return ();
    }
}

@v1 {
    val: "v1"
}
@v2 {
    val: "v2"
}
@v3 {
    val: "v3"
}
@v4 {
    val: 4
}
@v5 {
    val: "v5"
}
@v6 {
    val: "v6"
}
@v7
@v8 {
    val: "v8"
}
@v9 {
    val: "v9"
}
@v11 {
    val: 11
}
@v12 {
    val: "v12"
}
@v13 {
    val: "v13"
}
@v15 {
    val: false
}
const annotation map<string> v14 on source annotation;

@v1 {
    val: "v1"
}
@v2 {
    val: "v2"
}
@v3 {
    val: "v3"
}
@v4 {
    val: 4
}
@v5 {
    val: "v5"
}
@v6 {
    val: "v6"
}
@v7
@v8 {
    val: "v8"
}
@v9 {
    val: "v9"
}
@v10 {
    val: "v10"
}
@v12 {
    val: "v12"
}
@v13 {
    val: "v13"
}
@v15 {
    val: false
}
int i = 12;

int b = let @v1 {
                val: "v1"
            }
            @v2 {
                val: "v2"
            }
            @v3 {
                val: "v3"
            }
            @v4 {
                val: 4
            }
            @v5 {
                val: "v5"
            }
            @v6 {
                val: "v6"
            }
            @v7
            @v8 {
                val: "v8"
            }
            @v9 {
                val: "v9"
            }
            @v10 {
                val: "v10"
            }
            @v12 {
                val: "v12"
            }
            @v13 {
                val: "v13"
            }
            @v15 {
                val: false
            } int x = 4 in 2 * x;

@v1 {
    val: "v1"
}
@v2 {
    val: "v2"
}
@v3 {
    val: "v3"
}
@v4 {
    val: 4
}
@v5 {
    val: "v5"
}
@v6 {
    val: "v6"
}
@v7
@v8 {
    val: "v8"
}
@v9 {
    val: "v9"
}
@v10 {
    val: "v10"
}
@v11 {
    val: 11
}
@v13 {
    val: "v13"
}
@v15 {
    val: false
}
const f = 123.4;

function externalFunctionOne(int fi, float ff) returns int = @v1 {
                                                                val: "v1"
                                                            }
                                                            @v2 {
                                                                val: "v2"
                                                            }
                                                            @v3 {
                                                                val: "v3"
                                                            }
                                                            @v4 {
                                                                val: 4
                                                            }
                                                            @v5 {
                                                                val: "v5"
                                                            }
                                                            @v6 {
                                                                val: "v6"
                                                            }
                                                            @v7
                                                            @v8 {
                                                                val: "v8"
                                                            }
                                                            @v9 {
                                                                val: "v9"
                                                            }
                                                            @v10 {
                                                                val: "v10"
                                                            }
                                                            @v11 {
                                                                val: 11
                                                            }
                                                            @v12 {
                                                                val: "v12"
                                                            }
                                                            @v15 {
                                                                val: false
                                                            } external;

@v8 {
    val: "invalid"
}
service serVar =
@v1 {
    val: "v1"
}
@v2 {
    val: "v2"
}
@v3 {
    val: "v3"
}
@v4 {
    val: 4
}
@v5 {
    val: "v5"
}
@v6 {
    val: "v6"
}
@v7
@v9 {
    val: "v9"
}
@v10 {
    val: "v10"
}
@v11 {
    val: 11
}
@v12 {
    val: "v12"
}
@v13 {
    val: "v13"
}
@v15 {
    val: false
}
service {

    resource function res() {

    }
};

function funcWithWorker() {

    @v1 {
        val: "v1"
    }
    @v2 {
        val: "v2"
    }
    @v3 {
        val: "v3"
    }
    @v4 {
        val: 4
    }
    @v5 {
        val: "v5"
    }
    @v6 {
        val: "v6"
    }
    @v7
    @v8 {
        val: "v8"
    }
    @v9 {
        val: "v9"
    }
    @v10 {
        val: "v10"
    }
    @v11 {
        val: 11
    }
    @v12 {
        val: "v12"
    }
    @v13 {
        val: "v13"
    }
    worker w1 {
        // do nothing
    }
}

future<()> fn =
@v1 {
    val: "v1"
}
@v2 {
    val: "v2"
}
@v3 {
    val: "v3"
}
@v4 {
    val: 4
}
@v5 {
    val: "v5"
}
@v6 {
    val: "v6"
}
@v7
@v8 {
    val: "v8"
}
@v9 {
    val: "v9"
}
@v10 {
    val: "v10"
}
@v11 {
    val: 11
}
@v12 {
    val: "v12"
}
@v13 {
    val: "v13"
}
start funcWithWorker();

public annotation v16 on field;
annotation map<int> v17 on object field;
const annotation v18 on source record field;

@v16 int glob = 1;

@v16
@v17 {}
@v18
function func2() {

}

@v16
@v17 {i: 1}
@v18
type Foo record {
    @v17 {} int i;
};

@v16
@v17 {i: 1}
@v18
class Bar {
    @v18 string s = "str";
}

public const annotation v19 on source type;

function typeConversionExpressionUserFunc() {
    string s = "hello";
    string k = <@v19> s;
    string j = <@v16> s;
}

public const annotation v20 on class;

@v20 @v19 class cls {
    int i;
    function init() {
        self.i = 2;
    }
}
