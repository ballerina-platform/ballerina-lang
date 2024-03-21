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

public function testSendAllowedLocations(boolean b) returns error? {
    worker w1 {
        foreach var i in [1, 2, 3] {
            i -> function; // error: position not allowed
        }

        while b {
            119 -> function; // error: position not allowed
        }

        lock {
            120 -> function; // error: position not allowed
        }

        1 -> function; // OK
        if b {
           2 -> function; // OK
            if b {
                3 -> function; // OK
            }
        }
        () _ = 4 -> function; // OK
        () _ = (4 -> function); // OK
        do {
            5 -> function; // OK
        }
        {
            5 -> function; // OK
        }
    } on fail {
        6 -> function; // OK
    }

    _ = <- w1;
    _ = check <- w1;
    _ = check <- w1;
    _ = <- w1;
    _ = <- w1;
    _ = <- w1;
    _ = <- w1;
    _ = <- w1;
    _ = <- w1;
    _ = <- w1;
    _ = <- w1;
}

public function testReceiveAllowedLocations(boolean b) returns error? {
    worker w1 {
        foreach int _ in [1, 2, 3] {
            _ = <- function; // error: position not allowed
        }

        while b {
            _ = <- function; // error: position not allowed
        }

        lock {
            _ = <- function; // error: position not allowed
        }

        _ = <- function; // OK
        if b {
          _ = <- function; // error: position not allowed
            if b {
                _ = <- function; // error: position not allowed
            }
        }
        _ = <- function; // OK
        _ = (<- function); // OK
        do {
            _ = <- function; // OK
        }
        {
            _ = <- function; // OK
        }
    } on fail {
        _ = <- function; // OK
    }

    1 -> w1;
    1 -> w1;
    1 -> w1;
    1 -> w1;
    1 -> w1;
    1 -> w1;
    1 -> w1;
    1 -> w1;
    1 -> w1;
    1 -> w1;
    1 -> w1;
}
