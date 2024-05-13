// Copyright (c) 2024 WSO2 LLC. (http://www.wso2.com).
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

public function testNoMessageErrorForOnFailAsyncSend() {
    worker w1 {
        0 -> w2;
    } on fail {
        1 -> w2;
    }

    worker w2 {
        int _ = <- w1;
        int _ = <- w1; // found 'int|error:NoMessage'
    }

    wait w2;
}

public function testNoMessageErrorForOnFailSyncSend() {
    worker w1 {
        0 ->> w2;
    } on fail {
        1 ->> w2;
    }

    worker w2 {
        int _ = <- w1;
        int _ = <- w1; // found 'int|error:NoMessage'
    }

    wait w1;
}

public function testNoMessageErrorWithWorkerOnFail() {
    worker w1 {
        boolean b = true;
        check error("err");
        0 -> w2;
        if b {
            1 -> w2;
        }
    } on fail {
        boolean c = true;
        2 -> w2;
        if c {
            3 ->> w2;
        } else {
            4 -> w2;
        }
    }

    worker w2 {
        int _ = <- w1; // found 'int|error:NoMessage'
        int _ = <- w1; // found 'int|error:NoMessage'
        int _ = <- w1; // found 'int|error:NoMessage'
        int _ = <- w1; // found 'int|error:NoMessage'
        int _ = <- w1; // found 'int|error:NoMessage'
    }

    wait w2;
}
