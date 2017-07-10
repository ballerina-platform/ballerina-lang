<#list readMeFileLines as readMeFileLine>
${readMeFileLine}
</#list>

## API Docs:

<#if documentationFiles??>
<#list documentationFiles as documentationFile>
1. <a href="./api/${documentationFile}">${documentationFile?remove_ending(".md")}</a>
</#list>
</#if>
