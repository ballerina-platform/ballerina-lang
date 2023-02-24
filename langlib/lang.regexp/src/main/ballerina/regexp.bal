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

# The type RegExp refers to the tagged data basic type with tag `re`.
@builtinSubtype
public type RegExp any;

# A span of a string.
# A span is a substring of another string.
public type Span readonly & object {
    # The index within the string where the span starts.
    public int startIndex;
    # The index within the string following the end of the span.
    # The length of the span is `endIndex - startIndex`.
    public int endIndex;
    # Returns a string with the content of the span.
    public isolated function substring() returns string;
};

# A list providing detailed information about the match of a regular expression within string.
# Each member of the list identifies the `Span` within the string matched
# by each of the regular expression's capturing groups.
# The member with index 0 corresponds to the entire regular expression.
# The group with index i, where i > 1,is the i-th capturing group;
# this will be nil if the match of the regular expression did not use
# a match of the capturing group.
# The capturing groups within a regular expression are ordered by the position
# of their opening parenthesis.
public type Groups readonly & [Span, Span?...];

type SpanAsTupleType [int, int, string];

type GroupsAsSpanArrayType SpanAsTupleType[];

type GroupsArrayType GroupsAsSpanArrayType[];

# Returns the first match of a regular expression within a string.
#
# ```ballerina
# string:RegExp r = re `World`;
#
# r.find("Not A Match") is () ⇒ true
#
# r.find("Hello World") is regexp:Span ⇒ true
#
# r.find("Hello World", 7) is regexp:Span ⇒ true
# ```
#
# + re - the regular expression
# + str - the string in which to look for a match of `re`
# + startIndex - the index within `str` at which to start looking for a match
# + return - a `Span` describing the match, or nil if no match was found
public isolated function find(RegExp re, string str, int startIndex = 0) returns Span? {
    SpanAsTupleType? resultArr = findImpl(re, str, startIndex);
    if (resultArr is SpanAsTupleType) {
        Span spanObj = new java:SpanImpl(resultArr[0], resultArr[1], resultArr[2]);
        return spanObj;
    }
}

isolated function findImpl(RegExp reExp, string str, int startIndex = 0) returns SpanAsTupleType? = @java:Method {
    'class: "org.ballerinalang.langlib.regexp.Find",
    name: "find"
} external;

isolated function findAllImpl(RegExp reExp, string str, int startIndex = 0) returns GroupsAsSpanArrayType? = @java:Method {
    'class: "org.ballerinalang.langlib.regexp.Find",
    name: "findAll"
} external;

# Returns the `Groups` for the first match of a regular expression within a string.
#
# ```ballerina
# string:RegExp r = re `([bB].tt[a-z]*)`;
#
# r.findGroups("Not A Match") is () ⇒ true
#
# r.findGroups("Butter was bought by Betty but the butter was bitter.") is regexp:Groups ⇒ true
#
# r.findGroups("Butter was bought by Betty but the butter was bitter.", 7) is regexp:Groups ⇒ true
# ```
#
# + re - the regular expression
# + str - the string in which to look for a match of `re`
# + startIndex - the index within `str` at which to start looking for a match
# + return - a `Groups` list describing the match, or nil if no match was found
public isolated function findGroups(RegExp re, string str, int startIndex = 0) returns Groups? {
    GroupsAsSpanArrayType? resultArr = findGroupsImpl(re, str, startIndex);
    if (resultArr is GroupsAsSpanArrayType) {
        return getGroupWithCaptureGroupSpans(resultArr);
    }
}

isolated function findGroupsImpl(RegExp reExp, string str, int startIndex = 0) returns GroupsAsSpanArrayType? = @java:Method {
    'class: "org.ballerinalang.langlib.regexp.Find",
    name: "findGroups"
} external;

