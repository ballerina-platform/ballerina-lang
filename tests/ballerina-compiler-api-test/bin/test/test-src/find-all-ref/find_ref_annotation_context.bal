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

type Annot record {
    string foo;
    int bar?;
};

public annotation Annot v1 on type, class;
annotation Annot[] v2 on class;
public annotation Annot v3 on function;
annotation Annot v4 on parameter;
public annotation v5 on return;

string strValue = "v1 value";

@v1 {
    foo: strValue,
    bar: 1
}
public type T1 record {
    string name;
};

T1 a = { name: "John" };

function testTypeAnnotAccess() {
    typedesc<any> t = typeof a;
    Annot? annot = t.@v1;
}

@v1 {
    foo: strValue
}
@v2 {
    foo: "v2 value 1"
}
@v2 {
    foo: "v2 value 2"
}
class Foo {
    string name = "ballerina";

    @v3 {
        foo: "v31 value"
    }
    public function setName(@v4 { foo: "v41 value required" } string name,
                            @v4 { foo: "v41 value defaultable" } int id = 0,
                            @v4 { foo: "v41 value rest" } string... others) returns @v5 () {
        self.name = name;
    }
}

annotation vx on type;

public function testAnnot() {
    typedesc<any> td = int;
    _ = td.@vx;
    typedesc _ = td;
}
