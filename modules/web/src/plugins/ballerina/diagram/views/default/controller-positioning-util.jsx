/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React from 'react';
import { COMMANDS } from 'plugins/ballerina/constants';
import Button from '../../../interactions/button';
import LifelineButton from '../../../interactions/lifeline-button';
import WorkerButton from '../../../interactions/worker-button';
import Area from '../../../interactions/area';
import Menu from '../../../interactions/menu';
import Item from '../../../interactions/item';
import TopLevelElements from '../../../tool-palette/item-provider/compilation-unit-tools';
import WorkerTools from '../../../tool-palette/item-provider/worker-tools';
import LifelineTools from '../../../tool-palette/item-provider/lifeline-tools';
import TreeUtil from './../../../model/tree-util';

class ControllerPositioningUtil {

    setContext(context) {
        this.context = context;
    }

    setMode(mode) {
        this.mode = mode;
    }

    setConfig(config) {
        this.config = config;
    }

    /**
     * Calculate position of Action node controllers.
     *
     * @param {object} node
     *
     */
    positionActionNodeControllers(node) {
        if (node.id !== node.parent.initAction.id) {
            return this.positionFunctionNodeControllers(node);
        }
        return undefined;
    }


    /**
     * Calculate position of Annotation node controllers.
     *
     * @param {object} node
     *
     */
    positionAnnotationNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of AnnotationAttachment node controllers.
     *
     * @param {object} node
     *
     */
    positionAnnotationAttachmentNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of AnnotationAttribute node controllers.
     *
     * @param {object} node
     *
     */
    positionAnnotationAttributeNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Catch node controllers.
     *
     * @param {object} node
     *
     */
    positionCatchNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of CompilationUnit node controllers.
     *
     * @param {object} node
     *
     */
    positionCompilationUnitNodeControllers(node) {
        const offsetX = node.getPackageDeclaration()
                || node.viewState.packageDefExpanded
                ? 320
                : 40;
        const x = this.config.panel.wrapper.gutter.h + offsetX;
        const y = this.config.panel.wrapper.gutter.v;
        const w = 50;
        const h = 50;
        if (node.isEmpty()) {
            // y = (node.viewState.bBox.h / 2);
        }
        const items = this.convertToAddItems(TopLevelElements, node);
        const addBtn = (
            <Area bBox={{ x, y, w, h }}>
                <Button
                    buttonX={0}
                    buttonY={0}
                    showAlways
                    buttonRadius={12}
                >
                    <Menu>
                        {items}
                    </Menu>
                </Button>
            </Area>
        );

        const modeBtn = (
            <Area bBox={{ x: x + 40, y, w, h }}>
                <Button
                    icon={this.mode === 'default' ? 'default-view' : 'action-view'}
                    buttonX={0}
                    buttonY={0}
                    showAlways
                    hideIconBackground
                    buttonColor={'#eee'}
                    buttonIconColor={'black'}
                    buttonRadius={12}
                    onClick={() => {
                        this.context.command.dispatch(COMMANDS.DIAGRAM_MODE_CHANGE,
                            { mode: this.mode === 'default' ? 'action' : 'default' });
                    }}
                    tooltip={`switch to ${this.mode === 'default' ? 'action' : 'default'} mode`}
                />
            </Area>
        );
        return ([addBtn, modeBtn]);
    }

    convertToAddItems(tools, node, index) {
        return tools.map((element) => {
            const data = {
                node,
                item: element,
            };

            if (element.seperator) {
                return <hr />;
            }

            return (
                <Item
                    label={element.name}
                    icon={`fw fw-${element.icon}`}
                    callback={(menuItem) => {
                        const newNode = menuItem.item.nodeFactoryMethod();
                        menuItem.node.acceptDrop(newNode);
                    }}
                    data={data}
                />
            );
        });
    }

