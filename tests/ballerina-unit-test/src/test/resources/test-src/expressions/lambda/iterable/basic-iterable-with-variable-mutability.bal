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

// ARRAYS WITH SINGLE ITERABLE OPERATION

function testInt1() returns int {
    int add = 0;
    int[] fa = [-5, 2, 4, 5, 7, -8, -3, 2];
    fa.foreach(function (int i) {
        add = add + i;
    });
    return add;
}

function testFloat1() returns float {
    float fadd = 0.0;
    float[] fa = [1.1, 2.2, -3.3, 4.4, 5.5];
    fa.foreach(function (float i) {
        fadd = fadd + i;
     });
    return fadd;
}

// ARRAYS WITH NESTED ITERABLE OPERATIONS

function testBasicArray1(string[] values) returns string {
    string str1 = "";
    values.map(function (string x) returns (string, string) {
                   string up = x.toUpper();
                   string lower = x.toLower();
                   return (up, lower);
               })
           .foreach(function ((string, string) value) {
                    var (up, lower) = value;
                    str1 = str1 + " " + up + ":" + lower;
           });
    return str1.trim();
}

function testBasicArray2(string[] values) returns string {
    string str1 = "";
    int index = 0;
    values.map(function (string s) returns string {
                    var value = string.convert(index) + s;
                    index += 1;
                    return value;
               })
    .foreach(function (string s) {
                 str1 = str1 + s + " ";
             });
    return str1.trim();
}

// MAPS WITH NESTED ITERABLE OPERATIONS

function testBasicMap1() returns (int, string[]) {
    map<string> m = {a:"A", b:"B", c:"C", d:"D", e:"E"};
    int count = 0;
    string[] values = m.map(function ((string, string) value) returns string {
                                count = count + 10;
                                var (k, v) = value;
                                return k.toLower();
                            })
                      .filter(function (string v) returns boolean {
                                  if (v == "a" || v == "e" && count != 0) {
                                      count = count + 10;
                                      return true;
                                  }
                                  return false;
                            });
    return (count, values);
}

function testBasicMap2() returns (int, string, string[]) {
    int count = 0;
    string str = "start";
    map<string> m = {a:"A", b:"B", c:"C", d:"D", e:"E"};
    string[] values = m.map(function ((string, string) tuple) returns (string, string) {
                                var (key, value) = tuple;
                                str = str + "-"  + key + " : " + value;
                                count = count + 10;
                                return (key, value);
                            })
                      .filter(function ((string, string) v) returns boolean {
                                  var (k, t) = v;
                                  if (k == "a" || k == "e") {
                                      count = count + 10;
                                      return true;
                                  }
                                  return false;
                              })
                      .map(function ((string, string) v) returns string {
                               var (v1, v2) = v;
                               count = count + 10;
                               return v1 + v2;
                           });
    str = str + "-end";
    count = count + 1;
    return (count, str, values);
}

// XML WITH NESTED ITERABLE OPERATIONS

function xmlTest() returns (string, map<any>) {
    string str = "start";
    xml xdata = xml `<p:person xmlns:p="foo" xmlns:q="bar">
        <p:name>bob</p:name>
        <p:address>
            <p:city>NY</p:city>
            <q:country>US</q:country>
        </p:address>
        <q:ID>1131313</q:ID>
    </p:person>`;

    int index = 0;
    map<xml> m = xdata.*.elements()[1].*.elements()
                 .map(function (xml|string x) returns (string, xml) {
                            index += 1;
                            if x is xml {
                                str = str + "-" + string.convert(index);
                                return (string.convert(index), x);
                            }
                            return ("", xml ` `);
                      });
    str = str + "-end";
    return (str, m);
}

// RECORD WITH NESTED ITERABLE OPERATIONS

type Student record {
    string fname;
    int age;
};

function recordTest() returns (int, string[]) {
    boolean status = true;
    int randomNum = 0;

    Student s1 = {fname:"aa", age:30};
    Student s2 = {fname:"bb", age:20};
    Student s3 = {fname:"cc", age:24};
    Student[] studentArr = [s1, s2, s3];

    int count = studentArr.filter(function (Student stu) returns boolean {
                             randomNum = randomNum + 2;
                             return status && stu.age < 25;
                         }).count();
    string[] names = studentArr.map(function (Student stu) returns string {
                               return stu.fname;
                           });
    randomNum = randomNum + 10;
    return (randomNum, names);
}

function testIgnoredValue() returns (string) {
    string str = "The start-";
    string[] s = ["abc", "cde", "pqr", "xy"];
    _ = s.filter(function (string s) returns boolean {
            return s.length() == 3;
          })
         .map(function (string s) returns string {
            str = str + " hello " + s + " :) bye :) ";
            return (str + s);
          });
    str = str + "-The end";
    return str.trim();
}

// TEST IN EXPRESSION

function testInExpression() returns (string, int) {
    string str = "";
    string[] strArr = ["abc", "cde", "pqr", "xyz"];
    float[] floatArr = [1.1, -2.2, 3.3, 4.4];

    var x = function (any a) {
        var s = <string>a;
        str = s;
        // Add a new element to the array
        strArr[4] = "mno";
    };

    x.call("total count " + strArr.filter(function (string str) returns boolean {
                                                // Add a new element to the array
                                                strArr[5] = "stu";
                                                return str.length() == 3;
                                            })
                                  .count());

    int i = strArr.count() + floatArr.count();
    return (str, i);
}

// TEST IN STATEMENT

function testInStatement() returns int {
    map<string> m = {a:"abc", b:"cd", c:"pqr"};
    int count = 2;
    if (5 > m.filter(function ((string, string) value) returns boolean {
                         count = count * 2;
                         var (k, v) = value;
                         return v.length() == 3 && count % 2 == 0;
                     })
              .count()) {
        return count;
    }
    return 0;
}
