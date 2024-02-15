// Copyright (c) 2023 WSO2 LLC. (http://www.wso2.com).
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

type ErrorTypeA distinct error;

const TYPE_A_ERROR_REASON = "TypeA_Error";

const TYPE_B_ERROR_REASON = "TypeB_Error";

type ErrorTypeB distinct error;

function trxError() returns error {
    return error("TransactionError");
}

function testUnreachableAfterFail() returns error? {
    int count = 0;
    retry(3) {
        count = count + 1;
        fail trxError();
        count = count + 1;
    } on fail error e {
        return error("Custom Error");
    }
}

function testUnreachableInOnFail() returns int? {
    int count = 0;
    retry(3) {
        count += 1;
        fail error ErrorTypeA(TYPE_A_ERROR_REASON, message = "Error Type A");
    } on fail ErrorTypeA e {
        count += 1;
        return count;
        count += 1;
    }
    return ();
}

function testNestedRetryWithLessOnFails() returns string {
    int count = 0;
    retry(3) {
        count += 1;
        retry(2) {
            count += 1;
            fail error ErrorTypeA(TYPE_A_ERROR_REASON, message = "Error Type A");
        }
        fail error ErrorTypeA(TYPE_A_ERROR_REASON, message = "Error Type A");
    } on fail error e1 {
        count += 1;
    }
    count += 1;
    return count.toString();
}

function testOnFailWithUnion() returns string {
    string str = "";
    var getTypeBError = function() returns int|ErrorTypeB {
        ErrorTypeB errorB = error ErrorTypeB(TYPE_B_ERROR_REASON, message = "Error Type B");
        return errorB;
    };
    retry(3) {
        str += "Before failure throw";
        int resB = check getTypeBError();
    } on fail ErrorTypeB e {
        str += "-> Error caught : ";
        str = str.concat(e.message());
    }
    str += "-> Execution continues...";
    return str;
}
