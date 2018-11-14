/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import _ from 'lodash';
import Node from './tree/node';

const isRetry = n => n.kind === 'Retry';
const anonTypes = {};

// TODO: Move this to a generic place.
function requireAll(requireContext) {
    const comp = {};
    requireContext.keys().forEach((item) => {
        const module = requireContext(item);
        if (module.default) {
            comp[module.default.name] = module.default;
        }
    });
    return comp;
}

const treeNodes = requireAll(require.context('./tree/', true, /\.js$/));

/**
 * A utill class to build the client side AST from serialized JSON.
 *
 * @class TreeBuilder
 */
class TreeBuilder {

    /**
     * Will convert any branch of json serialized ballerina AST tree to a node branch
     * of client side model.
     *
     * @static
     * @param {Object} json Serialized json of a ast tree or branch.
     * @param {Node} parent Parent node.
     * @returns {Node} object tree of node elements.
     * @memberof TreeBuilder
     */
    static build(json, parent, parentKind) {
        let childName;
        // 1. Backend API will send a serialized json of the AST tree.
        // 2. We consider all the json objects in the serialized json as nodes.
        // 3. If a node is found we bind the node objects prototype to that.
        // 4. If there is a more specific node defined based on node kind we will use
        //    that object instead of generic object.

        // Special case node creation with kind.
        let node;
        const kind = json.kind;
        if (kind && treeNodes[kind + 'Node']) {
            node = new (treeNodes[kind + 'Node'])();
        } else {
            node = new Node();
        }
        // with the following loop we will recursivle dive in to the child nodes and convert.
        for (childName in json) {
            // if child name is position || whitespace skip convection.
            if (childName !== 'position' && childName !== 'ws') {
                const child = json[childName];
                if (_.isPlainObject(child) && child.kind) {
                    json[childName] = TreeBuilder.build(child, node, kind);
                } else if (child instanceof Array) {
                    for (let i = 0; i < child.length; i++) {
                        const childItem = child[i];
                        if (kind === 'CompilationUnit' && childItem.kind === 'Function' && childItem.lambda) {
                            child.splice(i, 1);
                            i--;
                        } else if (_.isPlainObject(childItem) && childItem.kind) {
                            child[i] = TreeBuilder.build(childItem, node, kind);
                        }
                    }
                }
            }
        }

        TreeBuilder.modifyNode(json, parentKind);

        json.parent = parent;
        Object.assign(node, json);
        node.setChildrenCompoundStatus();
        return node;
    }

