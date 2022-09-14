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
//import ballerina/lang.array;

// Given that we cannot introduce a new keyword, we need some magic
// to define a type that refers to RegExp.  This is one possible way.
@builtinSubtype
public type RegExp anydata;

public type Span readonly & object {
   public int startIndex;
   public int endIndex;
   // This avoids constructing a potentially long string unless and until it is needed
   public isolated function substring() returns string;
};

public type Groups readonly & [Span, Span?...];

# Returns the span of the first match that starts at or after startIndex.
public function find(RegExp reExp, string str, int startIndex = 0) returns Span? {
    [int, int, string]? resultArr = findImpl(reExp, str, startIndex);
    if (resultArr is [int, int, string]) {
        Span s = new java:SpanImpl(resultArr[0], resultArr[1], resultArr[2]);
        return s;
    }
}

function findImpl(RegExp reExp, string str, int startIndex = 0) returns [int, int, string]? = @java:Method {
   'class: "org.ballerinalang.langlib.regexp.Find",
   name: "find"
} external;

type TupleType [int, int, string];
type TupleTypeArr TupleType[];
type TupleArrType TupleTypeArr[];

public function findGroups(RegExp reExp, string str, int startIndex = 0) returns Groups? {
    TupleTypeArr? resultArr = findGroupsImpl(reExp, str, startIndex);
    if (resultArr is TupleTypeArr) {
        TupleType firstMatch = resultArr[0];
        Span firstMatchSpan = new java:SpanImpl(firstMatch[0], firstMatch[1], firstMatch[2]);
        Span[] spanArr = [];
        foreach int index in 1 ..< resultArr.length() {
            TupleType matchGroup = resultArr[index];
            Span s = new java:SpanImpl(matchGroup[0], matchGroup[1], matchGroup[2]);
            spanArr.push(s);
        }
        return [firstMatchSpan, ...spanArr];
    }
}

function findGroupsImpl(RegExp reExp, string str, int startIndex = 0) returns [int, int, string][]? = @java:Method {
   'class: "org.ballerinalang.langlib.regexp.FindGroups",
   name: "findGroups"
} external;


//// We can in the future add a type parameter to RegExp corresponding to the type of Groups
//// Then we would get
//// function findGroups(RegExp<GroupsType>, string, int = 0) returns GroupsType?;
//
# Return all non-overlapping matches
// XXX better for the next two to return iterable object
public function findAll(RegExp reExp, string str, int startIndex = 0) returns Span[] {
    Span[] spanArr = [];
    TupleTypeArr? resultArr = findGroupsImpl(reExp, str, startIndex);
    if (resultArr is TupleTypeArr) {
        foreach TupleType tpl in resultArr {
            spanArr.push( new java:SpanImpl(tpl[0], tpl[1], tpl[2]));
        }
    }
    return spanArr;
}

public function findAllGroups(RegExp reExp, string str, int startIndex = 0) returns Groups[]? {
    TupleArrType? resultArr = findAllGroupsImpl(reExp, str, startIndex);
    if (resultArr is TupleArrType) {
        Groups[] groupArrRes = [];
        foreach TupleTypeArr groupArr in resultArr {
                    int resultArrLength = groupArr.length();
                    TupleType firstMatch = groupArr[0];
                    Span firstMatchSpan = new java:SpanImpl(firstMatch[0], firstMatch[1], firstMatch[2]);
                    Span[] spanArr = [];
                    foreach int index in 1 ..< resultArrLength {
                        TupleType matchGroup = groupArr[index];
                        Span s = new java:SpanImpl(matchGroup[0], matchGroup[1], matchGroup[2]);
                        spanArr.push(s);
                    }
                    Groups g = [firstMatchSpan, ...spanArr];
                    groupArrRes.push(g);
        }
        return groupArrRes;
    }
}

function findAllGroupsImpl(RegExp reExp, string str, int startIndex = 0) returns TupleArrType? = @java:Method {
   'class: "org.ballerinalang.langlib.regexp.FindGroups",
   name: "findAllGroups"
} external;

public function matchAt(RegExp reExp, string str, int startIndex = 0) returns Span? {
    [int, int, string]? resultArr = matchAtImpl(reExp, str, startIndex);
    if (resultArr is [int, int, string]) {
        Span s = new java:SpanImpl(resultArr[0], resultArr[1], resultArr[2]);
        return s;
    }
}

function matchAtImpl(RegExp reExp, string str, int startIndex = 0) returns [int, int, string]? = @java:Method {
   'class: "org.ballerinalang.langlib.regexp.Matches",
   name: "matchAt"
} external;

public function matchGroupsAt(RegExp reExp, string str, int startIndex = 0) returns  Groups? {
    TupleTypeArr? resultArr = matchGroupsAtImpl(reExp, str, startIndex);
    if (resultArr is TupleTypeArr) {
        TupleType firstMatch = resultArr[0];
        Span firstMatchSpan = new java:SpanImpl(firstMatch[0], firstMatch[1], firstMatch[2]);
        Span[] spanArr = [];
        foreach int index in 1 ..< resultArr.length() {
            TupleType matchGroup = resultArr[index];
            Span s = new java:SpanImpl(matchGroup[0], matchGroup[1], matchGroup[2]);
            spanArr.push(s);
        }
        return [firstMatchSpan, ...spanArr];
    }
}

function matchGroupsAtImpl(RegExp reExp, string str, int startIndex = 0) returns TupleTypeArr? = @java:Method {
   'class: "org.ballerinalang.langlib.regexp.Matches",
   name: "matchGroupsAt"
} external;


// We cannot use "all" here in the name because "findAll" is using "all" with another meaning
# Is there a match of the RegExp that starts at the beginning of the string and ends at the end of the string?
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

type ReplacerFunction function(Groups groups) returns string;
public type Replacement ReplacerFunction|string;

// Replaces the first occurrence if any.
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

//// Replace all non-overlapping occurrences.
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

//function fromString(string str) returns RegExp|error;

public function print(any|error value) = @java:Method {
    'class: "org.ballerinalang.langlib.regexp.Find",
    name: "print"
} external;
