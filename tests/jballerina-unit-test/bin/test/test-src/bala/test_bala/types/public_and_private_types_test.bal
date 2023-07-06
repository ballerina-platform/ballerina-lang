// Copyright (c) 2022, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

import ballerina/test;
import testOrg/public_and_private_types as test_module;

function testModulePublicAndPrivateTypes() {
    any timeZone = test_module:loadZone();
    test:assertEquals(timeZone is test_module:Zone, true);
    if (timeZone is test_module:Zone) {
        test:assertEquals(timeZone.getInt({year:1, month: 2, day: 3}), 6);
    }
}

type DistinctError distinct error;

function testAnonymousDistinctErrorTypes() {
    error err = error("");
    boolean ans = err is test_module:InvalidDocumentError;
    test:assertFalse(ans);
    test_module:InvalidDocumentError|error err2 = trap <test_module:InvalidDocumentError>err;
    test:assertTrue(err2 is error);
    test:assertFalse(err2 is test_module:InvalidDocumentError);

    test_module:InvalidDocumentError err3 = error("err3", errors = []);
    test_module:PayloadBindingError err4 = error("err4", errors = ());
    test_module:RequestError err5 = error test_module:RequestError("err5");
    test_module:ClientError err6 = error("err6");
    error<record {| test_module:ErrorDetail[]? errors; |}> err7 = error("err7", errors = []);
    test:assertTrue(err3 is error);
    test:assertTrue(err4 is error);
    test:assertFalse(err3 is test_module:PayloadBindingError);
    test:assertFalse(err4 is test_module:InvalidDocumentError);
    test:assertFalse(err4 is DistinctError);
    test:assertTrue(err3 is test_module:RequestError);
    test:assertTrue(err3 is test_module:ClientError);
    test:assertFalse(err4 is test_module:RequestError);
    test:assertTrue(err3 is error<record {| test_module:ErrorDetail[]? errors; |}>);
    test:assertFalse(err5 is test_module:PayloadBindingError);
    test:assertFalse(err6 is test_module:PayloadBindingError);
    test:assertFalse(err7 is test_module:InvalidDocumentError);
    test:assertTrue(err3 is (test_module:RequestError & error<record {| test_module:ErrorDetail[]? errors; |}>));

    DistinctError err8 = error DistinctError("err8");
    test:assertFalse(err8 is test_module:InvalidDocumentError);
}
