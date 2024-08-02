// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/test;

public type Info record {
    string x = "Info";
    int y = 10;
};

public type Person record {
    string name = "Waruna";
    int age = 10;
};

public enum Colors {
    RED,
    GREEN,
    YELLOW
}

public type MyRecord1 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord2 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord3 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord4 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord5 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord6 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord7 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord8 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord9 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord10 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord11 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord12 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord13 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord14 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord15 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord16 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord17 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord18 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord19 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord20 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord21 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord22 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord23 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord24 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord25 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord26 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord27 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord28 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord29 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord30 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord31 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord32 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord33 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord34 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord35 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord36 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord37 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord38 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord39 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord40 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord41 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord42 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord43 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord44 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord45 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord46 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord47 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord48 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord49 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord50 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord51 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord52 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord53 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord54 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord55 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord56 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord57 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord58 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord59 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord60 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord61 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord62 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord63 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord64 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord65 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord66 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord67 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord68 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord69 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord70 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord71 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord72 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord73 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord74 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord75 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord76 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord77 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord78 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord79 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord80 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord81 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord82 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord83 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord84 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord85 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord86 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord87 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord88 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord89 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord90 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord91 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord92 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord93 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord94 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord95 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord96 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord97 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord98 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord99 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord100 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord101 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord102 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord103 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord104 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord105 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord106 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord107 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord108 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord109 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord110 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord111 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord112 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord113 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord114 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord115 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord116 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord117 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord118 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord119 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord120 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord121 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord122 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord123 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord124 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord125 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord126 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord127 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord128 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord129 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord130 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord131 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord132 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord133 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord134 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord135 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord136 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord137 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord138 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord139 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord140 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord141 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord142 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord143 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord144 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord145 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord146 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord147 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord148 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord149 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord150 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord151 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord152 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord153 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord154 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord155 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord156 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord157 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord158 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord159 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord160 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord161 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord162 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord163 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord164 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord165 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord166 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord167 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord168 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord169 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord170 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord171 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord172 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord173 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord174 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord175 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord176 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord177 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord178 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord179 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord180 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord181 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord182 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord183 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord184 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord185 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord186 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord187 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord188 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord189 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord190 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord191 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord192 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord193 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord194 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord195 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord196 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord197 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord198 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord199 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord200 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord201 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord202 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord203 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord204 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord205 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord206 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord207 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord208 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord209 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord210 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord211 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord212 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord213 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord214 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord215 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord216 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord217 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord218 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord219 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord220 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord221 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord222 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord223 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord224 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord225 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord226 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord227 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord228 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord229 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord230 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord231 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord232 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord233 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord234 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord235 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord236 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord237 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord238 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord239 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord240 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord241 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord242 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord243 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord244 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord245 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord246 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord247 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord248 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord249 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord250 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord251 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord252 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord253 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord254 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord255 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord256 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord257 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord258 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord259 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord260 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord261 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord262 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord263 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord264 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord265 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord266 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord267 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord268 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord269 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord270 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord271 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord272 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord273 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord274 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord275 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord276 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord277 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord278 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord279 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord280 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord281 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord282 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord283 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord284 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord285 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord286 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord287 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord288 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord289 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord290 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord291 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord292 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord293 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord294 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord295 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord296 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord297 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord298 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord299 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord300 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord301 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord302 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord303 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord304 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord305 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord306 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord307 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord308 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord309 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord310 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord311 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord312 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord313 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord314 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord315 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord316 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord317 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord318 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord319 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord320 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord321 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord322 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord323 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord324 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord325 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord326 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord327 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord328 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord329 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord330 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord331 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord332 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord333 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord334 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord335 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord336 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord337 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord338 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord339 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord340 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord341 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord342 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord343 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord344 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord345 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord346 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord347 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord348 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord349 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord350 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord351 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord352 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord353 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord354 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord355 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord356 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord357 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord358 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord359 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord360 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord361 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord362 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord363 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord364 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord365 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord366 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord367 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord368 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord369 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord370 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord371 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord372 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord373 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord374 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord375 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord376 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord377 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord378 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord379 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord380 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord381 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord382 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord383 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord384 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord385 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord386 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord387 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord388 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord389 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord390 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord391 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord392 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord393 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord394 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord395 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord396 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord397 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord398 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord399 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord400 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord401 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord402 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord403 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord404 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord405 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord406 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord407 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord408 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord409 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord410 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord411 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord412 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord413 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord414 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord415 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord416 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord417 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord418 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord419 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord420 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord421 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord422 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord423 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord424 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord425 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord426 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord427 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord428 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord429 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord430 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord431 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord432 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord433 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord434 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord435 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord436 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord437 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord438 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord439 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord440 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord441 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord442 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord443 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord444 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord445 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord446 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord447 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord448 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord449 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord450 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord451 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord452 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord453 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord454 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord455 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord456 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord457 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord458 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord459 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord460 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord461 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord462 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord463 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord464 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord465 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord466 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord467 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord468 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord469 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord470 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord471 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord472 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord473 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord474 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord475 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord476 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord477 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord478 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord479 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord480 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord481 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord482 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord483 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord484 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord485 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord486 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord487 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord488 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord489 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord490 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord491 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord492 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord493 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord494 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord495 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord496 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord497 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord498 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord499 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord500 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord501 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord502 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord503 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord504 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord505 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord506 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord507 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord508 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord509 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord510 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord511 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord512 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord513 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord514 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord515 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord516 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord517 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord518 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord519 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord520 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord521 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord522 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord523 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord524 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord525 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord526 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord527 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord528 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord529 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord530 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord531 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord532 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord533 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord534 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord535 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord536 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord537 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord538 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord539 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord540 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord541 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord542 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord543 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord544 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord545 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord546 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord547 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord548 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord549 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord550 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord551 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord552 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord553 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord554 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord555 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord556 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord557 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord558 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord559 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord560 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord561 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord562 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord563 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord564 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord565 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord566 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord567 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord568 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord569 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord570 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord571 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord572 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord573 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord574 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord575 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord576 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord577 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord578 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord579 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord580 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord581 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord582 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord583 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord584 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord585 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord586 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord587 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord588 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord589 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord590 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord591 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord592 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord593 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord594 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord595 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord596 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord597 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord598 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord599 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord600 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord601 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord602 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord603 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord604 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord605 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord606 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord607 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord608 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord609 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord610 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord611 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord612 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord613 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord614 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord615 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord616 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord617 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord618 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord619 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord620 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord621 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord622 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord623 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord624 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord625 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord626 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord627 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord628 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord629 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord630 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord631 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord632 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord633 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord634 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord635 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord636 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord637 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord638 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord639 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord640 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord641 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord642 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord643 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord644 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord645 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord646 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord647 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord648 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord649 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord650 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord651 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord652 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord653 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord654 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord655 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord656 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord657 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord658 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord659 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord660 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord661 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord662 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord663 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord664 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord665 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord666 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord667 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord668 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord669 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord670 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord671 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord672 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord673 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord674 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord675 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord676 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord677 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord678 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord679 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord680 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord681 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord682 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord683 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord684 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord685 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord686 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord687 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord688 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord689 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord690 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord691 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord692 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord693 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord694 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord695 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord696 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord697 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord698 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord699 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord700 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord701 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord702 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord703 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord704 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord705 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord706 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord707 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord708 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord709 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord710 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord711 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord712 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord713 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord714 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord715 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord716 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord717 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord718 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord719 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord720 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord721 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord722 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord723 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord724 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord725 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord726 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord727 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord728 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord729 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord730 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord731 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord732 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord733 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord734 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord735 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord736 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord737 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord738 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord739 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord740 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord741 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord742 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord743 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord744 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord745 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord746 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord747 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord748 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord749 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord750 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord751 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord752 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord753 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord754 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord755 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord756 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord757 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord758 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord759 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord760 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord761 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord762 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord763 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord764 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord765 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord766 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord767 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord768 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord769 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord770 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord771 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord772 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord773 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord774 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord775 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord776 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord777 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord778 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord779 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord780 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord781 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord782 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord783 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord784 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord785 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord786 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord787 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord788 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord789 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord790 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord791 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord792 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord793 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord794 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord795 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord796 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord797 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord798 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord799 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord800 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord801 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord802 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord803 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord804 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord805 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord806 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord807 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord808 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord809 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord810 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord811 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord812 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord813 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord814 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord815 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord816 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord817 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord818 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord819 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord820 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord821 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord822 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord823 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord824 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord825 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord826 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord827 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord828 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord829 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord830 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord831 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord832 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord833 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord834 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord835 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord836 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord837 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord838 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord839 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord840 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord841 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord842 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord843 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord844 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord845 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord846 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord847 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord848 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord849 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord850 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord851 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord852 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord853 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord854 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord855 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord856 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord857 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord858 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord859 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord860 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord861 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord862 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord863 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord864 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord865 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord866 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord867 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord868 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord869 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord870 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord871 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord872 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord873 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord874 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord875 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord876 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord877 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord878 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord879 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord880 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord881 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord882 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord883 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord884 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord885 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord886 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord887 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord888 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord889 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord890 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord891 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord892 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord893 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord894 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord895 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord896 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord897 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord898 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord899 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord900 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord901 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord902 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord903 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord904 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord905 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord906 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord907 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord908 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord909 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord910 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord911 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord912 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord913 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord914 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord915 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord916 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord917 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord918 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord919 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord920 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord921 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord922 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord923 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord924 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord925 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord926 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord927 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord928 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord929 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord930 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord931 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord932 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord933 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord934 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord935 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord936 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord937 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord938 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord939 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord940 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord941 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord942 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord943 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord944 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord945 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord946 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord947 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord948 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord949 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord950 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord951 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord952 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord953 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord954 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord955 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord956 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord957 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord958 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord959 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord960 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord961 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord962 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord963 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord964 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord965 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord966 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord967 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord968 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord969 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord970 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord971 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord972 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord973 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord974 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord975 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord976 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord977 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord978 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord979 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord980 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord981 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord982 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord983 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord984 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord985 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord986 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord987 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord988 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord989 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord990 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord991 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord992 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord993 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord994 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord995 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord996 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord997 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord998 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord999 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public type MyRecord1000 record {
    *Info;
    int a = 10;
    byte b = 5;
    float f = 4.5;
    boolean bl = true;
    decimal d = 123.456;
    Person p = {};
    [int, string] t = [5, "Apple"];
    int[] array = [100, 200, 300];
    Colors c = RED;
    string|() status = ();
};

public function testLargeNumberOfRecords() {
    MyRecord1 r1 = {};
    MyRecord1000 r2 = {p: {name:"Gabilan"}};
    test:assertEquals(r1.p.name, "Waruna");
    test:assertEquals(r2.p.name, "Gabilan");
    test:assertEquals(r1.x, "Info");
    test:assertEquals(r2.x, "Info");
}
