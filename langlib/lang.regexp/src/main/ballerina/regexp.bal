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
// These functions will all be isolated and public
// regexp module

import ballerina/jballerina.java;

@builtinSubtype
public type RegExp anydata;

public type Span readonly & object {
    public int startIndex;
    public int endIndex;
    public isolated function substring() returns string;
};

public type Groups readonly & [Span, Span?...];

type SpanAsTupleType [int, int, string];

type GroupsAsSpanArrayType SpanAsTupleType[];

type GroupsArrayType GroupsAsSpanArrayType[];

# Returns the span of the first match that starts at or after startIndex.
public function find(RegExp reExp, string str, int startIndex = 0) returns Span? {
    SpanAsTupleType? resultArr = findImpl(reExp, str, startIndex);
    if (resultArr is SpanAsTupleType) {
        Span spanObj = new java:SpanImpl(resultArr[0], resultArr[1], resultArr[2]);
        return spanObj;
    }
}

function findImpl(RegExp reExp, string str, int startIndex = 0) returns SpanAsTupleType? = @java:Method {
    'class: "org.ballerinalang.langlib.regexp.Find",
    name: "find"
} external;

public function findGroups(RegExp reExp, string str, int startIndex = 0) returns Groups? {
    GroupsAsSpanArrayType? resultArr = findGroupsImpl(reExp, str, startIndex);
    if (resultArr is GroupsAsSpanArrayType) {
        SpanAsTupleType firstMatch = resultArr[0];
        Span firstMatchSpan = new java:SpanImpl(firstMatch[0], firstMatch[1], firstMatch[2]);
        Span[] spanArr = [];
        foreach int index in 1 ..< resultArr.length() {
            SpanAsTupleType matchGroup = resultArr[index];
            Span spanObj = new java:SpanImpl(matchGroup[0], matchGroup[1], matchGroup[2]);
            spanArr.push(spanObj);
        }
        return [firstMatchSpan, ...spanArr];
    }
}

function findGroupsImpl(RegExp reExp, string str, int startIndex = 0) returns GroupsAsSpanArrayType? = @java:Method {
    'class: "org.ballerinalang.langlib.regexp.Find",
    name: "findGroups"
} external;

public function findAll(RegExp reExp, string str, int startIndex = 0) returns Span[] {
    Span[] spanArr = [];
    GroupsAsSpanArrayType? resultArr = findGroupsImpl(reExp, str, startIndex);
    if (resultArr is GroupsAsSpanArrayType) {
        foreach SpanAsTupleType tpl in resultArr {
            spanArr.push(new java:SpanImpl(tpl[0], tpl[1], tpl[2]));
        }
    }
    return spanArr;
}

public function findAllGroups(RegExp reExp, string str, int startIndex = 0) returns Groups[]? {
    GroupsArrayType? resultArr = findAllGroupsImpl(reExp, str, startIndex);
    if (resultArr is GroupsArrayType) {
        Groups[] groupArrRes = [];
        foreach GroupsAsSpanArrayType groupArr in resultArr {
            int resultArrLength = groupArr.length();
            SpanAsTupleType firstMatch = groupArr[0];
            Span firstMatchSpan = new java:SpanImpl(firstMatch[0], firstMatch[1], firstMatch[2]);
            Span[] spanArr = [];
            foreach int index in 1 ..< resultArrLength {
                SpanAsTupleType matchGroup = groupArr[index];
                Span spanObj = new java:SpanImpl(matchGroup[0], matchGroup[1], matchGroup[2]);
                spanArr.push(spanObj);
            }
            Groups g = [firstMatchSpan, ...spanArr];
            groupArrRes.push(g);
        }
        return groupArrRes;
    }
}

function findAllGroupsImpl(RegExp reExp, string str, int startIndex = 0) returns GroupsArrayType? = @java:Method {
    'class: "org.ballerinalang.langlib.regexp.Find",
    name: "findAllGroups"
} external;

public function matchAt(RegExp reExp, string str, int startIndex = 0) returns Span? {
    SpanAsTupleType? resultArr = matchAtImpl(reExp, str, startIndex);
    if (resultArr is SpanAsTupleType) {
        Span spanObj = new java:SpanImpl(resultArr[0], resultArr[1], resultArr[2]);
        return spanObj;
    }
}

function matchAtImpl(RegExp reExp, string str, int startIndex = 0) returns [int, int, string]? = @java:Method {
    'class: "org.ballerinalang.langlib.regexp.Matches",
    name: "matchAt"
} external;

public function matchGroupsAt(RegExp reExp, string str, int startIndex = 0) returns Groups? {
    GroupsAsSpanArrayType? resultArr = matchGroupsAtImpl(reExp, str, startIndex);
    if (resultArr is GroupsAsSpanArrayType) {
        SpanAsTupleType firstMatch = resultArr[0];
        Span firstMatchSpan = new java:SpanImpl(firstMatch[0], firstMatch[1], firstMatch[2]);
        Span[] spanArr = [];
        foreach int index in 1 ..< resultArr.length() {
            SpanAsTupleType matchGroup = resultArr[index];
            Span spanObj = new java:SpanImpl(matchGroup[0], matchGroup[1], matchGroup[2]);
            spanArr.push(spanObj);
        }
        return [firstMatchSpan, ...spanArr];
    }
}

function matchGroupsAtImpl(RegExp reExp, string str, int startIndex = 0) returns GroupsAsSpanArrayType? = @java:Method {
    'class: "org.ballerinalang.langlib.regexp.Matches",
    name: "matchGroupsAt"
} external;

public function isFullMatch(RegExp reExp, string str) returns boolean {
    return isFullMatchImpl(reExp, str);
}

function isFullMatchImpl(RegExp reExp, string str) returns boolean = @java:Method {
    'class: "org.ballerinalang.langlib.regexp.Matches",
    name: "isFullMatch"
} external;

public function fullMatchGroups(RegExp reExp, string str) returns Groups? {
    return matchGroupsAt(reExp, str);
}

type ReplacerFunction function (Groups groups) returns string;

public type Replacement ReplacerFunction|string;

public function replace(RegExp reExp, string str, Replacement replacement, int startIndex = 0) returns string {
    string replacementStr = "";
    Groups? findResult = findGroups(reExp, str, startIndex);
    if findResult is () {
        return str;
    }
    if (replacement is ReplacerFunction) {
        replacementStr = replacement(findResult);
    } else {
        replacementStr = replacement;
    }
    return replaceFromString(reExp, str, replacementStr, startIndex);
}

function replaceFromString(RegExp reExp, string str, string replacementStr, int startIndex) returns string = @java:Method {
    'class: "org.ballerinalang.langlib.regexp.Replace",
    name: "replaceFromString"
} external;

public function replaceAll(RegExp reExp, string str, Replacement replacement, int startIndex = 0) returns string {
    string replacementStr = "";
    Groups? findResult = findGroups(reExp, str, startIndex);
    if findResult is () {
        return str;
    }
    if (replacement is ReplacerFunction) {
        replacementStr = replacement(findResult);
    } else {
        replacementStr = replacement;
    }
    return replaceAllFromString(reExp, str, replacementStr, startIndex);
}

function replaceAllFromString(RegExp reExp, string str, string replacementStr, int startIndex) returns string = @java:Method {
    'class: "org.ballerinalang.langlib.regexp.Replace",
    name: "replaceAllFromString"
} external;

//todo @chiran
//function fromString(string str) returns RegExp|error;

public function print(any|error value) = @java:Method {
    'class: "org.ballerinalang.langlib.regexp.Find",
    name: "print"
} external;
