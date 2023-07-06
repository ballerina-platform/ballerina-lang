// Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
// under the License.package internal;

public class ObjectA {
    int someInt = 13;
    float someFloat = 1.1;

    function someInt() returns int {
        self.someInt += 10;
        return self.someInt;
    }

    function someFloat() returns float {
        return 2.2;
    }

    function getInt() returns int {
        return self.someInt;
    }

    function testFloat() returns [float, float] {
        float f1 = self.someFloat;
        float f2 = self.someFloat();
        return [f1, f2];
    }
}

function testFieldWithSameNameAsMethod() returns [int, int, int, float, float, float, float] {
    ObjectA obj = new();
    int a = obj.someInt;
    int b = obj.someInt();
    int c = obj.getInt();

    [float, float] [d, e] = obj.testFloat();
    float f = obj.someFloat;
    float g = obj.someFloat();

    return [a, b, c, d, e, f, g];
}
