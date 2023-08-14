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

type AnnotRecordOne record {|
    string value;
|};

annotation AnnotRecordOne annotOne on type, field;

public string gVar = "chiranS";

public [@annotOne {value: gVar} int, @annotOne {value: "k"} int, string...] g1 = [1, 2, "hello", "world",
"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
"21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38",
"39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56",
"57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74",
"75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92",
"93", "94", "95", "96", "97", "98", "99", "100", "101", "102", "103", "104", "105", "106", "107", "108", "109",
"110", "111", "112", "113", "114", "115", "116", "117", "118", "119", "120", "121", "122", "123", "124", "125",
"126", "127", "128", "129", "130", "131", "132", "133", "134", "135", "136", "137", "138", "139", "140", "141",
"142", "143", "144", "145", "146", "147", "148", "149", "150", "151", "152", "153", "154", "155", "156", "157",
 "158", "159", "160", "161", "162", "163", "164", "165", "166", "167", "168", "169", "170", "171", "172", "173",
 "174", "175", "176", "177", "178", "179", "180", "181", "182", "183", "184", "185", "186", "187", "188", "189",
 "190", "191", "192", "193", "194", "195", "196", "197", "198", "199", "200", "201", "202", "203", "204", "205",
 "206", "207", "208", "209", "210", "211", "212", "213", "214", "215", "216", "217", "218", "219", "220", "221",
 "222", "223", "224", "225", "226", "227", "228", "229", "230", "231", "232", "233", "234", "235", "236", "237",
 "238", "239", "240", "241", "242", "243", "244", "245", "246", "247", "248", "249", "250", "251", "252", "253",
 "254", "255", "256", "257", "258", "259", "260", "261", "262", "263", "264", "265", "266", "267", "268", "269",
 "270", "271", "272", "273", "274", "275", "276", "277", "278", "279", "280", "281", "282", "283", "284", "285",
 "286", "287", "288", "289", "290", "291", "292", "293", "294", "295", "296", "297", "298", "299", "300", "301",
 "302", "303", "304", "305", "306", "307", "308", "309", "310", "311", "312", "313", "314", "315", "316", "317",
 "318", "319", "320", "321", "322", "323", "324", "325", "326", "327", "328", "329", "330", "331", "332", "333",
 "334", "335", "336", "337", "338", "339", "340", "341", "342", "343", "344", "345", "346", "347", "348", "349",
 "350", "351", "352", "353", "354", "355", "356", "357", "358", "359", "360", "361", "362", "363", "364", "365",
 "366", "367", "368", "369", "370", "371", "372", "373", "374", "375", "376", "377", "378", "379", "380", "381",
 "382", "383", "384", "385", "386", "387", "388", "389", "390", "391", "392", "393", "394", "395", "396", "397",
 "398", "399", "400", "401", "402", "403", "404", "405", "406", "407", "408", "409", "410", "411", "412", "413",
 "414", "415", "416", "417", "418", "419", "420", "421", "422", "423", "424", "425", "426", "427", "428", "429",
 "430", "431", "432", "433", "434", "435", "436", "437", "438", "439", "440", "441", "442", "443", "444", "445",
 "446", "447", "448", "449", "450", "451", "452", "453", "454", "455", "456", "457", "458", "459", "460", "461",
 "462", "463", "464", "465", "466", "467", "468", "469", "470", "471", "472", "473", "474", "475", "476", "477",
 "478", "479", "480", "481", "482", "483", "484", "485", "486", "487", "488", "489", "490", "491", "492", "493",
 "494", "495", "496", "497", "498", "499", "500", "501", "502"];

