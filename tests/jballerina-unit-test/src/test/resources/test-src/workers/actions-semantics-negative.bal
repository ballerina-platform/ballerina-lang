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

type firstRec record {
    int id = 1;
    string name = "default";
};

type secondRec record {
    int f1 = 1;
    string f2 = "default";
};

type thirdRec record {
    int f1;
    string f2;
    int f4?;
};

class Person {
    public int age = 1;
    public string name = "default";
}

function workerActionFirstTest() {
    worker w1 {
        Person p1 = new Person();
        // Async send expr should be of anydata
        p1 -> w2;
        // Sync send expr should be of anydata
        var result = p1 ->> w2;
        // Invalid worker
        var x = flush w4;
    }
    worker w2 returns error? {
        // Receive expr should get anydata
        if(false){
             error err = error("err", message = "err msg");
             return err;
        }
        Person p2 = <- w1;
        Person p3 = new Person();
        p3 = <- w1;
        return;
    }
    worker w3 {
        // No send actions to particular worker
        flush w1;
    }
}

function workerActionSecTest() {
    worker w1 {
        int i =10;
        i -> w2;

        string msg = "hello";
        msg -> w2;

        if (true) {
            i -> w2;
        }
    }
    worker w2 {
        print(<- w1);
        string msg = "default";
        if (true) {
            msg = <- w1;
        }
    }
}

function workerActionThirdTest() {
    worker w1 {
        int i = 5;
        var x1 = i ->> w2;
        var x2 = i ->> w2;
        var result = flush w2;
    }
    worker w2 returns error?{
        if(false){
             error err = error("err", message = "err msg");
             return err;
        }
        int j =0 ;
        j = <- w1;
        j = <- w1;
        return;
    }
}

function invalidReceiveUsage() {
    fork {
        worker w1 {
            int a = 5;
            a -> w2;
        }
        worker w2 {
            var a = <- w1;
        }
    }
}

function print(string str) {
    string result = str.toUpperAscii();
}

function getId() returns int {
    return 10;
}

function getName() returns string {
    return "Natasha";
}

function getStatus() returns boolean {
    return true;
}

function getStdId() returns future<int> {
    future <int> id = start getId();
    return id;
}
