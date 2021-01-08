<#-- @ftlvariable name="" type="io.ballerina.shell.invoker.classload.context.ClassLoadContext" -->
<#list imports as import>
${import}
</#list>

handle context_id = java:fromString("${contextId}");

// Java methods: Memory
function recall(handle context_id, handle name) returns any|error = @java:Method {
    'class: "${memoryRef}"
} external;
function memorize(handle context_id, handle name, any|error value) = @java:Method {
    'class: "${memoryRef}"
} external;

// Java Methods: IO utilities
function printerr(any|error value) = @java:Method {
    'class: "${memoryRef}"
} external;
function println(any|error... values) = @java:Method {
    'class: "${memoryRef}"
} external;
function sprintfh(handle template, any|error... values) returns handle = @java:Method {
    name: "sprintf",
    'class: "${memoryRef}"
} external;

// Helper methods
function recall_h(string name) returns any|error {
    return trap recall(context_id, java:fromString(name));
}
function memorize_h(string name, any|error value) {
    memorize(context_id, java:fromString(name), value);
}
function sprintf(string template, any|error... values) returns string {
    handle out = sprintfh(java:fromString(template), ...values);
    return java:toString(out) ?: "";
}

// Module level declarations
<#list moduleDclns as dcln>
${dcln}
</#list>

// Variable declarations
<#list varDclns as varDcln>
<#if varDcln.new>
(${varDcln.type})? ${varDcln.name} = (); // There is an issue with the name or type
<#else>
<#if varDcln.isAny()>
${varDcln.type} ${varDcln.name} = <${varDcln.type}> checkpanic recall_h("${varDcln.name?j_string}");
<#else>
${varDcln.type} ${varDcln.name} = <${varDcln.type}> recall_h("${varDcln.name?j_string}");
</#if>
</#if>
</#list>

// Will run current statement/expression and return its result.
function run() returns @untainted any|error {
    <#if lastStmt.statement>
    if (true) {
        ${lastStmt.code}
    }
    return ();
    <#else>
    return trap (
        ${lastStmt.code}
    );
    </#if>
}

// This will execute the statement and initialize and save var dcln.
// The variable is declared in local context to enable various expressions.
public function stmts() returns any|error {
    any|error ${exprVarName} = trap run();
    ${lastVarDcln}
    memorize_h("${exprVarName?j_string}", ${exprVarName});
    <#list varDclns as varDcln>
    memorize_h("${varDcln.name?j_string}", ${varDcln.name});
    </#list>
    return ${exprVarName};
}

public function main() returns error? {
    any|error ${exprVarName} = trap stmts();
     if (${exprVarName} is error){
        printerr(${exprVarName});
        return ${exprVarName};
    }
}