    /**
     * Calculate position of Connector node controllers.
     *
     * @param {object} node
     *
     */
    positionConnectorNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Enum node controllers.
     *
     * @param {object} node
     *
     */
    positionEnumNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Enumerator node controllers.
     *
     * @param {object} node
     *
     */
    positionEnumeratorNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Function node controllers.
     *
     * @param {object} node
     *
     */
    positionFunctionNodeControllers(node) {
        const y = node.viewState.components.defaultWorker.y - 20;
        let x = node.viewState.components.defaultWorker.x + node.viewState.components.defaultWorker.w +
            this.config.lifeLine.gutter.h;
        let workerButton = <span />;
        if (node.workers.length > 0) {
            x = node.workers[node.workers.length - 1].viewState.bBox.x +
                node.workers[node.workers.length - 1].viewState.bBox.w +
                this.config.lifeLine.gutter.h;
        } else {
            // add default worker plus.
            workerButton = this.getWorkerButton(node.viewState.components.defaultWorker, node.body);
        }

        // Set the size of the connector declarations
        const statements = node.body.statements;
        if (statements instanceof Array) {
            statements.forEach((statement) => {
                if (TreeUtil.isEndpointTypeVariableDef(statement)) {
                    x = statement.viewState.bBox.w + statement.viewState.bBox.x + this.config.lifeLine.gutter.h;
                }
            });
        }

        if (TreeUtil.isInitFunction(node)) {
            return <span />;
        }

        const w = 50;
        const h = 50;

        const items = this.convertToAddItems(LifelineTools, node);

        return [
            <LifelineButton bBox={{ x, y, w, h }} model={node} items={items} />,
            workerButton];
    }


    /**
     * Calculate position of Identifier node controllers.
     *
     * @param {object} node
     *
     */
    positionIdentifierNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Import node controllers.
     *
     * @param {object} node
     *
     */
    positionImportNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Package node controllers.
     *
     * @param {object} node
     *
     */
    positionPackageNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of PackageDeclaration node controllers.
     *
     * @param {object} node
     *
     */
    positionPackageDeclarationNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of RecordLiteralKeyValue node controllers.
     *
     * @param {object} node
     *
     */
    positionRecordLiteralKeyValueNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Resource node controllers.
     *
     * @param {object} node
     *
     */
    positionResourceNodeControllers(node) {
        // Not implemented.
        return this.positionFunctionNodeControllers(node);
    }


    /**
     * Calculate position of Retry node controllers.
     *
     * @param {object} node
     *
     */
    positionRetryNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Service node controllers.
     *
     * @param {object} node
     *
     */
    positionServiceNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Struct node controllers.
     *
     * @param {object} node
     *
     */
    positionStructNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Variable node controllers.
     *
     * @param {object} node
     *
     */
    positionVariableNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Worker node controllers.
     *
     * @param {object} node
     *
     */
    positionWorkerNodeControllers(node) {
        if (node.parent) { // TODO This is a bug fix need to find the root cause.
            return this.getWorkerButton(node.viewState.bBox, node.body);
        }
        return undefined;
    }

    getWorkerButton(bBox, node) {
        const x = bBox.x + 48;
        const y = bBox.y + bBox.h - 40;
        const w = 50;
        const h = 50;

        const items = this.convertToAddItems(WorkerTools, node);
        // Not implemented.
        return (
            <WorkerButton bBox={{ x, y, w, h }} model={node} items={items} />
        );
    }


