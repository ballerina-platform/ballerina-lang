//  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
//  WSO2 Inc. licenses this file to you under the Apache License,
//  Version 2.0 (the "License"); you may not use this file except
//  in compliance with the License.
//  You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

//Test module level mapping binding pattern
type Age record {
    int age;
    string format;
};

type Person record {|
    string name;
    boolean married;
|};

Person {name:Fname, married:Married} = {name:"Jhone", married:true};

public function testBasic() returns [string, boolean] {
    return [Fname, Married];
}


// Record var inside record var ------------------------------------------------------------
type PersonWithAge record {
    string name;
    Age age;
    boolean married;
};
PersonWithAge {name: fName1, age: {age: theAge1, format:format1}, married} = getPersonWithAge();

function recordVarInRecordVar() returns [string, int, string, boolean] {
    return [fName1, theAge1, format1, married];
}

function getPersonWithAge() returns PersonWithAge {
    return { name: "Peter", age: {age:29, format: "Y"}, married: true, "work": "SE" };
}
// ------------------------------------------------------------------------------------------------

// Tuple var inside record var ------------------------------------------------------------
type PersonWithAge2 record {
    string name;
    [int, string] age;
    boolean married;
};
PersonWithAge2 {name: fName2, age: [age2, format2], married:married2} = getPersonWithAge2();

function tupleVarInRecordVar() returns [string, int, string, boolean] {
    return [fName2, age2, format2, married2];
}

function getPersonWithAge2() returns PersonWithAge2 {
    return { name: "Mac", age:[21, "Y"], married: false};
}
// ------------------------------------------------------------------------------------------------


// Check annotation support ---------------------------------------------------------------------
const annotation annot on source var;

@annot
Age {age, format} = {age:24, format:"myFormat"};
