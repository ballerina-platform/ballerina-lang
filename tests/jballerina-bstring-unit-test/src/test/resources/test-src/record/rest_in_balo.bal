// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import testorg/records;

function testORRestFieldInOR() returns records:OpenFoo {
    records:OpenBar ob = {x:1.0};
    records:OpenFoo of = {name:"Open Foo"};
    of["ob"] = ob;
    return of;
}

function testORRestFieldInCR() returns records:ClosedFoo {
    records:OpenBar ob = {x:2.0};
    records:ClosedFoo cf = {name:"Closed Foo"};
    cf["ob"] = ob;
    return cf;
}

function testCRRestFieldInOR() returns records:OpenFoo {
    records:ClosedBar cb = {x:3.0};
    records:OpenFoo2 of = {name:"Open Foo"};
    of["cb"] = cb;
    return of;
}

function testCRRestFieldInCR() returns records:ClosedFoo {
    records:ClosedBar cb = {x:4.0};
    records:ClosedFoo2 cf = {name:"Closed Foo"};
    cf["cb"] = cb;
    return cf;
}
