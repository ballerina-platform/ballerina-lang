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

import testorg/test_proj_with_init_funcs.c_d;

int i = 10;

int? iFromInit = ();
string? sFromInit = ();

function init() {
    iFromInit = i + 100;
    sFromInit = c_d:getS();
}

public function main() returns error? {
    error e = error("errorCode", i = iFromInit, s = sFromInit);
    return e;
}
