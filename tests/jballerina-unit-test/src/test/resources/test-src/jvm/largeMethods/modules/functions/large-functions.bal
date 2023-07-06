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

import ballerina/test;

type Rec record {|
    string a = "*";
    string b;
    string x0 = "x0";
    string x1 = "x1";
    string x2 = "x2";
    string x3 = "x3";
    string x4 = "x4";
    string x5 = "x5";
    string x6 = "x6";
    string x7 = "x7";
    string x8 = "x8";
    string x9 = "x9";
    string x10 = "x10";
    string x11 = "x11";
    string x12 = "x12";
    string x13 = "x13";
    string x14 = "x14";
    string x15 = "x15";
    string x16 = "x16";
    string x17 = "x17";
    string x18 = "x18";
    string x19 = "x19";
    string x20 = "x20";
    string x21 = "x21";
    string x22 = "x22";
    string x23 = "x23";
    string x24 = "x24";
    string x25 = "x25";
    string x26 = "x26";
    string x27 = "x27";
    string x28 = "x28";
    string x29 = "x29";
    string x30 = "x30";
    string x31 = "x31";
    string x32 = "x32";
    string x33 = "x33";
    string x34 = "x34";
    string x35 = "x35";
    string x36 = "x36";
    string x37 = "x37";
    string x38 = "x38";
    string x39 = "x39";
    string x40 = "x40";
    string x41 = "x41";
    string x42 = "x42";
    string x43 = "x43";
    string x44 = "x44";
    string x45 = "x45";
    string x46 = "x46";
    string x47 = "x47";
    string x48 = "x48";
    string x49 = "x49";
    string x50 = "x50";
    string x51 = "x51";
    string x52 = "x52";
    string x53 = "x53";
    string x54 = "x54";
    string x55 = "x55";
    string x56 = "x56";
    string x57 = "x57";
    string x58 = "x58";
    string x59 = "x59";
    string x60 = "x60";
    string x61 = "x61";
    string x62 = "x62";
    string x63 = "x63";
    string x64 = "x64";
    string x65 = "x65";
    string x66 = "x66";
    string x67 = "x67";
    string x68 = "x68";
    string x69 = "x69";
    string x70 = "x70";
    string x71 = "x71";
    string x72 = "x72";
    string x73 = "x73";
    string x74 = "x74";
    string x75 = "x75";
    string x76 = "x76";
    string x77 = "x77";
    string x78 = "x78";
    string x79 = "x79";
    string x80 = "x80";
    string x81 = "x81";
    string x82 = "x82";
    string x83 = "x83";
    string x84 = "x84";
    string x85 = "x85";
    string x86 = "x86";
    string x87 = "x87";
    string x88 = "x88";
    string x89 = "x89";
    string x90 = "x90";
    string x91 = "x91";
    string x92 = "x92";
    string x93 = "x93";
    string x94 = "x94";
    string x95 = "x95";
    string x96 = "x96";
    string x97 = "x97";
    string x98 = "x98";
    string x99 = "x99";
    string x100 = "x100";
    string x101 = "x101";
    string x102 = "x102";
    string x103 = "x103";
    string x104 = "x104";
    string x105 = "x105";
    string x106 = "x106";
    string x107 = "x107";
    string x108 = "x108";
    string x109 = "x109";
    string x110 = "x110";
    string x111 = "x111";
    string x112 = "x112";
    string x113 = "x113";
    string x114 = "x114";
    string x115 = "x115";
    string x116 = "x116";
    string x117 = "x117";
    string x118 = "x118";
    string x119 = "x119";
    string x120 = "x120";
    string x121 = "x121";
    string x122 = "x122";
    string x123 = "x123";
    string x124 = "x124";
    string x125 = "x125";
    string x126 = "x126";
    string x127 = "x127";
    string x128 = "x128";
    string x129 = "x129";
    string x130 = "x130";
    string x131 = "x131";
    string x132 = "x132";
    string x133 = "x133";
    string x134 = "x134";
    string x135 = "x135";
    string x136 = "x136";
    string x137 = "x137";
    string x138 = "x138";
    string x139 = "x139";
|};

