// Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

public function bar(string x) returns string {
    return x;
}

public function foo() {
    int a\3 = 0;
    int a3 = 2;
    int student\-performance = 3;
    int student\u{002D}performance = 2;
    string resource\\1path = "https:\\";
    string resource\u{005c}1path = "http";
    _ = bar(resource\\1path);
}
