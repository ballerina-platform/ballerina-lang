//  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
//  WSO2 Inc. licenses this file to you under the Apache License,
//  Version 2.0 (the "License"); you may not use this file except
//  in compliance with the License.
//  You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ErrorNegativeProject.module1 as er;

function testErrorTypeAccess() {
    error e1 = error er:E5("error!");
    error e2 = error er:E6("error!");
    error e3 = error er:E7("error!");
    error e4 = error er:E8("error!");
    error e5 = error er:E9("error!");
    error e6 = error er:E10("error!");

    error e1_public = error er:E5_Public("error!");
    error e2_public = error er:E6_Public("error!");
    error e3_public = error er:E7_Public("error!");
    error e4_public = error er:E8_Public("error!");
    error e5_public = error er:E9_Public("error!");
    error e6_public = error er:E10_Public("error!");
}
