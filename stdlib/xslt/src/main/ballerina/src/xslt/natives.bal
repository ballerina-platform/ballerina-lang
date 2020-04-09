// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/java;

# Transforms the single-rooted XML content to another XML/HTML/plain text using XSL transformations.
# ```ballerina
# xml|error target = xslt:transform(sourceXml, xsl);
# ```
#
# + input - An XML object, which needs to be transformed
# + xsl - The XSL style sheet represented in an XML object
# + return - The transformed result represented in an XML object or else an `error` if the given `XML` object
#            can't be transformed
public function transform(xml input, xml xsl) returns xml|error = @java:Method {
    class: "org.ballerinalang.xslt.XsltTransformer"
} external;
