// Copyright (c) 2023 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import numericliteral/testproject as module1;

function testNumericLiteralAssignmentNegative() {
    module1:Foo _ = 3; //error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo', found 'float'
    module1:Foo _ = 2;
    module1:Foo _ = 2.0;
    module1:Foo _ = 1.0; //error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo', found 'float'
    module1:Foo _ = 1.2; //error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo', found 'float'
    module1:Foo _ = 2f;
    module1:Foo _ = 3f; //error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo', found 'float'
    module1:Foo _ = 2d;
    module1:Foo _ = 3d;
    module1:Foo2 _ = 1;
    module1:Foo2 _ = 2; // error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo2', found 'int'
    module1:Foo2 _ = 3; // error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo2', found 'int'
    module1:Foo2 _ = 4; // error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo2', found 'int'
    module1:Foo2 _ = 5; // error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo2', found 'int'
    module1:Foo2 _ = 6;
    module1:Foo2 _ = 7; // error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo2', found 'int'
    module1:Foo2 _ = 7.1; // error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo2', found 'float'
    module1:Foo3 _ = 2;
    module1:Foo3 _ = 3;
    module1:Foo3 _ = 4; // error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo3', found 'float'
    module1:Foo3 _ = 5; // error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo3', found 'float'
    module1:Foo3 _ = 7; // error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo3', found 'float'
    module1:Foo3 _ = 7.1; // error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo3', found 'float'
    module1:Foo4 _ = 1;
    module1:Foo4 _ = 2;
    module1:Foo4 _ = 3;
    module1:Foo4 _ = 4;
    module1:Foo4 _ = 5;
    module1:Foo4 _ = 7;
    module1:Foo4 _ = 7.1;
    module1:Foo5 _ = 4; //error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo5', found 'int'
    module1:Foo5 _ = 4.0; //error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo5', found 'float'
    module1:Foo5 _ = 5.0; //error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo5', found 'float'
    module1:Foo5 _ = 5.2; //error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo5', found 'float'
    module1:Foo5 _ = 4f; //error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo5', found 'float'
    module1:Foo5 _ = 4d;
    module1:Foo5 _ = 5f; //error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo5', found 'float'
    module1:Foo5 _ = 5d;
    module1:Foo5 _ = 2.0;
    module1:Foo5 _ = 2; //error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo5', found 'int'
    module1:Foo5 _ = 6.0; //error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo5', found 'float'
    module1:Foo5 _ = 6.2; //error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo5', found 'float'
    module1:Foo5 _ = 2f;
    module1:Foo5 _ = 2d;
    module1:Foo5 _ = 6f; //error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo5', found 'float'
    module1:Foo5 _ = 6d;
    module1:Foo5 _ = 3.0;
    module1:Foo5 _ = 3; //error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo5', found 'int'
    module1:Foo5 _ = 7.0; //error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo5', found 'float'
    module1:Foo5 _ = 7.2; //error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo5', found 'float'
    module1:Foo5 _ = 3f;
    module1:Foo5 _ = 3d;
    module1:Foo5 _ = 7f; //error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo5', found 'float'
    module1:Foo5 _ = 7d;
    module1:Foo5 _ = 1;
    module1:Foo5 _ = 10; //error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo5', found 'int'
    module1:Foo5 _ = 1.0; //error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo5', found 'float'
    module1:Foo5 _ = 1.2; //error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo5', found 'float'
    module1:Foo5 _ = 1f; //error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo5', found 'float'
    module1:Foo5 _ = 1d;
    module1:Foo5 _ = 10f; //error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo5', found 'float'
    module1:Foo5 _ = 10d;
    module1:Foo5 _ = 2.1;
    module1:Foo5 _ = 2.1f;
    module1:Foo5 _ = 2.1d;
    module1:Foo5 _ = 8.0; //error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo5', found 'float'
    module1:Foo5 _ = 8.2; //error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo5', found 'float'
    module1:Foo5 _ = 8d;
    module1:Foo5 _ = 8f; //error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo5', found 'float'
    module1:Foo5 _ = 9.0; //error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo5', found 'float'
    module1:Foo5 _ = 9.2; //error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo5', found 'float'
    module1:Foo5 _ = 9d;
    module1:Foo5 _ = 9f; //error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo5', found 'float'
    module1:Foo5 _ = 24; //error: incompatible types: expected 'numericliteral/testproject:0.1.0:Foo5', found 'int'
}
