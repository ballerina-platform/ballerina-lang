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
    public static final String RETRY_TRANSACTION = "retry transaction";
    public static final String TRAP = "trap";
    public static final String WHILE = "while";
    public static final String DO = "do";
    public static final String ERROR = "error";
    public static final String LOCK = "lock";
    public static final String ON = "on";
    public static final String NEW = "new";
    public static final String CLIENT = "client";
    public static final String READONLY = "readonly";
    public static final String EXTERNAL = "external";
    public static final String ABSTRACT = "abstract";
    public static final String CONTINUE = "continue";
    public static final String BREAK = "break";
    public static final String FOREACH = "foreach";
    public static final String MATCH = "match";
    public static final String PANIC = "panic";
    public static final String TYPEOF = "typeof";
    public static final String COMMIT = "commit";
    public static final String IS = "is";
    public static final String ASCENDING = "ascending";
    public static final String DESCENDING = "descending";
    public static final String DEFAULT = "default";

    public static final String FUNCTION = "function";
    public static final String FUNCTION_SIGNATURE = "function signature";
    public static final String MAIN_FUNCTION = "public main function";
    public static final String SERVICE_HTTP = "service/http";
    public static final String SERVICE = "service";
    public static final String TABLE = "table";
    public static final String STRING = "string";
    public static final String XML = "xml";
    public static final String LET = "let";
    public static final String KEY = "key";
    public static final String SERVICE_WEBSOCKET = "service/webSocket";
    public static final String SERVICE_WEBSOCKET_CLIENT = "service/webSocketClient";
    public static final String SERVICE_WEBSUB = "service/webSub";
    public static final String SERVICE_GRPC = "service/gRPC";
    public static final String ANNOTATION = "annotation";
    public static final String XMLNS = "xmlns";
    public static final String CLASS = "class";
    public static final String DISTINCT = "distinct";
    public static final String HTTP_RESOURCE = "http resource";
    public static final String RESOURCE = "resource";
    
    public static final String FROM_CLAUSE = "from clause";
    public static final String LET_CLAUSE = "let clause";
    public static final String JOIN_CLAUSE = "join clause";
    public static final String ON_FAIL_CLAUSE = "on fail clause";

    // End of Basic Constructs

    // Package Name Context Constants
    public static final String IMPORT = "import";
    public static final String TYPE = "type ";
    // End of Package Name Context Constants

    // Symbol Types Constants
    public static final String ANNOTATION_TYPE = "Annotation";
    public static final String MODULE_TYPE = "Module";
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
    public static final String OBJECT_TYPE_DESC = "object {}";
    public static final String RECORD_TYPE = "type <RecordName> record";
    public static final String RECORD_TYPE_DESC = "record {}";
    public static final String ERROR_TYPE = "type <ErrorName> error<?>";
    public static final String CLOSED_RECORD_TYPE = "type <RecordName> closed record";
    public static final String CLOSED_RECORD_TYPE_DESC = "record {||}";
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
    public static final String IN_KEYWORD = "in";
    public static final String ENUM_KEYWORD = "enum";
    public static final String ROLLBACK_KEYWORD = "rollback";
    public static final String CHECK_KEYWORD = "check";
    public static final String CHECKPANIC_KEYWORD = "checkpanic";
    public static final String WAIT_KEYWORD = "wait";
    public static final String START_KEYWORD = "start";
    public static final String AS_KEYWORD = "as";
    public static final String VERSION_KEYWORD = "version";
    public static final String FROM_KEYWORD = "from";
    public static final String WHERE_KEYWORD = "where";
    public static final String JOIN_KEYWORD = "join";
    public static final String ORDERBY_KEYWORD = "order by";
    public static final String LIMIT_KEYWORD = "limit";
    public static final String SELECT_KEYWORD = "select";
    public static final String EQUALS_KEYWORD = "equals";
    public static final String FLUSH_KEYWORD = "flush";
    public static final String LISTENER_KEYWORD = "listener";
    public static final String RETURNS_KEYWORD = "returns";
    public static final String UNTAINTED_KEYWORD = "untainted";
    public static final String TRUE_KEYWORD = "true";
    public static final String FALSE_KEYWORD = "false";
    public static final String PUBLIC_KEYWORD = "public";
    public static final String ISOLATED_KEYWORD = "isolated";
    public static final String PRIVATE_KEYWORD = "private";
    public static final String FINAL_KEYWORD = "final";
    public static final String FAIL_KEYWORD = "fail";
    public static final String REMOTE_KEYWORD = "remote";
    public static final String CONST_KEYWORD = "const";
    public static final String RECORD_KEYWORD = "record";
    public static final String OBJECT_KEYWORD = "object";
}
