<#--
  ~ Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ WSO2 Inc. licenses this file to you under the Apache License,
  ~ Version 2.0 (the "License"); you may not use this file except
  ~ in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied. See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  -->
<#import "utils.ftl" as utils>
<#assign skipMaxHeadingLevel = -1>
<#if extensionRepositoryName == CONSTANTS.FREEMARKER_SIDDHI_REPOSITORY_ARTIFACT_ID>
    <#assign repositoryOwner = "wso2">
<#else>
    <#assign repositoryOwner = "wso2-extensions">
    <#assign extensionRepositoryName = extensionRepositoryName?replace(CONSTANTS.FREEMARKER_EXTENSION_REPOSITORY_PARENT_POSTFIX, "", "r")>
</#if>
<#macro renderLine line>
${line}
<#if line?starts_with(CONSTANTS.FREEMARKER_FEATURES_HEADING)>
<#assign skipMaxHeadingLevel = line?index_of(" ")>

<#if metaData??>
<#list metaData as namespace>
<#list namespace.extensionMap as extensionType, extensionsList>
<#list extensionsList as extension>
* <a target="_blank" href="https://${repositoryOwner}.github.io/${extensionRepositoryName}/api/${latestDocumentationVersion}/#<@utils.getHTMLIDForHeading heading=(extension.name + "-" + extensionType)/>">${extension.name}</a> *<@utils.renderLinkToExtensionTypeDoc extensionType=extensionType/>*<br><div style="padding-left: 1em;"><p>${formatDescription(extension.description)}</p></div>
</#list>
</#list>
<#else>
No Features Currently Available
</#list>
</#if>

<#elseif line?starts_with(CONSTANTS.FREEMARKER_LATEST_API_DOCS_HEADING)>
<#assign skipMaxHeadingLevel = line?index_of(" ")>

Latest API Docs is <a target="_blank" href="https://${repositoryOwner}.github.io/${extensionRepositoryName}/api/${latestDocumentationVersion}">${latestDocumentationVersion}</a>.

</#if>
</#macro>
<#list inputFileLines as inputFileLine>
<#if skipMaxHeadingLevel == -1>
<@renderLine line=inputFileLine/>
<#elseif inputFileLine?starts_with("#") && skipMaxHeadingLevel gte inputFileLine?index_of(" ")>
<#assign skipMaxHeadingLevel = -1>
<@renderLine line=inputFileLine/>
</#if>
</#list>
