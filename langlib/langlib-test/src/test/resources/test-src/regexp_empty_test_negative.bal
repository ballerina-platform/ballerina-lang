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

import ballerina/lang.regexp;

function emptyRegexpCompilationErrTest() {
    _ = re ``;
    _ = regexp:find(re = re ``, str = "HelloWorld");
    _ = regexp:find(re = re ``, str = "");
    _ = regexp:findAll(re ``, "There once was a king who liked to sing");
    _ = regexp:findAll(re ``, "");
    _ = regexp:findGroups(re ``, "Butter was bought by Betty but the butter was bitter");
    _ = regexp:findGroups(re ``, "");
    _ = regexp:findAllGroups(re ``, "rubble, trouble, bubble, hubble");
    _ = regexp:findAllGroups(re ``, "");
    _ = regexp:fullMatchGroups(re = re ``, str = "HelloWorld");
    _ = regexp:fullMatchGroups(re = re ``, str = "");
    _ = regexp:matchAt(re = re ``, str = "HelloWorld");
    _ = regexp:matchAt(re = re ``, str = "HelloWorld", startIndex = 4);
}
