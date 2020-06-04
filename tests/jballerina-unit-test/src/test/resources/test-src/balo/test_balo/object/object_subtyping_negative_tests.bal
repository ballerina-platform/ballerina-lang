// Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import testorg/foo;

public function testEqOfObjectsWithAndWithoutRemoteMethods1() {
    foo:NonClientObject e = new foo:NonClientObject("email-1");
    foo:ObjectWithRemoteMethod p = e;
}

public function testEqOfObjectsWithAndWithoutRemoteMethods2() {
    foo:ClientObjectWithoutRemoteMethod e = new foo:ClientObjectWithoutRemoteMethod("email-2");
    foo:ObjectWithRemoteMethod p = e;
}

public function testEqOfObjectsWithAndWithoutRemoteMethods3() {
    foo:ClientObjectWithoutRemoteMethod e = new foo:ClientObjectWithoutRemoteMethod("email-3");
    foo:ObjectWithOnlyRemoteMethod p = e;
}

public function testEqOfObjectsWithAndWithoutRemoteMethods4() {
    foo:NonClientObject e = new foo:NonClientObject("email-4");
    foo:ObjectWithOnlyRemoteMethod p = e;
}
