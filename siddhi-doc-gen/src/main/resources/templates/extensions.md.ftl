# Siddhi Extensions

## Available Extensions

Siddhi currently have several prewritten extensions as follows;

<#macro displayRepositoriesList title extensionRepositories>
<#list extensionRepositories>
### ${title}
<#items as extensionRepository>
1. <a target="_blank" href="https://${extensionsOwner}.github.io/${extensionRepository}">${extensionRepository?replace("^siddhi-gpl-", "", "rf")?replace("^siddhi-", "", "rf")?replace("-", " ")?capitalize}</a>
</#items>
</#list>

</#macro>
<@displayRepositoriesList title="Extensions released under GPL License" extensionRepositories=gplExtensionRepositories/>
<@displayRepositoriesList title="Extensions released under Apache 2.0 License" extensionRepositories=apacheExtensionRepositories/>
