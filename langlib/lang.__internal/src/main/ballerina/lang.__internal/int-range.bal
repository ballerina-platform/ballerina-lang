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

type IntRange object {
    private int iStart;
    private int iEnd;
    private int iCurrent;

    public function __init(int s, int e) {
        self.iStart = s;
        self.iEnd = e;
        self.iCurrent = s;
    }

    public function hasNext() returns boolean {
        return (self.iStart <= self.iCurrent) && (self.iCurrent <= self.iEnd);
    }

    public function next() returns record {|
        int value;
    |}? {
        int currentValue = self.iCurrent;
        self.iCurrent += 1;
        if (self.hasNext()) {
            return {value : self.iCurrent};
        }

        return ();
    }
};

function createIntRange(int s, int e) returns
        abstract object {public function next() returns record {|int value;|}?;} {
    IntRange intRange = new(s, e);
    return intRange;
}
