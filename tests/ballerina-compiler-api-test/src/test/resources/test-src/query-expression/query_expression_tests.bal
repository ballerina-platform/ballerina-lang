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

type Student record {
    string fname;
    string lname;
    int age;
    float gpa;
};

public function testQueryExpression() {
    Student s1 = {fname: "Jon", lname: "Doe", age: 21, gpa: 2.1};
    Student s2 = {fname: "Jane", lname: "Doe", age: 25, gpa: 3.2};
    Student s3 = {fname: "Amy", lname: "Melina", age: 30, gpa: 1.3};

    Student[] students = [s1, s2, s3];

    var x = from var st in students
        where st.fname == "Jon"
        select {name: st.fname};

    string[] fullName =   from var {fname, lname} in students
                            let int len1 = fname.length()
                            where len1 > 0
                            let int len2 = lname.length()
                            where len2 > 0
                            let string name = fname + " " + lname  select name;

    Student[] gpaRanking =  from var st in students order by st.gpa ascending
                                select st;

    Student[] selectedStudents =  from var st in students order by st.gpa ascending
                          limit 2 select st;

}
