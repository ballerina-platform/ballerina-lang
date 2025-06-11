// Copyright (c) 2023 WSO2 LLC. (http://www.wso2.org).
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

final readonly & map<json> func = {
    "f1": f1()
};

public isolated function testFunc() {
    json _ = func["f1"];
}

public isolated function f1() {
    var _ = testFunc();
}

type Func isolated function ();

final readonly & map<Func?> func1 = {
    "f1": f2()
};

public isolated function testFunc1() {
    Func? _ = func1["f1"];
}

public isolated function f2() {
    var _ = testFunc1();
}
