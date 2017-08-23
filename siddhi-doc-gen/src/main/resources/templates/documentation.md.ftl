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
# API Docs

<#list metaData as namespace>
## ${namespace.name?capitalize}

<#list namespace.extensionMap as extensionType, extensionsList>
<#list extensionsList as extension>
### ${extension.name} *(<@utils.renderLinkToExtensionTypeDoc extensionType=extensionType/>)*

<p style="word-wrap: break-word">${formatDescription(extension.description)}</p>

<@utils.renderHeadingFourWithStylesOnly heading="Syntax"/>

<#if [EXTENSION_TYPE.FUNCTION, EXTENSION_TYPE.ATTRIBUTE_AGGREGATOR]?seq_index_of(extensionType) != -1>
```
<#if extension.returnAttributes??><#list extension.returnAttributes><<#items as returnAttribute>${returnAttribute.type?join("|", "")}</#items>> </#list></#if><#if namespace.name != CONSTANTS.CORE_NAMESPACE>${namespace.name}:</#if>${extension.name}(<#list extension.parameters><#items as parameter><${parameter.type?join("|", "")}> ${parameter.name}<#sep>, </#items></#list>)
```
<#elseif [EXTENSION_TYPE.STREAM_PROCESSOR, EXTENSION_TYPE.STREAM_FUNCTION, EXTENSION_TYPE.WINDOW]?seq_index_of(extensionType) != -1>
```
<#if namespace.name != CONSTANTS.CORE_NAMESPACE>${namespace.name}:</#if>${extension.name}(<#list extension.parameters><#items as parameter><${parameter.type?join("|", "")}> ${parameter.name}<#sep>, </#items></#list>)
```
<#elseif [EXTENSION_TYPE.SOURCE, EXTENSION_TYPE.SINK]?seq_index_of(extensionType) != -1>
```
@${extensionType?lower_case}(type="${extension.name}"<#list extension.parameters as parameter>, ${parameter.name}="<${parameter.type?join("|", "")}>"</#list>, @map(...)))
```
<#elseif extensionType == EXTENSION_TYPE.SOURCE_MAPPER>
```
@source(..., @map(type="${extension.name}"<#list extension.parameters as parameter>, ${parameter.name}="<${parameter.type?join("|", "")}>"</#list>)
```
<#elseif extensionType == EXTENSION_TYPE.SINK_MAPPER>
```
@sink(..., @map(type="${extension.name}"<#list extension.parameters as parameter>, ${parameter.name}="<${parameter.type?join("|", "")}>"</#list>)
```
<#elseif extensionType == EXTENSION_TYPE.STORE>
```
@Store(type="${extension.name}"<#list extension.parameters as parameter>, ${parameter.name}="<${parameter.type?join("|", "")}>"</#list>)
@PrimaryKey("PRIMARY_KEY")
@Index("INDEX")
```
<#elseif extensionType == EXTENSION_TYPE.SCRIPT>
```
define function <FunctionName>[${extension.name}] return <type> {
    // Script code
};
```
</#if>
<#list extension.parameters>

<@utils.renderHeadingFiveWithStylesOnly heading="Query Parameters"/>

<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Data Types</th>
        <th>Optional</th>
        <th>Dynamic</th>
    </tr>
    <#items as parameter>
    <tr>
        <td style="vertical-align: top">${parameter.name}</td>
        <td style="vertical-align: top; word-wrap: break-word">${formatDescription(parameter.description)}</td>
        <td style="vertical-align: top">${parameter.defaultValue}</td>
        <td style="vertical-align: top">${parameter.type?join("<br>", "")}</td>
        <td style="vertical-align: top"><#if parameter.optional>Yes<#else>No</#if></td>
        <td style="vertical-align: top"><#if parameter.dynamic>Yes<#else>No</#if></td>
    </tr>
    </#items>
</table>
</#list>
<#list extension.systemParameters>

<@utils.renderHeadingFourWithStylesOnly heading="System Parameters"/>

<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Parameters</th>
    </tr>
    <#items as systemParameter>
    <tr>
        <td style="vertical-align: top">${systemParameter.name}</td>
        <td style="vertical-align: top; word-wrap: break-word">${formatDescription(systemParameter.description)}</td>
        <td style="vertical-align: top">${systemParameter.defaultValue}</td>
        <td style="vertical-align: top">${systemParameter.possibleParameters?join("<br>", "")}</td>
    </tr>
    </#items>
</table>
</#list>
<#if [EXTENSION_TYPE.STREAM_PROCESSOR, EXTENSION_TYPE.STREAM_FUNCTION]?seq_index_of(extensionType) != -1>
<#list extension.returnAttributes>
<@utils.renderHeadingFourWithStylesOnly heading="Extra Return Attributes"/>

<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Possible Types</th>
    </tr>
    <#items as returnAttribute>
    <tr>
        <td style="vertical-align: top">${returnAttribute.name}</td>
        <td style="vertical-align: top; word-wrap: break-word">${formatDescription(returnAttribute.description)}</td>
        <td style="vertical-align: top">${returnAttribute.type?join("<br>", "")}</td>
    </tr>
    </#items>
</table>
</#list>
</#if>

<#list extension.examples>
<@utils.renderHeadingFourWithStylesOnly heading="Examples"/>

<#items as example>
<@utils.renderHeadingFiveWithStylesOnly heading=("Example " + (example?index + 1))/>

```
${example.syntax}
```
<p style="word-wrap: break-word">${formatDescription(example.description)}</p>

</#items>
</#list>
</#list>
</#list>
</#list>
