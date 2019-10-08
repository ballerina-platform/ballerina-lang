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

/**
 * Constants for Item Resolver.
 */
public class ItemResolverConstants {

    // Start of Basic Constructs
    public static final String BUT = "but";
    public static final String RETURN = "return";
    public static final String IF = "if";
    public static final String ELSE = "else";
    public static final String ELSE_IF = "else if";
    public static final String FORK = "fork";
    public static final String WORKER = "worker";
    public static final String TRANSACTION = "transaction";
    public static final String ABORT = "abort";
    public static final String RETRY = "retry";
    public static final String TRAP = "trap";
    public static final String WHILE = "while";
    public static final String ERROR = "error";
    public static final String LOCK = "lock";
    public static final String ON = "on";
    public static final String NEW = "new";
    public static final String CLIENT = "client";
    public static final String EXTERNAL = "external";
    public static final String ABSTRACT = "abstract";
    public static final String CONTINUE = "continue";
    public static final String BREAK = "break";
    public static final String FOREACH = "foreach";
    public static final String MATCH = "match";
    public static final String PANIC = "panic";
    public static final String LENGTHOF = "lengthof";
    public static final String TYPEOF = "typeof";

    public static final String FUNCTION = "function";
    public static final String FUNCTION_SIGNATURE = "function signature";
    public static final String MAIN_FUNCTION = "public main function";
    public static final String SERVICE_HTTP = "service/http";
    public static final String SERVICE = "service";
    public static final String SERVICE_WEBSOCKET = "service/webSocket";
    public static final String SERVICE_WEBSOCKET_CLIENT = "service/webSocketClient";
    public static final String SERVICE_WEBSUB = "service/webSub";
    public static final String SERVICE_GRPC = "service/gRPC";
    public static final String ANNOTATION = "annotation";
    public static final String XMLNS = "xmlns";
    public static final String HTTP_RESOURCE = "http resource";
    public static final String RESOURCE = "resource";

    // End of Basic Constructs

    // Package Name Context Constants
    public static final String IMPORT = "import";
    public static final String TYPE = "type ";
    // End of Package Name Context Constants

    // Symbol Types Constants
    public static final String ANNOTATION_TYPE = "Annotation";
    public static final String PACKAGE_TYPE = "Package";
    public static final String CONSTANT_TYPE = "Constant";
    public static final String FUNCTION_TYPE = "Function";
    public static final String KEYWORD_TYPE = "Keyword";
    public static final String SNIPPET_TYPE = "Snippet";
    public static final String STATEMENT_TYPE = "Statement";
    public static final String B_TYPE = "BType";
    public static final String FIELD_TYPE = "Field";
    public static final String NONE = "none";
    public static final String BOOLEAN_TYPE = "boolean";
    public static final String OBJECT_TYPE = "type <ObjectName> object";
    public static final String RECORD_TYPE = "type <RecordName> record";
    public static final String CLOSED_RECORD_TYPE = "type <RecordName> closed record";
    public static final String TYPE_TYPE = "type";
    public static final String REMOTE_FUNCTION_TYPE = "remote function";
    public static final String NEW_OBJECT_INITIALIZER_TYPE = "init function";
    public static final String ATTACH_FUNCTION_TYPE = "attach function";
    public static final String DETACH_FUNCTION_TYPE = "detach function";
    public static final String START_FUNCTION_TYPE = "start function";
    public static final String GRACEFUL_STOP_FUNCTION_TYPE = "graceful stop function";
    public static final String IMMEDIATE_STOP_FUNCTION_TYPE = "immediate stop function";
    // End Symbol Types Constants
    
    // Keyword constants
    public static final String VAR_KEYWORD = "var";
    public static final String CHECK_KEYWORD = "check";
    public static final String CHECKPANIC_KEYWORD = "checkpanic";
    public static final String WAIT_KEYWORD = "wait";
    public static final String START_KEYWORD = "start";
    public static final String FLUSH_KEYWORD = "flush";
    public static final String LISTENER_KEYWORD = "listener";
    public static final String RETURNS_KEYWORD = "returns";
    public static final String UNTAINTED_KEYWORD = "untainted";
    public static final String TRUE_KEYWORD = "true";
    public static final String FALSE_KEYWORD = "false";
    public static final String PUBLIC_KEYWORD = "public";
    public static final String PRIVATE_KEYWORD = "private";
    public static final String FINAL_KEYWORD = "final";
    public static final String CONST_KEYWORD = "const";

    // Iterable operators completion item labels
    public static final String ITR_FOREACH_LABEL = "foreach(<@lambda:function>)";
    public static final String ITR_MAP_LABEL = "map(<@lambda:function>)";
    public static final String ITR_FILTER_LABEL = "filter(<@lambda:function>)";
    public static final String ITR_COUNT_LABEL = "count()";
    public static final String ITR_SELECT_LABEL = "select(functionReference)";
    public static final String ITR_MIN_LABEL = "min()";
    public static final String ITR_MAX_LABEL = "max()";
    public static final String ITR_AVERAGE_LABEL = "average()";
    public static final String ITR_SUM_LABEL = "sum()";

    // Builtin Functions completion item labels
    public static final String BUILTIN_LENGTH_LABEL = "length()";
    public static final String BUILTIN_IS_NAN_LABEL = "isNaN()";
    public static final String BUILTIN_IS_FINITE_LABEL = "isFinite()";
    public static final String BUILTIN_IS_INFINITE_LABEL = "isInfinite()";
    public static final String BUILTIN_CLONE_LABEL = "clone()";
    public static final String BUILTIN_FREEZE_LABEL = "freeze()";
    public static final String BUILTIN_IS_FROZEN_LABEL = "isFrozen()";
    public static final String BUILTIN_STAMP_LABEL = "stamp(anydata a)";
    public static final String BUILTIN_HASKEY_LABEL = "hasKey(string s)";
    public static final String BUILTIN_REMOVE_LABEL = "remove(string s)";
    public static final String BUILTIN_KEYS_LABEL = "keys()";
    public static final String BUILTIN_GET_VALUES_LABEL = "values()";
    public static final String BUILTIN_CLEAR_LABEL = "clear()";
    public static final String BUILTIN_CONVERT_LABEL = "convert(anydata a)";
    public static final String BUILTIN_DETAIL_LABEL = "detail()";
    public static final String BUILTIN_REASON_LABEL = "reason()";
}
