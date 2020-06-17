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

type Annot record {
    string foo;
    int bar?;
};

public annotation Annot v1 on type;
public annotation Annot v3 on function;

string strValue = "v1 value";

@v1 {
    foo: strValue,
    bar: 1
}
public type T1 record {
    string name;
};

T1 a = { name: "John" };

function testRecordTypeAnnotationReadonlyValueEdit()  {
    typedesc<any> t = typeof a;
    Annot? annot = t.@v1;
    if (annot is Annot) {
        annot.foo = "EDITED";
    }
}

@v1 {
    foo: strValue
}
type T2 object {
    string name = "ballerina";
};

function testAnnotationOnObjectTypeReadonlyValueEdit() {
    T2 c = new;
    typedesc<any> t = typeof c;
    Annot? annot = t.@v1;
    if (annot is Annot) {
        annot.foo = "EDITED";
    }
}

@v3 {
    foo: "func",
    bar: 1
}
function funcWithAnnots() {}

function testAnnotationOnFunctionTypeReadonlyValueEdit() {
    typedesc<any> t = typeof funcWithAnnots;
    Annot? annot = t.@v3;
    if (annot is Annot) {
        annot.foo = "EDITED";
    }
}