# Returns a list of all the matches of a regular expression within a string.
# After one match is found, it looks for the next match starting where the previous
# match ended, so the list of matches will be non-overlapping.
#
# ```ballerina
# string:RegExp r = re `[bB].tt[a-z]*`;
#
# r.findAll("Not A Match").length() ⇒ 0
#
# r.findAll("Butter was bought by Betty but the butter was bitter.").length() ⇒ 4
#
# r.findAll("Butter was bought by Betty but the butter was bitter.", 7).length() ⇒ 3
# ```
#
# + re - the regular expression
# + str - the string in which to look for matches of `re`
# + startIndex - the index within `str` at which to start looking for matches
# + return - a list containing a `Span` for each match found
public isolated function findAll(RegExp re, string str, int startIndex = 0) returns Span[] {
    Span[] spanArr = [];
    GroupsAsSpanArrayType? resultArr = findAllImpl(re, str, startIndex);
    if (resultArr is GroupsAsSpanArrayType) {
        foreach SpanAsTupleType tpl in resultArr {
            spanArr.push(new java:SpanImpl(tpl[0], tpl[1], tpl[2]));
        }
    }
    return spanArr;
}

# Returns the `Groups` of all the matches of a regular expression within a string.
# After one match is found, it looks for the next match starting where the previous
# match ended, so the list of matches will be non-overlapping.
#
# ```ballerina
# string:RegExp r = re `(([a-z]u)(bble))`;
#
# r.findAllGroups("Not A Match").length() ⇒ 0
#
# r.findAllGroups("rubble, trouble, bubble, hubble").length() ⇒ 3
#
# r.findAllGroups("rubble, trouble, bubble, hubble", 7) ⇒ 2
# ```
#
# + re - the regular expression
# + str - the string in which to look for matches of `re`
# + startIndex - the index within `str` at which to start looking for matches
# + return - a list containing a `Group` for each match found
public isolated function findAllGroups(RegExp re, string str, int startIndex = 0) returns Groups[] {
    GroupsArrayType? resultArr = findAllGroupsImpl(re, str, startIndex);
    if (resultArr is GroupsArrayType) {
        Groups[] groupArrRes = [];
        foreach GroupsAsSpanArrayType groupArr in resultArr {
            Groups groups = getGroupWithCaptureGroupSpans(groupArr);
            groupArrRes.push(groups);
        }
        return groupArrRes;
    }
    return [];
}

isolated function getGroupWithCaptureGroupSpans(GroupsAsSpanArrayType groupArr) returns Groups {
    int resultArrLength = groupArr.length();
    SpanAsTupleType firstMatch = groupArr[0];
    Span firstMatchSpan = new java:SpanImpl(firstMatch[0], firstMatch[1], firstMatch[2]);
    Span[] spanArr = [];
    foreach int index in 1 ..< resultArrLength {
        SpanAsTupleType matchGroup = groupArr[index];
        Span spanObj = new java:SpanImpl(matchGroup[0], matchGroup[1], matchGroup[2]);
        spanArr.push(spanObj);
    }
    Groups groups = [firstMatchSpan, ...spanArr];
    return groups;
}

isolated function findAllGroupsImpl(RegExp reExp, string str, int startIndex = 0) returns GroupsArrayType? = @java:Method {
    'class: "org.ballerinalang.langlib.regexp.Find",
    name: "findAllGroups"
} external;

# Tests whether there is a match of a regular expression at a specific index in the string.
#
# ```ballerina
# string:RegExp r = re `World`;
#
# r.matchAt("Hello World") is () ⇒ true
#
# r.matchAt("Hello World", 6) is regexp:Span ⇒ true
# ```
#
# + re - the regular expression
# + str - the string in which to look for a match of `re`
# + startIndex - the index within `str` at which to look for a match; defaults to zero
# + return - a `Span` describing the match, or nil if `re` did not match at that index; the startIndex of the
# `Span` will always be equal to `startIndex`
public isolated function matchAt(RegExp re, string str, int startIndex = 0) returns Span? {
    SpanAsTupleType? resultArr = matchAtImpl(re, str, startIndex);
    if (resultArr is SpanAsTupleType) {
        Span spanObj = new java:SpanImpl(resultArr[0], resultArr[1], resultArr[2]);
        return spanObj;
    }
}

