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

channel<string> strChn = new;
channel<int> intChn = new;

public function basicWorkerTest() returns int {
    int i = 10;
    worker w1 {
      i = i + 40;
      i -> w2;
      "message" -> strChn, 66;
    }

    worker w2 returns int {
      string strResult = <- strChn, 66;
      int j = 25;
      i = j;
      j = <- w1;
      return j;
    }

    _ = wait {w1, w2};
    return i;
}


public function testWithTuples() returns (string, int) {
    string str = "Hello Ballerina!!!";
    int i = 10;
    worker w1 {
      str = "Changed inside worker 1!!!";
      i = i + 40;
      i -> w2;
      "message" -> strChn, 66;
    }

    worker w2 returns int {
      string strResult = <- strChn, 66;
      i = 100 + i;
      int j = <- w1;
      str = str + " -- Changed inside worker 2!!!";
      return j;
    }

    _ = wait {w1, w2};
    return (str, i);
}

public function testWithMaps() returns map<string> {
    map<string> m1 = {a: "A", b: "B", c: "C", d: "D"};
    worker w1 {
      m1["e"] = "EE";
      m1["a"] = "AA";
      "message" -> strChn, 66;
    }

    worker w2 {
       string strResult = <- strChn, 66;
       m1["a"] = "AAA";
       m1["n"] = "N";
       "message" -> strChn, 33;
    }

     worker w3 {
        string strResult = <- strChn, 33;
        m1["e"] = "EEE";
        m1["a"] = "AAAA";
     }

    _ = wait {w1, w2, w3};

    m1["x"] = "X";
    return m1;
}

public function complexWorkerTest() returns (int, map<string>) {
    int i = 5;
    map<string> m1 = {a: "A", b: "B", c: "C", d: "D"};

    worker w1 {
      m1["e"] = "EE";
      m1["a"] = "AA";
      4 -> intChn, 24;

      fork {
        worker w4 {
            int intResult = <- intChn, 24;
            int j = 100 * 2;
            i = j;
            m1["b"] = "BB";
            1 -> intChn, 11;
        }

        worker w5 {
            int intResult = <- intChn, 11;
            5 -> intChn, 55;
            i = i + 50;
            m1["m"] = "M";
            fork {
                worker w6 {
                    intResult = <- intChn, 55;
                    i = i + 100;
                    m1["m"] = "MMM";
                    m1["a"] = "AAAA";
                }
            }
            _ = wait w6;
        }

      }
      _ = wait {w4, w5};
    }
    _ = wait w1;

    // Modify 'i' and the map
    i = i + 50;
    m1["b"] = "BBBB";

    return (i, m1);
}

public type Student record {|
    string name;
    int age;
    string...;
|};

public function testWithRecords() returns Student {
    Student stu = {name: "John Doe", age: 17};
    worker w1 {
      "message" -> strChn, 100;
       stu.name = "Adam Page";
    }

    worker w2 {
       string strResult = <- strChn, 100;
       stu = {name: "Adam Page", age: 24};
       stu.email = "adamp@gmail.com";
       "message" -> strChn, 111;
    }

     worker w3 {
        string strResult = <- strChn, 111;
        stu.email = "adamp@wso2.com";
     }

    _ = wait {w1, w2, w3};

    return stu;
}

public type Person object {
    public int age;
    public string name;
    public string fullName;

    function __init(int age, string name, string fullName) {
        self.age = age;
        self.name = name;
        self.fullName = fullName;
    }
};

public function testWithObjects() returns Person {
    Person p1 = new(5, "John", "John Doe");
    worker w1 {
      "message" -> strChn, 100;
       p1.age = 10;
       p1.name = "Joe";
    }

    worker w2 {
       string res = <- strChn, 100;
       p1.age = 25;
       "message" -> strChn, 111;
    }

     worker w3 {
        string res1 = <- strChn, 111;
        p1 = new(40, "Adam", "Adam Adam Page");
     }

    _ = wait {w1, w2, w3};
    return p1;
}
