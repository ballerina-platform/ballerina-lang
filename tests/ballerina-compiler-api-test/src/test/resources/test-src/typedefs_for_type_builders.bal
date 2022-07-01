// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type XmlEle xml:Element;

type XmlPi xml:ProcessingInstruction;

type XmlCmnt xml:Comment;

type XmlTxt xml:Text;

type MyInt int;

type XmlUnionA xml:Element|xml:ProcessingInstruction|xml:Text;

type XmlUnionB XmlEle|XmlTxt|XmlCmnt;

type MixXmlA XmlUnionA|XmlUnionB;

type MixXmlB XmlPi|MixXmlC;

type MixXmlC XmlUnionA|XmlTxt|MixXmlA;

type NewEle XmlEle;

type EleTxtCmnt XmlCmnt|xml:Text|NewEle;

type Detail record {
    int code;
    string message?;
    error cause?;
};

type SampleErrorData record {
    string message?;
    error cause?;
    string info;
    boolean fatal;
};

type TrxErrorData record {|
    string message = "";
    error cause?;
    string data = "";
|};

const FOO = "foo";

type FooError distinct error<Detail>;

error e = error FooError(FOO, code = 3456);

error<Detail> ex = error(FOO, code = 212);

type State "on"|"off";

type Num 1;

State a = "on";

const ab = 1;
const cd = "cd1";
var xyz1 = 23;

type Mp map<int|string>;

type A 5.0;

A b = 5.0;

type MySingleton A;

type Customer record {
    readonly int id;
    readonly string name;
    string lname;
};

type Employee record {
    readonly string name;
    int salary;
};

type Student record {|
    int id;
    string name;
|};

type Bar record {|
    string b;
|};

function complexArrayTypes() {
    int a;
    int[] b;
    string[1] c;
    int[][2] d;
    int[2][*][3] e;
    (int|string)[1][2] f;
    (int|string)[4][] g;
    (Bar & readonly)[1][2][*] h;
    (Bar & readonly)[3][4] i;
    (Bar & readonly)[4] j;
}
