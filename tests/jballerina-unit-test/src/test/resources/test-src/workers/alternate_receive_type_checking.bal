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

function testAltReceiveNoMessageErrorType(boolean b) {
    worker w1 {
        // case 1
        1 -> w2;
        2 -> w2;

        // case 2
        if b {
            3 -> w2;
            4 -> w2;
        }

        // case 3
        if b {
            5 -> w2;
        }
        6 -> w2;

        // case 4
        7 -> w2;
        if b {
            8 -> w2;
        }

        // case 5
        9 -> w2;
        10 -> w2;
        if b {
            11 -> w2;
        }
        12 -> w2;

        // case 5
        13.3d -> w2;
        if b {
            "xx" -> w2;
            14 -> w2;
            true -> w2;
        }

        // case 6
        if !b {
            13.3d -> w2;
        }
        if b {
            "xx" -> w2;
            14 -> w2;
            true -> w2;
        }
    }

    worker w2 {
        string _ = <- w1|w1; // found 'int'
        string _ = <- w1|w1; // found 'int|error:NoMessage'
        string _ = <- w1|w1; // found 'int'
        string _ = <- w1|w1; // found 'int'
        string _ = <- w1|w1|w1|w1; // found 'int'
        string _ = <- w1|w1|w1|w1; // found 'decimal|string|int|boolean'
        string _ = <- w1|w1|w1|w1; // found 'decimal|string|int|boolean|error:NoMessage'
    }

    _ = wait {a: w1, b: w2};
}
