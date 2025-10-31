// Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

annotation MyAnnot on parameter, function, class, field, service, type, return;
annotation YourAnnot on function, parameter, field, return;

public type Person record {
    string name;
    @MyAnnot
    int age = add(10, 20);
};

@MyAnnot
public type Student record {
    Person person;
    string school;
};

public isolated function add(int a, int b) returns int {
    return a + b;
}

Person p = {name: "John"};

@MyAnnot
type INT int;

@MyAnnot
@divAnnot {
    value: "ZeroDiv",
    out: zeroDiv(10)
}
service class Review {
    int id;
    @MyAnnot
    @divAnnot {
        out: zeroDiv(10),
        value: "ZeroDiv"
    }
    string content;
    int groupId;
    @YourAnnot
    float rating;

    function init(
            @MyAnnot
                int id, string content,
                @YourAnnot int groupId,
                float rating) {
        self.id = id;
        self.content = content;
        self.groupId = groupId;
        self.rating = rating;
    }
}

type ZeroDiffAnnot record {
    string value;
    boolean out;
};

annotation ZeroDiffAnnot zeroDivAnnot on function,return;
annotation ZeroDiffAnnot divAnnot on function,return, field, class;

function zeroDiv(int n) returns boolean {
   return n / 10 == 0;
}

@zeroDivAnnot {
    value: "ZeroDiv",
    out: zeroDiv(10)
}
@divAnnot {
    value: "ZeroDiv",
    out: zeroDiv(10)
}
function foo(int n)
                returns
                    @zeroDivAnnot {
                        value: "ZeroDiv",
                        out: zeroDiv(10)
                    }
                    int {
    var _ =
            @MyAnnot
                service object {
        resource function get .() returns string {
            return "Hello, World!";
        }
    };
    return n;
}

@MyAnnot
function bar() returns int {
    Student s = { person: p, school: "ABC College"};
    INT i = 10;
    int x = foo(i);
    Review r = new(1, "hello", x, 1.234);
    return 1;
}
