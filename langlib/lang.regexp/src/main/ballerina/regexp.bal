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

// Given that we cannot introduce a new keyword, we need some magic
// to define a type that refers to RegExp.  This is one possible way.
@builtinSubtype
type RegExp anydata;

type RegExp string;

public type Span readonly & object {
   public int startIndex;
   public int endIndex;
   public string substr;
   // This avoids constructing a potentially long string unless and until it is needed
   public isolated function substring() returns string;
};

type Groups readonly & [Span, Span?...];

# Returns the span of the first match that starts at or after startIndex.
function find(RegExp re, string str, int startIndex = 0) returns Span? {
    anydata[] resultArr = findImpl(re, str, startIndex);
    if (resultArr is [int, int, string]) {
        Span s = new java:SpanImpl(resultArr[0], resultArr[1], resultArr[2]);
        return s;
    }
}

function findImpl(RegExp re, string str, int startIndex = 0) returns anydata[] = @java:Method {
   'class: "org.ballerinalang.langlib.regexp.Find",
   name: "find"
} external;

//// If we have named groups, then we will add another function to handle them.
//
//function findGroups(RegExp re, string str, int startIndex = 0) returns Groups?;
//// We can in the future add a type parameter to RegExp corresponding to the type of Groups
//// Then we would get
//// function findGroups(RegExp<GroupsType>, string, int = 0) returns GroupsType?;
//
//# Return all non-overlapping matches
//// XXX better for the next two to return iterable object
//function findAll(RegExp re, string str, int startIndex = 0) returns Span[];
//function findAllGroups(RegExp re, string str, int startIndex = 0) returns Groups[]
//
//function matchAt(RegExp re, string str, int startIndex = 0) returns Span?;
//function matchGroupsAt(RegExp re, string str, int startIndex = 0) returns  Groups?;
//
//// We cannot use "all" here in the name because "findAll" is using "all" with another meaning
//# Is there a match of the RegExp that starts at the beginning of the string and ends at the end of the string?
//function isFullMatch(RegExp re, string str) returns boolean;
//function fullMatchGroups(RegExp re, string str) return Groups?;
//
//type ReplacerFunction function(Groups groups) returns string;
//type Replacement ReplacerFunction|string;
//
//// Replaces the first occurrence if any.
//function replace(RegExp re, Replacement replacement, int startIndex = 0) returns string;
//// Replace all non-overlapping occurrences.
//function replaceAll(RegExp re, Replacement replacement, int startIndex = 0) returns string;
//
//function fromString(string str) returns RegExp|error;

// TBD: split


// string module
//
//import ballerina/lang.regexp;
//
//type RegExp regexp:RegExp;
//
//# True if there is a match of `re` against all of `str`.
//# Use `includesMatch` to test whether `re` matches somewhere in `str`.
//// Arguable that this should be called `fullyMatches`, but string is providing
//// simplified conceptual model of regular expressions, and at this level
//// `matches` feels good enough.
//// Need to be careful about cycle between this module and regexp module.
//function matches(string str, RegExp re) return boolean {
//   returns re.fullyMatches(str);
//}
//
//# True if there is a match for `re` anywhere in `str`
//// we already have includes(string, substr, int startIndex)
//function includesMatch(string str, RegExp re, int startIndex = 0) returns boolean {
//   returns re.find(str, startIndex) != ();
//}
