// Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

import syntaxtree.moduleA;
import syntaxtree.moduleB;

public function main() {
    moduleB:Person p1 = new moduleB:Person(1001, "John", 20);
    moduleB:Student s1 = new moduleB:Student(1001, "John", 8.25);
    int age1 = p1.getAge();
    decimal score1 = s1.getScore();
    int id1 = s1.getId();

    moduleB:Person p2 = new moduleB:Person(1002, "Jane", 21);
    moduleB:Student s2 = new moduleB:Student(1002, "Jane", 9.25);

    moduleA:Examiner e1 = new moduleA:Examiner(2001, "Peter", "MSc");
    string name1 = e1.getName();
    string degree1 = e1.getQualification();
}
