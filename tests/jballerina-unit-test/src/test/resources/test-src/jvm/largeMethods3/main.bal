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

type Person record {|
    int age;
|};

type ExpectedString record {|
    (any|error)[] expectedArray = [["globalVarStringVal1","localVarStringVal1","globalVarStringVal2",
    "localVarStringVal2","globalVarStringVal3","localVarStringVal3","globalVarStringVal4","localVarStringVal4",
    "globalVarStringVal5","localVarStringVal5","globalVarStringVal6","localVarStringVal6","globalVarStringVal7",
    "localVarStringVal7","globalVarStringVal8","localVarStringVal8","globalVarStringVal9","localVarStringVal9",
    "globalVarStringVal10","localVarStringVal10","globalVarStringVal11","localVarStringVal11","globalVarStringVal12",
    "localVarStringVal12","globalVarStringVal13","localVarStringVal13","globalVarStringVal14","localVarStringVal14",
    "globalVarStringVal15","localVarStringVal15","globalVarStringVal16","localVarStringVal16","globalVarStringVal17",
    "localVarStringVal17","globalVarStringVal18","localVarStringVal18","globalVarStringVal19","localVarStringVal19",
    "globalVarStringVal20","localVarStringVal20","globalVarStringVal21","localVarStringVal21","globalVarStringVal22",
    "localVarStringVal22","globalVarStringVal23","localVarStringVal23","globalVarStringVal24","localVarStringVal24",
    "globalVarStringVal25","localVarStringVal25","globalVarStringVal26","localVarStringVal26","globalVarStringVal27",
    "localVarStringVal27","globalVarStringVal28","localVarStringVal28","globalVarStringVal29","localVarStringVal29",
    "globalVarStringVal30","localVarStringVal30","globalVarStringVal31","localVarStringVal31","globalVarStringVal32",
    "localVarStringVal32","globalVarStringVal33","localVarStringVal33","globalVarStringVal34","localVarStringVal34",
    "globalVarStringVal35","localVarStringVal35","globalVarStringVal36","localVarStringVal36","globalVarStringVal37",
    "localVarStringVal37","globalVarStringVal38","localVarStringVal38","globalVarStringVal39","localVarStringVal39",
    "globalVarStringVal40","localVarStringVal40","globalVarStringVal41","localVarStringVal41","globalVarStringVal42",
    "localVarStringVal42","globalVarStringVal43","localVarStringVal43","globalVarStringVal44","localVarStringVal44",
    "globalVarStringVal45","localVarStringVal45","globalVarStringVal46","localVarStringVal46","globalVarStringVal47",
    "localVarStringVal47","globalVarStringVal48","localVarStringVal48","globalVarStringVal49","localVarStringVal49",
    "globalVarStringVal50","localVarStringVal50","globalVarStringVal51","localVarStringVal1","globalVarStringVal52",
    "localVarStringVal2","globalVarStringVal53","localVarStringVal3","globalVarStringVal54","localVarStringVal4",
    "globalVarStringVal55","localVarStringVal5","globalVarStringVal56","localVarStringVal6","globalVarStringVal57",
    "localVarStringVal7","globalVarStringVal58","localVarStringVal8","globalVarStringVal59","localVarStringVal9",
    "globalVarStringVal60","localVarStringVal1","globalVarStringVal61","localVarStringVal1","globalVarStringVal62",
    "localVarStringVal2","globalVarStringVal63","localVarStringVal3","globalVarStringVal64","localVarStringVal4",
    "globalVarStringVal65","localVarStringVal5","globalVarStringVal66","localVarStringVal6","globalVarStringVal67",
    "localVarStringVal7","globalVarStringVal68","localVarStringVal8","globalVarStringVal69","localVarStringVal9",
    "globalVarStringVal70","localVarStringVal10","globalVarStringVal71","localVarStringVal1","globalVarStringVal72",
    "localVarStringVal2","globalVarStringVal73","localVarStringVal3","globalVarStringVal74","localVarStringVal4",
    "globalVarStringVal75","localVarStringVal5","globalVarStringVal76","localVarStringVal6","globalVarStringVal77",
    "localVarStringVal7","globalVarStringVal78","localVarStringVal8","globalVarStringVal79","localVarStringVal9",
    "globalVarStringVal80","localVarStringVal10","globalVarStringVal81","localVarStringVal1","globalVarStringVal82",
    "localVarStringVal2","globalVarStringVal83","localVarStringVal3","globalVarStringVal84","localVarStringVal4",
    "globalVarStringVal85","localVarStringVal5","globalVarStringVal86","localVarStringVal6","globalVarStringVal87",
    "localVarStringVal7","globalVarStringVal88","localVarStringVal8","globalVarStringVal89","localVarStringVal9",
    "globalVarStringVal90","localVarStringVal10","globalVarStringVal91","localVarStringVal1","globalVarStringVal92",
    "localVarStringVal2","globalVarStringVal93","localVarStringVal3","globalVarStringVal94","localVarStringVal4",
    "globalVarStringVal95","localVarStringVal5","globalVarStringVal96","localVarStringVal6","globalVarStringVal97",
    "localVarStringVal7","globalVarStringVal98","localVarStringVal8","globalVarStringVal99","localVarStringVal9",
    "globalVarStringVal100","localVarStringVal10",1,1,2,2,3,3,4,4,5,5,6,6,7,7,8,8,9,9,10,10,11,11,12,12,13,13,14,
    14,15,15,16,16,17,17,18,18,19,19,20,20,21,21,22,22,23,23,24,24,25,25,26,26,27,27,28,28,29,29,30,30,31,31,32,32,
    33,33,34,34,35,35,36,36,37,37,38,38,39,39,40,40,41,41,42,42,43,43,44,44,45,45,46,46,47,47,48,48,49,49,50,50,51,
    1,52,2,53,3,54,4,55,5,56,6,57,7,58,8,59,9,60,10,61,1,62,2,63,3,64,4,65,5,66,6,67,7,68,8,69,9,70,10,71,1,72,2,73,
    3,74,4,75,5,76,6,77,7,78,8,79,9,80,10,81,1,82,2,83,3,84,4,85,5,86,6,87,7,88,8,89,9,90,10,91,1,92,2,93,3,94,4,
    95,5,96,6,97,7,98,8,99,9,100,10,"a","b","c","d",1,2,3,4,true,false,1.0,2.0,1.0,2.0,null,
    {"a":1},[1,[2]],[[1,2]],{"b":3},{"name":"name1","age":1},{"name":"name2","age":2},{"name":"name3","age":3},
    {"name":"name4","age":4},{"name":"name5","age":5},{"name":"name6","age":6},{"name":"name7","age":7},
    {"name":"name8","age":8},{"name":"name9","age":9},{"name":"name10","age":10},{"name":"name11","age":11},
    {"name":"name12","age":12},{"name":"name13","age":13},{"name":"name14","age":14},{"name":"name15","age":15},
    {"name":"name16","age":16},{"name":"name17","age":17},{"name":"name18","age":18},{"name":"name19","age":19},
    {"name":"name20","age":20},{"name":"name21","age":21},{"name":"name22","age":22},{"name":"name23","age":23},
    {"name":"name24","age":24},{"name":"name25","age":25},{"name":"name26","age":26},{"name":"name27","age":27},
    {"name":"name28","age":28},{"name":"name29","age":29},{"name":"name30","age":30},{"name":"name31","age":31},
    {"name":"name32","age":32},{"name":"name33","age":33},{"name":"name34","age":34},{"name":"name35","age":35},
    {"name":"name36","age":36},{"name":"name37","age":37},{"name":"name38","age":38},{"name":"name39","age":39},
    {"name":"name40","age":40},{"name":"name41","age":41},{"name":"name42","age":42},{"name":"name43","age":43},
    {"name":"name44","age":44},{"name":"name45","age":45},{"name":"name46","age":46},{"name":"name47","age":47},
    {"name":"name48","age":48},{"name":"name49","age":49},{"name":"name50","age":50},{"name":"name51","age":51},
    {"name":"name52","age":52},{"name":"name53","age":53},{"name":"name54","age":54},{"name":"name55","age":55},
    {"name":"name56","age":56},{"name":"name57","age":57},{"name":"name58","age":58},{"name":"name59","age":59},
    {"name":"name60","age":60},{"name":"name61","age":61},{"name":"name62","age":62},{"name":"name63","age":63},
    {"name":"name64","age":64},{"name":"name65","age":65},{"name":"name66","age":66},{"name":"name67","age":67},
    {"name":"name68","age":68},{"name":"name69","age":69},{"name":"name70","age":70},{"name":"name71","age":71},
    {"name":"name72","age":72},{"name":"name73","age":73},{"name":"name74","age":74},{"name":"name75","age":75},
    {"name":"name76","age":76},{"name":"name77","age":77},{"name":"name78","age":78},{"name":"name79","age":79},
    {"name":"name80","age":80},{"name":"name81","age":81},{"name":"name82","age":82},{"name":"name83","age":83},
    {"name":"name84","age":84},{"name":"name85","age":85},{"name":"name86","age":86},{"name":"name87","age":87},
    {"name":"name88","age":88},{"name":"name89","age":89},{"name":"name90","age":90},{"name":"name91","age":91},
    {"name":"name92","age":92},{"name":"name93","age":93},{"name":"name94","age":94},{"name":"name95","age":95},
    {"name":"name96","age":96},{"name":"name97","age":97},{"name":"name98","age":98},{"name":"name99","age":99},
    {"name":"name100","age":100},["a","b","barval","c","globalVarStringVal1",1,"d","appleval","e",1,
    "localVarStringVal1","f","orangeval","g","h","grapeval","i","j","globalVarStringVal1vala",
    "localVarStringVal1name",2,3],
    error("{ballerina/lang.xml}XMLOperationError",message="Failed to set children to xml element: Cycle detected"),
    trap testXMLCycleInnerNonError(),true,false,true,true,true,["y","a",new NoFillerObject(1)],["y","str"],
    {"name":"John","age":30,"male":true,"height":1.8,"weight":80,"friends":"Peter"},[2,4],[5,9,2,4],true,true,true,
    [7,7,7,7],[[[100,200,3],[2,5,6]],[[100,200,3],[2,5,6],[12,15,16]]],"globalVarStringVal1","localVarStringVal1",
    "globalVarStringVal2","localVarStringVal2","globalVarStringVal3","localVarStringVal3","globalVarStringVal4",
    "localVarStringVal4","globalVarStringVal5","localVarStringVal5","globalVarStringVal6","localVarStringVal6",
    "globalVarStringVal7","localVarStringVal7","globalVarStringVal8","localVarStringVal8","globalVarStringVal9",
    "localVarStringVal9","globalVarStringVal10","localVarStringVal10","globalVarStringVal11","localVarStringVal11",
    "globalVarStringVal12","localVarStringVal12","globalVarStringVal13","localVarStringVal13","globalVarStringVal14",
    "localVarStringVal14","globalVarStringVal15","localVarStringVal15","globalVarStringVal16","localVarStringVal16",
    "globalVarStringVal17","localVarStringVal17","globalVarStringVal18","localVarStringVal18","globalVarStringVal19",
    "localVarStringVal19","globalVarStringVal20","localVarStringVal20","globalVarStringVal21","localVarStringVal21",
    "globalVarStringVal22","localVarStringVal22","globalVarStringVal23","localVarStringVal23","globalVarStringVal24",
    "localVarStringVal24","globalVarStringVal25","localVarStringVal25","globalVarStringVal26","localVarStringVal26",
    "globalVarStringVal27","localVarStringVal27","globalVarStringVal28","localVarStringVal28","globalVarStringVal29",
    "localVarStringVal29","globalVarStringVal30","localVarStringVal30","globalVarStringVal31","localVarStringVal31",
    "globalVarStringVal32","localVarStringVal32","globalVarStringVal33","localVarStringVal33","globalVarStringVal34",
    "localVarStringVal34","globalVarStringVal35","localVarStringVal35","globalVarStringVal36","localVarStringVal36",
    "globalVarStringVal37","localVarStringVal37","globalVarStringVal38","localVarStringVal38","globalVarStringVal39",
    "localVarStringVal39","globalVarStringVal40","localVarStringVal40","globalVarStringVal41","localVarStringVal41",
    "globalVarStringVal42","localVarStringVal42","globalVarStringVal43","localVarStringVal43","globalVarStringVal44",
    "localVarStringVal44","globalVarStringVal45","localVarStringVal45","globalVarStringVal46","localVarStringVal46",
    "globalVarStringVal47","localVarStringVal47","globalVarStringVal48","localVarStringVal48","globalVarStringVal49",
    "localVarStringVal49","globalVarStringVal50","localVarStringVal50","globalVarStringVal51","localVarStringVal1",
    "globalVarStringVal52","localVarStringVal2","globalVarStringVal53","localVarStringVal3","globalVarStringVal54",
    "localVarStringVal4","globalVarStringVal55","localVarStringVal5","globalVarStringVal56","localVarStringVal6",
    "globalVarStringVal57","localVarStringVal7","globalVarStringVal58","localVarStringVal8","globalVarStringVal59",
    "localVarStringVal9","globalVarStringVal60","localVarStringVal1","globalVarStringVal61","localVarStringVal1",
    "globalVarStringVal62","localVarStringVal2","globalVarStringVal63","localVarStringVal3","globalVarStringVal64",
    "localVarStringVal4","globalVarStringVal65","localVarStringVal5","globalVarStringVal66","localVarStringVal6",
    "globalVarStringVal67","localVarStringVal7","globalVarStringVal68","localVarStringVal8","globalVarStringVal69",
    "localVarStringVal9","globalVarStringVal70","localVarStringVal10","globalVarStringVal71","localVarStringVal1",
    "globalVarStringVal72","localVarStringVal2","globalVarStringVal73","localVarStringVal3","globalVarStringVal74",
    "localVarStringVal4","globalVarStringVal75","localVarStringVal5","globalVarStringVal76","localVarStringVal6",
    "globalVarStringVal77","localVarStringVal7","globalVarStringVal78","localVarStringVal8","globalVarStringVal79",
    "localVarStringVal9","globalVarStringVal80","localVarStringVal10","globalVarStringVal81","localVarStringVal1",
    "globalVarStringVal82","localVarStringVal2","globalVarStringVal83","localVarStringVal3","globalVarStringVal84",
    "localVarStringVal4","globalVarStringVal85","localVarStringVal5","globalVarStringVal86","localVarStringVal6",
    "globalVarStringVal87","localVarStringVal7","globalVarStringVal88","localVarStringVal8","globalVarStringVal89",
    "localVarStringVal9","globalVarStringVal90","localVarStringVal10","globalVarStringVal91","localVarStringVal1",
    "globalVarStringVal92","localVarStringVal2","globalVarStringVal93","localVarStringVal3","globalVarStringVal94",
    "localVarStringVal4","globalVarStringVal95","localVarStringVal5","globalVarStringVal96","localVarStringVal6",
    "globalVarStringVal97","localVarStringVal7","globalVarStringVal98","localVarStringVal8","globalVarStringVal99",
    "localVarStringVal9","globalVarStringVal100","localVarStringVal10",1,1,2,2,3,3,4,4,5,5,6,6,7,7,8,8,9,9,10,10,11,
    11,12,12,13,13,14,14,15,15,16,16,17,17,18,18,19,19,20,20,21,21,22,22,23,23,24,24,25,25,26,26,27,27,28,28,29,29,
    30,30,31,31,32,32,33,33,34,34,35,35,36,36,37,37,38,38,39,39,40,40,41,41,42,42,43,43,44,44,45,45,46,46,47,47,48,
    48,49,49,50,50,51,1,52,2,53,3,54,4,55,5,56,6,57,7,58,8,59,9,60,10,61,1,62,2,63,3,64,4,65,5,66,6,67,7,68,8,69,9,
    70,10,71,1,72,2,73,3,74,4,75,5,76,6,77,7,78,8,79,9,80,10,81,1,82,2,83,3,84,4,85,5,86,6,87,7,88,8,89,9,90,10,91,
    1,92,2,93,3,94,4,95,5,96,6,97,7,98,8,99,9,100,10,"a","b","c","d",1,2,3,4,true,false,1.0,2.0,1.0,2.0,null,
    {"a":1},[1,[2]],[[1,2]],{"b":3},{"name":"name1","age":1},{"name":"name2","age":2},{"name":"name3","age":3},
    {"name":"name4","age":4},{"name":"name5","age":5},{"name":"name6","age":6},{"name":"name7","age":7},
    {"name":"name8","age":8},{"name":"name9","age":9},{"name":"name10","age":10},{"name":"name11","age":11},
    {"name":"name12","age":12},{"name":"name13","age":13},{"name":"name14","age":14},{"name":"name15","age":15},
    {"name":"name16","age":16},{"name":"name17","age":17},{"name":"name18","age":18},{"name":"name19","age":19},
    {"name":"name20","age":20},{"name":"name21","age":21},{"name":"name22","age":22},{"name":"name23","age":23},
    {"name":"name24","age":24},{"name":"name25","age":25},{"name":"name26","age":26},{"name":"name27","age":27},
    {"name":"name28","age":28},{"name":"name29","age":29},{"name":"name30","age":30},{"name":"name31","age":31},
    {"name":"name32","age":32},{"name":"name33","age":33},{"name":"name34","age":34},{"name":"name35","age":35},
    {"name":"name36","age":36},{"name":"name37","age":37},{"name":"name38","age":38},{"name":"name39","age":39},
    {"name":"name40","age":40},{"name":"name41","age":41},{"name":"name42","age":42},{"name":"name43","age":43},
    {"name":"name44","age":44},{"name":"name45","age":45},{"name":"name46","age":46},{"name":"name47","age":47},
    {"name":"name48","age":48},{"name":"name49","age":49},{"name":"name50","age":50},{"name":"name51","age":51},
    {"name":"name52","age":52},{"name":"name53","age":53},{"name":"name54","age":54},{"name":"name55","age":55},
    {"name":"name56","age":56},{"name":"name57","age":57},{"name":"name58","age":58},{"name":"name59","age":59},
    {"name":"name60","age":60},{"name":"name61","age":61},{"name":"name62","age":62},{"name":"name63","age":63},
    {"name":"name64","age":64},{"name":"name65","age":65},{"name":"name66","age":66},{"name":"name67","age":67},
    {"name":"name68","age":68},{"name":"name69","age":69},{"name":"name70","age":70},{"name":"name71","age":71},
    {"name":"name72","age":72},{"name":"name73","age":73},{"name":"name74","age":74},{"name":"name75","age":75},
    {"name":"name76","age":76},{"name":"name77","age":77},{"name":"name78","age":78},{"name":"name79","age":79},
    {"name":"name80","age":80},{"name":"name81","age":81},{"name":"name82","age":82},{"name":"name83","age":83},
    {"name":"name84","age":84},{"name":"name85","age":85},{"name":"name86","age":86},{"name":"name87","age":87},
    {"name":"name88","age":88},{"name":"name89","age":89},{"name":"name90","age":90},{"name":"name91","age":91},
    {"name":"name92","age":92},{"name":"name93","age":93},{"name":"name94","age":94},{"name":"name95","age":95},
    {"name":"name96","age":96},{"name":"name97","age":97},{"name":"name98","age":98},{"name":"name99","age":99},
    {"name":"name100","age":100}]];
|};

