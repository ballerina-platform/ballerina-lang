<#-- @ftlvariable name="" type="io.ballerina.shell.invoker.classload.context.ClassLoadContext" -->
<#list imports as import>
    ${import}
</#list>

<#list moduleDclns as dcln>
    ${dcln}
</#list>

function printerr(any|error value) { }
function println(any|error... values) { }
function recall_h(string name) returns any|error { }
function memorize_h(string name, any|error value) { }
function sprintf(string template, any|error... values) returns string { return ""; }

<#list varDclns as varDcln>
<#if !varDcln.new>
<#if varDcln.isAny()>
${varDcln.type} ${varDcln.name} = // value
<${varDcln.type}> checkpanic recall_h("x");
<#else>
${varDcln.type} ${varDcln.name} = // value
<${varDcln.type}> recall_h("x");
</#if>
</#if>
</#list>

public function main() returns error? {
    // Redefine to restrict user
    <#list varDclns as varDcln>
    <#if !varDcln.new>
    <#if varDcln.isAny()>
    ${varDcln.type} ${varDcln.name} = // value
    <${varDcln.type}> checkpanic recall_h("x");
    <#else>
    ${varDcln.type} ${varDcln.name} = // value
    <${varDcln.type}> recall_h("x");
    </#if>
    </#if>
    </#list>

    ${lastVarDcln}

    _ = java:JavaClassNotFoundError;
}
