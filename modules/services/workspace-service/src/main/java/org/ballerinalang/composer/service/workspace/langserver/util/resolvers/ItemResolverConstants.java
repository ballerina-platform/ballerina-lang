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

package org.ballerinalang.composer.service.workspace.langserver.util.resolvers;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Constants for Item Resolver
 */
public class ItemResolverConstants {

    // Start of Basic Constructs
    public static final String REPLY = "reply";
    public static final String RETURN = "return";
    public static final String IF = "if";
    public static final String ELSE = "else";
    public static final String CREATE = "create";
    public static final String FORK = "fork";
    public static final String JOIN = "join";
    public static final String ALL = "all";
    public static final String SOME = "some";
    public static final String TIMEOUT = "timeout";
    public static final String WORKER = "worker";
    public static final String TRANSFORM = "transform";
    public static final String TRANSACTION = "transaction";
    public static final String ABORT = "abort";
    public static final String ABORTED = "aborted";
    public static final String COMMITTED = "committed";
    public static final String TRY = "try";
    public static final String CATCH = "catch";
    public static final String FINALLY = "finally";
    public static final String ITERATE = "iterate";
    public static final String WHILE = "while";
    public static final String CONTINUE = "continue";
    public static final String BREAK = "break";
    public static final String THROW = "throw";
    public static final String TRIGGER_WORKER = "->";
    public static final String WORKER_REPLY = "<-";
    private static final String[] constructsArr = {REPLY, RETURN, IF, ELSE, CREATE, FORK, JOIN, ALL, SOME, TIMEOUT,
            TRANSFORM, TRANSACTION, ABORT, ABORTED, COMMITTED, TRY, CATCH, FINALLY, ITERATE, WHILE, CONTINUE,
            BREAK, THROW};

    public static final String FUNCTION = "function";
    public static final String SERVICE = "service";
    public static final String CONNECTOR = "connector";
    public static final String ACTION = "action";
    public static final String STRUCT = "struct";
    public static final String ANNOTATION = "annotation";
    public static final String XMLNS = "xmlns";


    public static final String FUNCTION_TEMPLATE = "function ${1:name} (${2}) {\n    ${3}\n}";
    public static final String SERVICE_TEMPLATE = "service<${1}> ${2:serviceName}{\n\t@http:GET { }" +
            "\n\tresource ${3:resourceName} (message m) {\n\t}\n}";
    public static final String RESOURCE_TEMPLATE = "resource ${1:name} (message ${2:m}){\n    ${3}\n}";
    public static final String CONNECTOR_DEFINITION_TEMPLATE = "connector ${1:name} (${2}) {\n\t${3}\n}";
    public static final String CONNECTOR_ACTION_TEMPLATE = "action ${1:name} (${2}) (${3}) {\n\t${4}\n}";
    public static final String WORKER_TEMPLATE = "worker ${1:name} {\n\t${2}\n}";
    public static final String STRUCT_DEFINITION_TEMPLATE = "struct ${1:name}{\n    ${2}\n}";
    public static final String ANNOTATION_DEFINITION_TEMPLATE = "annotation ${1:name}{\n    ${2 }\n}";
    public static final String IF_TEMPLATE = "if (${1:true}) {\n\t${2}\n}";
    public static final String ITERATE_TEMPLATE = "iterate (${1}) {\n\t${2}\n}";
    public static final String WHILE_TEMPLATE = "while (${1:true}) {\n\t${2}\n}";
    public static final String CONTINUE_TEMPLATE = "continue;";
    public static final String BREAK_TEMPLATE = "break;";
    public static final String FORK_TEMPLATE = "fork {\n\t${1}\n}";
    public static final String TRY_CATCH_TEMPLATE =
            "try {\n\t${1}\n} catch (${2:errors}:${3:Error} ${4:err}) {\n\t${5}\n}";
    public static final String RETURN_TEMPLATE = "return;";
    public static final String REPLY_TEMPLATE = "reply ${1};";
    public static final String ABORT_TEMPLATE = "abort;";
    public static final String TRIGGER_WORKER_TEMPLATE = "${1} -> ${2};";
    public static final String WORKER_REPLY_TEMPLATE = "${1} <- ${2};";
    public static final String TRANSFORM_TEMPLATE = "transform {\n\t${1}\n}";
    public static final String NAMESPACE_DECLARATION_TEMPLATE = "xmlns \"${1}\" as ${2:ns};";
    public static final String XML_ATTRIBUTE_REFERENCE_TEMPLATE = "\"${1}\"@[\"${2}\"]";

    // End of Basic Constructs

    public static ArrayList<String> getBasicConstructs () {
        ArrayList<String> constructs = new ArrayList<>();
        constructs.addAll(Arrays.asList(constructsArr));
        return constructs;
    }

    // Package Name Context Constants
    public static final String PACKAGE = "package";
    public static final String IMPORT = "import";
    // End of Package Name Context Constants

    // Symbol Types Constants
    public static final String PACKAGE_TYPE = "package";
    public static final String FUNCTION_TYPE = "function";
    public static final String STRUCT_TYPE = "struct";
    public static final String ACTION_TYPE = "action";
    public static final String RESOURCE_TYPE = "resource";
    public static final String WORKER_TYPE = "worker";
    public static final String KEYWORD_TYPE = "keyword";
    public static final String SNIPPET_TYPE = "snippet";
    public static final String ANNOTATION_TYPE = "annotation";
    public static final String CLIENT_CONNECTOR_TYPE = "connector";
    public static final String STATEMENT_TYPE = "statement";
    public static final String B_TYPE = "BType";
    public static final String NONE = "none";
    // Symbol Types Constants

    // Sorting priorities
    public static final int PRIORITY_1 = 1;
    public static final int PRIORITY_2 = 2;
    public static final int PRIORITY_3 = 3;
    public static final int PRIORITY_4 = 4;
    public static final int PRIORITY_5 = 5;
    public static final int PRIORITY_6 = 6;
    public static final int PRIORITY_7 = 7;

}
