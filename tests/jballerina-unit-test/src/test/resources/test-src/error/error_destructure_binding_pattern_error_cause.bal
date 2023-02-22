//  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type SampleErrorData record {|
    int code;
    string reason;
|};

type SampleError error<SampleErrorData>;

 public function main() {
    string errorMsg;
    string errorCause;
    error(errorMsg, error(errorCause)) = error("msg", error("Error cause"));

    SampleError sampleErr = error("Transaction Failure", error("Database Error"), code = 20,
                            reason = "deadlock condition");

    var error(message1, _, code = code1, reason = reason1) = sampleErr;

    var error(_, _, code = code2, reason = reason2) = sampleErr;

    SampleError error(message3, _, code = code3, reason = reason3) = sampleErr;

    SampleError error(_, _, code = code4, reason = reason4) = sampleErr;
}
