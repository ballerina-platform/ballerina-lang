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
    public static final String FORK = "fork";
    public static final String WORKER = "worker";
    public static final String TRANSACTION = "transaction";
    public static final String ABORT = "abort";
    public static final String RETRY = "retry";
    public static final String TRAP = "trap";
    public static final String WHILE = "while";
    public static final String ERROR = "error";
    public static final String LOCK = "lock";
    public static final String BIND = "bind";
    public static final String ENDPOINT = "endpoint";
    public static final String CONTINUE = "continue";
    public static final String BREAK = "break";
    public static final String TRIGGER_WORKER = "wtrg   Worker Trigger Statement";
    public static final String WORKER_REPLY = "wrply   Worker Reply Statement";
    public static final String FOREACH = "foreach";
    public static final String MATCH = "match";
    public static final String PANIC = "panic";
    public static final String LENGTHOF = "lengthof";

    public static final String FUNCTION = "function";
    public static final String FUNCTION_SIGNATURE = "function signature";
    public static final String MAIN_FUNCTION = "public main function";
    public static final String SERVICE = "service";
    public static final String SERVICE_WEBSOCKET = "service/webSocket";
    public static final String SERVICE_WEBSUB = "service/webSub";
    public static final String ANNOTATION = "annotation";
    public static final String XMLNS = "xmlns";
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
    public static final String RESOURCE_TYPE = "Resource";
    public static final String KEYWORD_TYPE = "Keyword";
    public static final String SNIPPET_TYPE = "Snippet";
    public static final String STATEMENT_TYPE = "Statement";
    public static final String B_TYPE = "BType";
    public static final String FIELD_TYPE = "Field";
    public static final String NONE = "none";
    public static final String BOOLEAN_TYPE = "boolean";
    public static final String OBJECT_TYPE = "type <ObjectName> object";
    public static final String RECORD_TYPE = "type <RecordName> record";
    public static final String TYPE_TYPE = "type";
    public static final String NEW_OBJECT_CONSTRUCTOR_TYPE = "new object constructor";
    // End Symbol Types Constants
    
    // Keyword constants
    public static final String VAR_KEYWORD = "var";
    public static final String CHECK_KEYWORD = "check";
    public static final String EXTERN_KEYWORD = "extern";
    public static final String TRUE_KEYWORD = "true";
    public static final String FALSE_KEYWORD = "false";
    public static final String PUBLIC_KEYWORD = "public";
    public static final String FINAL_KEYWORD = "final";
    public static final String CONST_KEYWORD = "const";
    
    // Iterable operators completion item labels
    public static final String ITR_FOREACH_LABEL = "foreach(<@lambda:function>)";
    public static final String ITR_MAP_LABEL = "map(<@lambda:function>)";
    public static final String ITR_FILTER_LABEL = "filter(<@lambda:function>)";
    public static final String ITR_COUNT_LABEL = "count()";
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
}
