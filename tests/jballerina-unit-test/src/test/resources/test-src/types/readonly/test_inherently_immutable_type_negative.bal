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

 function cannotAssignReadonlyToAny() {
     readonly y = "sd";
     any x = y;
 }

  function cannotDeepEqualReadonly() returns boolean {
      readonly arr = [1, 2 , 3];
      return arr == [1, 2 , 3];
  }

  function testReadOnlyAssignabilityToUnions() {
       readonly readonlyVal = 1;
       error? errOrNil = readonlyVal;
       int|any intOrAny = readonlyVal;
       readonly|any s = "s";
       string|readonly stringOrReadonly = s;
       readonly|string s2 = "1";
       any? o = s2;
   }
