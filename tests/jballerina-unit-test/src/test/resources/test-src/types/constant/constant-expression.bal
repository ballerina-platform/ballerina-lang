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

const int CAI = 10 + 5;
const float CAF = 10.0 + 5;
const decimal CAD = 11.5 + 4;
const string CAS = "hello" + "world";

function getConstAdditions() returns [int, float, decimal, string] {
    return [CAI, CAF, CAD, CAS];
}

const int CSI = 10 - 5;
const float CSF = 10.5 - 5;
const decimal CSD = 10.5 - 5;

function getConstSubtracts() returns [int, float, decimal] {
    return [CSI, CSF, CSD];
}

const int CMI = 10 * 5;
const float CMF = 10.5 * 5;
const decimal CMD = 10.5 * 5;

function getConstMultiplications() returns [int, float, decimal] {
    return [CMI, CMF, CMD];
}

const int CDI = 10 / 5;
const float CDF = 10.5 / 5;
const float NAN = 0.0 / 0;
const float IFN = 1.0 / 0;
const decimal CDD = 10.5 / 5;

function getConstDivisions() returns [int, float, float, float, decimal] {
    return [CDI, CDF, NAN, IFN, CDD];
}

const int CGI1 = (10 + (5 + (10 - 5)));
const int CGI2 = (10 / (-5 - (10 - 5)));
const int CGI3 = (10 + (5 * (10 - 5)));

function getConstGrouping() returns [int, int, int] {
    return [CGI1, CGI2, CGI3];
}

const float CF1 = 1.0 + 2.0;
const float CF2 = 2.0 + 3.0;

const map<float> CFMap = { v1 : CF1, v2 : CF2};

function checkMapAccessReference()  returns map<float> {
    return CFMap;
}
