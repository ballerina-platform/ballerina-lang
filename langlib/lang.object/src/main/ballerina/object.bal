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

# Distinct Iterable type.
# An object can make itself iterable by using `*object:Iterable;`,
# and then defining an `iterator` method.
public type Iterable distinct object {
    # Create a new iterator.
    #
    # + return - a new iterator object
    public function iterator() returns object {
        public isolated function next() returns record {| any|error value; |}|error?;
    };
};

# Integer range expression is represented using `IntRange` object.
#
# + iStart - start expression of range expression
# + iEnd - second expression on range expression
# + iCurrent - current cursor
public class IntRange {

    *Iterable;
    private int iStart;
    private int iEnd;
    private int iCurrent;

    public isolated function init(int s, int e) {
        self.iStart = s;
        self.iEnd = e;
        self.iCurrent = s;
    }

    public isolated function hasNext() returns boolean {
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

    public isolated function iterator() returns
        object {public isolated function next() returns record {|int value;|}?;} {
            return new IntRange(self.iStart, self.iEnd);
    }
}