    static modifyNode(node, parentKind) {
        const kind = node.kind;
        if (kind === 'If') {
            if (node.elseStatement) {
                node.ladderParent = true;
            }

            if (node.ws && node.ws.length > 1 && node.ws[0].text === 'else' && node.ws[1].text === 'if') {
                node.isElseIfBlock = true;
            }
        }

        if (kind === 'Transaction' && node.condition && node.condition.value) {
            const retry = (node.failedBody ? node.failedBody.statements.filter(isRetry)[0] : null) ||
                (node.committedBody ? node.committedBody.statements.filter(isRetry)[0] : null) ||
                node.transactionBody.statements.filter(isRetry)[0];
            if (retry) {
                retry.count = node.condition.value;
            }
        }

        if ((kind === 'XmlCommentLiteral' ||
                kind === 'XmlElementLiteral' ||
                kind === 'XmlTextLiteral' ||
                kind === 'XmlPiLiteral')
            && node.ws
            && node.ws[0]
            && node.ws[0].text.includes('xml')
            && node.ws[0].text.includes('`')) {
            node.root = true;
            node.startLiteral = node.ws[0].text;
        }

        if (parentKind === 'XmlElementLiteral' ||
            parentKind === 'XmlTextLiteral' ||
            parentKind === 'XmlPiLiteral') {
            node.inTemplateLiteral = true;
        }

        if (kind === 'XmlPiLiteral' && node.ws) {
            let startTagWS = {
                text: '<?',
                ws: '',
            };

            let endTagWS = {
                text: '?>',
                ws: '',
            };

            if (node.root && (node.ws.length > 1)) {
                node.ws.splice(1, 0, startTagWS);
                node.ws.splice(2, 0, endTagWS);
            }

            if (!node.root) {
                node.ws.splice(0, 0, startTagWS);
                node.ws.splice(node.ws.length, 0, endTagWS);
            }

            if (node.target && node.target.unescapedValue) {
                for (let i = 0; i < node.target.ws.length; i++) {
                    if (node.target.ws[i].text.includes('<?') &&
                        node.target.ws[i].text.includes(node.target.unescapedValue)) {
                        node.target.unescapedValue = node.target.ws[i].text.replace('<?', '');
                    }
                }
            }
        }

        if (kind === 'Annotation' && node.attachmentPoints && node.attachmentPoints.length <= 0) {
            node.noAttachmentPoints = true;
        }

        if (kind === 'AnnotationAttachment' && node.packageAlias.value === 'builtin') {
            node.builtin = true;
        }

        if (kind === 'Identifier') {
            if (node.literal) {
                node.valueWithBar = '^"' + node.value + '"';
            } else {
                node.valueWithBar = node.value;
            }
        }

        if (kind === 'Import') {
            if (node.alias
                && node.alias.value
                && node.packageName
                && node.packageName.length
                && (node.alias.value !== node.packageName[node.packageName.length - 1].value)) {
                node.userDefinedAlias = true;
            }

            if ((node.packageName.length === 2
                    && node.packageName[0].value === 'transactions'
                    && node.packageName[1].value === 'coordinator')
                || (node.alias
                    && node.alias.value
                    && node.alias.value.startsWith('.'))) {
                node.isInternal = true;
            }
        }

        if (parentKind === 'CompilationUnit' && (kind === 'Variable' || kind === 'Xmlns')) {
            node.global = true;
        }

        if (node.kind === 'VariableDef'
            && node.variable.typeNode
            && (node.variable.typeNode.kind === 'EndpointType')) {
            node.variable.endpoint = true;
            node.endpoint = true;
        }

        if (node.kind === 'Variable') {
            if (parentKind === 'ObjectType') {
                node.inObject = true;
            }

            if (node.typeNode && node.typeNode.isAnonType) {
                node.isAnonType = true;
            }

            if (node.initialExpression && node.initialExpression.async && node.ws) {
                for (let i = 0; i < node.ws.length; i++) {
                    if ((node.ws[i].text === 'start') && node.initialExpression.ws) {
                        node.initialExpression.ws.splice(0, 0, node.ws[i]);
                        node.ws.splice(i, 1);
                    }
                }
            }

            if (node.typeNode && node.typeNode.nullable && node.typeNode.ws) {
                for (let i = 0; i < node.typeNode.ws.length; i++) {
                    if (node.typeNode.ws[i].text === '?') {
                        node.typeNode.nullableOperatorAvailable = true;
                        break;
                    }
                }
            }

            if (node.typeNode && node.typeNode.ws && !node.ws) {
                node.noVisibleName = true;
            }

            if (node.ws) {
                for (let i = 0; i < node.ws.length; i++) {
                    if (node.ws[i].text === ';') {
                        node.endWithSemicolon = true;
                    }

                    if (node.ws[i].text === ',') {
                        node.endWithComma = true;
                    }
                }
            }
        }

        if (node.kind === 'Service') {
            if (!node.serviceTypeStruct) {
                node.isServiceTypeUnavailable = true;
            }

            if (!node.anonymousEndpointBind
                && node.boundEndpoints
                && node.boundEndpoints.length <= 0
                && !_.find(node.ws, (ws) => ws.text === 'bind')) {
                node.bindNotAvailable = true;
            }
        }

        // Mark the first argument ad a service endpoint.
        if (node.kind === 'Resource'
            && node.parameters[0]
            && node.parameters[0].ws
            && _.find(node.parameters[0].ws, (ws) => ws.text === 'endpoint')) {
            const endpointParam = node.parameters[0];
            const valueWithBar = endpointParam.name.valueWithBar || endpointParam.name.value;
            endpointParam.serviceEndpoint = true;
            endpointParam.name.setValue(endpointParam.name.getValue().replace('$', ''));
            endpointParam.name.valueWithBar = valueWithBar.replace('$', '');
        }

        // Add the positions for the join and timeout bodies.
        if (node.kind === 'ForkJoin') {
            if (node.joinBody) {
                node.joinBody.position = node.joinResultVar.position;
            }

            if (node.timeoutBody) {
                node.timeoutBody.position = node.timeOutExpression.position;
            }
        }

        // Check if sorrounded by curlies
        if ((node.kind === 'MatchPatternClause'
                || node.kind === 'MatchExpressionPatternClause')
            && node.ws
            && node.ws.length > 2) {
            node.withCurlies = true;
        }

        // Check if sorrounded by parantheses
        if (node.kind === 'ValueType') {
            if (node.ws && node.ws.length > 2) {
                node.withParantheses = true;
            }

            if (node.typeKind && node.typeKind === 'nil' && node.ws) {
                node.emptyParantheses = true;
            }

            if (node.nullable && node.ws) {
                for (let i = 0; i < node.ws.length; i++) {
                    if (node.ws[i].text === '?') {
                        node.nullableOperatorAvailable = true;
                    }
                }
            }
        }

        if (node.kind === 'UnionTypeNode' && node.ws) {
            if (node.ws.length > 2 && _.find(node.ws, (ws) => ws.text === '(')) {
                node.withParantheses = true;
            }

            for (let i = 0; i < node.memberTypeNodes.length; i++) {
                if (node.memberTypeNodes[i].nullable && _.find(node.ws, (ws) => ws.text === '?')) {
                    node.memberTypeNodes[i].nullableOperatorAvailable = true;
                }
            }
        }

        if (node.kind === 'Function') {
            if (node.returnTypeNode && node.returnTypeNode.ws && node.returnTypeNode.ws.length > 0) {
                node.hasReturns = true;
            }

            if (node.defaultableParameters) {
                for (let i = 0; i < node.defaultableParameters.length; i++) {
                    node.defaultableParameters[i].defaultable = true;
                    node.defaultableParameters[i].variable.defaultable = true;
                }
            }

            node.allParams = _.concat(node.parameters, node.defaultableParameters);
            node.allParams.sort((a, b) => {
                return (((a.position.endColumn > b.position.startColumn)
                    && (a.position.endLine === b.position.endLine))
                    || (a.position.endLine > b.position.endLine));
            });

            if (node.receiver && !node.receiver.ws) {

                if (_.find(node.ws, (ws) => ws.text === '::')
                    && node.receiver.typeNode
                    && node.receiver.typeNode.ws
                    && node.receiver.typeNode.ws.length > 0) {
                    node.objectOuterFunction = true;
                    if (node.receiver.typeNode.ws[0].text === 'function') {
                        node.receiver.typeNode.ws.splice(0, 1);
                    }
                    node.objectOuterFunctionTypeName = node.receiver.typeNode.typeName;
                } else {
                    node.noVisibleReceiver = true;
                }
            }

            if (node.restParameters && (node.allParams && node.allParams.length > 0)) {
                node.hasRestParams = true;
            }

            if (node.restParameters && node.restParameters.typeNode) {
                node.restParameters.typeNode.isRestParam = true;
            }
        }

        if (node.kind === 'TypeDefinition' && node.typeNode) {
            if (!node.ws) {
                node.notVisible = true;
            }

            if (node.name && node.name.value.startsWith('$anonType$')) {
                anonTypes[node.name.value] = node.typeNode;
            }

            if (node.typeNode.kind === 'ObjectType') {
                node.isObjectType = true;
                if (node.ws) {
                    for (let i = 0; i < node.ws.length; i++) {
                        if (node.ws[i].text === "abstract") {
                            node.isAbstractKeywordAvailable = true;
                        }
                    }
                }
            }

            if (node.typeNode.kind === 'RecordType') {
                node.isRecordType = true;
                if (node.ws) {
                    for (let i = 0; i < node.ws.length; i++) {
                        if (node.ws[i].text === 'record') {
                            node.isRecordKeywordAvailable = true;
                        }
                    }
                }
            }
        }

        if (node.kind === 'ObjectType' && node.initFunction) {
            if (!node.initFunction.ws) {
                node.initFunction.defaultConstructor = true;
            } else {
                node.initFunction.isConstructor = true;
            }
        }

        if (node.kind === 'RecordType' && node.restFieldType) {
            node.isRestFieldAvailable = true;
        }

        if (node.kind === 'TypeInitExpr') {
            if (node.expressions.length <= 0) {
                node.noExpressionAvailable = true;
            }

            if (node.ws) {
                for (let i = 0; i < node.ws.length; i++) {
                    if (node.ws[i].text === "(") {
                        node.hasParantheses = true;
                        break;
                    }
                }
            }

            if (!node.type) {
                node.noTypeAttached = true;
            } else {
                node.typeName = node.type.typeName;
            }
        }

        if (node.kind === 'Return' && node.expression && node.expression.kind === 'Literal') {
            if (node.expression.value === '()') {
                node.noExpressionAvailable = true;
            }

            if (node.expression.value === 'null') {
                node.expression.emptyParantheses = true;
            }
        }

        if (node.kind === "Documentation") {
            if (node.ws && node.ws.length > 1) {
                node.startDoc = node.ws[0].text;
            }

            for (let j = 0; j < node.attributes.length; j++) {
                let attribute = node.attributes[j];
                if (attribute.ws) {
                    for (let i = 0; i < attribute.ws.length; i++) {
                        let text = attribute.ws[i].text;
                        if (text.includes('{{') && !attribute.paramType) {
                            let lastIndex = text.indexOf('{{');
                            let paramType = text.substr(0, lastIndex);
                            let startCurl = text.substr(lastIndex, text.length);
                            attribute.ws[i].text = paramType;
                            attribute.paramType = paramType;
                            attribute.ws.splice((++i), 0, {text: startCurl, ws: "", static: false});
                        }
                    }
                }
            }
        }

        // Tag rest variable nodes
        if ((node.kind === 'Function' || node.kind === 'Resource') && node.restParameters) {
            node.restParameters.rest = true;
        }

        if (node.kind === 'PostIncrement') {
            node.operator = node.operatorKind + node.operatorKind;
        }

        if (node.kind === 'SelectExpression' && node.identifier) {
            node.identifierAvailable = true;
        }

        if (node.kind === 'StreamAction' && node.invokableBody && node.invokableBody.functionNode) {
            node.invokableBody.functionNode.isStreamAction = true;
        }

        if (node.kind === 'StreamingInput' && node.alias) {
            node.aliasAvailable = true;
        }

        if (node.kind === 'IntRangeExpr' && node.ws && node.ws.length > 0) {
            if (node.ws[0].text === '[') {
                node.isWrappedWithBracket = true;
            } else if (node.ws[0].text === '(') {
                node.isWrappedWithParenthesis = true;
            }
        }

        if (node.kind === 'FunctionType') {
            if (node.returnTypeNode && node.returnTypeNode.ws) {
                node.hasReturn = true;
            }

            if (node.ws && node.ws[0] && node.ws[0].text === '(') {
                node.withParantheses = true;
            }
        }

        if (node.kind === 'Literal' && parentKind !== 'StringTemplateLiteral') {
            if (node.ws && node.ws.length === 1 && node.ws[0] && node.ws[0].text) {
                node.value = node.ws[0].text;
            }

            if ((node.value === 'nil' || node.value === 'null')
                && node.ws
                && node.ws.length < 3
                && node.ws[0]
                && node.ws[0].text === '(') {
                node.emptyParantheses = true;
            }
        }

        if (node.kind === 'Foreach' && node.ws && _.find(node.ws, (ws) => ws.text === '(')) {
            node.withParantheses = true;
        }

        if (node.kind === 'Endpoint'
            && node.ws
            && _.find(node.ws, (ws) => ws.text === '=')) {
            node.isConfigAssignment = true;
        }

        if (node.kind === 'UserDefinedType') {
            if (node.ws && node.ws[0].text === '*' && node.ws[node.ws.length - 1].text === ";") {
                node.isTypeReference = true;
            }

            if (node.ws && node.nullable && _.find(node.ws, (ws) => ws.text === '?')) {
                node.nullableOperatorAvailable = true;
            }

            if (node.typeName && node.typeName.value && anonTypes[node.typeName.value]) {
                node.isAnonType = true;
                node.anonType = anonTypes[node.typeName.value];
                delete anonTypes[node.typeName.value];
            }
        }

        if (node.kind === 'ArrayType' && node.dimensions > 0 && node.ws) {
            node.dimensionAsString = "";
            let startingBracket;
            let endingBracket;
            let content = "";

            for (let j = 0; j < node.ws.length; j++) {
                if (node.ws[j].text === '[') {
                    startingBracket = node.ws[j];
                } else if (node.ws[j].text === ']') {
                    endingBracket = node.ws[j];
                    node.dimensionAsString += startingBracket.text + content +
                        endingBracket.ws + endingBracket.text;

                    content = "";
                    startingBracket = null;
                    endingBracket = null;
                } else if (node.ws[j].text === '?') {
                    node.dimensionAsString += node.ws[j].ws + node.ws[j].text;
                } else if (startingBracket) {
                    content += node.ws[j].ws + node.ws[j].text;
                }
            }
        }

        if (node.kind === 'Block' && node.ws && node.ws[0] && node.ws[0].text === 'else') {
            node.isElseBlock = true;
        }

        if (node.kind === 'FieldBasedAccessExpr' && node.ws && node.ws[0] && node.ws[0].text === '!') {
            node.errorLifting = true;
        }

        // Function to assign ws for template literals.
        let literalWSAssignForTemplates = function (currentWs, nextWs, literals, ws, wsStartLocation) {
            if (literals.length === (ws.length - wsStartLocation)) {
                for (let i = 0; i < literals.length; i++) {
                    if (literals[i].kind === 'Literal') {
                        if (!literals[i].ws) {
                            literals[i].ws = [];
                        }

                        if (ws[currentWs].text.includes('{{')) {
                            literals[i].ws.push(ws[currentWs]);
                            literals[i].value = ws[currentWs].text;
                            ws.splice(currentWs, 1);
                            literals[i].startTemplateLiteral = true;
                        } else if (ws[currentWs].text.includes('}}')) {
                            literals[i].ws.push(ws[currentWs]);
                            if (ws[nextWs].text.includes('{{')) {
                                literals[i].ws.push(ws[nextWs]);
                                literals[i].value = ws[nextWs].text;
                                literals[i].startTemplateLiteral = true;
                                ws.splice(nextWs, 1);
                            }
                            ws.splice(currentWs, 1);
                            literals[i].endTemplateLiteral = true;
                        }

                        if (i === (literals.length - 1)) {
                            literals[i].ws.push(ws[currentWs]);
                            literals[i].value = ws[currentWs].text;
                            literals[i].lastNodeValue = true;
                            ws.splice(currentWs, 1);
                        }
                    }
                }
            } else if ((literals.length - 1) === (ws.length - wsStartLocation)) {
                for (let i = 0; i < literals.length; i++) {
                    if (literals[i].kind === 'Literal') {
                        if (!literals[i].ws) {
                            literals[i].ws = [];
                        }

                        if (ws[currentWs].text.includes('{{')) {
                            literals[i].ws.push(ws[currentWs]);
                            literals[i].value = ws[currentWs].text;
                            ws.splice(currentWs, 1);
                            literals[i].startTemplateLiteral = true;
                        } else if (ws[currentWs].text.includes('}}')) {
                            literals[i].ws.push(ws[currentWs]);
                            if (ws[nextWs].text.includes('{{')) {
                                literals[i].ws.push(ws[nextWs]);
                                literals[i].value = ws[nextWs].text;
                                literals[i].startTemplateLiteral = true;
                                ws.splice(nextWs, 1);
                            }
                            ws.splice(currentWs, 1);
                            literals[i].endTemplateLiteral = true;
                        }
                    }
                }
            }
        };

        if (node.kind === 'StringTemplateLiteral'
            && node.ws
            && node.ws[0]
            && node.ws[0].text.includes('string')
            && node.ws[0].text.includes('`')) {
            node.startTemplate = node.ws[0].text;
            literalWSAssignForTemplates(1, 2, node.expressions, node.ws, 2);
        }

        if (kind === 'XmlCommentLiteral' && node.ws) {
            let length = node.ws.length;
            for (let i = 0; i < length; i++) {
                if (node.ws[i].text.includes('-->') && node.ws[i].text.length > 3) {
                    let ws = {
                        text: '-->',
                        ws: '',
                    };
                    node.ws[i].text = node.ws[i].text.replace('-->', '');
                    node.ws.splice(i + 1, 0, ws);
                    break;
                }
            }

            if (node.root) {
                literalWSAssignForTemplates(2, 3, node.textFragments, node.ws, 4);
            } else {
                literalWSAssignForTemplates(1, 2, node.textFragments, node.ws, 2);
            }
        }

        if (kind === 'ArrowExpr') {
            if (node.ws && node.ws.length > 0 && node.ws[0].text === '(') {
                node.hasParantheses = true;
            }

            for (let i = 0; i < node.parameters.length; i++) {
                node.parameters[i].arrowExprParam = true;
            }
        }

        if (kind === 'PatternStreamingInput' && node.ws && node.ws[0].text === '(') {
            node.enclosedInParenthesis = true;
        }

        if (kind === 'SelectClause' && !node.ws) {
            node.notVisible = true;
        }

        if (kind === 'OrderByVariable') {
            if (!node.ws) {
                node.noVisibleType = true;
            } else {
                node.typeString = node.ws[0].text;
            }
        }

        if (kind === 'Deprecated' && node.ws && node.ws.length > 0) {
            node.deprecatedStart = node.ws[0].text;
        }
    }

    static modify(tree, parentKind = null) {
        TreeBuilder.modifyNode(tree, parentKind);
        for (const childKeyVal of tree) {
            TreeBuilder.modify(childKeyVal[1], tree.kind);
        }
    }
}

export default TreeBuilder;