public function testInvalidRegExp() {
    Rec rec = {b: "+"};
    string:RegExp|error x1 = trap re `abc(?i:${rec.b})(${rec.x0})(${rec.x1})(${rec.x2})(${rec.x3})
    (${rec.x4})(${rec.x5})(${rec.x6})(${rec.x7})(${rec.x8})(${rec.x9})(${rec.x10})(${rec.x11})
    (${rec.x12})(${rec.x13})(${rec.x14})(${rec.x15})(${rec.x16})(${rec.x17})(${rec.x18})
    (${rec.x19})(${rec.x20})(${rec.x21})(${rec.x22})(${rec.x23})(${rec.x24})(${rec.x25})
    (${rec.x26})(${rec.x27})(${rec.x28})(${rec.x29})(${rec.x30})(${rec.x31})(${rec.x32})
    (${rec.x33})(${rec.x34})(${rec.x35})(${rec.x36})(${rec.x37})(${rec.x38})(${rec.x39})
    (${rec.x40})(${rec.x41})(${rec.x42})(${rec.x43})(${rec.x44})(${rec.x45})(${rec.x46})
    (${rec.x47})(${rec.x48})(${rec.x49})(${rec.x50})(${rec.x51})(${rec.x52})(${rec.x53})
    (${rec.x54})(${rec.x55})(${rec.x56})(${rec.x57})(${rec.x58})(${rec.x59})(${rec.x60})
    (${rec.x61})(${rec.x62})(${rec.x63})(${rec.x64})(${rec.x65})(${rec.x66})(${rec.x67})
    (${rec.x68})(${rec.x69})(${rec.x70})(${rec.x71})(${rec.x72})(${rec.x73})(${rec.x74})
    (${rec.x75})(${rec.x76})(${rec.x77})(${rec.x78})(${rec.x79})(${rec.x80})(${rec.x81})
    (${rec.x82})(${rec.x83})(${rec.x84})(${rec.x85})(${rec.x86})(${rec.x87})(${rec.x88})
    (${rec.x89})(${rec.x90})(${rec.x91})(${rec.x92})(${rec.x93})(${rec.x94})(${rec.x95})
    (${rec.x96})(${rec.x97})(${rec.x98})(${rec.x99})(${rec.x100})(${rec.x101})(${rec.x102})
    (${rec.x103})(${rec.x104})(${rec.x105})(${rec.x106})(${rec.x107})(${rec.x108})(${rec.x109})
    (${rec.x110})(${rec.x111})(${rec.x112})(${rec.x113})(${rec.x114})(${rec.x115})(${rec.x116})
    (${rec.x117})(${rec.x118})(${rec.x119})(${rec.x120})(${rec.x121})(${rec.x122})(${rec.x123})
    (${rec.x124})(${rec.x125})(${rec.x126})(${rec.x127})(${rec.x128})(${rec.x129})(${rec.x130})
    (${rec.x131})(${rec.x132})(${rec.x133})(${rec.x134})(${rec.x135})(${rec.x136})(${rec.x137})
    (${rec.x138})(${rec.x139})`;
    test:assertTrue(x1 is error);
    if (x1 is error) {
        test:assertEquals("{ballerina}RegularExpressionParsingError", x1.message());
        test:assertEquals("missing backslash before '+' token in insertion substring '+'",
        <string> checkpanic x1.detail()["message"]);
    }

    string:RegExp[]|error x2 = trap [re `abc(?i:${rec.a})(${rec.x0})`, re `abc(?i:${rec.b})(${rec.x1})`,
    re `abc(?i:${rec.b})(${rec.x2})`, re `abc(?i:${rec.b})(${rec.x3})`, re `abc(?i:${rec.b})(${rec.x4})`,
    re `abc(?i:${rec.b})(${rec.x5})`, re `abc(?i:${rec.b})(${rec.x6})`, re `abc(?i:${rec.b})(${rec.x7})`,
    re `abc(?i:${rec.b})(${rec.x8})`, re `abc(?i:${rec.b})(${rec.x9})`, re `abc(?i:${rec.b})(${rec.x10})`,
    re `abc(?i:${rec.b})(${rec.x11})`, re `abc(?i:${rec.b})(${rec.x12})`, re `abc(?i:${rec.b})(${rec.x13})`,
    re `abc(?i:${rec.b})(${rec.x14})`, re `abc(?i:${rec.b})(${rec.x15})`, re `abc(?i:${rec.b})(${rec.x16})`,
    re `abc(?i:${rec.b})(${rec.x17})`, re `abc(?i:${rec.b})(${rec.x18})`, re `abc(?i:${rec.b})(${rec.x19})`,
    re `abc(?i:${rec.b})(${rec.x20})`, re `abc(?i:${rec.b})(${rec.x21})`, re `abc(?i:${rec.b})(${rec.x22})`,
    re `abc(?i:${rec.b})(${rec.x23})`, re `abc(?i:${rec.b})(${rec.x24})`, re `abc(?i:${rec.b})(${rec.x25})`,
    re `abc(?i:${rec.b})(${rec.x26})`, re `abc(?i:${rec.b})(${rec.x27})`, re `abc(?i:${rec.b})(${rec.x28})`,
    re `abc(?i:${rec.b})(${rec.x29})`, re `abc(?i:${rec.b})(${rec.x30})`, re `abc(?i:${rec.b})(${rec.x31})`,
    re `abc(?i:${rec.b})(${rec.x32})`, re `abc(?i:${rec.b})(${rec.x33})`, re `abc(?i:${rec.b})(${rec.x33})`];
    test:assertTrue(x2 is error);
    if (x2 is error) {
        test:assertEquals("{ballerina}RegularExpressionParsingError", x2.message());
        test:assertEquals("missing backslash before '*' token in insertion substring '*'",
        <string> checkpanic x2.detail()["message"]);
    }
}
