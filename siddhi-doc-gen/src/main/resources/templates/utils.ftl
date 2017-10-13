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
<#macro getHTMLIDForHeading heading>${heading?lower_case?replace(" ", "-", "r")}</#macro>

<#macro renderLinkToExtensionTypeDoc extensionType><a target="_blank" href="${CONSTANTS.FREEMARKER_SIDDHI_HOME_PAGE}/documentation/siddhi-4.0/#<@getHTMLIDForHeading heading=extensionType/>">(${extensionType})</a></#macro>

<#macro renderHeadingOneWithStylesOnly heading><span id="<@getHTMLIDForHeading heading=heading/>" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 31.25px; font-weight: 300; margin: 0 0 40px 0">${heading}</span></#macro>

<#macro renderHeadingTwoWithStylesOnly heading><span id="<@getHTMLIDForHeading heading=heading/>" class="md-typeset" style="display: block; font-size: 25px; font-weight: 300; margin: 40px 0 16px 0">${heading}</span></#macro>

<#macro renderHeadingThreeWithStylesOnly heading><span id="<@getHTMLIDForHeading heading=heading/>" class="md-typeset" style="display: block; font-size: 20px;">${heading}</span></#macro>

<#macro renderHeadingFourWithStylesOnly heading><span id="<@getHTMLIDForHeading heading=heading/>" class="md-typeset" style="display: block; font-weight: bold;">${heading}</span></#macro>

<#macro renderHeadingFiveWithStylesOnly heading><span id="<@getHTMLIDForHeading heading=heading/>" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">${heading?upper_case}</span></#macro>
