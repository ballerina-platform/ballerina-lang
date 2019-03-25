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

import ballerina/io;
import ballerina/bir;
import ballerina/jvm;
import ballerina/reflect;

public type JarFile record {|
    map<string> manifestEntries;
    map<byte[]> jarEntries;
|};

bir:BIRContext currentBIRContext = new;

public function main(string... args) {
    //do nothing
}

public type IntSort object {

    public function sort(int[] arr) {
        self.sortInternal(arr, 0, arr.length() - 1);
    }

    function sortInternal(int[] arr, int l, int r) {
        if (l < r) {
            // Find the middle point
            int m = (l + r) / 2;

            // Sort first and second halves
            self.sortInternal(arr, l, m);
            self.sortInternal(arr, m + 1, r);

            // Merge the sorted halves
            self.merge(arr, l, m, r);
        }
    }

    function merge(int[] arr, int l, int m, int r) {
        // Find sizes of two subarrays to be merged
        int n1 = m - l + 1;
        int n2 = r - m;

        // Create temp arrays
        int[] L = [];
        int[] R = [];

        // Copy data to temp arrays
        int i = 0;
        while (i < n1) {
            L[i] = arr[l + i];
            i = i + 1;
        }

        int j = 0;
        while (j < n2) {
            R[j] = arr[m + 1 + j];
            j = j + 1;
        }

        // Initial indexes of first and second subarrays
        i = 0;
        j = 0;

        // Initial index of merged subarry array
        int k = l;
        while (i < n1 && j < n2) {
            if (L[i] <= R[j]) {
                arr[k] = L[i];
                i += 1;
            } else {
                arr[k] = R[j];
                j += 1;
            }
            k += 1;
        }

        // Copy remaining elements of L[] if any
        while (i < n1) {
            arr[k] = L[i];
            i += 1;
            k += 1;
        }

        // Copy remaining elements of R[] if any
        while (j < n2) {
            arr[k] = R[j];
            j += 1;
            k += 1;
        }
    }
};

function generateJarBinary(bir:BIRContext birContext, bir:ModuleID entryModId, string progName) returns JarFile {
    currentBIRContext = birContext;
    bir:Package entryMod = birContext.lookupBIRModule(entryModId);

    map<byte[]> jarEntries = {};
    map<string> manifestEntries = {};

    foreach var importModule in entryMod.importModules {
        bir:Package module = lookupModule(importModule, birContext);
        generateImportedPackage(module, jarEntries);
    }

    generateEntryPackage(entryMod, progName, jarEntries, manifestEntries);

    JarFile jarFile = {jarEntries : jarEntries, manifestEntries : manifestEntries};
    return jarFile;
}
