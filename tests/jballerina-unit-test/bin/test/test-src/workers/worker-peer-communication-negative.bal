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

function testSimpleWorkerVM(string msg) returns string {
    fork {
        worker first returns string {
            string d = <- function;
            d -> function;
            "a" -> sampleWorker;
            string result = "";
            result = <- sampleWorker;
            return result;
        }

        worker sampleWorker {
            string m = "";
            m = <- first;
            msg -> first;
            msg -> first1; // should not be allowed
        }
    }

    fork {
        worker first1 returns string {
            "a" -> sampleWorker1;
            string result = "";
            result = <- sampleWorker1;
            string s = <- sampleWorker; // should not be allowed
            return result;
        }

        worker sampleWorker1 {
            string m = "";
            m = <- first1;
            msg -> first1;
        }
    }

    "a" -> first;
    string k = <- first;

    return wait first;
}
