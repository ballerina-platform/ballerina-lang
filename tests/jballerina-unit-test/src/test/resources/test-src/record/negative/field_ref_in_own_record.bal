// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type Foo record {|
    int a;
    int b = a + 10;
    record {
        int x = a;
    } c;

    object {
        int z;
    } d = object { int z = a; };
|};

function fieldRefTest() {
    record {|
        string fname = "Pubudu";
        string lname = "Fernando";
        string fullName = fname + " " + lname;
    |} person = {};

    record {|
        int a;
        int b = a + 10;

        record {
            int x = a;
        } c;

        object {
            int z;
        } d = object { int z = a; };
    |} rec;

    int p = 25;
    record {
        int x = 10;
        int marks = let int e = 3, int z = 5 in z * e + x;
        int y = p;
    } student = {};

    object {
        record {
            int a;
            int b = a;
            record {
                int d = a;
            } nestd = {};
        } f;
    } obj = object {
                    record {
                        int a;
                        int b = a;
                        record {
                            int d = a;
                        } nestd = {};
                    } f = {a: 10};
                };
}

int glbA = 100;

type Bar record {
    string glbA;
    string z = glbA;
};