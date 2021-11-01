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

import ballerina/lang.'object as lang_object;

# Integer range expression is represented using `IterableIntegerRange` object.
#
# This type is created to have an Iterable object without having to expose actual
# implementation details with private fields
type IterableIntegerRange isolated object {
    *lang_object:Iterable;
};

# Integer range expression is implemented using `__IntRange` object.
#
# + iStart - start expression of range expression
# + iEnd - second expression on range expression
# + iCurrent - current cursor
public isolated class __IntRange {

    *IterableIntegerRange;
    private final int iStart;
    private final int iEnd;
    private int iCurrent;

    public isolated function init(int s, int e) {
        self.iStart = s;
        self.iEnd = e;
        self.iCurrent = s;
    }

    private isolated function hasNext() returns boolean {
        int currentVal;
        lock {
            currentVal = self.iCurrent;
        }
        return (self.iStart <= currentVal) && (currentVal <= self.iEnd);
    }

    public isolated function next() returns record {|
        int value;
    |}? {

        if (self.hasNext()) {
            record {|int value;|} nextVal;
            lock {
                nextVal = {value : self.iCurrent};
                self.iCurrent += 1;
            }
            return nextVal;
        }

        return ();
    }

    public isolated function iterator() returns
        isolated object {public isolated function next() returns record {|int value;|}?;} {
            return new __IntRange(self.iStart, self.iEnd);
    }
}

# The `createIntRange` function creates a `__IntRange` object and returns it. This function is used to replace the binary
# integer range expression in Desugar phase.
#
# + s - The lower bound of the integer range inclusive
# + e - The upper bound if the integer range inclusive
# + return - `Iterable<int,()>` object
public isolated function createIntRange(int s, int e) returns isolated object {
                                                                  *IterableIntegerRange;
                                                                  public isolated function iterator()
                                                                  returns isolated object {
                                                                              public isolated function next()
                                                                              returns record {| int value; |}?;
                                                                          };
                                                              } {
    __IntRange intRange = new (s, e);
    return intRange;
}
