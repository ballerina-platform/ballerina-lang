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

import listenerProject/ext;

public listener ext:ABCD ep = new ext:ABCD();
public listener ep1 = new ext:ABCD();
listener ep2 = new ext:ABCD();

service sampleService1 on ep {

    resource function foo(string b) {
    }

    resource function bar(string b) {
    }
}

service sampleService2 on ep1 {
    resource function foo(string b) {}
}

service sampleService3 on ep2 {
    resource function foo(string b) {}
}

public function getStartAndAttachCount() {
    string result = ext:getStartAndAttachCount();
    if (result != "3_3") {
        panic error("Attached listener count differ");
    }
}