public function testAnnotationsOnLocalRecordFields() {
    record {
     @annotOne {value: "10"}
     string x;
     string y;
     string x0;
     string x1;
     string x2;
     string x3;
     string x4;
     string x5;
     string x6;
     string x7;
     string x8;
     string x9;
     string x10;
     string x11;
     string x12;
     string x13;
     string x14;
     string x15;
     string x16;
     string x17;
     string x18;
     string x19;
     string x20;
     string x21;
     string x22;
     string x23;
     string x24;
     string x25;
     string x26;
     string x27;
     string x28;
     string x29;
     string x30;
     string x31;
     string x32;
     string x33;
     string x34;
     string x35;
     string x36;
     string x37;
     string x38;
     string x39;
     string x40;
     string x41;
     string x42;
     string x43;
     string x44;
     string x45;
     string x46;
     string x47;
     string x48;
     string x49;
     string x50;
     string x51;
     string x52;
     string x53;
     string x54;
     string x55;
     string x56;
     string x57;
     string x58;
     string x59;
     string x60;
     string x61;
     string x62;
     string x63;
     string x64;
     string x65;
     string x66;
     string x67;
     string x68;
     string x69;
     string x70;
     string x71;
     string x72;
     string x73;
     string x74;
     string x75;
     string x76;
     string x77;
     string x78;
     string x79;
     string x80;
     string x81;
     string x82;
     string x83;
     string x84;
     string x85;
     string x86;
     string x87;
     string x88;
     string x89;
     string x90;
     string x91;
     string x92;
     string x93;
     string x94;
     string x95;
     string x96;
     string x97;
     string x98;
     string x99;
     string x100;
     string x101;
     string x102;
     string x103;
     string x104;
     string x105;
     string x106;
     string x107;
     string x108;
     string x109;
     string x110;
     string x111;
     string x112;
     string x113;
     string x114;
     string x115;
     string x116;
     string x117;
     string x118;
     string x119;
     string x120;
     string x121;
     string x122;
     string x123;
     string x124;
     string x125;
     string x126;
     string x127;
     string x128;
     string x129;
     string x130;
     string x131;
     string x132;
     string x133;
     string x134;
     string x135;
     string x136;
     string x137;
     string x138;
     string x139;
     string x140;
     string x141;
     string x142;
     string x143;
     string x144;
     string x145;
     string x146;
     string x147;
     string x148;
     string x149;
     string x150;
     string x151;
     string x152;
     string x153;
     string x154;
     string x155;
     string x156;
     string x157;
     string x158;
     string x159;
     string x160;
     string x161;
     string x162;
     string x163;
     string x164;
     string x165;
     string x166;
     string x167;
     string x168;
     string x169;
     string x170;
     string x171;
     string x172;
     string x173;
     string x174;
     string x175;
     string x176;
     string x177;
     string x178;
     string x179;
     string x180;
     string x181;
     string x182;
     string x183;
     string x184;
     string x185;
     string x186;
     string x187;
     string x188;
     string x189;
     string x190;
     string x191;
     string x192;
     string x193;
     string x194;
     string x195;
     string x196;
     string x197;
     string x198;
     string x199;
     string x200;
     string x201;
     string x202;
     string x203;
     string x204;
     string x205;
     string x206;
     string x207;
     string x208;
     string x209;
     string x210;
     string x211;
     string x212;
     string x213;
     string x214;
     string x215;
     string x216;
     string x217;
     string x218;
     string x219;
     string x220;
     string x221;
     string x222;
     string x223;
     string x224;
     string x225;
     string x226;
     string x227;
     string x228;
     string x229;
     string x230;
     string x231;
     string x232;
     string x233;
     string x234;
     string x235;
     string x236;
     string x237;
     string x238;
     string x239;
     string x240;
     string x241;
     string x242;
     string x243;
     string x244;
     string x245;
     string x246;
     string x247;
     string x248;
     string x249;
     string x250;
     string x251;
     string x252;
     string x253;
     string x254;
     string x255;
     string x256;
     string x257;
     string x258;
     string x259;
     string x260;
     string x261;
     string x262;
     string x263;
     string x264;
     string x265;
     string x266;
     string x267;
     string x268;
     string x269;
     string x270;
     string x271;
     string x272;
     string x273;
     string x274;
     string x275;
     string x276;
     string x277;
     string x278;
     string x279;
     string x280;
     string x281;
     string x282;
     string x283;
     string x284;
     string x285;
     string x286;
     string x287;
     string x288;
     string x289;
     string x290;
     string x291;
     string x292;
     string x293;
     string x294;
     string x295;
     string x296;
     string x297;
     string x298;
     string x299;
     string x300;
     string x301;
     string x302;
     string x303;
     string x304;
     string x305;
     string x306;
     string x307;
     string x308;
     string x309;
     string x310;
     string x311;
     string x312;
     string x313;
     string x314;
     string x315;
     string x316;
     string x317;
     string x318;
     string x319;
     string x320;
     string x321;
     string x322;
     string x323;
     string x324;
     string x325;
     string x326;
     string x327;
     string x328;
     string x329;
     string x330;
     string x331;
     string x332;
     string x333;
     string x334;
     string x335;
     string x336;
     string x337;
     string x338;
     string x339;
     string x340;
     string x341;
     string x342;
     string x343;
     string x344;
     string x345;
     string x346;
     string x347;
     string x348;
     string x349;
     string x350;
     string x351;
     string x352;
     string x353;
     string x354;
     string x355;
     string x356;
     string x357;
     string x358;
     string x359;
     string x360;
     string x361;
     string x362;
     string x363;
     string x364;
     string x365;
     string x366;
     string x367;
     string x368;
     string x369;
     string x370;
     string x371;
     string x372;
     string x373;
     string x374;
     string x375;
     string x376;
     string x377;
     string x378;
     string x379;
     string x380;
     string x381;
     string x382;
     string x383;
     string x384;
     string x385;
     string x386;
     string x387;
     string x388;
     string x389;
     string x390;
     string x391;
     string x392;
     string x393;
     string x394;
     string x395;
     string x396;
     string x397;
     string x398;
     string x399;
     string x400;
     string x401;
     string x402;
     string x403;
     string x404;
     string x405;
     string x406;
     string x407;
     string x408;
     string x409;
     string x410;
     string x411;
     string x412;
     string x413;
     string x414;
     string x415;
     string x416;
     string x417;
     string x418;
     string x419;
     string x420;
     string x421;
     string x422;
     string x423;
     string x424;
     string x425;
     string x426;
     string x427;
     string x428;
     string x429;
     string x430;
     string x431;
     string x432;
     string x433;
     string x434;
     string x435;
     string x436;
     string x437;
     string x438;
     string x439;
     string x440;
     string x441;
     string x442;
     string x443;
     string x444;
     string x445;
     string x446;
     string x447;
     string x448;
     string x449;
     string x450;
     string x451;
     string x452;
     string x453;
     string x454;
     string x455;
     string x456;
     string x457;
     string x458;
     string x459;
     string x460;
     string x461;
     string x462;
     string x463;
     string x464;
     string x465;
     string x466;
     string x467;
     string x468;
     string x469;
     string x470;
     string x471;
     string x472;
     string x473;
     string x474;
     string x475;
     string x476;
     string x477;
     string x478;
     string x479;
     string x480;
     string x481;
     string x482;
     string x483;
     string x484;
     string x485;
     string x486;
     string x487;
     string x488;
     string x489;
     string x490;
     string x491;
     string x492;
     string x493;
     string x494;
     string x495;
     string x496;
     string x497;
     string x498;
     string x499;
     } r = {x: "", y: "", x0: "", x1: "", x2: "", x3: "", x4: "", x5: "", x6: "", x7: "", x8: "", x9: "", x10: "",
    x11: "", x12: "", x13: "", x14: "", x15: "", x16: "", x17: "", x18: "", x19: "", x20: "", x21: "", x22: "",
    x23: "", x24: "", x25: "", x26: "", x27: "", x28: "", x29: "", x30: "", x31: "", x32: "", x33: "", x34: "",
    x35: "", x36: "", x37: "", x38: "", x39: "", x40: "", x41: "", x42: "", x43: "", x44: "", x45: "", x46: "",
    x47: "", x48: "", x49: "", x50: "", x51: "", x52: "", x53: "", x54: "", x55: "", x56: "", x57: "", x58: "",
    x59: "", x60: "", x61: "", x62: "", x63: "", x64: "", x65: "", x66: "", x67: "", x68: "", x69: "", x70: "",
    x71: "", x72: "", x73: "", x74: "", x75: "", x76: "", x77: "", x78: "", x79: "", x80: "", x81: "", x82: "",
    x83: "", x84: "", x85: "", x86: "", x87: "", x88: "", x89: "", x90: "", x91: "", x92: "", x93: "", x94: "",
    x95: "", x96: "", x97: "", x98: "", x99: "", x100: "", x101: "", x102: "", x103: "", x104: "", x105: "", x106: "",
    x107: "", x108: "", x109: "", x110: "", x111: "", x112: "", x113: "", x114: "", x115: "", x116: "", x117: "",
    x118: "", x119: "", x120: "", x121: "", x122: "", x123: "", x124: "", x125: "", x126: "", x127: "", x128: "",
    x129: "", x130: "", x131: "", x132: "", x133: "", x134: "", x135: "", x136: "", x137: "", x138: "", x139: "",
    x140: "", x141: "", x142: "", x143: "", x144: "", x145: "", x146: "", x147: "", x148: "", x149: "", x150: "",
    x151: "", x152: "", x153: "", x154: "", x155: "", x156: "", x157: "", x158: "", x159: "", x160: "", x161: "",
    x162: "", x163: "", x164: "", x165: "", x166: "", x167: "", x168: "", x169: "", x170: "", x171: "", x172: "",
    x173: "", x174: "", x175: "", x176: "", x177: "", x178: "", x179: "", x180: "", x181: "", x182: "", x183: "",
    x184: "", x185: "", x186: "", x187: "", x188: "", x189: "", x190: "", x191: "", x192: "", x193: "", x194: "",
    x195: "", x196: "", x197: "", x198: "", x199: "", x200: "", x201: "", x202: "", x203: "", x204: "", x205: "",
    x206: "", x207: "", x208: "", x209: "", x210: "", x211: "", x212: "", x213: "", x214: "", x215: "", x216: "",
    x217: "", x218: "", x219: "", x220: "", x221: "", x222: "", x223: "", x224: "", x225: "", x226: "", x227: "",
    x228: "", x229: "", x230: "", x231: "", x232: "", x233: "", x234: "", x235: "", x236: "", x237: "", x238: "",
    x239: "", x240: "", x241: "", x242: "", x243: "", x244: "", x245: "", x246: "", x247: "", x248: "", x249: "",
    x250: "", x251: "", x252: "", x253: "", x254: "", x255: "", x256: "", x257: "", x258: "", x259: "", x260: "",
    x261: "", x262: "", x263: "", x264: "", x265: "", x266: "", x267: "", x268: "", x269: "", x270: "", x271: "",
    x272: "", x273: "", x274: "", x275: "", x276: "", x277: "", x278: "", x279: "", x280: "", x281: "", x282: "",
    x283: "", x284: "", x285: "", x286: "", x287: "", x288: "", x289: "", x290: "", x291: "", x292: "", x293: "",
    x294: "", x295: "", x296: "", x297: "", x298: "", x299: "", x300: "", x301: "", x302: "", x303: "", x304: "",
    x305: "", x306: "", x307: "", x308: "", x309: "", x310: "", x311: "", x312: "", x313: "", x314: "", x315: "",
    x316: "", x317: "", x318: "", x319: "", x320: "", x321: "", x322: "", x323: "", x324: "", x325: "", x326: "",
    x327: "", x328: "", x329: "", x330: "", x331: "", x332: "", x333: "", x334: "", x335: "", x336: "", x337: "",
    x338: "", x339: "", x340: "", x341: "", x342: "", x343: "", x344: "", x345: "", x346: "", x347: "", x348: "",
    x349: "", x350: "", x351: "", x352: "", x353: "", x354: "", x355: "", x356: "", x357: "", x358: "", x359: "",
    x360: "", x361: "", x362: "", x363: "", x364: "", x365: "", x366: "", x367: "", x368: "", x369: "", x370: "",
    x371: "", x372: "", x373: "", x374: "", x375: "", x376: "", x377: "", x378: "", x379: "", x380: "", x381: "",
    x382: "", x383: "", x384: "", x385: "", x386: "", x387: "", x388: "", x389: "", x390: "", x391: "", x392: "",
    x393: "", x394: "", x395: "", x396: "", x397: "", x398: "", x399: "", x400: "", x401: "", x402: "", x403: "",
    x404: "", x405: "", x406: "", x407: "", x408: "", x409: "", x410: "", x411: "", x412: "", x413: "", x414: "",
    x415: "", x416: "", x417: "", x418: "", x419: "", x420: "", x421: "", x422: "", x423: "", x424: "", x425: "",
    x426: "", x427: "", x428: "", x429: "", x430: "", x431: "", x432: "", x433: "", x434: "", x435: "", x436: "",
    x437: "", x438: "", x439: "", x440: "", x441: "", x442: "", x443: "", x444: "", x445: "", x446: "", x447: "",
    x448: "", x449: "", x450: "", x451: "", x452: "", x453: "", x454: "", x455: "", x456: "", x457: "", x458: "",
    x459: "", x460: "", x461: "", x462: "", x463: "", x464: "", x465: "", x466: "", x467: "", x468: "", x469: "",
    x470: "", x471: "", x472: "", x473: "", x474: "", x475: "", x476: "", x477: "", x478: "", x479: "", x480: "",
    x481: "", x482: "", x483: "", x484: "", x485: "", x486: "", x487: "", x488: "", x489: "", x490: "", x491: "",
    x492: "", x493: "", x494: "", x495: "", x496: "", x497: "", x498: "", x499: ""};
    test:assertEquals(r.x, "");
    test:assertEquals(r.y, "");
    test:assertEquals(g1[0], 1);
    test:assertEquals(g1[2], "hello");
    test:assertEquals(g1[3], "world");
}
