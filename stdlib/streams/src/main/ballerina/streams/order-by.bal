// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public type OrderBy object {

    public function (StreamEvent?[]) nextProcessorPointer;

    public (function (map<anydata>) returns anydata)?[] fieldFuncs;
    // contains the field name to be sorted and the sort type (ascending/descending)
    public string[] sortTypes;
    public MergeSort mergeSort;

    public function __init(function (StreamEvent?[]) nextProcessorPointer,
                           (function (map<anydata>) returns anydata)?[] fieldFuncs, string[] sortTypes) {
        self.nextProcessorPointer = nextProcessorPointer;
        self.fieldFuncs = fieldFuncs;
        self.sortTypes = sortTypes;
        self.mergeSort = new(self.fieldFuncs, self.sortTypes);
    }

    public function process(StreamEvent?[] streamEvents) {
        self.mergeSort.topDownMergeSort(streamEvents);
        self.nextProcessorPointer.call(streamEvents);
    }
};

public function createOrderBy(function (StreamEvent?[]) nextProcessorPointer,
                              (function (map<anydata>) returns anydata)?[] fields, string[] sortFieldMetadata)
                    returns OrderBy {
    return new(nextProcessorPointer, fields, sortFieldMetadata);
}

public type MergeSort object {

    public (function (map<anydata>) returns anydata)?[] fieldFuncs;
    // contains the field name to be sorted and the sort type (ascending/descending)
    public string[] sortTypes;

    public function __init((function (map<anydata>) returns anydata)?[] fieldFuncs, string[] sortTypes) {
        self.fieldFuncs = fieldFuncs;
        self.sortTypes = sortTypes;
    }

    public function topDownMergeSort(StreamEvent?[] a) {
        int index = 0;
        int n = a.length();
        StreamEvent?[] b = [];
        while (index < n) {
            b[index] = a[index];
            index += 1;
        }
        self.topDownSplitMerge(b, 0, n, a);
    }

    function topDownSplitMerge(StreamEvent?[] b, int iBegin, int iEnd, StreamEvent?[] a) {
        if (iEnd - iBegin < 2) {
            return;
        }
        int iMiddle = (iEnd + iBegin) / 2;
        self.topDownSplitMerge(a, iBegin, iMiddle, b);
        self.topDownSplitMerge(a, iMiddle, iEnd, b);
        self.topDownMerge(b, iBegin, iMiddle, iEnd, a);
    }

    function topDownMerge(StreamEvent?[] a, int iBegin, int iMiddle, int iEnd, StreamEvent?[] b) {
        int i = iBegin;
        int j = iMiddle;

        int k = iBegin;
        while (k < iEnd) {
            if (i < iMiddle && (j >= iEnd || self.sortFunc(<StreamEvent>a[i], <StreamEvent>a[j], 0) < 0)) {
                b[k] = <StreamEvent>a[i];
                i = i + 1;
            } else {
                b[k] = <StreamEvent>a[j];
                j = j + 1;
            }
            k += 1;
        }
    }

    function numberSort(int|float x, int|float y) returns int {
        if (x is int) {
            if (y is int) {
                return x - y;
            } else {
                return <float>x < y ? -1 : <float>x == y ? 0 : 1;
            }
        } else {
            if (y is int) {
                return x < (<float>y) ? -1 : x == <float>y ? 0 : 1;
            }
            else {
                return x < y ? -1 : x == y ? 0 : 1;
            }
        }
    }

    function stringSort(string x, string y) returns int {

        byte[] v1 = x.toByteArray("UTF-8");
        byte[] v2 = y.toByteArray("UTF-8");

        int len1 = v1.length();
        int len2 = v2.length();
        int lim = len1 < len2 ? len1 : len2;
        int k = 0;
        while (k < lim) {
            int c1 = <int>v1[k];
            int c2 = <int>v2[k];
            if (c1 != c2) {
                return c1 - c2;
            }
            k += 1;
        }
        return len1 - len2;
    }

    function sortFunc(StreamEvent x, StreamEvent y, int fieldIndex) returns int {
        var fieldFunc = self.fieldFuncs[fieldIndex];
        var xFieldFuncResult = fieldFunc.call(x.data);

        if (xFieldFuncResult is string) {
            var yFieldFuncResult = fieldFunc.call(y.data);
            if (yFieldFuncResult is string) {
                int c;
                //odd indices contain the sort type (ascending/descending)
                if (self.sortTypes[fieldIndex].equalsIgnoreCase(ASCENDING)) {
                    c = self.stringSort(xFieldFuncResult, yFieldFuncResult);
                } else {
                    c = self.stringSort(yFieldFuncResult, xFieldFuncResult);
                }
                // if c == 0 then check for the next sort field
                return self.callNextSortFunc(x, y, c, fieldIndex + 1);
            } else {
                error err = error("Values to be orderred contain non-string values in fieldIndex: " +
                    fieldIndex + ", sortType: " + self.sortTypes[fieldIndex]);
                panic err;
            }

        } else if (xFieldFuncResult is (int|float)) {
            var yFieldFuncResult = fieldFunc.call(y.data);
            if (yFieldFuncResult is (int|float)) {
                int c;
                if (self.sortTypes[fieldIndex].equalsIgnoreCase(ASCENDING)) {
                    c = self.numberSort(xFieldFuncResult, yFieldFuncResult);
                } else {
                    c = self.numberSort(yFieldFuncResult, xFieldFuncResult);
                }
                return self.callNextSortFunc(x, y, c, fieldIndex + 1);
            } else {
                error err = error("Values to be orderred contain non-number values in fieldIndex: " +
                    fieldIndex + ", sortType: " + self.sortTypes[fieldIndex]);
                panic err;
            }
        } else {
            error err = error("Values of types other than strings and numbers cannot be sorted in fieldIndex:
                 " + fieldIndex + ", sortType: " + self.sortTypes[fieldIndex]);
            panic err;
        }
    }

    function callNextSortFunc(StreamEvent x, StreamEvent y, int c, int fieldIndex) returns int {
        int result = c;
        if (result == 0 && (self.sortTypes.length() > fieldIndex)) {
            result = self.sortFunc(x, y, fieldIndex);
        }
        return result;
    }
};
