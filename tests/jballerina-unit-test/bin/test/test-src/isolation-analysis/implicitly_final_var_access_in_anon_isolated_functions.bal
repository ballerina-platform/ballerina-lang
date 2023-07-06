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

public function testAnonIsolatedFuncAccessingImplicitlyFinalVars(int i) {
   var fn1 = isolated function () returns int => i;

   int[] x = [];
   foreach var j in x {
      var fn2 = isolated function () returns int {
         return j + i;
      };
   }

   var fn3 = let int k = 1 + i in isolated function () returns int => k;

   var fn4 = let int k = 1 + i in isolated function (int l) returns int {
      return k + l;
   };

   // https://github.com/ballerina-platform/ballerina-lang/issues/30987
   // (isolated function () returns int)[] fn5 =
   //    from var m in x
   //    where m > 10
   //    let int n = m + 1
   //    select isolated function () returns int => m + n;


   isolated function () returns int fn6 = () => i;

   foreach var j in x {
      isolated function () returns int fn7 = () => j;
   }

   isolated function () returns int fn8 = let int k = 1 + i in () => k;

   isolated function (int) returns int fn9 = let int k = 1 + i in (l) => k + l;

   isolated function (int[]) returns int[] fn10 = let int[] k = [i] in (l) => l;
}
