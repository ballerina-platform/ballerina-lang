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
import ballerina/test;

// TODO: Record variable definition should work with only a single rest parameter
@test:Config {
    groups: ["deviation"]
}
function testRecordTypedBindingPatternWithOnlyRestParamBroken() {
    //BindingPattern { ...restParam } = bindingPattern;
    //test:assertEquals(restParam.field1, 11, msg = "expected record value to be destructured to variable defintions");
    //test:assertEquals(restParam.field2, "string4", msg =
    //    "expected record value to be destructured to variable defintions");
    //test:assertEquals(restParam.field3, 19.9, msg = "expected record value to be destructured to variable defintions");
    //test:assertEquals(restParam.var4, <decimal>18.9, msg =
    //    "expected record value to be destructured to variable defintions");
    //test:assertEquals(restParam.var5, false, msg = "expected record value to be destructured to variable defintions");
    //test:assertEquals(restParam.field6, (), msg = "expected record value to be destructured to variable defintions");
    //expectedArray = [8, 9, 10, 11];
    //test:assertEquals(restParam.field7, expectedArray, msg =
    //    "expected record value to be destructured to variable defintions");
}
