// Copyright (c) 2024 WSO2 LLC. (http://www.wso2.com).
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

public type ErrorA distinct error;

public type ErrorB distinct error;

public function testSendFailureType1(boolean b, boolean c) {
    worker w1 returns boolean|ErrorA|ErrorB {
        int i = 2;

        i -> w2;
        if (0 > 1) {
            return true;
        }
        i = i + 1;
        i -> w2;

        if b {
            return error ErrorA("error A");
        }
        44 -> w2;

        if c {
            return error ErrorB("error B");
        }
        "xxx" -> w2;
        "yyy" -> w2;
        "zzz" -> w2;

        return true;
    }

    worker w2 {
      int _ = <- w1; // OK
      int _ = <- w1; // error: found 'int|error:NoMessage'
      int _ = <- w1; // error: found 'int|ErrorA|error:NoMessage'
      int _ = <- w1; // error: found 'string|ErrorA|ErrorB|error:NoMessage'
      string|ErrorA|ErrorB|error:NoMessage m = <- w1; // OK
      string|error n = <- w1; // OK

      println(m);
      println(n);
    }
}

public function testSendFailureType2(boolean b) {
    worker w1 returns boolean|ErrorA|ErrorB {
        ErrorA|ErrorB e = error ErrorA("error A");
        if b {
            return e;
        }
        11 -> w2;
        return true;
    }

    worker w2 {
      int _ = <- w1; // error: found '(ErrorA|ErrorB|int)'
    }
}

public function testSendFailureType3(boolean b) {
    worker w1 returns boolean|ErrorA|ErrorB {
        ErrorA|ErrorB|boolean e = error ErrorA("error A");
        if b {
            return e;
        }
        22 -> w2;
        return true;
    }

    worker w2 {
      int _ = <- w1; // error: found '(ErrorA|ErrorB|int|error:NoMessage)
    }
}

public function testReceiveFailureType(boolean b, boolean c) {
    worker w1 returns boolean|ErrorA|ErrorB {
        int i = 2;

        _ =  <- w2;
        if (0 > 1) {
            return true;
        }
        i = i + 1;
        _ =  <- w2;

        if b {
            return error ErrorA("error A");
        }
        _ =  <- w2;

        if c {
            return error ErrorB("error B");
        }
        _ =  <- w2;
        _ =  <- w2;
        _ =  <- w2;

        return true;
    }

    worker w2 {
      () _ = 1 ->> w1; // OK
      () _ = 2 ->> w1; // OK
      () p = 3 ->> w1; // error: found 'ErrorA?'
      () q = 4 ->> w1; // error: found '(ErrorA|ErrorB)?'
      ErrorA|ErrorB? r = 5 ->> w1; // OK
      error? s = 6 ->> w1; // OK

      println(p);
      println(q);
      println(r);
      println(s);
    }
}

public function println(any|error value)  {
    return;
}
