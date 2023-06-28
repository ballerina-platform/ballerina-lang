// Copyright (c) 2023 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

configurable int intVar = ?;
configurable float floatVar = 9.5;
configurable string stringVar = "hello";
configurable boolean booleanVar = ?;

public function getAverage() returns float {
    return <float>(<float>intVar + floatVar) / 2.0;
}

public function getString() returns string {
    return stringVar;
}

public function getBoolean() returns boolean {
    return booleanVar;
}
