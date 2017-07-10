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
    private static final String[] constructsArr = {REPLY, RETURN, IF, ELSE, CREATE, FORK, JOIN, ALL, SOME, TIMEOUT,
            WORKER, TRANSFORM, TRANSACTION, ABORT, ABORTED, COMMITTED, TRY, CATCH, FINALLY, ITERATE, WHILE, CONTINUE,
            BREAK, THROW};

    public static final String FUNCTION = "function";
    public static final String SERVICE = "service";

    public static final String FUNCTION_TEMPLATE = "function ${1:name} (${2}) {\n    ${3}\n}";
    public static final String SERVICE_TEMPLATE = "service<${1}> ${2:name}{\n    ${3}\n}";

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
    public static final String KEYWORD_TYPE = "keyword";
    public static final String ANNOTATION_TYPE = "annotation";
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
