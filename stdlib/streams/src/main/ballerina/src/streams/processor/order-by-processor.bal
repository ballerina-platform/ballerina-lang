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

import ballerina/lang.'int as langint;
import ballerina/stringutils;

# The `OrderBy` object represents the desugared code of `order by` clause of a streaming query. This object takes 3
# parameters to initialize itself. `nextProcessPointer` is the `process` method of the next processor. `fieldFuncs`
# is an array of function pointers which returns the field values to be sorted. `sortTypes` is an array of string
# specifying whether the sort order (ascending or descending). Internally this processor uses a `MergeSort` object
# to sort.
#
# + nextProcessorPointer - description
# + fieldFuncs - description
# + sortTypes - description
# + mergeSort - description
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

    # Sorts the given array of stream events according to the given parameters (fieldFuncs and sortTypes).
    #
    # + streamEvents - The array of stream events to be sorted.
    public function process(StreamEvent?[] streamEvents) {
        self.mergeSort.topDownMergeSort(streamEvents);
        function (StreamEvent?[]) processorPointer = self.nextProcessorPointer;
        processorPointer(streamEvents);
    }
};

# Creates an `OrderBy` object and return it.
#
# + nextProcessorPointer - A function pointer to the `process` function of the next processor.
# + fields - An array of function pointers which each returns a field by which the events are sorted. Events are
#            sorted by the first field, if there are elements of same value, the second field is used and so on.
# + sortFieldMetadata - sortTypes of the fields (`streams:ASCENDING` or `streams:DESCENDING`). First element is the
#                       sort type of the first element of `fields` and so on.
#
# + return - Returns a `OrderBy` object.
public function createOrderBy(function (StreamEvent?[]) nextProcessorPointer,
                              (function (map<anydata>) returns anydata)?[] fields, string[] sortFieldMetadata)
                    returns OrderBy {
    return new(nextProcessorPointer, fields, sortFieldMetadata);
}

# This object implements the merge sort algorithm to sort the provided value arrays. `fieldFuncs` are function pointers
# which returns the field values of each stream event's `data` map's values. `sortTypes` are an array of (
# streams:ASCENDING or streams:DESCENDING).
#
# + fieldFuncs - description
# + sortTypes - description
public type MergeSort object {

    public (function (map<anydata>) returns anydata)?[] fieldFuncs;
    // contains the field name to be sorted and the sort type (ascending/descending)
    public string[] sortTypes;

    public function __init((function (map<anydata>) returns anydata)?[] fieldFuncs, string[] sortTypes) {
        self.fieldFuncs = fieldFuncs;
        self.sortTypes = sortTypes;
    }

    # Sorts the given stream events using the merge sort algorithm.
    #
    # + events - The array of stream events to be sorted.
    public function topDownMergeSort(StreamEvent?[] events) {
        int index = 0;
        int n = events.length();
        StreamEvent?[] b = [];
        while (index < n) {
            b[index] = events[index];
            index += 1;
        }
        self.topDownSplitMerge(b, 0, n, events);
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

        byte[] v1 = x.toBytes();
        byte[] v2 = y.toBytes();

        int len1 = v1.length();
        int len2 = v2.length();
        int lim = len1 < len2 ? len1 : len2;
        int k = 0;
        while (k < lim) {
            var c1 = langint:fromString(v1[k].toString());
            var c2 = langint:fromString(v2[k].toString());
            if (c1 is int && c2 is int) {
                if (c1 != c2) {
                    return c1 - c2;
                }
            }
            k += 1;
        }
        return len1 - len2;
    }

    function sortFunc(StreamEvent x, StreamEvent y, int fieldIndex) returns int {
        var fieldFunc = self.fieldFuncs[fieldIndex];
        if (fieldFunc is (function (map<anydata>) returns anydata)) {
            var xFieldFuncResult = fieldFunc(x.data);
            if (xFieldFuncResult is string) {
                var yFieldFuncResult = fieldFunc(y.data);
                if (yFieldFuncResult is string) {
                    int c;
                    //odd indices contain the sort type (ascending/descending)
                    if (stringutils:equalsIgnoreCase(self.sortTypes[fieldIndex], ASCENDING)) {
                        c = self.stringSort(xFieldFuncResult, yFieldFuncResult);
                    } else {
                        c = self.stringSort(yFieldFuncResult, xFieldFuncResult);
                    }
                    // if c == 0 then check for the next sort field
                    return self.callNextSortFunc(x, y, c, fieldIndex + 1);
                } else {
                    error err = error("Values to be orderred contain non-string values in fieldIndex: " +
                        fieldIndex.toString() + ", sortType: " + self.sortTypes[fieldIndex]);
                    panic err;
                }
            } else if (xFieldFuncResult is (int|float)) {
                var yFieldFuncResult = fieldFunc(y.data);
                if (yFieldFuncResult is (int|float)) {
                    int c;
                    if (stringutils:equalsIgnoreCase(self.sortTypes[fieldIndex], ASCENDING)) {
                        c = self.numberSort(xFieldFuncResult, yFieldFuncResult);
                    } else {
                        c = self.numberSort(yFieldFuncResult, xFieldFuncResult);
                    }
                    return self.callNextSortFunc(x, y, c, fieldIndex + 1);
                } else {
                    error err = error("Values to be orderred contain non-number values in fieldIndex: " +
                        fieldIndex.toString() + ", sortType: " + self.sortTypes[fieldIndex]);
                    panic err;
                }
            } else {
                error err = error("Values of types other than strings and numbers cannot be sorted in fieldIndex:"
                                            + fieldIndex.toString() + ", sortType: " + self.sortTypes[fieldIndex]);
                panic err;
            }
        } else {
            //TODO: Add a meaningful error
            error err = error("Function does not exist");
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
