/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.langserver.completions.util;

import org.ballerinalang.langserver.common.utils.CommonUtil;

/**
 * Snippet for the Ballerina language constructs.
 */
public enum Snippet {
    ABORT("abort;"),

    ANNOTATION_DEFINITION("annotation<${1:attachmentPoint}> ${2:name};"),

    BIND("bind "),

    BREAK("break;"),

    BUT("but {" + CommonUtil.LINE_SEPARATOR + "\t${1}" + CommonUtil.LINE_SEPARATOR + "};"),

    CHECK_KEYWORD_SNIPPET("check "),

    ENDPOINT("endpoint ${1:http:Listener} ${2:listener} {" + CommonUtil.LINE_SEPARATOR + "\t${3}"
            + CommonUtil.LINE_SEPARATOR + "};"),

    FOREACH("foreach ${1:varRefList} in ${2:listReference} {" + CommonUtil.LINE_SEPARATOR + "\t${3}"
            + CommonUtil.LINE_SEPARATOR + "}"),

    FORK("fork {" + CommonUtil.LINE_SEPARATOR + "\t${1}" + CommonUtil.LINE_SEPARATOR
            + "} join (${2:all}) (map ${3:results}) {" + CommonUtil.LINE_SEPARATOR + "\t${4}"
            + CommonUtil.LINE_SEPARATOR + "}"),

    FUNCTION("function ${1:name} (${2}) {" + CommonUtil.LINE_SEPARATOR + "\t${3}" + CommonUtil.LINE_SEPARATOR + "}"),

    FUNCTION_SIGNATURE("function ${1:name} (${2});"),

    IF("if (${1:true}) {" + CommonUtil.LINE_SEPARATOR + "\t${2}" + CommonUtil.LINE_SEPARATOR + "}"),

    LOCK("lock {" + CommonUtil.LINE_SEPARATOR + "\t${1}" + CommonUtil.LINE_SEPARATOR + "}"),

    MAIN_FUNCTION("function main (string... args) {" + CommonUtil.LINE_SEPARATOR + "\t${1}"
            + CommonUtil.LINE_SEPARATOR + "}"),

    MATCH("match "),

    NAMESPACE_DECLARATION("xmlns \"${1}\" as ${2:ns};"),

    NEW_OBJECT_CONSTRUCTOR("new (${1:args}) {" + CommonUtil.LINE_SEPARATOR + "\t${2}" + CommonUtil.LINE_SEPARATOR
            + "}"),

    NEXT("next;"),

    OBJECT_SNIPPET("type ${1:ObjectName} object {" + CommonUtil.LINE_SEPARATOR + "\t${2}"
            + CommonUtil.LINE_SEPARATOR + "};"),

    PRIVATE_BLOCK("private {" + CommonUtil.LINE_SEPARATOR + "\t${1}" + CommonUtil.LINE_SEPARATOR + "}"),

    PUBLIC_BLOCK("public {" + CommonUtil.LINE_SEPARATOR + "\t${1}" + CommonUtil.LINE_SEPARATOR + "}"),

    RECORD_SNIPPET("type ${1:RecordName} {" + CommonUtil.LINE_SEPARATOR + "\t${2}" + CommonUtil.LINE_SEPARATOR + "};"),

    RESOURCE("${1:newResource} (endpoint ${2:caller}, ${3:http:Request request}) {" + CommonUtil.LINE_SEPARATOR
            + "\t${4}" + CommonUtil.LINE_SEPARATOR + "}"),

    RETURN("return;"),

    SERVICE("service<${1:http:Service}> ${2:serviceName} {" + CommonUtil.LINE_SEPARATOR
            + "\t${3:newResource} (endpoint ${4:caller}, " + "${5:http:Request request}) {"
            + CommonUtil.LINE_SEPARATOR + "\t}" + CommonUtil.LINE_SEPARATOR + "}"),

    THROW("throw "),

    TRANSACTION("transaction with retries = ${1:1}, oncommit = ${2:onCommitFunction}, "
            + "onabort = ${3:onAbortFunction} " + "{" + CommonUtil.LINE_SEPARATOR
            + "\t${4}" + CommonUtil.LINE_SEPARATOR + "} onretry {" + CommonUtil.LINE_SEPARATOR + "\t${5}"
            + CommonUtil.LINE_SEPARATOR + "}"),

    TRIGGER_WORKER("${1} -> ${2};"),

    TRY_CATCH("try {" + CommonUtil.LINE_SEPARATOR + "\t${1}" + CommonUtil.LINE_SEPARATOR
            + "} catch (${2:error} ${3:err}) {" + CommonUtil.LINE_SEPARATOR + "\t${4}"
            + CommonUtil.LINE_SEPARATOR + "}"),

    VAR_KEYWORD_SNIPPET("var "),

    WHILE("while (${1:true}) {" + CommonUtil.LINE_SEPARATOR + "\t${2}" + CommonUtil.LINE_SEPARATOR + "}"),

    WORKER_REPLY("${1} <- ${2};"),

    WORKER("worker ${1:name} {" + CommonUtil.LINE_SEPARATOR + "\t${2}" + CommonUtil.LINE_SEPARATOR + "}"),

    XML_ATTRIBUTE_REFERENCE("\"${1}\"@[\"${2}\"]"),


    // Constants for the Iterable operators
    ITR_FOREACH("foreach(function (%params%) {" + CommonUtil.LINE_SEPARATOR + "\t${1}"
            + CommonUtil.LINE_SEPARATOR + "});"),

    ITR_MAP("map(function (%params%) (any){" + CommonUtil.LINE_SEPARATOR + "\t${1}"
            + CommonUtil.LINE_SEPARATOR + "});"),

    ITR_FILTER("filter(function (%params%) (boolean){" + CommonUtil.LINE_SEPARATOR + "\t${1}"
            + CommonUtil.LINE_SEPARATOR + "});"),

    ITR_COUNT("count();"),

    ITR_MIN("min();"),

    ITR_MAX("max();"),

    ITR_AVERAGE("average();"),

    ITR_SUM("sum();"),


    // Iterable operators' lambda function parameters
    ITR_ON_MAP_PARAMS("string k, any v"),

    ITR_ON_JSON_PARAMS("json v"),

    ITR_ON_XML_PARAMS("xml v");

    private String value;

    Snippet(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
