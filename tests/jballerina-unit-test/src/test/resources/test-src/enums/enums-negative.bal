// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

enum Colour {
    RED,
}

enum Colours {
    BLUE = "Blue",
}

public enum Shape {
    CIRCLE,
    SQ = "SQUARE"
}

public enum Artist {
    EMINEM,
    LANA,
    ED = "Ed Shereen"
}

public enum Food {
    BIRIYANI = 1,
    KOTTU = 2.0
}

public enum LiftStatus {
    OPEN,
    OPEN,
    CLOSED = "0",
    HOLD = "HO" + "D"
}

public enum TrailStatus {
    OPEN,
    CLOSED,
    HOLD = "h" + "d"
}

function testBasicEnumSupportNegative() {
    int circle = CIRCLE;
    float em = EMINEM;
}

function testEnumAsType() {
    Shape c = EMINEM;
    Artist e = "Adele";
    ED p = "EMINEM";
}

const string A = x;
const B = 1;

enum E1 {
    A = "1",
    B = "1"
}
