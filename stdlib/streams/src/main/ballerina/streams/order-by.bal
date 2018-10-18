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
import ballerina/io;

public type OrderBy object {

    public function (StreamEvent[]) nextProcessorPointer;

    public (function(map) returns any)[] fieldFuncs;
    // contains the field name to be sorted and the sort type (ascending/descending)
    public string[] sortTypes;

    public new(nextProcessorPointer, fieldFuncs, sortTypes) {

    }

    public function process(StreamEvent[] streamEvents) {
        topDownMergeSort(streamEvents, sortTypes);
        nextProcessorPointer(streamEvents);
    }

    function topDownMergeSort(StreamEvent[] a, string[] tmpSortTypes) {
        int index = 0;
        int n = lengthof a;
        StreamEvent[] b;
        while (index < n) {
            b[index] = a[index];
            index += 1;
        }
        topDownSplitMerge(b, 0, n, a, sortFunc, tmpSortTypes);
    }

    function topDownSplitMerge(StreamEvent[] b, int iBegin, int iEnd, StreamEvent[] a,
                               function (StreamEvent, StreamEvent, string[], int) returns int sortFunc,
                               string[] tmpSortTypes) {

        if (iEnd - iBegin < 2) {
            return;
        }
        int iMiddle = (iEnd + iBegin) / 2;
        topDownSplitMerge(a, iBegin, iMiddle, b, sortFunc, tmpSortTypes);
        topDownSplitMerge(a, iMiddle, iEnd, b, sortFunc, tmpSortTypes);
        topDownMerge(b, iBegin, iMiddle, iEnd, a, sortFunc, tmpSortTypes);
    }

    function topDownMerge(StreamEvent[] a, int iBegin, int iMiddle, int iEnd, StreamEvent[] b,
                          function (StreamEvent, StreamEvent, string[], int) returns int sortFunc,
                          string[] sortFieldMetadata) {
        int i = iBegin;
        int j = iMiddle;

        int k = iBegin;
        while (k < iEnd) {
            if (i < iMiddle && (j >= iEnd || sortFunc(a[i], a[j], sortFieldMetadata, 0) < 0)) {
                b[k] = a[i];
                i = i + 1;
            } else {
                b[k] = a[j];
                j = j + 1;
            }
            k += 1;
        }
    }

    function numberSort(int|float x, int|float y) returns int {
        match x {
            int ix => {
                match y {
                    int iy => {
                        return ix - iy;
                    }
                    float fy => {
                        return <float>ix < fy ? -1 : <float>ix == fy ? 0 : 1;
                    }
                }
            }

            float fx => {
                match y {
                    int iy => {
                        return fx < (<float>iy) ? -1 : fx == <float>iy ? 0 : 1;
                    }
                    float fy => {
                        return fx < fy ? -1 : fx == fy ? 0 : 1;
                    }
                }
            }
        }
    }

    function stringSort(string x, string y) returns int {

        byte[] v1 = x.toByteArray("UTF-8");
        byte[] v2 = y.toByteArray("UTF-8");

        int len1 = lengthof v1;
        int len2 = lengthof v2;
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

    function sortFunc(StreamEvent x, StreamEvent y, string[] sortFieldMetadata, int fieldIndex) returns int {
        var fieldFunc = fieldFuncs[fieldIndex];
        match fieldFunc(x.data) {
            string sx => {
                match fieldFunc(y.data) {
                    string sy => {
                        int c;
                        //odd indices contain the sort type (ascending/descending)
                        if (sortFieldMetadata[fieldIndex].equalsIgnoreCase(ASCENDING)) {
                            c = stringSort(sx, sy);
                        } else {
                            c = stringSort(sy, sx);
                        }
                        // if c == 0 then check for the next sort field
                        return callNextSortFunc(x, y, c, sortFieldMetadata, fieldIndex + 1);
                    }
                    any a => {
                        error err = { message: "Values to be orderred contain non-string values in fieldIndex: " +
                            fieldIndex + ", sortType: " + sortFieldMetadata[fieldIndex]};
                        throw err;
                    }
                }
            }

            int|float ax => {
                match fieldFunc(y.data) {
                    int|float ay => {
                        int c;
                        if (sortFieldMetadata[fieldIndex].equalsIgnoreCase(ASCENDING)) {
                            c = numberSort(ax, ay);
                        } else {
                            c = numberSort(ay, ax);
                        }
                        return callNextSortFunc(x, y, c, sortFieldMetadata,fieldIndex + 1);
                    }
                    any aa => {
                        error err = { message: "Values to be orderred contain non-number values in fieldIndex: " +
                            fieldIndex + ", sortType: " + sortFieldMetadata[fieldIndex]};
                        throw err;
                    }
                }

            }
            any a => {
                error err = { message: "Values of types other than strings and numbers cannot be sorted in fieldIndex:
                 " + fieldIndex + ", sortType: " + sortFieldMetadata[fieldIndex]};
                throw err;
            }
        }
    }

    function callNextSortFunc(StreamEvent x, StreamEvent y, int c, string[] sortFieldMetadata, int fieldIndex) returns int {
        int result = c;
        if (result == 0 && (lengthof sortFieldMetadata > fieldIndex)) {
            result = sortFunc(x, y, sortFieldMetadata, fieldIndex);
        }
        return result;
    }
};

public function createOrderBy(function (StreamEvent[]) nextProcessorPointer,
                              (function (map) returns any)[] fields, string[] sortFieldMetadata)
                    returns OrderBy {
    return new(nextProcessorPointer, fields, sortFieldMetadata);
}
