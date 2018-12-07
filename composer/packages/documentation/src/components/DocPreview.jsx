/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the ''License''); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * ''AS IS'' BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import React from 'react';
import Documentation from './Documentation';
import { ASTUtil } from '@ballerina/ast-model'

export default class DocPreview extends React.Component {
    getDocumentationDetails(node) {
        let parameters = {};
        if(this[`_get${node.kind}Parameters`]) {
            parameters = this[`_get${node.kind}Parameters`](node);
        }

        let returnParameter;
        if (node.returnTypeNode) {
            returnParameter = {
                type: ASTUtil.genSource(node.returnTypeNode),
            };
        }

        const { markdownDocumentationAttachment: mdDoc } = node;
        let description;
        if (mdDoc) {
            description = mdDoc.documentation;
            mdDoc.parameters.map((param) => {
                const name = param.parameterName.value;
                parameters[name].description = param.parameterDocumentation;
            });

            if (mdDoc.returnParameterDocumentation) {
                returnParameter.description = mdDoc.returnParameterDocumentation;
            }
        }

        let kind = node.kind;
        if (node.typeNode) {
            kind = node.typeNode.kind;
        }

        const documentationDetails = {
            kind: kind,
            title: node.name.value,
            description,
            parameters,
            returnParameter
        };

        return documentationDetails;
    }

    _getFunctionParameters(node) {
        const parameters = {};
        node.parameters.forEach(param => {
            if(!(param.name && param.name.value)) {
                return;
            }

            parameters[param.name.value] = {
                name: param.name.value,
                type: ASTUtil.genSource(param.typeNode),
            };
        });
        node.defaultableParameters.forEach(param => {
            parameters[param.variable.name.value] = {
                name: param.variable.name.value,
                type: ASTUtil.genSource(param.variable.typeNode),
                defaultValue: param.variable.initialExpression.value,
            };
        });

        if(node.restParameters) {
            parameters[node.restParameters.name.value] = {
                name: node.restParameters.name.value,
                type: `${ASTUtil.genSource(node.restParameters.typeNode)}...`,
            }
        }

        return parameters;
    }

    _getTypeDefinitionParameters(node) {
        const parameters = {};
        node.typeNode.fields.forEach(field => {
            parameters[field.name.value] = {
                name: field.name.value,
                type: ASTUtil.genSource(field.typeNode),
                defaultValue: field.initialExpression ? field.initialExpression.value: "",
            };
        });
        return parameters;
    }

    render() {
        const docElements = [];
        this.props.ast.topLevelNodes.forEach(node => {
            const documentables = ['Function', 'Service', 'TypeDefinition', 'Variable', 'Endpoint'];
            if (!documentables.includes(node.kind)) {
                return;
            }

            // Skip anon nodes related to service definitions
            if (node.kind === "TypeDefinition" && node.service) {
                return;
            }
            if (node.kind === "Variable" && node.service) {
                return;
            }

            try {
                const docDetails = this.getDocumentationDetails(node);
                docElements.push(
                    <Documentation docDetails={docDetails} />
                );
            } catch (e) {
                console.log(`error when getting doc details for ${node.id}`);
                console.error(e);
            }
        });
        return docElements;
    }
}