isolated function matchAtImpl(RegExp reExp, string str, int startIndex = 0) returns [int, int, string]? = @java:Method {
    'class: "org.ballerinalang.langlib.regexp.Matches",
    name: "matchAt"
} external;

# Returns the `Groups` of the match of a regular expression at a specific index in the string.
#
# ```ballerina
# string:RegExp r = re `([01][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])?`;
#
# r.matchGroupsAt("time: 14:35:59") is () ⇒ true
#
# r.matchGroupsAt("time: 14:35:59", 6) is regexp:Groups ⇒ true
# ```
#
# + re - the regular expression
# + str - the string in which to look for a match of `re`
# + startIndex - the index within `str` at which to look for a match; defaults to zero
# + return - a `Groups` list describing the match, or nil if `re` did not match at that index; the startIndex of the
# first `Span` in the list will always be equal to the `startIndex` of the first member of the list
public isolated function matchGroupsAt(RegExp re, string str, int startIndex = 0) returns Groups? {
    GroupsAsSpanArrayType? resultArr = matchGroupsAtImpl(re, str, startIndex);
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

isolated function matchGroupsAtImpl(RegExp reExp, string str, int startIndex = 0) returns GroupsAsSpanArrayType? = @java:Method {
    'class: "org.ballerinalang.langlib.regexp.Matches",
    name: "matchGroupsAt"
} external;

# Tests whether there is full match of regular expression with a string.
# A match of a regular expression in a string is a full match if it
# starts at index 0 and ends at index `n`, where `n` is the length of the string.
#
# ```ballerina
# string:RegExp r = re `A|Th.*ch|^`;
#
# r.isFullMatch("This is a Match") ⇒ true
#
# r.isFullMatch("Not a complete Match") ⇒ false
# ```
#
# + re - the regular expression
# + str - the string
# + return - true if there is full match of `re` with `str`, and false otherwise
public isolated function isFullMatch(RegExp re, string str) returns boolean {
    return isFullMatchImpl(re, str);
}

isolated function isFullMatchImpl(RegExp reExp, string str) returns boolean = @java:Method {
    'class: "org.ballerinalang.langlib.regexp.Matches",
    name: "isFullMatch"
} external;

# Returns the `Groups` of the match of a regular expression that is a full match of a string.
# A match of the regular expression in a string is a full match if it
# starts at index 0 and ends at index `n`, where `n` is the length of the string.
#
# ```ballerina
# string:RegExp r = re `([0-9]+)×([0-9]+)`;
#
# r.fullMatchGroups("test: 1440×900") is () ⇒ true
#
# r.fullMatchGroups("1440×900") is regexp:Groups ⇒ true
# ```
#
# + re - the regular expression
# + str - the string in which to look for a match of `re`
# + return - a `Groups` list describing the match, or nil if there is not a full match; the
# first `Span` in the list will be all of `str`
public isolated function fullMatchGroups(RegExp re, string str) returns Groups? {
    return matchGroupsAt(re, str);
}

# A function that constructs the replacement for the match of a regular expression.
# The `groups` parameter describes the match for which the replacement is to be constructed.
public type ReplacerFunction isolated function (Groups groups) returns string;

# The replacement for the match of a regular expression found within a string.
# A string value specifies that the replacement is a fixed string.
# A function that specifies that the replacement is constructed by calling a function for each match.
public type Replacement ReplacerFunction|string;

# Replaces the first match of a regular expression.
#
# ```ballerina
# string:RegExp r = re `0+`;
#
# r.replace("10010011", "*") ⇒ 1*10011
#
# r.replace("10010011", "*", 4) ⇒ 1001*11
#
# r.replace("122111", "*") ⇒ 122111
#
# r.replace("10010011", replaceFunction) ⇒ 1*10011
#
# r.replace("10010011", replaceFunction, 4) ⇒ 1001*11
#
# isolated function replaceFunction(regexp:Groups groups) returns string => "*";
# ```
#
# + re - the regular expression
# + str - the string in which to perform the replacements
# + replacement - a `Replacement` that gives the replacement for the match
# + startIndex - the index within `str` at which to start looking for a match; defaults to zero
# + return - `str` with the first match, if any, replaced by the string specified by `replacement`
public isolated function replace(RegExp re, string str, Replacement replacement, int startIndex = 0) returns string {
    Groups? findResult = findGroups(re, str, startIndex);
    if findResult is () {
        return str;
    }
    Span span = findResult[0];
    int index = 0;
    int strLength = length(str);
    string updatedString = substring(str, index, span.startIndex) +
                                        getReplacementString(findResult, replacement);
    index = span.endIndex;
    if index < strLength {
        updatedString += substring(str, index, strLength);
    }
    return updatedString;
}

# Replaces all matches of a regular expression.
# After one match is found, it looks for the next match starting where the previous
# match ended, so the matches will be non-overlapping.
#
# ```ballerina
# string:RegExp r = re `0+`;
#
# r.replaceAll("10010011", "*") ⇒ 1*1*11
#
# r.replaceAll("10010011", "*", 4) ⇒ 1001*11
#
# r.replaceAll("122111", "*") ⇒ 122111
#
# r.replaceAll("10010011", replaceFunction) ⇒ 121211
#
# r.replaceAll("10010011", replaceFunction, 4) ⇒ 1001211
#
# isolated function replaceFunction(regexp:Groups groups) returns string => groups[0].substring().length().toString();
# ```
#
# + re - the regular expression
# + str - the string in which to perform the replacements
# + replacement - a `Replacement` that gives the replacement for each match
# + startIndex - the index within `str` at which to start looking for matches; defaults to zero
# + return - `str` with every match replaced by the string specified by `replacement`
public isolated function replaceAll(RegExp re, string str, Replacement replacement, int startIndex = 0) returns string {
    Groups[] findResult = findAllGroups(re, str, startIndex);
    if findResult.length() == 0 {
        return str;
    }
    string updatedString = "";
    int index = 0;
    foreach Groups groups in findResult {
        Span span = groups[0];
        updatedString += substring(str, index, span.startIndex) +
                                            getReplacementString(groups, replacement);
        index = span.endIndex;
    }
    if index < length(str) {
        updatedString += substring(str, index, length(str));
    }
    return updatedString;
}

isolated function substring(string str, int startIndex, int endIndex = length(str)) returns string =
@java:Method {
    'class: "org.ballerinalang.langlib.regexp.RegexUtil",
    name: "substring"
} external;

# Returns the length of the string.
#
# + str - the string
# + return - the number of characters (code points) in parameter `str`
isolated function length(string str) returns int = @java:Method {
    'class: "org.ballerinalang.langlib.regexp.RegexUtil",
    name: "length"
} external;

isolated function getReplacementString(Groups groups, Replacement replacement) returns string {
    if replacement is string {
        return replacement;
    }
    return replacement(groups);
}

# Splits a string into substrings separated by matches of a regular expression.
# This finds the the non-overlapping matches of a regular expression and
# returns a list of substrings of `str` that occur before the first match,
# between matches, or after the last match.  If there are no matches, then
# `[str]` will be returned.
#
# ```ballerina
# string:RegExp r = re `,`;
#
# r.split("abc,cde,efg") ⇒ ["abc","cde","efg"]
#
# r.split("abc cde efg") ⇒ ["abc cde efg"]
# ```
#
# + re - the regular expression that specifies the separator
# + str - the string to be split
# + return - a list of substrings of `str` separated by matches of `re`
public isolated function split(RegExp re, string str) returns string[] = @java:Method {
    'class: "org.ballerinalang.langlib.regexp.Split",
    name: "split"
} external;

# Constructs a regular expression from a string.
# The syntax of the regular expression is the same as accepted by the `re` tagged data template expression.
#
# ```ballerina
# regexp:fromString("AB+C*D{1,4}") ⇒ re `AB+C*D{1,4}`
#
# regexp:fromString("AB+^*") ⇒ error
# ```
#
# + str - the string representation of a regular expression
# + return - the regular expression, or an error value if `str` is not a valid regular expression
public isolated function fromString(string str) returns RegExp|error = @java:Method {
    'class: "org.ballerinalang.langlib.regexp.FromString",
    name: "fromString"
} external;