    /**
     * Calculate position of Xmlns node controllers.
     *
     * @param {object} node
     *
     */
    positionXmlnsNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Transformer node controllers.
     *
     * @param {object} node
     *
     */
    positionTransformerNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of AnnotationAttachmentAttribute node controllers.
     *
     * @param {object} node
     *
     */
    positionAnnotationAttachmentAttributeNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of AnnotationAttachmentAttributeValue node controllers.
     *
     * @param {object} node
     *
     */
    positionAnnotationAttachmentAttributeValueNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of ArrayLiteralExpr node controllers.
     *
     * @param {object} node
     *
     */
    positionArrayLiteralExprNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of BinaryExpr node controllers.
     *
     * @param {object} node
     *
     */
    positionBinaryExprNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of ConnectorInitExpr node controllers.
     *
     * @param {object} node
     *
     */
    positionConnectorInitExprNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of FieldBasedAccessExpr node controllers.
     *
     * @param {object} node
     *
     */
    positionFieldBasedAccessExprNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of IndexBasedAccessExpr node controllers.
     *
     * @param {object} node
     *
     */
    positionIndexBasedAccessExprNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Invocation node controllers.
     *
     * @param {object} node
     *
     */
    positionInvocationNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Lambda node controllers.
     *
     * @param {object} node
     *
     */
    positionLambdaNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Literal node controllers.
     *
     * @param {object} node
     *
     */
    positionLiteralNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of RecordLiteralExpr node controllers.
     *
     * @param {object} node
     *
     */
    positionRecordLiteralExprNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of SimpleVariableRef node controllers.
     *
     * @param {object} node
     *
     */
    positionSimpleVariableRefNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of StringTemplateLiteral node controllers.
     *
     * @param {object} node
     *
     */
    positionStringTemplateLiteralNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of TernaryExpr node controllers.
     *
     * @param {object} node
     *
     */
    positionTernaryExprNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of TypeofExpression node controllers.
     *
     * @param {object} node
     *
     */
    positionTypeofExpressionNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of TypeCastExpr node controllers.
     *
     * @param {object} node
     *
     */
    positionTypeCastExprNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of TypeConversionExpr node controllers.
     *
     * @param {object} node
     *
     */
    positionTypeConversionExprNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of UnaryExpr node controllers.
     *
     * @param {object} node
     *
     */
    positionUnaryExprNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of XmlQname node controllers.
     *
     * @param {object} node
     *
     */
    positionXmlQnameNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of XmlAttribute node controllers.
     *
     * @param {object} node
     *
     */
    positionXmlAttributeNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of XmlQuotedString node controllers.
     *
     * @param {object} node
     *
     */
    positionXmlQuotedStringNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of XmlElementLiteral node controllers.
     *
     * @param {object} node
     *
     */
    positionXmlElementLiteralNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of XmlTextLiteral node controllers.
     *
     * @param {object} node
     *
     */
    positionXmlTextLiteralNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of XmlCommentLiteral node controllers.
     *
     * @param {object} node
     *
     */
    positionXmlCommentLiteralNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of XmlPiLiteral node controllers.
     *
     * @param {object} node
     *
     */
    positionXmlPiLiteralNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Abort node controllers.
     *
     * @param {object} node
     *
     */
    positionAbortNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Assignment node controllers.
     *
     * @param {object} node
     *
     */
    positionAssignmentNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Bind node controllers.
     *
     * @param {object} node
     *
     */
    positionBindNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Block node controllers.
     *
     * @param {object} node
     *
     */
    positionBlockNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Break node controllers.
     *
     * @param {object} node
     *
     */
    positionBreakNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Next node controllers.
     *
     * @param {object} node
     *
     */
    positionNextNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of ExpressionStatement node controllers.
     *
     * @param {object} node
     *
     */
    positionExpressionStatementNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of ForkJoin node controllers.
     *
     * @param {object} node
     *
     */
    positionForkJoinNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of If node controllers.
     *
     * @param {object} node
     *
     */
    positionIfNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Reply node controllers.
     *
     * @param {object} node
     *
     */
    positionReplyNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Return node controllers.
     *
     * @param {object} node
     *
     */
    positionReturnNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Comment node controllers.
     *
     * @param {object} node
     *
     */
    positionCommentNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Throw node controllers.
     *
     * @param {object} node
     *
     */
    positionThrowNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Transaction node controllers.
     *
     * @param {object} node
     *
     */
    positionTransactionNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of Try node controllers.
     *
     * @param {object} node
     *
     */
    positionTryNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of VariableDef node controllers.
     *
     * @param {object} node
     *
     */
    positionVariableDefNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of While node controllers.
     *
     * @param {object} node
     *
     */
    positionWhileNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of WorkerReceive node controllers.
     *
     * @param {object} node
     *
     */
    positionWorkerReceiveNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of WorkerSend node controllers.
     *
     * @param {object} node
     *
     */
    positionWorkerSendNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of ArrayType node controllers.
     *
     * @param {object} node
     *
     */
    positionArrayTypeNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of BuiltInRefType node controllers.
     *
     * @param {object} node
     *
     */
    positionBuiltInRefTypeNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of ConstrainedType node controllers.
     *
     * @param {object} node
     *
     */
    positionConstrainedTypeNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of FunctionType node controllers.
     *
     * @param {object} node
     *
     */
    positionFunctionTypeNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of UserDefinedType node controllers.
     *
     * @param {object} node
     *
     */
    positionUserDefinedTypeNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of EndpointType node controllers.
     *
     * @param {object} node
     *
     */
    positionEndpointTypeNodeControllers(node) {
        // Not implemented.
    }


    /**
     * Calculate position of ValueType node controllers.
     *
     * @param {object} node
     *
     */
    positionValueTypeNodeControllers(node) {
        // Not implemented.
    }


}

export default ControllerPositioningUtil;
