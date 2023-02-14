// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function testRegExpNegative() {
    string:RegExp _ = re `[z-a]`;
    string:RegExp _ = re `A{5,2}`;
    string:RegExp _ = re `[z-a]{5}`;
    string:RegExp _ = re `[z-a]{5,}`;
    string:RegExp _ = re `(?i-i:A)`;
    string:RegExp _ = re `(?im-ms:A)`;
    string:RegExp _ = re `(?i-msi:A)`;
    string:RegExp _ = re `(?imsi:A)`;
    var _ = re `(?s-ism:A)`;
    var _ = re `(?s-ism:[z-a]{5,2})`;
    var _ = re `[z-a]A{4,2}[a-bf-F]{1,}(?im-ms:[c-a](?ixmx:[z-a](?ixm-xs:[r-m])))`;
    string:RegExp _ = re `Dd\csdgfdsdfs`;
}
