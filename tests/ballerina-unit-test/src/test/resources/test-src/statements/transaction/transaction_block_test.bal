// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/io;

public type TrxError record {
    string message;
    error? cause;
    string data;
    !...
};

function testTransactionStmtWithCommitedAndAbortedBlocks(int failureCutOff, boolean requestAbort) returns (string) {
    string a = "";
    a = (a + "start");
    int count = 0;
    try {
        transaction with retries=2 {
            a = a + " inTrx";
            count = count + 1;
            int i = 0;
            if (count <= failureCutOff) {
                int bV = blowUp();
                if (bV == 0) {
                    a = a + " blown";
                } else {
                    a = a + " notBlown";
                }
            }

            if (requestAbort) {
                abort;
            }

            a = a + " endTrx";
        } onretry {
            a = a + " retry";
        } committed {
            a = a + " committed";
        } aborted {
            a = a + " aborted";
        }
    } catch (error err) {
        a = a + " [" + err.message + "]";
    }
    a = (a + " end");
    return a;
}

function blowUp()  returns int {
    if (5 == 5) {
        error err = { message: "TransactionError" };
        throw err;
    }
    return 5;
}
