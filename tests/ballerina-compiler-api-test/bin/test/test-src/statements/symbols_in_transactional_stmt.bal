// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function test() returns error? {
    transaction {
        int value = 2000;
        call1(value);
        call2();
        check commit;
    }

    transaction {
        call1(20);
        check commit;
    } on fail error e {
        call3(e);
    }

    transaction {
        rollback call2();
    }
}

function call1(int val) { 
}

function call2() { 
}

function call3(error e) {
    
}
