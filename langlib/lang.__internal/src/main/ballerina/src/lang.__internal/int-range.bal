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

# Integer range expression is represented using `IntRange` object.
#
# + iStart - start expression of range expression
# + iEnd - second expression on range expression
# + iCurrent - current cursor
class IntRange {
    private int iStart;
    private int iEnd;
    private int iCurrent;

    public function init(int s, int e) {
        self.iStart = s;
        self.iEnd = e;
        self.iCurrent = s;
    }

    public function hasNext() returns boolean {
        return (self.iStart <= self.iCurrent) && (self.iCurrent <= self.iEnd);
    }

    public isolated function next() returns record {|
        int value;
    |}? {

        if (self.hasNext()) {
            record {|int value;|} nextVal = {value : self.iCurrent};
            self.iCurrent += 1;
            return nextVal;
        }

        return ();
    }

    public function __iterator() returns object {public isolated function next() returns record {|int value;|}?;} {
            return new IntRange(self.iStart, self.iEnd);
    }
}

# The `createIntRange` function creates a `IntRange` object and returns it. This function is used to replace the binary
# integer range expression in Desugar phase.
#
# + s - The lower bound of the integer range inclusive
# + e - The upper bound if the integer range inclusive
# + return - `IntRange` object
public function createIntRange(int s, int e) returns
        object {
            public function __iterator() returns
                object {
                    public isolated function next() returns
                        record {|int value;|}?;
                };
        } {
    IntRange intRange = new(s, e);
    return intRange;
}
