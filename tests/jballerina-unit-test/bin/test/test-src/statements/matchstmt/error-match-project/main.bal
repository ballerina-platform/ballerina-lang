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

import ErrorMatchProject.module1;

type FooError error<record {|string msg; boolean fatal = false;|}>;

function testErrorMatchWithQualifiedReference() {
    error err1 = error("OMG!");
    error err2 = error(module1:ERROR_MESSAGE_2);
    error err3 = error("SOMETHING'S WRONG!");
    error err4 = error("OH, NO!", error("SOMETHING'S WRONG!"));
    FooError err5 = error("ERROR!", msg = "OH, NO!", fatal = false);
    FooError err6 = error("ERROR!", msg = "OH, NO!", fatal = true);
    error err7 = error("ERROR!");

    assertEquals(1, errorMatchWithQualifiedReference(err1));
    assertEquals(2, errorMatchWithQualifiedReference(err2));
    assertEquals(3, errorMatchWithQualifiedReference(err3));
    assertEquals(4, errorMatchWithQualifiedReference(err4));
    assertEquals(5, errorMatchWithQualifiedReference(err5));
    assertEquals(0, errorMatchWithQualifiedReference(err6));
    assertEquals(0, errorMatchWithQualifiedReference(err7));
}

function errorMatchWithQualifiedReference(error e) returns int {
    match  e {
        error(module1:ERROR_MESSAGE_1) => {
            return 1;
        }
        error(module1:ERROR_MESSAGE_2) => {
            return 2;
        }
        error(module1:ERROR_MESSAGE_3) => {
            return 3;
        }
        error(module1:ERROR_MESSAGE_4, error(module1:ERROR_MESSAGE_3)) => {
            return 4;
        }
        error(_, msg = module1:ERROR_MESSAGE_4, fatal = module1:DEFAULT_FATAL_STATUS) => {
            return 5;
        }
    }

    return 0;
}

function assertEquals(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
