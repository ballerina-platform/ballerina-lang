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

type CustomRecord record { string a; };
CustomRecord customRecord = { a: "value"};

type CustomIntegerType int;
CustomIntegerType customIntegerType = 3;

public function testClientResourceReturnValuesAndParamArgumentsError() {
    var successClient = client object {
        resource function get stringParamPath(string a) returns string {
            return "string";
        }

        resource function get intParamPath(int a) returns int {
            return 1;
        }

        resource function get booleanParamPath(boolean a) returns boolean {
            return true;
        }

        resource function get floatParamPath(float a) returns float {
            return 1.2f;
        }

        resource function get decimalPath(decimal a) returns decimal {
            return 2.3d;
        }

        resource function get recordParamPath(record {int a;} a) returns record {int a;} {
            return {a: 1};
        }

        resource function get xmlParamPath(xml a) returns xml {
            return xml `<a></a>`;
        }

        resource function get mapParamPath(map<string> a) returns map<string> {
            return {a: "string value"};
        }

        resource function get arrayParamPath(int[] a) returns int[] {
            return [1, 2, 3];
        }

        resource function get customTypeParamPath(CustomRecord a) returns CustomRecord {
            return customRecord;
        }

        resource function get customIntTypeParamPath(CustomIntegerType a) returns CustomIntegerType {
            return customIntegerType;
        }
    };

    xml _ = successClient->/stringParamPath("string");
    xml _ = successClient->/intParamPath(2);
    xml _ = successClient->/booleanParamPath(true);
    xml _ = successClient->/floatParamPath(2.3f);
    xml _ = successClient->/decimalPath(3.2d);

    string _ = successClient->/stringParamPath(2);
    int _ = successClient->/intParamPath("string");
    boolean _ = successClient->/booleanParamPath(3.4f);
    float _ = successClient->/floatParamPath(2.3d);
    decimal _ = successClient->/decimalPath(true);

    string _ = successClient->/recordParamPath({a: 2});
    string _ = successClient->/xmlParamPath(xml `<a></a>`);
    string _ = successClient->/mapParamPath({a: "value"});
    string _ = successClient->/arrayParamPath([1, 2, 3]);
    string _ = successClient->/customTypeParamPath(customRecord);
    string _ = successClient->/customIntTypeParamPath(customIntegerType);

    record {int a;} _ = successClient->/recordParamPath(customRecord);
    xml _ = successClient->/xmlParamPath({a: 2});
    map<string> _ = successClient->/mapParamPath(xml `<a></a>`);
    int[] _ = successClient->/arrayParamPath({a: "value"});
    CustomRecord _ = successClient->/customTypeParamPath([1, 2, 3]);
    CustomIntegerType _ = successClient->/customIntTypeParamPath([1, 2, 3]);
}
