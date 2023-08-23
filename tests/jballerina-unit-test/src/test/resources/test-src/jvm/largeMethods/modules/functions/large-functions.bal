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
    string x140 = "x140";
    string x141 = "x141";
    string x142 = "x142";
    string x143 = "x143";
    string x144 = "x144";
    string x145 = "x145";
    string x146 = "x146";
    string x147 = "x147";
    string x148 = "x148";
    string x149 = "x149";
    string x150 = "x150";
    string x151 = "x151";
    string x152 = "x152";
    string x153 = "x153";
    string x154 = "x154";
    string x155 = "x155";
    string x156 = "x156";
    string x157 = "x157";
    string x158 = "x158";
    string x159 = "x159";
    string x160 = "x160";
    string x161 = "x161";
    string x162 = "x162";
    string x163 = "x163";
    string x164 = "x164";
    string x165 = "x165";
    string x166 = "x166";
    string x167 = "x167";
    string x168 = "x168";
    string x169 = "x169";
    string x170 = "x170";
    string x171 = "x171";
    string x172 = "x172";
    string x173 = "x173";
    string x174 = "x174";
    string x175 = "x175";
    string x176 = "x176";
    string x177 = "x177";
    string x178 = "x178";
    string x179 = "x179";
    string x180 = "x180";
    string x181 = "x181";
    string x182 = "x182";
    string x183 = "x183";
    string x184 = "x184";
    string x185 = "x185";
    string x186 = "x186";
    string x187 = "x187";
    string x188 = "x188";
    string x189 = "x189";
    string x190 = "x190";
    string x191 = "x191";
    string x192 = "x192";
    string x193 = "x193";
    string x194 = "x194";
    string x195 = "x195";
    string x196 = "x196";
    string x197 = "x197";
    string x198 = "x198";
    string x199 = "x199";
    string x200 = "x200";
    string x201 = "x201";
    string x202 = "x202";
    string x203 = "x203";
    string x204 = "x204";
    string x205 = "x205";
    string x206 = "x206";
    string x207 = "x207";
    string x208 = "x208";
    string x209 = "x209";
    string x210 = "x210";
    string x211 = "x211";
    string x212 = "x212";
    string x213 = "x213";
    string x214 = "x214";
    string x215 = "x215";
    string x216 = "x216";
    string x217 = "x217";
    string x218 = "x218";
    string x219 = "x219";
    string x220 = "x220";
    string x221 = "x221";
    string x222 = "x222";
    string x223 = "x223";
    string x224 = "x224";
    string x225 = "x225";
    string x226 = "x226";
    string x227 = "x227";
    string x228 = "x228";
    string x229 = "x229";
    string x230 = "x230";
    string x231 = "x231";
    string x232 = "x232";
    string x233 = "x233";
    string x234 = "x234";
    string x235 = "x235";
    string x236 = "x236";
    string x237 = "x237";
    string x238 = "x238";
    string x239 = "x239";
    string x240 = "x240";
    string x241 = "x241";
    string x242 = "x242";
    string x243 = "x243";
    string x244 = "x244";
    string x245 = "x245";
    string x246 = "x246";
    string x247 = "x247";
    string x248 = "x248";
    string x249 = "x249";
    string x250 = "x250";
    string x251 = "x251";
    string x252 = "x252";
    string x253 = "x253";
    string x254 = "x254";
    string x255 = "x255";
    string x256 = "x256";
    string x257 = "x257";
    string x258 = "x258";
    string x259 = "x259";
    string x260 = "x260";
    string x261 = "x261";
    string x262 = "x262";
    string x263 = "x263";
    string x264 = "x264";
    string x265 = "x265";
    string x266 = "x266";
    string x267 = "x267";
    string x268 = "x268";
    string x269 = "x269";
    string x270 = "x270";
    string x271 = "x271";
    string x272 = "x272";
    string x273 = "x273";
    string x274 = "x274";
    string x275 = "x275";
    string x276 = "x276";
    string x277 = "x277";
    string x278 = "x278";
    string x279 = "x279";
    string x280 = "x280";
    string x281 = "x281";
    string x282 = "x282";
    string x283 = "x283";
    string x284 = "x284";
    string x285 = "x285";
    string x286 = "x286";
    string x287 = "x287";
    string x288 = "x288";
    string x289 = "x289";
    string x290 = "x290";
    string x291 = "x291";
    string x292 = "x292";
    string x293 = "x293";
    string x294 = "x294";
    string x295 = "x295";
    string x296 = "x296";
    string x297 = "x297";
    string x298 = "x298";
    string x299 = "x299";
    string x300 = "x300";
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
    (${rec.x138})(${rec.x139})(${rec.x140})(${rec.x141})(${rec.x142})(${rec.x143})(${rec.x144})
    (${rec.x145})(${rec.x146})(${rec.x147})(${rec.x148})(${rec.x149})(${rec.x150})(${rec.x151})
    (${rec.x152})(${rec.x153})(${rec.x154})(${rec.x155})(${rec.x156})(${rec.x157})(${rec.x158})
    (${rec.x159})(${rec.x160})(${rec.x161})(${rec.x162})(${rec.x163})(${rec.x164})(${rec.x165})
    (${rec.x166})(${rec.x167})(${rec.x168})(${rec.x169})(${rec.x170})(${rec.x171})(${rec.x172})
    (${rec.x173})(${rec.x174})(${rec.x175})(${rec.x176})(${rec.x177})(${rec.x178})(${rec.x179})
    (${rec.x180})(${rec.x181})(${rec.x182})(${rec.x183})(${rec.x184})(${rec.x185})(${rec.x186})
    (${rec.x187})(${rec.x188})(${rec.x189})(${rec.x190})(${rec.x191})(${rec.x192})(${rec.x193})
    (${rec.x194})(${rec.x195})(${rec.x196})(${rec.x197})(${rec.x198})(${rec.x199})(${rec.x200})
    (${rec.x201})(${rec.x202})(${rec.x203})(${rec.x204})(${rec.x205})(${rec.x206})(${rec.x207})
    (${rec.x208})(${rec.x209})(${rec.x210})(${rec.x211})(${rec.x212})(${rec.x213})(${rec.x214})
    (${rec.x215})(${rec.x216})(${rec.x217})(${rec.x218})(${rec.x219})(${rec.x220})(${rec.x221})
    (${rec.x222})(${rec.x223})(${rec.x224})(${rec.x225})(${rec.x226})(${rec.x227})(${rec.x228})
    (${rec.x229})(${rec.x230})(${rec.x231})(${rec.x232})(${rec.x233})(${rec.x234})(${rec.x235})
    (${rec.x236})(${rec.x237})(${rec.x238})(${rec.x239})(${rec.x240})(${rec.x241})(${rec.x242})
    (${rec.x243})(${rec.x244})(${rec.x245})(${rec.x246})(${rec.x247})(${rec.x248})(${rec.x249})
    (${rec.x250})(${rec.x251})(${rec.x252})(${rec.x253})(${rec.x254})(${rec.x255})(${rec.x256})
    (${rec.x257})(${rec.x258})(${rec.x259})(${rec.x260})(${rec.x261})(${rec.x262})(${rec.x263})
    (${rec.x264})(${rec.x265})(${rec.x266})(${rec.x267})(${rec.x268})(${rec.x269})(${rec.x270})
    (${rec.x271})(${rec.x272})(${rec.x273})(${rec.x274})(${rec.x275})(${rec.x276})(${rec.x277})
    (${rec.x278})(${rec.x279})(${rec.x280})(${rec.x281})(${rec.x282})(${rec.x283})(${rec.x284})
    (${rec.x285})(${rec.x286})(${rec.x287})(${rec.x288})(${rec.x289})(${rec.x290})(${rec.x291})
    (${rec.x292})(${rec.x293})(${rec.x294})(${rec.x295})(${rec.x296})(${rec.x297})(${rec.x298})
    (${rec.x299})(${rec.x300})`;
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
    re `abc(?i:${rec.b})(${rec.x32})`, re `abc(?i:${rec.b})(${rec.x33})`, re `abc(?i:${rec.b})(${rec.x34})`,
    re `abc(?i:${rec.b})(${rec.x35})`, re `abc(?i:${rec.b})(${rec.x36})`, re `abc(?i:${rec.b})(${rec.x37})`,
    re `abc(?i:${rec.b})(${rec.x38})`, re `abc(?i:${rec.b})(${rec.x39})`, re `abc(?i:${rec.b})(${rec.x40})`,
    re `abc(?i:${rec.b})(${rec.x41})`, re `abc(?i:${rec.b})(${rec.x42})`, re `abc(?i:${rec.b})(${rec.x43})`,
    re `abc(?i:${rec.b})(${rec.x44})`, re `abc(?i:${rec.b})(${rec.x45})`, re `abc(?i:${rec.b})(${rec.x46})`,
    re `abc(?i:${rec.b})(${rec.x47})`, re `abc(?i:${rec.b})(${rec.x48})`, re `abc(?i:${rec.b})(${rec.x49})`,
    re `abc(?i:${rec.b})(${rec.x50})`, re `abc(?i:${rec.b})(${rec.x51})`, re `abc(?i:${rec.b})(${rec.x52})`,
    re `abc(?i:${rec.b})(${rec.x53})`, re `abc(?i:${rec.b})(${rec.x54})`, re `abc(?i:${rec.b})(${rec.x55})`,
    re `abc(?i:${rec.b})(${rec.x56})`, re `abc(?i:${rec.b})(${rec.x57})`, re `abc(?i:${rec.b})(${rec.x58})`,
    re `abc(?i:${rec.b})(${rec.x59})`, re `abc(?i:${rec.b})(${rec.x60})`, re `abc(?i:${rec.b})(${rec.x61})`,
    re `abc(?i:${rec.b})(${rec.x62})`, re `abc(?i:${rec.b})(${rec.x63})`, re `abc(?i:${rec.b})(${rec.x64})`,
    re `abc(?i:${rec.b})(${rec.x65})`, re `abc(?i:${rec.b})(${rec.x66})`, re `abc(?i:${rec.b})(${rec.x67})`,
    re `abc(?i:${rec.b})(${rec.x68})`, re `abc(?i:${rec.b})(${rec.x69})`, re `abc(?i:${rec.b})(${rec.x70})`,
    re `abc(?i:${rec.b})(${rec.x71})`, re `abc(?i:${rec.b})(${rec.x72})`, re `abc(?i:${rec.b})(${rec.x73})`,
    re `abc(?i:${rec.b})(${rec.x74})`, re `abc(?i:${rec.b})(${rec.x75})`, re `abc(?i:${rec.b})(${rec.x76})`,
    re `abc(?i:${rec.b})(${rec.x77})`, re `abc(?i:${rec.b})(${rec.x78})`, re `abc(?i:${rec.b})(${rec.x79})`,
    re `abc(?i:${rec.b})(${rec.x80})`, re `abc(?i:${rec.b})(${rec.x81})`, re `abc(?i:${rec.b})(${rec.x82})`,
    re `abc(?i:${rec.b})(${rec.x83})`, re `abc(?i:${rec.b})(${rec.x84})`, re `abc(?i:${rec.b})(${rec.x85})`,
    re `abc(?i:${rec.b})(${rec.x86})`, re `abc(?i:${rec.b})(${rec.x87})`, re `abc(?i:${rec.b})(${rec.x88})`,
    re `abc(?i:${rec.b})(${rec.x89})`, re `abc(?i:${rec.b})(${rec.x90})`, re `abc(?i:${rec.b})(${rec.x91})`,
    re `abc(?i:${rec.b})(${rec.x92})`, re `abc(?i:${rec.b})(${rec.x93})`, re `abc(?i:${rec.b})(${rec.x94})`,
    re `abc(?i:${rec.b})(${rec.x95})`, re `abc(?i:${rec.b})(${rec.x96})`, re `abc(?i:${rec.b})(${rec.x97})`,
    re `abc(?i:${rec.b})(${rec.x98})`, re `abc(?i:${rec.b})(${rec.x99})`, re `abc(?i:${rec.b})(${rec.x100})`];
    test:assertTrue(x2 is error);
    if (x2 is error) {
        test:assertEquals("{ballerina}RegularExpressionParsingError", x2.message());
        test:assertEquals("missing backslash before '*' token in insertion substring '*'",
        <string> checkpanic x2.detail()["message"]);
    }
}
