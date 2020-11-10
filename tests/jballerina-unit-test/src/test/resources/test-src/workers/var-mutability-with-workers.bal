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

public function basicWorkerTest() returns int {
    int i = 10;
    @strand{thread:"any"}
    worker w1 {
      i = i + 40;
      i -> w2;
    }

    @strand{thread:"any"}
    worker w2 returns int {
      int j = 25;
      j = <- w1;
      i = j;
      return j;
    }

    _ = wait {w1, w2};
    return i;
}


public function testWithTuples() returns [string, int] {
    string str = "Hello Ballerina!!!";
    int i = 10;
    @strand{thread:"any"}
    worker w1 {
      str = "Changed inside worker 1!!!";
      i = i + 40;
      i -> w2;
    }

    @strand{thread:"any"}
    worker w2 returns int {
      int j = <- w1;
      i = 100 + i;
      str = str + " -- Changed inside worker 2!!!";
      return i;
    }

    _ = wait {w1, w2};
    return [str, i];
}

const TOKEN = "token";
public function testWithMaps() returns map<string> {
    map<string> m1 = {a: "A", b: "B", c: "C", d: "D"};
    @strand{thread:"any"}
    worker w1 {
      m1["e"] = "EE";
      m1["a"] = "AA";
      TOKEN -> w3;
    }

    @strand{thread:"any"}
    worker w2 {
       m1["a"] = "AAA";
       m1["n"] = "N";
       TOKEN -> w3;
    }

     @strand{thread:"any"}
     worker w3 {
        _ = <- w1;
        _ = <- w2;
        m1["e"] = "EEE";
        m1["a"] = "AAAA";
     }

    _ = wait {w1, w2, w3};

    m1["x"] = "X";
    return m1;
}

public function complexWorkerTest() returns [int, map<string>] {
    int i = 5;
    map<string> m1 = {a: "A", b: "B", c: "C", d: "D"};

    @strand{thread:"any"}
    worker w1 {
      m1["e"] = "EE";
      m1["a"] = "AA";

      fork {
        worker w4 {
            int j = 100 * 2;
            i = j;
            m1["b"] = "BB";

            i = i + 50;
            m1["m"] = "M";
            fork {
                worker w6 {
                    i = i + 100;
                    m1["m"] = "MMM";
                    m1["a"] = "AAAA";
                }
            }
            _ = wait w6;
        }

      }
      _ = wait w4;
    }
    _ = wait w1;

    // Modify 'i' and the map
    i = i + 50;
    m1["b"] = "BBBB";

    return [i, m1];
}

public type Student record {|
    string name;
    int age;
    string email?;
    string...;
|};

public function testWithRecords() returns Student {
    Student stu = {name: "John Doe", age: 17};
    @strand{thread:"any"}
    worker w1 {
       stu.name = "Adam Page";
    }

    @strand{thread:"any"}
    worker w2 {
       stu = {name: "Adam Page", age: 24};
       stu.email = "adamp@gmail.com";
    }

    // worker w3 {
    //    wait w2;
    //    stu.email = "adamp@wso2.com";
    //}

    var f = function () {
        wait w2;
        stu.email = "adamp@wso2.com";
    };
    var fw = @strand{thread:"any"} start f();
    _ = wait {w1, w2, fw};

    return stu;
}

public class Person {
    public int age;
    public string name;
    public string fullName;

    function init(int age, string name, string fullName) {
        self.age = age;
        self.name = name;
        self.fullName = fullName;
    }
}

public function testWithObjects() returns Person {
    Person p1 = new(5, "John", "John Doe");
    @strand{thread:"any"}
    worker w1 {
       p1.age = 10;
       p1.name = "Joe";
    }

    @strand{thread:"any"}
    worker w2 {
       p1.age = 25;
    }

    var f = function () {
        _ = wait {w1, w2};
        p1 = new(40, "Adam", "Adam Adam Page");
    };
    future<()> w3 = @strand{thread:"any"} start f();

    _ = wait {w1, w2, w3};
    return p1;
}
