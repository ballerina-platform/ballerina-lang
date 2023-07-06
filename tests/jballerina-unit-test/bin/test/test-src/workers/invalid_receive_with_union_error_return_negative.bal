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

const FOO = "foo";

type FooError distinct error;
type TrxError distinct error<TrxErrorData>;

type TrxErrorData record {|
    string message?;
    error cause?;
|};

public function main() {
    worker w1 returns FooError|TrxError? {
        int j = 25;
        if (0 > 1) {
            return error FooError(FOO);
        }

        j = <- w2;

        if (1 == 2) {
            TrxError e = error TrxError("trxError");
            return e;
        }
        j = <- w2;
        j = <- w2;
        return;
    }

    worker w2 returns boolean|error {
        int i = 2;
        TrxError? success = i ->> w1;
        success = i ->> w1;
        () _ = i ->> w1;
        return true;
    }
}
