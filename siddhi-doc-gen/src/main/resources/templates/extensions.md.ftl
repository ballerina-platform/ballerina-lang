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
# Siddhi Extensions

## Available Extensions

Following are some prewritten extensions that are supported with Siddhi;

<#macro displayRepositoriesList title extensionRepositories>
<#list extensionRepositories>
### ${title}
<#items as extensionRepository>
1. <a target="_blank" href="https://${extensionsOwner}.github.io/${extensionRepository}">${extensionRepository?replace(CONSTANTS.GITHUB_GPL_EXTENSION_REPOSITORY_PREFIX, "", "rf")?replace(CONSTANTS.GITHUB_APACHE_EXTENSION_REPOSITORY_PREFIX, "", "rf")?replace("-", " ")}</a>
</#items>
</#list>

</#macro>
<@displayRepositoriesList title="Extensions released under Apache 2.0 License" extensionRepositories=apacheExtensionRepositories/>
<@displayRepositoriesList title="Extensions released under GPL License" extensionRepositories=gplExtensionRepositories/>

## Extension Repositories

All the extension repositories maintained by WSO2 can be found <a target="_blank" href="https://github.com/${extensionsOwner}/?utf8=%E2%9C%93&q=siddhi&type=&language=">here</a>
