// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function testMissingDigitAfterExponentIndicator() {
    _ = 32.99E-;
    _ = 32.99E+;
    _ = 22.99E;
    _ = 22.99e-;
    _ = 22.99e+;
    _ = 22.99e;
    _ = 22.99E-f;
    _ = 22.99E+f;
    _ = 22.99Ef;
    _ = 22.99e-f;
    _ = 22.99e+f;
    _ = 22.99ef;
    _ = 22.99E-d;
    _ = 22.99E+d;
    _ = 22.99Ed;
    _ = 22.99e-d;
    _ = 22.99e+d;
    _ = 22.99ed;
    _ = 99E-;
    _ = 99E+;
    _ = 99E;
    _ = 99e-;
    _ = 99e+;
    _ = 99e;
    _ = 99E-f;
    _ = 99E+f;
    _ = 99Ef;
    _ = 99e-f;
    _ = 99e+f;
    _ = 99ef;
    _ = 99E-d;
    _ = 99E+d;
    _ = 99Ed;
    _ = 99e-d;
    _ = 99e+d;
    _ = 99ed;
    _ = .99E-;
    _ = .99E+;
    _ = .99E;
    _ = .99e-;
    _ = .99e+;
    _ = .99e;
    _ = .99E-f;
    _ = .99E+f;
    _ = .99Ef;
    _ = .99e-f;
    _ = .99e+f;
    _ = .99ef;
    _ = .99E-d;
    _ = .99E+d;
    _ = .99Ed;
    _ = .99e-d;
    _ = .99e+d;
    _ = .99ed;
    _ = 0x2p;
    _ = 0x2p+;
    _ = 0x3p-;
    _ = 0xcp;
    _ = 0xcp+;
    _ = 0xcp-;
    _ = 0xc.4bp;
    _ = 0xc.4bp+;
    _ = 0xc.4bp-;
    _ = 0X2p;
    _ = 0X2p+;
    _ = 0X3p-;
    _ = 0Xcp;
    _ = 0Xcp+;
    _ = 0Xcp-;
    _ = 0Xc.4bp;
    _ = 0Xc.4bp+;
    _ = 0Xc.4bp-;
    _ = 0xp6;
    _ = 0XP6;
    _ = 0xp-6;
    _ = 0XP-6;
    
    10f _ = 100Ef;
    0XA _ = 0XAAP+;
    0XA _ = 0XP+0;
}
