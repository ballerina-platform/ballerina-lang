// Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

type Grades record {|
    int maths;
    int physics;
    int chemistry;
    int...;
|};

type Student record {
    string name;
    int age;
    Grades grades;
    string city;
};

public function main() {
    Student Kamal = {
        name : "Kamal",
        age : 10,
        grades : {
            maths : 90,
            physics : 99,
            chemistry : 95
        },
        city : "Colombo"
    };

    Student Amal = {
            name : "Amal",
            age : 10,
            grades : {
                maths : 98,
                physics : 100,
                chemistry : 95
            },
            city : "Galle"
        };

    Grades Kamal_grades = mapStudentToGrades(Kamal);

    Grades Amal_grades = Amal;
}



function mapStudentToGrades (Student student) returns Grades {
// Some record fields might be missing in the AI based mapping.
	Grades grades = {maths: student.grades.maths, chemistry: student.grades.chemistry, physics: student.grades.physics};
	return grades;
}