type someType [X, int];

type anotherType [Y, string, NoFillerObject];

type oneMoreType [Y, string];

type someOtherType someType | anotherType | oneMoreType;

class NoFillerObject {
    int a;

    isolated function init(int arg) {
        self.a = arg;
    }
}

ExpectedString expectedResult = {};

(any|error)[] expectedStringArray = expectedResult.expectedArray;

isolated 'xml:Element catalog = xml `<CATALOG>
                       <CD>
                           <TITLE>Empire Burlesque</TITLE>
                           <ARTIST>Bob Dylan</ARTIST>
                       </CD>
                       <CD>
                           <TITLE>Hide your heart</TITLE>
                           <ARTIST>Bonnie Tyler</ARTIST>
                       </CD>
                       <CD>
                           <TITLE>Greatest Hits</TITLE>
                           <ARTIST>Dolly Parton</ARTIST>
                       </CD>
                   </CATALOG>`;

const X = "x";
const Y = "y";

string globalStr1 = "globalVarStringVal1";
string globalStr2 = "globalVarStringVal2";
string globalStr3 = "globalVarStringVal3";
string globalStr4 = "globalVarStringVal4";
string globalStr5 = "globalVarStringVal5";
string globalStr6 = "globalVarStringVal6";
string globalStr7 = "globalVarStringVal7";
string globalStr8 = "globalVarStringVal8";
string globalStr9 = "globalVarStringVal9";
string globalStr10 = "globalVarStringVal10";
string globalStr11 = "globalVarStringVal11";
string globalStr12 = "globalVarStringVal12";
string globalStr13 = "globalVarStringVal13";
string globalStr14 = "globalVarStringVal14";
string globalStr15 = "globalVarStringVal15";
string globalStr16 = "globalVarStringVal16";
string globalStr17 = "globalVarStringVal17";
string globalStr18 = "globalVarStringVal18";
string globalStr19 = "globalVarStringVal19";
string globalStr20 = "globalVarStringVal20";
string globalStr21 = "globalVarStringVal21";
string globalStr22 = "globalVarStringVal22";
string globalStr23 = "globalVarStringVal23";
string globalStr24 = "globalVarStringVal24";
string globalStr25 = "globalVarStringVal25";
string globalStr26 = "globalVarStringVal26";
string globalStr27 = "globalVarStringVal27";
string globalStr28 = "globalVarStringVal28";
string globalStr29 = "globalVarStringVal29";
string globalStr30 = "globalVarStringVal30";
string globalStr31 = "globalVarStringVal31";
string globalStr32 = "globalVarStringVal32";
string globalStr33 = "globalVarStringVal33";
string globalStr34 = "globalVarStringVal34";
string globalStr35 = "globalVarStringVal35";
string globalStr36 = "globalVarStringVal36";
string globalStr37 = "globalVarStringVal37";
string globalStr38 = "globalVarStringVal38";
string globalStr39 = "globalVarStringVal39";
string globalStr40 = "globalVarStringVal40";
string globalStr41 = "globalVarStringVal41";
string globalStr42 = "globalVarStringVal42";
string globalStr43 = "globalVarStringVal43";
string globalStr44 = "globalVarStringVal44";
string globalStr45 = "globalVarStringVal45";
string globalStr46 = "globalVarStringVal46";
string globalStr47 = "globalVarStringVal47";
string globalStr48 = "globalVarStringVal48";
string globalStr49 = "globalVarStringVal49";
string globalStr50 = "globalVarStringVal50";
string globalStr51 = "globalVarStringVal51";
string globalStr52 = "globalVarStringVal52";
string globalStr53 = "globalVarStringVal53";
string globalStr54 = "globalVarStringVal54";
string globalStr55 = "globalVarStringVal55";
string globalStr56 = "globalVarStringVal56";
string globalStr57 = "globalVarStringVal57";
string globalStr58 = "globalVarStringVal58";
string globalStr59 = "globalVarStringVal59";
string globalStr60 = "globalVarStringVal60";
string globalStr61 = "globalVarStringVal61";
string globalStr62 = "globalVarStringVal62";
string globalStr63 = "globalVarStringVal63";
string globalStr64 = "globalVarStringVal64";
string globalStr65 = "globalVarStringVal65";
string globalStr66 = "globalVarStringVal66";
string globalStr67 = "globalVarStringVal67";
string globalStr68 = "globalVarStringVal68";
string globalStr69 = "globalVarStringVal69";
string globalStr70 = "globalVarStringVal70";
string globalStr71 = "globalVarStringVal71";
string globalStr72 = "globalVarStringVal72";
string globalStr73 = "globalVarStringVal73";
string globalStr74 = "globalVarStringVal74";
string globalStr75 = "globalVarStringVal75";
string globalStr76 = "globalVarStringVal76";
string globalStr77 = "globalVarStringVal77";
string globalStr78 = "globalVarStringVal78";
string globalStr79 = "globalVarStringVal79";
string globalStr80 = "globalVarStringVal80";
string globalStr81 = "globalVarStringVal81";
string globalStr82 = "globalVarStringVal82";
string globalStr83 = "globalVarStringVal83";
string globalStr84 = "globalVarStringVal84";
string globalStr85 = "globalVarStringVal85";
string globalStr86 = "globalVarStringVal86";
string globalStr87 = "globalVarStringVal87";
string globalStr88 = "globalVarStringVal88";
string globalStr89 = "globalVarStringVal89";
string globalStr90 = "globalVarStringVal90";
string globalStr91 = "globalVarStringVal91";
string globalStr92 = "globalVarStringVal92";
string globalStr93 = "globalVarStringVal93";
string globalStr94 = "globalVarStringVal94";
string globalStr95 = "globalVarStringVal95";
string globalStr96 = "globalVarStringVal96";
string globalStr97 = "globalVarStringVal97";
string globalStr98 = "globalVarStringVal98";
string globalStr99 = "globalVarStringVal99";
string globalStr100 = "globalVarStringVal100";
int globalInt1 = 1;
int globalInt2 = 2;
int globalInt3 = 3;
int globalInt4 = 4;
int globalInt5 = 5;
int globalInt6 = 6;
int globalInt7 = 7;
int globalInt8 = 8;
int globalInt9 = 9;
int globalInt10 = 10;
int globalInt11 = 11;
int globalInt12 = 12;
int globalInt13 = 13;
int globalInt14 = 14;
int globalInt15 = 15;
int globalInt16 = 16;
int globalInt17 = 17;
int globalInt18 = 18;
int globalInt19 = 19;
int globalInt20 = 20;
int globalInt21 = 21;
int globalInt22 = 22;
int globalInt23 = 23;
int globalInt24 = 24;
int globalInt25 = 25;
int globalInt26 = 26;
int globalInt27 = 27;
int globalInt28 = 28;
int globalInt29 = 29;
int globalInt30 = 30;
int globalInt31 = 31;
int globalInt32 = 32;
int globalInt33 = 33;
int globalInt34 = 34;
int globalInt35 = 35;
int globalInt36 = 36;
int globalInt37 = 37;
int globalInt38 = 38;
int globalInt39 = 39;
int globalInt40 = 40;
int globalInt41 = 41;
int globalInt42 = 42;
int globalInt43 = 43;
int globalInt44 = 44;
int globalInt45 = 45;
int globalInt46 = 46;
int globalInt47 = 47;
int globalInt48 = 48;
int globalInt49 = 49;
int globalInt50 = 50;
int globalInt51 = 51;
int globalInt52 = 52;
int globalInt53 = 53;
int globalInt54 = 54;
int globalInt55 = 55;
int globalInt56 = 56;
int globalInt57 = 57;
int globalInt58 = 58;
int globalInt59 = 59;
int globalInt60 = 60;
int globalInt61 = 61;
int globalInt62 = 62;
int globalInt63 = 63;
int globalInt64 = 64;
int globalInt65 = 65;
int globalInt66 = 66;
int globalInt67 = 67;
int globalInt68 = 68;
int globalInt69 = 69;
int globalInt70 = 70;
int globalInt71 = 71;
int globalInt72 = 72;
int globalInt73 = 73;
int globalInt74 = 74;
int globalInt75 = 75;
int globalInt76 = 76;
int globalInt77 = 77;
int globalInt78 = 78;
int globalInt79 = 79;
int globalInt80 = 80;
int globalInt81 = 81;
int globalInt82 = 82;
int globalInt83 = 83;
int globalInt84 = 84;
int globalInt85 = 85;
int globalInt86 = 86;
int globalInt87 = 87;
int globalInt88 = 88;
int globalInt89 = 89;
int globalInt90 = 90;
int globalInt91 = 91;
int globalInt92 = 92;
int globalInt93 = 93;
int globalInt94 = 94;
int globalInt95 = 95;
int globalInt96 = 96;
int globalInt97 = 97;
int globalInt98 = 98;
int globalInt99 = 99;
int globalInt100 = 100;

byte? lhsval3 = 251;
int:Unsigned8? lhsval4 = 251;
int:Signed8 rhsval3 = -123;
int rhsval4 = -123;

public function main(int intArgA = 2) returns error? {
    int intB = intArgA.cloneReadOnly();
    int[] a = [1, 2, 3];
    int[][] b = [[1, 2, 3], [4, 5, 6]];
    any c = a;
    any d = b;
    int localInt1 = 1;
    int localInt2 = 2;
    int localInt3 = 3;
    int localInt4 = 4;
    int localInt5 = 5;
    int localInt6 = 6;
    int localInt7 = 7;
    int localInt8 = 8;
    int localInt9 = 9;
    int localInt10 = 10;
    int localInt11 = 11;
    int localInt12 = 12;
    int localInt13 = 13;
    int localInt14 = 14;
    int localInt15 = 15;
    int localInt16 = 16;
    int localInt17 = 17;
    int localInt18 = 18;
    int localInt19 = 19;
    int localInt20 = 20;
    int localInt21 = 21;
    int localInt22 = 22;
    int localInt23 = 23;
    int localInt24 = 24;
    int localInt25 = 25;
    int localInt26 = 26;
    int localInt27 = 27;
    int localInt28 = 28;
    int localInt29 = 29;
    int localInt30 = 30;
    int localInt31 = 31;
    int localInt32 = 32;
    int localInt33 = 33;
    int localInt34 = 34;
    int localInt35 = 35;
    int localInt36 = 36;
    int localInt37 = 37;
    int localInt38 = 38;
    int localInt39 = 39;
    int localInt40 = 40;
    int localInt41 = 41;
    int localInt42 = 42;
    int localInt43 = 43;
    int localInt44 = 44;
    int localInt45 = 45;
    int localInt46 = 46;
    int localInt47 = 47;
    int localInt48 = 48;
    int localInt49 = 49;
    int localInt50 = 50;
    string localStr1 = "localVarStringVal1";
    string localStr2 = "localVarStringVal2";
    string localStr3 = "localVarStringVal3";
    string localStr4 = "localVarStringVal4";
    string localStr5 = "localVarStringVal5";
    string localStr6 = "localVarStringVal6";
    string localStr7 = "localVarStringVal7";
    string localStr8 = "localVarStringVal8";
    string localStr9 = "localVarStringVal9";
    string localStr10 = "localVarStringVal10";
    string localStr11 = "localVarStringVal11";
    string localStr12 = "localVarStringVal12";
    string localStr13 = "localVarStringVal13";
    string localStr14 = "localVarStringVal14";
    string localStr15 = "localVarStringVal15";
    string localStr16 = "localVarStringVal16";
    string localStr17 = "localVarStringVal17";
    string localStr18 = "localVarStringVal18";
    string localStr19 = "localVarStringVal19";
    string localStr20 = "localVarStringVal20";
    string localStr21 = "localVarStringVal21";
    string localStr22 = "localVarStringVal22";
    string localStr23 = "localVarStringVal23";
    string localStr24 = "localVarStringVal24";
    string localStr25 = "localVarStringVal25";
    string localStr26 = "localVarStringVal26";
    string localStr27 = "localVarStringVal27";
    string localStr28 = "localVarStringVal28";
    string localStr29 = "localVarStringVal29";
    string localStr30 = "localVarStringVal30";
    string localStr31 = "localVarStringVal31";
    string localStr32 = "localVarStringVal32";
    string localStr33 = "localVarStringVal33";
    string localStr34 = "localVarStringVal34";
    string localStr35 = "localVarStringVal35";
    string localStr36 = "localVarStringVal36";
    string localStr37 = "localVarStringVal37";
    string localStr38 = "localVarStringVal38";
    string localStr39 = "localVarStringVal39";
    string localStr40 = "localVarStringVal40";
    string localStr41 = "localVarStringVal41";
    string localStr42 = "localVarStringVal42";
    string localStr43 = "localVarStringVal43";
    string localStr44 = "localVarStringVal44";
    string localStr45 = "localVarStringVal45";
    string localStr46 = "localVarStringVal46";
    string localStr47 = "localVarStringVal47";
    string localStr48 = "localVarStringVal48";
    string localStr49 = "localVarStringVal49";
    string localStr50 = "localVarStringVal50";

    (any|error)[][] anyArray = [
        [
            globalStr1, localStr1,
            globalStr2, localStr2,
            globalStr3, localStr3,
            globalStr4, localStr4,
            globalStr5, localStr5,
            globalStr6, localStr6,
            globalStr7, localStr7,
            globalStr8, localStr8,
            globalStr9, localStr9,
            globalStr10, localStr10,
            globalStr11, localStr11,
            globalStr12, localStr12,
            globalStr13, localStr13,
            globalStr14, localStr14,
            globalStr15, localStr15,
            globalStr16, localStr16,
            globalStr17, localStr17,
            globalStr18, localStr18,
            globalStr19, localStr19,
            globalStr20, localStr20,
            globalStr21, localStr21,
            globalStr22, localStr22,
            globalStr23, localStr23,
            globalStr24, localStr24,
            globalStr25, localStr25,
            globalStr26, localStr26,
            globalStr27, localStr27,
            globalStr28, localStr28,
            globalStr29, localStr29,
            globalStr30, localStr30,
            globalStr31, localStr31,
            globalStr32, localStr32,
            globalStr33, localStr33,
            globalStr34, localStr34,
            globalStr35, localStr35,
            globalStr36, localStr36,
            globalStr37, localStr37,
            globalStr38, localStr38,
            globalStr39, localStr39,
            globalStr40, localStr40,
            globalStr41, localStr41,
            globalStr42, localStr42,
            globalStr43, localStr43,
            globalStr44, localStr44,
            globalStr45, localStr45,
            globalStr46, localStr46,
            globalStr47, localStr47,
            globalStr48, localStr48,
            globalStr49, localStr49,
            globalStr50, localStr50,
            globalStr51, localStr1,
            globalStr52, localStr2,
            globalStr53, localStr3,
            globalStr54, localStr4,
            globalStr55, localStr5,
            globalStr56, localStr6,
            globalStr57, localStr7,
            globalStr58, localStr8,
            globalStr59, localStr9,
            globalStr60, localStr1,
            globalStr61, localStr1,
            globalStr62, localStr2,
            globalStr63, localStr3,
            globalStr64, localStr4,
            globalStr65, localStr5,
            globalStr66, localStr6,
            globalStr67, localStr7,
            globalStr68, localStr8,
            globalStr69, localStr9,
            globalStr70, localStr10,
            globalStr71, localStr1,
            globalStr72, localStr2,
            globalStr73, localStr3,
            globalStr74, localStr4,
            globalStr75, localStr5,
            globalStr76, localStr6,
            globalStr77, localStr7,
            globalStr78, localStr8,
            globalStr79, localStr9,
            globalStr80, localStr10,
            globalStr81, localStr1,
            globalStr82, localStr2,
            globalStr83, localStr3,
            globalStr84, localStr4,
            globalStr85, localStr5,
            globalStr86, localStr6,
            globalStr87, localStr7,
            globalStr88, localStr8,
            globalStr89, localStr9,
            globalStr90, localStr10,
            globalStr91, localStr1,
            globalStr92, localStr2,
            globalStr93, localStr3,
            globalStr94, localStr4,
            globalStr95, localStr5,
            globalStr96, localStr6,
            globalStr97, localStr7,
            globalStr98, localStr8,
            globalStr99, localStr9,
            globalStr100, localStr10,
            globalInt1, localInt1,
            globalInt2, localInt2,
            globalInt3, localInt3,
            globalInt4, localInt4,
            globalInt5, localInt5,
            globalInt6, localInt6,
            globalInt7, localInt7,
            globalInt8, localInt8,
            globalInt9, localInt9,
            globalInt10, localInt10,
            globalInt11, localInt11,
            globalInt12, localInt12,
            globalInt13, localInt13,
            globalInt14, localInt14,
            globalInt15, localInt15,
            globalInt16, localInt16,
            globalInt17, localInt17,
            globalInt18, localInt18,
            globalInt19, localInt19,
            globalInt20, localInt20,
            globalInt21, localInt21,
            globalInt22, localInt22,
            globalInt23, localInt23,
            globalInt24, localInt24,
            globalInt25, localInt25,
            globalInt26, localInt26,
            globalInt27, localInt27,
            globalInt28, localInt28,
            globalInt29, localInt29,
            globalInt30, localInt30,
            globalInt31, localInt31,
            globalInt32, localInt32,
            globalInt33, localInt33,
            globalInt34, localInt34,
            globalInt35, localInt35,
            globalInt36, localInt36,
            globalInt37, localInt37,
            globalInt38, localInt38,
            globalInt39, localInt39,
            globalInt40, localInt40,
            globalInt41, localInt41,
            globalInt42, localInt42,
            globalInt43, localInt43,
            globalInt44, localInt44,
            globalInt45, localInt45,
            globalInt46, localInt46,
            globalInt47, localInt47,
            globalInt48, localInt48,
            globalInt49, localInt49,
            globalInt50, localInt50,
            globalInt51, localInt1,
            globalInt52, localInt2,
            globalInt53, localInt3,
            globalInt54, localInt4,
            globalInt55, localInt5,
            globalInt56, localInt6,
            globalInt57, localInt7,
            globalInt58, localInt8,
            globalInt59, localInt9,
            globalInt60, localInt10,
            globalInt61, localInt1,
            globalInt62, localInt2,
            globalInt63, localInt3,
            globalInt64, localInt4,
            globalInt65, localInt5,
            globalInt66, localInt6,
            globalInt67, localInt7,
            globalInt68, localInt8,
            globalInt69, localInt9,
            globalInt70, localInt10,
            globalInt71, localInt1,
            globalInt72, localInt2,
            globalInt73, localInt3,
            globalInt74, localInt4,
            globalInt75, localInt5,
            globalInt76, localInt6,
            globalInt77, localInt7,
            globalInt78, localInt8,
            globalInt79, localInt9,
            globalInt80, localInt10,
            globalInt81, localInt1,
            globalInt82, localInt2,
            globalInt83, localInt3,
            globalInt84, localInt4,
            globalInt85, localInt5,
            globalInt86, localInt6,
            globalInt87, localInt7,
            globalInt88, localInt8,
            globalInt89, localInt9,
            globalInt90, localInt10,
            globalInt91, localInt1,
            globalInt92, localInt2,
            globalInt93, localInt3,
            globalInt94, localInt4,
            globalInt95, localInt5,
            globalInt96, localInt6,
            globalInt97, localInt7,
            globalInt98, localInt8,
            globalInt99, localInt9,
            globalInt100, localInt10,
            "a","b","c","d",1,2,3,4,true,false,1.0,2.0,1.0d,2.0d,
            (), {"a":1}, [1,[2]], [[1,2]], {"b": 3},
            {"name": "name1", "age": 1},
            {"name": "name2", "age": 2},
            {"name": "name3", "age": 3},
            {"name": "name4", "age": 4},
            {"name": "name5", "age": 5},
            {"name": "name6", "age": 6},
            {"name": "name7", "age": 7},
            {"name": "name8", "age": 8},
            {"name": "name9", "age": 9},
            {"name": "name10", "age": 10},
            {"name": "name11", "age": 11},
            {"name": "name12", "age": 12},
            {"name": "name13", "age": 13},
            {"name": "name14", "age": 14},
            {"name": "name15", "age": 15},
            {"name": "name16", "age": 16},
            {"name": "name17", "age": 17},
            {"name": "name18", "age": 18},
            {"name": "name19", "age": 19},
            {"name": "name20", "age": 20},
            {"name": "name21", "age": 21},
            {"name": "name22", "age": 22},
            {"name": "name23", "age": 23},
            {"name": "name24", "age": 24},
            {"name": "name25", "age": 25},
            {"name": "name26", "age": 26},
            {"name": "name27", "age": 27},
            {"name": "name28", "age": 28},
            {"name": "name29", "age": 29},
            {"name": "name30", "age": 30},
            {"name": "name31", "age": 31},
            {"name": "name32", "age": 32},
            {"name": "name33", "age": 33},
            {"name": "name34", "age": 34},
            {"name": "name35", "age": 35},
            {"name": "name36", "age": 36},
            {"name": "name37", "age": 37},
            {"name": "name38", "age": 38},
            {"name": "name39", "age": 39},
            {"name": "name40", "age": 40},
            {"name": "name41", "age": 41},
            {"name": "name42", "age": 42},
            {"name": "name43", "age": 43},
            {"name": "name44", "age": 44},
            {"name": "name45", "age": 45},
            {"name": "name46", "age": 46},
            {"name": "name47", "age": 47},
            {"name": "name48", "age": 48},
            {"name": "name49", "age": 49},
            {"name": "name50", "age": 50},
            {"name": "name51", "age": 51},
            {"name": "name52", "age": 52},
            {"name": "name53", "age": 53},
            {"name": "name54", "age": 54},
            {"name": "name55", "age": 55},
            {"name": "name56", "age": 56},
            {"name": "name57", "age": 57},
            {"name": "name58", "age": 58},
            {"name": "name59", "age": 59},
            {"name": "name60", "age": 60},
            {"name": "name61", "age": 61},
            {"name": "name62", "age": 62},
            {"name": "name63", "age": 63},
            {"name": "name64", "age": 64},
            {"name": "name65", "age": 65},
            {"name": "name66", "age": 66},
            {"name": "name67", "age": 67},
            {"name": "name68", "age": 68},
            {"name": "name69", "age": 69},
            {"name": "name70", "age": 70},
            {"name": "name71", "age": 71},
            {"name": "name72", "age": 72},
            {"name": "name73", "age": 73},
            {"name": "name74", "age": 74},
            {"name": "name75", "age": 75},
            {"name": "name76", "age": 76},
            {"name": "name77", "age": 77},
            {"name": "name78", "age": 78},
            {"name": "name79", "age": 79},
            {"name": "name80", "age": 80},
            {"name": "name81", "age": 81},
            {"name": "name82", "age": 82},
            {"name": "name83", "age": 83},
            {"name": "name84", "age": 84},
            {"name": "name85", "age": 85},
            {"name": "name86", "age": 86},
            {"name": "name87", "age": 87},
            {"name": "name88", "age": 88},
            {"name": "name89", "age": 89},
            {"name": "name90", "age": 90},
            {"name": "name91", "age": 91},
            {"name": "name92", "age": 92},
            {"name": "name93", "age": 93},
            {"name": "name94", "age": 94},
            {"name": "name95", "age": 95},
            {"name": "name96", "age": 96},
            {"name": "name97", "age": 97},
            {"name": "name98", "age": 98},
            {"name": "name99", "age": 99},
            {"name": "name100", "age": 100},
            ["a", "b", bar(), "c", globalStr1, globalInt1, "d", apple(), "e", localInt1, localStr1, "f",
            check trap orange(), "g", "h", check trap grape(), "i", "j", globalStr1 + "vala",
            localStr1 + "name", globalInt1 + 1, localInt1 + 2],
            trap testXMLCycleErrorInner(), trap testXMLCycleInnerNonError(),
            (c is int[] && d is int[][]), c is float[], d is json, d is json[], d is json[][],
            <someOtherType>[Y, "a", new NoFillerObject(1)],
            <someOtherType>["y", "str"],
            {name: "John", age: 30, male: true, "height": 1.8, "weight": 80, "friends": "Peter"},
            from var {x} in [{"x": 2, "y": 3}, {"x": 4, "y": 5}] collect [x],
            from var {x, y} in [{"x": 2, "y": 3}, {"x": 4, "y": 5}] let var sum = x + y collect [...[sum], ...[x]],
            from var {x, y} in [{"x": 2, "y": 3}, {"x": 4, "y": 5}, {"x": 6, "y": 7}] collect [x].some(n => n > 2),
            intArgA == intB, (intArgA.isReadOnly() && intB.isReadOnly()),
            [lhsval3 >>> rhsval3, lhsval4 >> rhsval3, lhsval3 >>> rhsval4, lhsval4 >> rhsval4],
            [[[100, 200, 3], [2, 5, 6]], [[100, 200, 3], [2, 5, 6], [12, 15, 16]]],
            globalStr1, localStr1,
            globalStr2, localStr2,
            globalStr3, localStr3,
            globalStr4, localStr4,
            globalStr5, localStr5,
            globalStr6, localStr6,
            globalStr7, localStr7,
            globalStr8, localStr8,
            globalStr9, localStr9,
            globalStr10, localStr10,
            globalStr11, localStr11,
            globalStr12, localStr12,
            globalStr13, localStr13,
            globalStr14, localStr14,
            globalStr15, localStr15,
            globalStr16, localStr16,
            globalStr17, localStr17,
            globalStr18, localStr18,
            globalStr19, localStr19,
            globalStr20, localStr20,
            globalStr21, localStr21,
            globalStr22, localStr22,
            globalStr23, localStr23,
            globalStr24, localStr24,
            globalStr25, localStr25,
            globalStr26, localStr26,
            globalStr27, localStr27,
            globalStr28, localStr28,
            globalStr29, localStr29,
            globalStr30, localStr30,
            globalStr31, localStr31,
            globalStr32, localStr32,
            globalStr33, localStr33,
            globalStr34, localStr34,
            globalStr35, localStr35,
            globalStr36, localStr36,
            globalStr37, localStr37,
            globalStr38, localStr38,
            globalStr39, localStr39,
            globalStr40, localStr40,
            globalStr41, localStr41,
            globalStr42, localStr42,
            globalStr43, localStr43,
            globalStr44, localStr44,
            globalStr45, localStr45,
            globalStr46, localStr46,
            globalStr47, localStr47,
            globalStr48, localStr48,
            globalStr49, localStr49,
            globalStr50, localStr50,
            globalStr51, localStr1,
            globalStr52, localStr2,
            globalStr53, localStr3,
            globalStr54, localStr4,
            globalStr55, localStr5,
            globalStr56, localStr6,
            globalStr57, localStr7,
            globalStr58, localStr8,
            globalStr59, localStr9,
            globalStr60, localStr1,
            globalStr61, localStr1,
            globalStr62, localStr2,
            globalStr63, localStr3,
            globalStr64, localStr4,
            globalStr65, localStr5,
            globalStr66, localStr6,
            globalStr67, localStr7,
            globalStr68, localStr8,
            globalStr69, localStr9,
            globalStr70, localStr10,
            globalStr71, localStr1,
            globalStr72, localStr2,
            globalStr73, localStr3,
            globalStr74, localStr4,
            globalStr75, localStr5,
            globalStr76, localStr6,
            globalStr77, localStr7,
            globalStr78, localStr8,
            globalStr79, localStr9,
            globalStr80, localStr10,
            globalStr81, localStr1,
            globalStr82, localStr2,
            globalStr83, localStr3,
            globalStr84, localStr4,
            globalStr85, localStr5,
            globalStr86, localStr6,
            globalStr87, localStr7,
            globalStr88, localStr8,
            globalStr89, localStr9,
            globalStr90, localStr10,
            globalStr91, localStr1,
            globalStr92, localStr2,
            globalStr93, localStr3,
            globalStr94, localStr4,
            globalStr95, localStr5,
            globalStr96, localStr6,
            globalStr97, localStr7,
            globalStr98, localStr8,
            globalStr99, localStr9,
            globalStr100, localStr10,
            globalInt1, localInt1,
            globalInt2, localInt2,
            globalInt3, localInt3,
            globalInt4, localInt4,
            globalInt5, localInt5,
            globalInt6, localInt6,
            globalInt7, localInt7,
            globalInt8, localInt8,
            globalInt9, localInt9,
            globalInt10, localInt10,
            globalInt11, localInt11,
            globalInt12, localInt12,
            globalInt13, localInt13,
            globalInt14, localInt14,
            globalInt15, localInt15,
            globalInt16, localInt16,
            globalInt17, localInt17,
            globalInt18, localInt18,
            globalInt19, localInt19,
            globalInt20, localInt20,
            globalInt21, localInt21,
            globalInt22, localInt22,
            globalInt23, localInt23,
            globalInt24, localInt24,
            globalInt25, localInt25,
            globalInt26, localInt26,
            globalInt27, localInt27,
            globalInt28, localInt28,
            globalInt29, localInt29,
            globalInt30, localInt30,
            globalInt31, localInt31,
            globalInt32, localInt32,
            globalInt33, localInt33,
            globalInt34, localInt34,
            globalInt35, localInt35,
            globalInt36, localInt36,
            globalInt37, localInt37,
            globalInt38, localInt38,
            globalInt39, localInt39,
            globalInt40, localInt40,
            globalInt41, localInt41,
            globalInt42, localInt42,
            globalInt43, localInt43,
            globalInt44, localInt44,
            globalInt45, localInt45,
            globalInt46, localInt46,
            globalInt47, localInt47,
            globalInt48, localInt48,
            globalInt49, localInt49,
            globalInt50, localInt50,
            globalInt51, localInt1,
            globalInt52, localInt2,
            globalInt53, localInt3,
            globalInt54, localInt4,
            globalInt55, localInt5,
            globalInt56, localInt6,
            globalInt57, localInt7,
            globalInt58, localInt8,
            globalInt59, localInt9,
            globalInt60, localInt10,
            globalInt61, localInt1,
            globalInt62, localInt2,
            globalInt63, localInt3,
            globalInt64, localInt4,
            globalInt65, localInt5,
            globalInt66, localInt6,
            globalInt67, localInt7,
            globalInt68, localInt8,
            globalInt69, localInt9,
            globalInt70, localInt10,
            globalInt71, localInt1,
            globalInt72, localInt2,
            globalInt73, localInt3,
            globalInt74, localInt4,
            globalInt75, localInt5,
            globalInt76, localInt6,
            globalInt77, localInt7,
            globalInt78, localInt8,
            globalInt79, localInt9,
            globalInt80, localInt10,
            globalInt81, localInt1,
            globalInt82, localInt2,
            globalInt83, localInt3,
            globalInt84, localInt4,
            globalInt85, localInt5,
            globalInt86, localInt6,
            globalInt87, localInt7,
            globalInt88, localInt8,
            globalInt89, localInt9,
            globalInt90, localInt10,
            globalInt91, localInt1,
            globalInt92, localInt2,
            globalInt93, localInt3,
            globalInt94, localInt4,
            globalInt95, localInt5,
            globalInt96, localInt6,
            globalInt97, localInt7,
            globalInt98, localInt8,
            globalInt99, localInt9,
            globalInt100, localInt10,
            "a","b","c","d",1,2,3,4,true,false,1.0,2.0,1.0d,2.0d,
            (), {"a":1}, [1,[2]], [[1,2]], {"b": 3},
            {"name": "name1", "age": 1},
            {"name": "name2", "age": 2},
            {"name": "name3", "age": 3},
            {"name": "name4", "age": 4},
            {"name": "name5", "age": 5},
            {"name": "name6", "age": 6},
            {"name": "name7", "age": 7},
            {"name": "name8", "age": 8},
            {"name": "name9", "age": 9},
            {"name": "name10", "age": 10},
            {"name": "name11", "age": 11},
            {"name": "name12", "age": 12},
            {"name": "name13", "age": 13},
            {"name": "name14", "age": 14},
            {"name": "name15", "age": 15},
            {"name": "name16", "age": 16},
            {"name": "name17", "age": 17},
            {"name": "name18", "age": 18},
            {"name": "name19", "age": 19},
            {"name": "name20", "age": 20},
            {"name": "name21", "age": 21},
            {"name": "name22", "age": 22},
            {"name": "name23", "age": 23},
            {"name": "name24", "age": 24},
            {"name": "name25", "age": 25},
            {"name": "name26", "age": 26},
            {"name": "name27", "age": 27},
            {"name": "name28", "age": 28},
            {"name": "name29", "age": 29},
            {"name": "name30", "age": 30},
            {"name": "name31", "age": 31},
            {"name": "name32", "age": 32},
            {"name": "name33", "age": 33},
            {"name": "name34", "age": 34},
            {"name": "name35", "age": 35},
            {"name": "name36", "age": 36},
            {"name": "name37", "age": 37},
            {"name": "name38", "age": 38},
            {"name": "name39", "age": 39},
            {"name": "name40", "age": 40},
            {"name": "name41", "age": 41},
            {"name": "name42", "age": 42},
            {"name": "name43", "age": 43},
            {"name": "name44", "age": 44},
            {"name": "name45", "age": 45},
            {"name": "name46", "age": 46},
            {"name": "name47", "age": 47},
            {"name": "name48", "age": 48},
            {"name": "name49", "age": 49},
            {"name": "name50", "age": 50},
            {"name": "name51", "age": 51},
            {"name": "name52", "age": 52},
            {"name": "name53", "age": 53},
            {"name": "name54", "age": 54},
            {"name": "name55", "age": 55},
            {"name": "name56", "age": 56},
            {"name": "name57", "age": 57},
            {"name": "name58", "age": 58},
            {"name": "name59", "age": 59},
            {"name": "name60", "age": 60},
            {"name": "name61", "age": 61},
            {"name": "name62", "age": 62},
            {"name": "name63", "age": 63},
            {"name": "name64", "age": 64},
            {"name": "name65", "age": 65},
            {"name": "name66", "age": 66},
            {"name": "name67", "age": 67},
            {"name": "name68", "age": 68},
            {"name": "name69", "age": 69},
            {"name": "name70", "age": 70},
            {"name": "name71", "age": 71},
            {"name": "name72", "age": 72},
            {"name": "name73", "age": 73},
            {"name": "name74", "age": 74},
            {"name": "name75", "age": 75},
            {"name": "name76", "age": 76},
            {"name": "name77", "age": 77},
            {"name": "name78", "age": 78},
            {"name": "name79", "age": 79},
            {"name": "name80", "age": 80},
            {"name": "name81", "age": 81},
            {"name": "name82", "age": 82},
            {"name": "name83", "age": 83},
            {"name": "name84", "age": 84},
            {"name": "name85", "age": 85},
            {"name": "name86", "age": 86},
            {"name": "name87", "age": 87},
            {"name": "name88", "age": 88},
            {"name": "name89", "age": 89},
            {"name": "name90", "age": 90},
            {"name": "name91", "age": 91},
            {"name": "name92", "age": 92},
            {"name": "name93", "age": 93},
            {"name": "name94", "age": 94},
            {"name": "name95", "age": 95},
            {"name": "name96", "age": 96},
            {"name": "name97", "age": 97},
            {"name": "name98", "age": 98},
            {"name": "name99", "age": 99},
            {"name": "name100", "age": 100}
        ]
    ];

    [[string, string, string, string, string, string, string, string, string, string, string, string, string, string,
    string, string, string, string, string, string, string, string, string, string, string, string, string, string,
    string, string, string, string, string, string, string, string, string, string, string, string, string, string,
    string, string, string, string, string, string, string, string, string, string, string, string, string, string,
    string, string, string, string, string, string, string, string, string, string, string, string, string, string,
    string, string, string, string, string, string, string, string, string, string, string, string, string, string,
    string, string, string, string, string, string, string, string, string, string, string, string, string, string,
    string, string, string, string, string, string, string, string, string, string, string, string, string, string,
    string, string, string, string, string, string, string, string, string, string, string, string, string, string,
    string, string, string, string, string, string, string, string, string, string, string, string, string, string,
    string, string, string, string, string, string, string, string, string, string, string, string, string, string,
    string, string, string, string, string, string, string, string, string, string, string, string, string, string,
    string, string, string, string, string, string, string, string, string, string, string, string, string, string,
    string, string, string, string, string, string, string, string, string, string, string, string, string, string,
    string, string, string, string, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
    int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
    int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
    int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
    int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
    int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
    int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
    int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
    int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
    string, string, string, string, int, int, int, int, boolean, boolean, float, float, decimal, decimal, (),
    record {|int a;|}, [int, [int]], [[int, int]], record {|int b;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    [string, string, string, string, string, int, string, string, string, int, string, string, string, string, string,
    string, string, string, string, string, int, int], xml|error, xml|error, boolean, boolean, boolean, boolean,
    boolean, someOtherType, someOtherType,
    record {|string name; int age; boolean male; float height; int weight; string friends;|}, [int...], [int...],
    boolean, boolean, boolean, [byte?, int:Unsigned8?, byte?, int:Unsigned8?],
    [[[int, int, int], [int, int, int]], [[int, int, int], [int, int, int], [int, int, int]]], string, string, string,
    string, string, string, string, string, string, string, string, string, string, string, string, string, string,
    string, string, string, string, string, string, string, string, string, string, string, string, string, string,
    string, string, string, string, string, string, string, string, string, string, string, string, string, string,
    string, string, string, string, string, string, string, string, string, string, string, string, string, string,
    string, string, string, string, string, string, string, string, string, string, string, string, string, string,
    string, string, string, string, string, string, string, string, string, string, string, string, string, string,
    string, string, string, string, string, string, string, string, string, string, string, string, string, string,
    string, string, string, string, string, string, string, string, string, string, string, string, string, string,
    string, string, string, string, string, string, string, string, string, string, string, string, string, string,
    string, string, string, string, string, string, string, string, string, string, string, string, string, string,
    string, string, string, string, string, string, string, string, string, string, string, string, string, string,
    string, string, string, string, string, string, string, string, string, string, string, string, string, string,
    string, string, string, string, string, string, string, string, string, string, string, string, string, string,
    string, string, string, string, string, string, string, string, string, string, string, string, string, string,
    string, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
    int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
    int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
    int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
    int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
    int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
    int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
    int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int,
    int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, string, string, string,
    string, int, int, int, int, boolean, boolean, float, float, decimal, decimal, (),
    record {|int a;|}, [int, [int]], [[int, int]], record {|int b;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}, record {|string name; int age;|}, record {|string name; int age;|},
    record {|string name; int age;|}...]] largeTuple = [
        [
            globalStr1, localStr1,
            globalStr2, localStr2,
            globalStr3, localStr3,
            globalStr4, localStr4,
            globalStr5, localStr5,
            globalStr6, localStr6,
            globalStr7, localStr7,
            globalStr8, localStr8,
            globalStr9, localStr9,
            globalStr10, localStr10,
            globalStr11, localStr11,
            globalStr12, localStr12,
            globalStr13, localStr13,
            globalStr14, localStr14,
            globalStr15, localStr15,
            globalStr16, localStr16,
            globalStr17, localStr17,
            globalStr18, localStr18,
            globalStr19, localStr19,
            globalStr20, localStr20,
            globalStr21, localStr21,
            globalStr22, localStr22,
            globalStr23, localStr23,
            globalStr24, localStr24,
            globalStr25, localStr25,
            globalStr26, localStr26,
            globalStr27, localStr27,
            globalStr28, localStr28,
            globalStr29, localStr29,
            globalStr30, localStr30,
            globalStr31, localStr31,
            globalStr32, localStr32,
            globalStr33, localStr33,
            globalStr34, localStr34,
            globalStr35, localStr35,
            globalStr36, localStr36,
            globalStr37, localStr37,
            globalStr38, localStr38,
            globalStr39, localStr39,
            globalStr40, localStr40,
            globalStr41, localStr41,
            globalStr42, localStr42,
            globalStr43, localStr43,
            globalStr44, localStr44,
            globalStr45, localStr45,
            globalStr46, localStr46,
            globalStr47, localStr47,
            globalStr48, localStr48,
            globalStr49, localStr49,
            globalStr50, localStr50,
            globalStr51, localStr1,
            globalStr52, localStr2,
            globalStr53, localStr3,
            globalStr54, localStr4,
            globalStr55, localStr5,
            globalStr56, localStr6,
            globalStr57, localStr7,
            globalStr58, localStr8,
            globalStr59, localStr9,
            globalStr60, localStr1,
            globalStr61, localStr1,
            globalStr62, localStr2,
            globalStr63, localStr3,
            globalStr64, localStr4,
            globalStr65, localStr5,
            globalStr66, localStr6,
            globalStr67, localStr7,
            globalStr68, localStr8,
            globalStr69, localStr9,
            globalStr70, localStr10,
            globalStr71, localStr1,
            globalStr72, localStr2,
            globalStr73, localStr3,
            globalStr74, localStr4,
            globalStr75, localStr5,
            globalStr76, localStr6,
            globalStr77, localStr7,
            globalStr78, localStr8,
            globalStr79, localStr9,
            globalStr80, localStr10,
            globalStr81, localStr1,
            globalStr82, localStr2,
            globalStr83, localStr3,
            globalStr84, localStr4,
            globalStr85, localStr5,
            globalStr86, localStr6,
            globalStr87, localStr7,
            globalStr88, localStr8,
            globalStr89, localStr9,
            globalStr90, localStr10,
            globalStr91, localStr1,
            globalStr92, localStr2,
            globalStr93, localStr3,
            globalStr94, localStr4,
            globalStr95, localStr5,
            globalStr96, localStr6,
            globalStr97, localStr7,
            globalStr98, localStr8,
            globalStr99, localStr9,
            globalStr100, localStr10,
            globalInt1, localInt1,
            globalInt2, localInt2,
            globalInt3, localInt3,
            globalInt4, localInt4,
            globalInt5, localInt5,
            globalInt6, localInt6,
            globalInt7, localInt7,
            globalInt8, localInt8,
            globalInt9, localInt9,
            globalInt10, localInt10,
            globalInt11, localInt11,
            globalInt12, localInt12,
            globalInt13, localInt13,
            globalInt14, localInt14,
            globalInt15, localInt15,
            globalInt16, localInt16,
            globalInt17, localInt17,
            globalInt18, localInt18,
            globalInt19, localInt19,
            globalInt20, localInt20,
            globalInt21, localInt21,
            globalInt22, localInt22,
            globalInt23, localInt23,
            globalInt24, localInt24,
            globalInt25, localInt25,
            globalInt26, localInt26,
            globalInt27, localInt27,
            globalInt28, localInt28,
            globalInt29, localInt29,
            globalInt30, localInt30,
            globalInt31, localInt31,
            globalInt32, localInt32,
            globalInt33, localInt33,
            globalInt34, localInt34,
            globalInt35, localInt35,
            globalInt36, localInt36,
            globalInt37, localInt37,
            globalInt38, localInt38,
            globalInt39, localInt39,
            globalInt40, localInt40,
            globalInt41, localInt41,
            globalInt42, localInt42,
            globalInt43, localInt43,
            globalInt44, localInt44,
            globalInt45, localInt45,
            globalInt46, localInt46,
            globalInt47, localInt47,
            globalInt48, localInt48,
            globalInt49, localInt49,
            globalInt50, localInt50,
            globalInt51, localInt1,
            globalInt52, localInt2,
            globalInt53, localInt3,
            globalInt54, localInt4,
            globalInt55, localInt5,
            globalInt56, localInt6,
            globalInt57, localInt7,
            globalInt58, localInt8,
            globalInt59, localInt9,
            globalInt60, localInt10,
            globalInt61, localInt1,
            globalInt62, localInt2,
            globalInt63, localInt3,
            globalInt64, localInt4,
            globalInt65, localInt5,
            globalInt66, localInt6,
            globalInt67, localInt7,
            globalInt68, localInt8,
            globalInt69, localInt9,
            globalInt70, localInt10,
            globalInt71, localInt1,
            globalInt72, localInt2,
            globalInt73, localInt3,
            globalInt74, localInt4,
            globalInt75, localInt5,
            globalInt76, localInt6,
            globalInt77, localInt7,
            globalInt78, localInt8,
            globalInt79, localInt9,
            globalInt80, localInt10,
            globalInt81, localInt1,
            globalInt82, localInt2,
            globalInt83, localInt3,
            globalInt84, localInt4,
            globalInt85, localInt5,
            globalInt86, localInt6,
            globalInt87, localInt7,
            globalInt88, localInt8,
            globalInt89, localInt9,
            globalInt90, localInt10,
            globalInt91, localInt1,
            globalInt92, localInt2,
            globalInt93, localInt3,
            globalInt94, localInt4,
            globalInt95, localInt5,
            globalInt96, localInt6,
            globalInt97, localInt7,
            globalInt98, localInt8,
            globalInt99, localInt9,
            globalInt100, localInt10,
            "a","b","c","d",1,2,3,4,true,false,1.0,2.0,1.0d,2.0d,
            (), {"a":1}, [1,[2]], [[1,2]], {"b": 3},
            {"name": "name1", "age": 1},
            {"name": "name2", "age": 2},
            {"name": "name3", "age": 3},
            {"name": "name4", "age": 4},
            {"name": "name5", "age": 5},
            {"name": "name6", "age": 6},
            {"name": "name7", "age": 7},
            {"name": "name8", "age": 8},
            {"name": "name9", "age": 9},
            {"name": "name10", "age": 10},
            {"name": "name11", "age": 11},
            {"name": "name12", "age": 12},
            {"name": "name13", "age": 13},
            {"name": "name14", "age": 14},
            {"name": "name15", "age": 15},
            {"name": "name16", "age": 16},
            {"name": "name17", "age": 17},
            {"name": "name18", "age": 18},
            {"name": "name19", "age": 19},
            {"name": "name20", "age": 20},
            {"name": "name21", "age": 21},
            {"name": "name22", "age": 22},
            {"name": "name23", "age": 23},
            {"name": "name24", "age": 24},
            {"name": "name25", "age": 25},
            {"name": "name26", "age": 26},
            {"name": "name27", "age": 27},
            {"name": "name28", "age": 28},
            {"name": "name29", "age": 29},
            {"name": "name30", "age": 30},
            {"name": "name31", "age": 31},
            {"name": "name32", "age": 32},
            {"name": "name33", "age": 33},
            {"name": "name34", "age": 34},
            {"name": "name35", "age": 35},
            {"name": "name36", "age": 36},
            {"name": "name37", "age": 37},
            {"name": "name38", "age": 38},
            {"name": "name39", "age": 39},
            {"name": "name40", "age": 40},
            {"name": "name41", "age": 41},
            {"name": "name42", "age": 42},
            {"name": "name43", "age": 43},
            {"name": "name44", "age": 44},
            {"name": "name45", "age": 45},
            {"name": "name46", "age": 46},
            {"name": "name47", "age": 47},
            {"name": "name48", "age": 48},
            {"name": "name49", "age": 49},
            {"name": "name50", "age": 50},
            {"name": "name51", "age": 51},
            {"name": "name52", "age": 52},
            {"name": "name53", "age": 53},
            {"name": "name54", "age": 54},
            {"name": "name55", "age": 55},
            {"name": "name56", "age": 56},
            {"name": "name57", "age": 57},
            {"name": "name58", "age": 58},
            {"name": "name59", "age": 59},
            {"name": "name60", "age": 60},
            {"name": "name61", "age": 61},
            {"name": "name62", "age": 62},
            {"name": "name63", "age": 63},
            {"name": "name64", "age": 64},
            {"name": "name65", "age": 65},
            {"name": "name66", "age": 66},
            {"name": "name67", "age": 67},
            {"name": "name68", "age": 68},
            {"name": "name69", "age": 69},
            {"name": "name70", "age": 70},
            {"name": "name71", "age": 71},
            {"name": "name72", "age": 72},
            {"name": "name73", "age": 73},
            {"name": "name74", "age": 74},
            {"name": "name75", "age": 75},
            {"name": "name76", "age": 76},
            {"name": "name77", "age": 77},
            {"name": "name78", "age": 78},
            {"name": "name79", "age": 79},
            {"name": "name80", "age": 80},
            {"name": "name81", "age": 81},
            {"name": "name82", "age": 82},
            {"name": "name83", "age": 83},
            {"name": "name84", "age": 84},
            {"name": "name85", "age": 85},
            {"name": "name86", "age": 86},
            {"name": "name87", "age": 87},
            {"name": "name88", "age": 88},
            {"name": "name89", "age": 89},
            {"name": "name90", "age": 90},
            {"name": "name91", "age": 91},
            {"name": "name92", "age": 92},
            {"name": "name93", "age": 93},
            {"name": "name94", "age": 94},
            {"name": "name95", "age": 95},
            {"name": "name96", "age": 96},
            {"name": "name97", "age": 97},
            {"name": "name98", "age": 98},
            {"name": "name99", "age": 99},
            {"name": "name100", "age": 100},
            ["a", "b", bar(), "c", globalStr1, globalInt1, "d", apple(), "e", localInt1, localStr1, "f",
            check trap orange(), "g", "h", check trap grape(), "i", "j", globalStr1 + "vala",
            localStr1 + "name", globalInt1 + 1, localInt1 + 2],
            trap testXMLCycleErrorInner(), trap testXMLCycleInnerNonError(),
            (c is int[] && d is int[][]), c is float[], d is json, d is json[], d is json[][],
            <someOtherType>[Y, "a", new NoFillerObject(1)],
            <someOtherType>["y", "str"],
            {name: "John", age: 30, male: true, "height": 1.8, "weight": 80, "friends": "Peter"},
            from var {x} in [{"x": 2, "y": 3}, {"x": 4, "y": 5}] collect [x],
            from var {x, y} in [{"x": 2, "y": 3}, {"x": 4, "y": 5}] let var sum = x + y collect [...[sum], ...[x]],
            from var {x, y} in [{"x": 2, "y": 3}, {"x": 4, "y": 5}, {"x": 6, "y": 7}] collect [x].some(n => n > 2),
            intArgA == intB, (intArgA.isReadOnly() && intB.isReadOnly()),
            [lhsval3 >>> rhsval3, lhsval4 >> rhsval3, lhsval3 >>> rhsval4, lhsval4 >> rhsval4],
            [[[100, 200, 3], [2, 5, 6]], [[100, 200, 3], [2, 5, 6], [12, 15, 16]]],
            globalStr1, localStr1,
            globalStr2, localStr2,
            globalStr3, localStr3,
            globalStr4, localStr4,
            globalStr5, localStr5,
            globalStr6, localStr6,
            globalStr7, localStr7,
            globalStr8, localStr8,
            globalStr9, localStr9,
            globalStr10, localStr10,
            globalStr11, localStr11,
            globalStr12, localStr12,
            globalStr13, localStr13,
            globalStr14, localStr14,
            globalStr15, localStr15,
            globalStr16, localStr16,
            globalStr17, localStr17,
            globalStr18, localStr18,
            globalStr19, localStr19,
            globalStr20, localStr20,
            globalStr21, localStr21,
            globalStr22, localStr22,
            globalStr23, localStr23,
            globalStr24, localStr24,
            globalStr25, localStr25,
            globalStr26, localStr26,
            globalStr27, localStr27,
            globalStr28, localStr28,
            globalStr29, localStr29,
            globalStr30, localStr30,
            globalStr31, localStr31,
            globalStr32, localStr32,
            globalStr33, localStr33,
            globalStr34, localStr34,
            globalStr35, localStr35,
            globalStr36, localStr36,
            globalStr37, localStr37,
            globalStr38, localStr38,
            globalStr39, localStr39,
            globalStr40, localStr40,
            globalStr41, localStr41,
            globalStr42, localStr42,
            globalStr43, localStr43,
            globalStr44, localStr44,
            globalStr45, localStr45,
            globalStr46, localStr46,
            globalStr47, localStr47,
            globalStr48, localStr48,
            globalStr49, localStr49,
            globalStr50, localStr50,
            globalStr51, localStr1,
            globalStr52, localStr2,
            globalStr53, localStr3,
            globalStr54, localStr4,
            globalStr55, localStr5,
            globalStr56, localStr6,
            globalStr57, localStr7,
            globalStr58, localStr8,
            globalStr59, localStr9,
            globalStr60, localStr1,
            globalStr61, localStr1,
            globalStr62, localStr2,
            globalStr63, localStr3,
            globalStr64, localStr4,
            globalStr65, localStr5,
            globalStr66, localStr6,
            globalStr67, localStr7,
            globalStr68, localStr8,
            globalStr69, localStr9,
            globalStr70, localStr10,
            globalStr71, localStr1,
            globalStr72, localStr2,
            globalStr73, localStr3,
            globalStr74, localStr4,
            globalStr75, localStr5,
            globalStr76, localStr6,
            globalStr77, localStr7,
            globalStr78, localStr8,
            globalStr79, localStr9,
            globalStr80, localStr10,
            globalStr81, localStr1,
            globalStr82, localStr2,
            globalStr83, localStr3,
            globalStr84, localStr4,
            globalStr85, localStr5,
            globalStr86, localStr6,
            globalStr87, localStr7,
            globalStr88, localStr8,
            globalStr89, localStr9,
            globalStr90, localStr10,
            globalStr91, localStr1,
            globalStr92, localStr2,
            globalStr93, localStr3,
            globalStr94, localStr4,
            globalStr95, localStr5,
            globalStr96, localStr6,
            globalStr97, localStr7,
            globalStr98, localStr8,
            globalStr99, localStr9,
            globalStr100, localStr10,
            globalInt1, localInt1,
            globalInt2, localInt2,
            globalInt3, localInt3,
            globalInt4, localInt4,
            globalInt5, localInt5,
            globalInt6, localInt6,
            globalInt7, localInt7,
            globalInt8, localInt8,
            globalInt9, localInt9,
            globalInt10, localInt10,
            globalInt11, localInt11,
            globalInt12, localInt12,
            globalInt13, localInt13,
            globalInt14, localInt14,
            globalInt15, localInt15,
            globalInt16, localInt16,
            globalInt17, localInt17,
            globalInt18, localInt18,
            globalInt19, localInt19,
            globalInt20, localInt20,
            globalInt21, localInt21,
            globalInt22, localInt22,
            globalInt23, localInt23,
            globalInt24, localInt24,
            globalInt25, localInt25,
            globalInt26, localInt26,
            globalInt27, localInt27,
            globalInt28, localInt28,
            globalInt29, localInt29,
            globalInt30, localInt30,
            globalInt31, localInt31,
            globalInt32, localInt32,
            globalInt33, localInt33,
            globalInt34, localInt34,
            globalInt35, localInt35,
            globalInt36, localInt36,
            globalInt37, localInt37,
            globalInt38, localInt38,
            globalInt39, localInt39,
            globalInt40, localInt40,
            globalInt41, localInt41,
            globalInt42, localInt42,
            globalInt43, localInt43,
            globalInt44, localInt44,
            globalInt45, localInt45,
            globalInt46, localInt46,
            globalInt47, localInt47,
            globalInt48, localInt48,
            globalInt49, localInt49,
            globalInt50, localInt50,
            globalInt51, localInt1,
            globalInt52, localInt2,
            globalInt53, localInt3,
            globalInt54, localInt4,
            globalInt55, localInt5,
            globalInt56, localInt6,
            globalInt57, localInt7,
            globalInt58, localInt8,
            globalInt59, localInt9,
            globalInt60, localInt10,
            globalInt61, localInt1,
            globalInt62, localInt2,
            globalInt63, localInt3,
            globalInt64, localInt4,
            globalInt65, localInt5,
            globalInt66, localInt6,
            globalInt67, localInt7,
            globalInt68, localInt8,
            globalInt69, localInt9,
            globalInt70, localInt10,
            globalInt71, localInt1,
            globalInt72, localInt2,
            globalInt73, localInt3,
            globalInt74, localInt4,
            globalInt75, localInt5,
            globalInt76, localInt6,
            globalInt77, localInt7,
            globalInt78, localInt8,
            globalInt79, localInt9,
            globalInt80, localInt10,
            globalInt81, localInt1,
            globalInt82, localInt2,
            globalInt83, localInt3,
            globalInt84, localInt4,
            globalInt85, localInt5,
            globalInt86, localInt6,
            globalInt87, localInt7,
            globalInt88, localInt8,
            globalInt89, localInt9,
            globalInt90, localInt10,
            globalInt91, localInt1,
            globalInt92, localInt2,
            globalInt93, localInt3,
            globalInt94, localInt4,
            globalInt95, localInt5,
            globalInt96, localInt6,
            globalInt97, localInt7,
            globalInt98, localInt8,
            globalInt99, localInt9,
            globalInt100, localInt10,
            "a","b","c","d",1,2,3,4,true,false,1.0,2.0,1.0d,2.0d,
            (), {"a":1}, [1,[2]], [[1,2]], {"b": 3},
            {"name": "name1", "age": 1},
            {"name": "name2", "age": 2},
            {"name": "name3", "age": 3},
            {"name": "name4", "age": 4},
            {"name": "name5", "age": 5},
            {"name": "name6", "age": 6},
            {"name": "name7", "age": 7},
            {"name": "name8", "age": 8},
            {"name": "name9", "age": 9},
            {"name": "name10", "age": 10},
            {"name": "name11", "age": 11},
            {"name": "name12", "age": 12},
            {"name": "name13", "age": 13},
            {"name": "name14", "age": 14},
            {"name": "name15", "age": 15},
            {"name": "name16", "age": 16},
            {"name": "name17", "age": 17},
            {"name": "name18", "age": 18},
            {"name": "name19", "age": 19},
            {"name": "name20", "age": 20},
            {"name": "name21", "age": 21},
            {"name": "name22", "age": 22},
            {"name": "name23", "age": 23},
            {"name": "name24", "age": 24},
            {"name": "name25", "age": 25},
            {"name": "name26", "age": 26},
            {"name": "name27", "age": 27},
            {"name": "name28", "age": 28},
            {"name": "name29", "age": 29},
            {"name": "name30", "age": 30},
            {"name": "name31", "age": 31},
            {"name": "name32", "age": 32},
            {"name": "name33", "age": 33},
            {"name": "name34", "age": 34},
            {"name": "name35", "age": 35},
            {"name": "name36", "age": 36},
            {"name": "name37", "age": 37},
            {"name": "name38", "age": 38},
            {"name": "name39", "age": 39},
            {"name": "name40", "age": 40},
            {"name": "name41", "age": 41},
            {"name": "name42", "age": 42},
            {"name": "name43", "age": 43},
            {"name": "name44", "age": 44},
            {"name": "name45", "age": 45},
            {"name": "name46", "age": 46},
            {"name": "name47", "age": 47},
            {"name": "name48", "age": 48},
            {"name": "name49", "age": 49},
            {"name": "name50", "age": 50},
            {"name": "name51", "age": 51},
            {"name": "name52", "age": 52},
            {"name": "name53", "age": 53},
            {"name": "name54", "age": 54},
            {"name": "name55", "age": 55},
            {"name": "name56", "age": 56},
            {"name": "name57", "age": 57},
            {"name": "name58", "age": 58},
            {"name": "name59", "age": 59},
            {"name": "name60", "age": 60},
            {"name": "name61", "age": 61},
            {"name": "name62", "age": 62},
            {"name": "name63", "age": 63},
            {"name": "name64", "age": 64},
            {"name": "name65", "age": 65},
            {"name": "name66", "age": 66},
            {"name": "name67", "age": 67},
            {"name": "name68", "age": 68},
            {"name": "name69", "age": 69},
            {"name": "name70", "age": 70},
            {"name": "name71", "age": 71},
            {"name": "name72", "age": 72},
            {"name": "name73", "age": 73},
            {"name": "name74", "age": 74},
            {"name": "name75", "age": 75},
            {"name": "name76", "age": 76},
            {"name": "name77", "age": 77},
            {"name": "name78", "age": 78},
            {"name": "name79", "age": 79},
            {"name": "name80", "age": 80},
            {"name": "name81", "age": 81},
            {"name": "name82", "age": 82},
            {"name": "name83", "age": 83},
            {"name": "name84", "age": 84},
            {"name": "name85", "age": 85},
            {"name": "name86", "age": 86},
            {"name": "name87", "age": 87},
            {"name": "name88", "age": 88},
            {"name": "name89", "age": 89},
            {"name": "name90", "age": 90},
            {"name": "name91", "age": 91},
            {"name": "name92", "age": 92},
            {"name": "name93", "age": 93},
            {"name": "name94", "age": 94},
            {"name": "name95", "age": 95},
            {"name": "name96", "age": 96},
            {"name": "name97", "age": 97},
            {"name": "name98", "age": 98},
            {"name": "name99", "age": 99},
            {"name": "name100", "age": 100}
        ]
    ];

    test:assertEquals(anyArray[0].length(), 1056);
    test:assertEquals(anyArray.toString(), expectedStringArray.toString());
    test:assertEquals(largeTuple[0].length(), 1056);
    test:assertEquals(largeTuple.toString(), expectedStringArray.toString());
}

function testXMLCycleErrorInner() returns xml {
    'xml:Element cat;
    lock {
        cat = <'xml:Element>catalog.clone();
    }
    'xml:Element fc = <'xml:Element>cat.getChildren().strip()[0];
    fc.setChildren(cat);
    return cat;
}

isolated function testXMLCycleInnerNonError() returns xml {
    'xml:Element cat;
    lock {
        cat = <'xml:Element>catalog.clone();
    }

    var cds = cat.getChildren().strip();
    'xml:Element fc = <'xml:Element>cds[0];
    fc.setChildren(cds[1]);
    return cat;
}

function bar() returns string {
    return "barval";
}

function apple() returns string {
    return "appleval";
}

function orange() returns string|error {
    return "orangeval";
}

function grape() returns string|error {
    return "grapeval";
}
