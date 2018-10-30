import React from 'react';
import Documentation from './Documentation';

export default class DocPreview extends React.Component {
    getDocumentationDetails(node) {
        const { markdownDocumentationAttachment: mdDoc } = node;

        let parameters = {};
        if(this[`_get${node.kind}Parameters`]) {
            parameters = this[`_get${node.kind}Parameters`](node);
        }

        const processedParameters = mdDoc.parameters.map((param) => {
            const name = param.parameterName.value
            const { type, defaultValue } =  parameters[name] || {};
            const description = param.parameterDocumentation;
            return {
                name, type, defaultValue, description
            };
        });

        let returnParameter;
        if (node.returnTypeNode) {
            returnParameter = {
                type: node.returnTypeNode.typeKind,
                description: mdDoc.returnParameterDocumentation,
            };
        }
        const documentationDetails = {
            kind: node.kind,
            title: node.name.value,
            description: mdDoc.documentation,
            parameters: processedParameters,
            returnParameter
        };
        return documentationDetails;
    }

    _getFunctionParameters(node) {
        const parameters = {};
        node.parameters.forEach(param => {
            parameters[param.name.value] = {
                name: param.name.value,
                type: param.typeNode.typeKind,
            };
        });
        node.defaultableParameters.forEach(param => {
            parameters[param.variable.name.value] = {
                name: param.variable.name.value,
                type: param.variable.typeNode.typeKind,
                defaultValue: param.variable.initialExpression.value,
            };
        });
        return parameters;
    }

    _getTypeDefinitionParameters(node) {
        const parameters = {};
        node.typeNode.fields.forEach(field => {
            parameters[field.name.value] = {
                name: field.name.value,
                type: field.typeNode.typeKind,
                defaultValue: field.initialExpression ? field.initialExpression.value: "",
            };
        });
        return parameters;
    }

    render() {
        const docElements = [];
        this.props.ast.topLevelNodes.forEach(node => {
            if(node.markdownDocumentationAttachment) {
                const docDetails = this.getDocumentationDetails(node);
                docElements.push(
                    <Documentation docDetails={docDetails}/>
                );
            }
        });
        return docElements;
    }
}
