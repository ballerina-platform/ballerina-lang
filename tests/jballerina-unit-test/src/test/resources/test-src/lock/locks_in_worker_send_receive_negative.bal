// Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
//
// WSO2 LLC. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

int a = 10;
int b = 20;

public function main() {
    worker w1 {
        lock {
            a += 1;
            3 ->> w2;
            b += 1;
        }
    }

    worker w2 {
        lock {
            b += 1;
            int x = <- w1;
            a += 1;
            x += 1;
        }
    }
}

public function test() {
    worker A {
        lock {
        int num = 10;
        num -> B;
        string msg = <- B;
        }
    }

    worker B {
        lock {
            int num;
            num = <- A;
            string msg = "Hello";
            msg -> A;
            num += 1;
        }
    }
}
