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
# API Docs

<#list metaData as namespace>
## ${namespace.name?capitalize}

<#list namespace.extensionMap as extensionType, extensionsList>
<#list extensionsList as extension>
### ${extension.name} _(${extensionType})_

<p style="word-wrap: break-word">${formatDescription(extension.description)}</p>

#### Syntax

<#if ["Function", "Attribute Aggregator"]?seq_index_of(extensionType) != -1>
```
<#if extension.returnAttributes??><#list extension.returnAttributes><<#items as returnAttribute>${returnAttribute.type?join("|", "")}</#items>> </#list></#if>${extension.name}(<#list extension.parameters><#items as parameter><${parameter.type?join("|", "")}> ${parameter.name}<#sep>, </#items></#list>)
```
<#elseif ["Stream Processor", "Stream Function", "Window"]?seq_index_of(extensionType) != -1>
```
${extension.name}(<#list extension.parameters><#items as parameter><${parameter.type?join("|", "")}> ${parameter.name}<#sep>, </#items></#list>)
```
<#elseif ["Source", "Sink"]?seq_index_of(extensionType) != -1>
```
@${extensionType?lower_case}(type="${extension.name}"<#list extension.parameters as parameter>, ${parameter.name}="<${parameter.type?join("|", "")}>"</#list>, @map(...)))
```
<#elseif extensionType == "Source Mapper">
```
@source(..., @map(type="${extension.name}"<#list extension.parameters as parameter>, ${parameter.name}="<${parameter.type?join("|", "")}>"</#list>)
```
<#elseif extensionType == "Sink Mapper">
```
@sink(..., @map(type="${extension.name}"<#list extension.parameters as parameter>, ${parameter.name}="<${parameter.type?join("|", "")}>"</#list>)
```
<#elseif extensionType == "Store">
```
@Store(type="${extension.name}"<#list extension.parameters as parameter>, ${parameter.name}="<${parameter.type?join("|", "")}>"</#list>)
@PrimaryKey("PRIMARY_KEY")
@Index("INDEX")
```
</#if>

<#list extension.parameters>
##### Query Parameters

<table>
    <tr>
        <th>Name</th>
        <th>Description</th>
        <th>Default Value</th>
        <th>Possible Data Types</th>
        <th>Optional</th>
        <th>Dynamic</th>
    </tr>
    <#items as parameter>
    <tr>
        <td valign="top">${parameter.name}</td>
        <td valign="top"><p style="word-wrap: break-word">${formatDescription(parameter.description)}</p></td>
        <td valign="top">${parameter.defaultValue}</td>
        <td valign="top">${parameter.type?join("<br>", "")}</td>
        <td valign="top"><#if parameter.optional>Yes<#else>No</#if></td>
        <td valign="top"><#if parameter.dynamic>Yes<#else>No</#if></td>
    </tr>
    </#items>
</table>
</#list>

<#list extension.systemParameters>
#### System Parameters

<table>
    <tr>
        <th>Name</th>
        <th>Description</th>
        <th>Default Value</th>
        <th>Possible Parameters</th>
    </tr>
    <#items as systemParameter>
    <tr>
        <td valign="top">${systemParameter.name}</td>
        <td valign="top"><p style="word-wrap: break-word">${formatDescription(systemParameter.description)}</p></td>
        <td valign="top">${systemParameter.defaultValue}</td>
        <td valign="top">${systemParameter.possibleParameters?join("<br>", "")}</td>
    </tr>
    </#items>
</table>
</#list>

<#if ["Stream Processor", "Stream Function"]?seq_index_of(extensionType) != -1>
<#list extension.returnAttributes>
#### Extra Return Attributes

<table>
    <tr>
        <th>Name</th>
        <th>Description</th>
        <th>Possible Types</th>
    </tr>
    <#items as returnAttribute>
    <tr>
        <td valign="top">${returnAttribute.name}</td>
        <td valign="top"><p style="word-wrap: break-word">${formatDescription(returnAttribute.description)}</p></td>
        <td valign="top">${returnAttribute.type?join("<br>", "")}</td>
    </tr>
    </#items>
</table>
</#list>
</#if>

<#list extension.examples>
#### Examples

<#items as example>
##### Example ${example?index + 1}

```
${example.syntax}
```
<p style="word-wrap: break-word">${formatDescription(example.description)}</p>

</#items>
</#list>
</#list>
</#list>
</#list>
