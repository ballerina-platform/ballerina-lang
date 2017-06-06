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
import React from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import {statement} from './../configs/designer-defaults';
import {lifeLine} from './../configs/designer-defaults';
import ASTNode from '../ast/node';
import ActionBox from './action-box';
import DragDropManager from '../tool-palette/drag-drop-manager';
import SimpleBBox from './../ast/simple-bounding-box';
import * as DesignerDefaults from './../configs/designer-defaults';
import MessageManager from './../visitors/message-manager';
import BallerinaASTFactory from './../ast/ballerina-ast-factory';
import './statement-decorator.css';
import select2 from 'select2';
import TransformRender from '../../ballerina/components/transform-render';
import ActiveArbiter from './active-arbiter';
import ImageUtil from './image-util';
import alerts from 'alerts';

const text_offset = 50;

class TransformStatementDecorator extends React.Component {

    constructor(props, context) {
        super(props, context);
        const {dragDropManager} = context;
        this.startDropZones = this.startDropZones.bind(this);
        this.stopDragZones = this.stopDragZones.bind(this);
        this.state = {
		    innerDropZoneActivated: false,
	        innerDropZoneDropNotAllowed: false,
	        innerDropZoneExist: false,
            active: 'hidden'
        };
    }

    componentDidMount() {
        const { dragDropManager } = this.context;
        dragDropManager.on('drag-start', this.startDropZones);
        dragDropManager.on('drag-stop', this.stopDragZones);
    }

    componentWillUnmount() {
        const { dragDropManager } = this.context;
        dragDropManager.off('drag-start', this.startDropZones);
        dragDropManager.off('drag-stop', this.stopDragZones);
    }

    startDropZones() {
        this.setState({innerDropZoneExist: true});
    }

    stopDragZones() {
        this.setState({innerDropZoneExist: false});
    }

    onDelete() {
        this.props.model.remove();
    }

    onJumptoCodeLine() {
        const {viewState: {fullExpression}} = this.props;
        const {renderingContext: {ballerinaFileEditor}} = this.context;

        const container = ballerinaFileEditor._container;
        $(container).find('.view-source-btn').trigger('click');
        ballerinaFileEditor.getSourceView().jumpToLine({expression: fullExpression});
    }

    renderBreakpointIndicator() {
        const breakpointSize = 14;
        const pointX = this.statementBox.x + this.statementBox.w - breakpointSize/2;
        const pointY = this.statementBox.y - breakpointSize/2;
        return (
                <Breakpoint
                        x={pointX}
                        y={pointY}
                        size={breakpointSize}
                        isBreakpoint={this.props.model.isBreakpoint}
                        onClick = { () => this.onBreakpointClick() }
                />
        );
    }

    onBreakpointClick() {
        const { model } = this.props;
        const { isBreakpoint = false } = model;
        if (model.isBreakpoint) {
                model.removeBreakpoint();
        } else {
                model.addBreakpoint();
        }
    }

    onExpand() {
        self = this;
        this._package = this.context.renderingContext.getPackagedScopedEnvironment().getCurrentPackage();
        var sourceId = 'sourceStructs' + this.props.model.id;
        var targetId = 'targetStructs' + this.props.model.id;

        var sourceContent = $('<div class="leftType">' +
                '<div class="source-view">' +
                '<select id="' + sourceId + '" class="type-mapper-combo">' +
                '<option value="-1">--Select--</option>' +
                '</select>'+
                ' <span id="btn-add-source" class="btn-add-type fw-stack fw-lg btn btn-add">'+
                '            <i class="fw fw-add fw-stack-1x"></i>'+
                '          </span>'+
                '           <span id="btn-remove-source" class="btn-remove-type fw-stack fw-lg btn btn-remove">'+
                '            <i class="fw fw-delete fw-stack-1x"></i>'+
                '          </span>' +
              '</div></div>');

        var middleContent =  $('<div class="middle-content"></div>');

        var targetContent = $('<div class="rightType">' +
                '<div class="target-view">' +
                '<select id="' + targetId + '" class="type-mapper-combo">' +
                '<option value="-1">--Select--</option>' +
                '</select>' +
                ' <span id="btn-add-target" class="btn-add-type fw-stack fw-lg btn btn-add">'+
                '            <i class="fw fw-add fw-stack-1x"></i>'+
                '          </span>'+
                '           <span id="btn-remove-target" class="btn-remove-type fw-stack fw-lg btn btn-remove">'+
                '            <i class="fw fw-delete fw-stack-1x"></i>'+
                '          </span>' +
              '</div></div>');

        var transformNameText = $('<p class="transform-header-text ">'
                                +'<i class="transform-header-icon fw fw-type-converter fw-inverse"></i>Transform</p>');
        var transformHeader = $('<div id ="transformHeader" class ="transform-header">'
                                +'<span class="close-transform">&times;</span></div>');
        var transformHeaderPadding = $('<div id ="transformHeaderPadding" class ="transform-header-padding"></div>');
        var transformMenuDiv = $('<div id ="transformContextMenu" class ="transformContextMenu"></div>');

        var transformOverlayContent = $('<div id = "transformOverlay-content" class="transformOverlay-content">'+
                                              '    </div>');

        var transformOverlay = $( '<div id="transformOverlay" class="transformOverlay">'+
                                     '  </div>' );
        var transformFooter = $('<div id ="transformFooter" class ="transform-footer"></div>');

        transformOverlayContent.append(transformHeader);
        transformHeader.append(transformNameText);
        transformOverlayContent.append(transformHeaderPadding);
        transformOverlayContent.append(sourceContent);
        transformOverlayContent.append(middleContent);
        transformOverlayContent.append(targetContent);
        transformOverlay.append(transformOverlayContent);
        transformOverlayContent.append(transformMenuDiv);
        transformOverlayContent.append(transformFooter)
        $('#tab-content-wrapper').append(transformOverlay);

        this.transformOverlayDiv = document.getElementById('transformOverlay');
		this.transformOverlayContentDiv = document.getElementById('transformOverlay-content');

		this.transformOverlayContentDiv.addEventListener('mouseover', e => {
			this.onTransformDropZoneActivate(e);
		});

		this.transformOverlayContentDiv.addEventListener('mouseout', e => {
	        this.onTransformDropZoneDeactivate(e);
		});

        var span = document.getElementsByClassName('close-transform')[0];

        this.predefinedStructs = [];
        var transformIndex = this.props.model.parent.getIndexOfChild(this.props.model);
        _.forEach(this.props.model.parent.getVariableDefinitionStatements(), variableDefStmt => {
            var currentIndex = this.props.model.parent.getIndexOfChild(variableDefStmt);
            var structInfo = variableDefStmt.getLeftExpression().split(' ');
            var isStruct = false;
               //Checks struct defined before the transform statement
            if(currentIndex < transformIndex) {
                _.forEach(this._package.getStructDefinitions(), predefinedStruct => {
                    if (structInfo[0] ==  predefinedStruct.getStructName()) {
                        var struct = self.createType(structInfo[1], predefinedStruct);
                        self.loadSchemaToComboBox(sourceId, struct.name);
                        self.loadSchemaToComboBox(targetId, struct.name);
                        isStruct = true;;
                    }
                });

                if(!isStruct) {
                    var variableType = {};
                    variableType.id = variableDefStmt.id;
                    var varInfo = variableDefStmt.getLeftExpression().split(' ');
                    variableType.name  = varInfo[1];
                    variableType.type = varInfo[0];
                    self.predefinedStructs.push(variableType);
                    self.loadSchemaToComboBox(sourceId, variableType.name);
                    self.loadSchemaToComboBox(targetId, variableType.name);
                }
            }
        });
        $('.type-mapper-combo').select2();

        $('#btn-add-source').click(function (e) {
            var currentSelection = $('#' + sourceId).val();
            if (self.setSource(currentSelection, self.predefinedStructs)) {
                var inputDef = BallerinaASTFactory
                                        .createVariableReferenceExpression({variableName: currentSelection});
                var inputs = self.props.model.getInput();
                inputs.push(inputDef);
                self.props.model.setInput(inputs);
            }
        });

        $('#btn-remove-source').click(function (e) {
             var currentSelection = $('#' + sourceId).val();
             if (currentSelection != -1) {
                self.mapper.removeType(currentSelection);
                self.props.model.setInput([]);
                var currentSelectionObj =  _.find(self.predefinedStructs, { name:currentSelection});
                currentSelectionObj.added = false;
                 self.props.model.children = [];
             }
        });

        $('#btn-add-target').click(function (e) {
            var currentSelection = $('#' + targetId).val();
            if (self.setTarget(currentSelection, self.predefinedStructs)) {
                var outDef = BallerinaASTFactory
                                        .createVariableReferenceExpression({variableName: currentSelection});
                var outputs = self.props.model.getOutput();
                outputs.push(outDef)
                self.props.model.setOutput(outputs);
            }
        });

        $('#btn-remove-target').click(function (e) {
             var currentSelection = $('#' + targetId).val();
             if (currentSelection != -1) {
                self.mapper.removeType(currentSelection);
                self.props.model.setOutput([]);
                var currentSelectionObj =  _.find(self.predefinedStructs, { name:currentSelection});
                currentSelectionObj.added = false;
                 self.props.model.children = [];
             }
        });

        $(window).on('resize', function(){
            self.mapper.reposition(self.mapper);
        });

        $(".leftType, .rightType, .middle-content").on('scroll', function(){
            self.mapper.reposition(self.mapper);
        });

        span.onclick = function() {
            document.getElementById('transformOverlay').style.display = 'none';
            $(transformOverlay).remove();
        };

        var onConnectionCallback = function(connection) {
            let sourceStruct = _.find(self.predefinedStructs, { name:connection.sourceStruct});
            let targetStruct = _.find(self.predefinedStructs, { name:connection.targetStruct});
            var sourceExpression;
            var targetExpression;

            if (sourceStruct.type == "struct") {
                sourceExpression = self.getStructAccessNode(connection.sourceStruct, connection.sourceProperty);
            } else {
                sourceExpression = BallerinaASTFactory
                                             .createVariableReferenceExpression({variableName: sourceStruct.name});
            }

            if (targetStruct.type == "struct") {
                targetExpression = self.getStructAccessNode(connection.targetStruct, connection.targetProperty);
            } else {
                targetExpression = BallerinaASTFactory
                                            .createVariableReferenceExpression({variableName: targetStruct.name});
            }

            if (!_.isUndefined(sourceStruct) && !_.isUndefined(targetStruct)) {
                    //Connection is from source struct to target struct.
                let assignmentStmt = BallerinaASTFactory.createAssignmentStatement();
                let leftOperand = BallerinaASTFactory.createLeftOperandExpression();
                leftOperand.addChild(targetExpression);
                let rightOperand = BallerinaASTFactory.createRightOperandExpression();
                rightOperand.addChild(sourceExpression);
                assignmentStmt.addChild(leftOperand);
                assignmentStmt.addChild(rightOperand);
                self.props.model.addChild(assignmentStmt);
                return assignmentStmt.id;
            } else if (!_.isUndefined(sourceStruct) && _.isUndefined(targetStruct)) {
                    // Connection source is not a struct and target is a struct.
                    // Source could be a function node.
                let assignmentStmtSource = self.findExistingAssignmentStatement(connection.targetStruct);
                assignmentStmtSource.getChildren()[1].getChildren()[0].addChild(targetExpression);
                return assignmentStmtSource.id;
            } else if (_.isUndefined(sourceStruct) && !_.isUndefined(targetStruct)) {
                    // Connection target is not a struct and source is a struct.
                    // Target could be a function node.
                let assignmentStmtTarget = self.findExistingAssignmentStatement(connection.sourceStruct);
                assignmentStmtTarget.getChildren()[0].addChild(sourceExpression);
                return assignmentStmtTarget.id;
            } else {
                    // Connection source and target are not structs
                    // Source and target could be function nodes.
                log.warn('multiple intermediate functions are not yet supported in design view');
            }
        };

        var onDisconnectionCallback = function(connection) {
            let sourceStruct = _.find(self.predefinedStructs, { name:connection.sourceStruct});
            let targetStruct = _.find(self.predefinedStructs, { name:connection.targetStruct});
            let sourceExpression = self.getStructAccessNode(connection.targetStruct, connection.targetProperty);
            let targetExpression = self.getStructAccessNode(connection.sourceStruct, connection.sourceProperty);

            if (!_.isUndefined(sourceStruct) && !_.isUndefined(targetStruct)) {
                let assignmentStmt =  _.find(self.props.model.children, { id:connection.id});
                self.props.model.removeChild(assignmentStmt);
            } else if (!_.isUndefined(sourceStruct) && _.isUndefined(targetStruct)) {
                // Connection source is not a struct and target is a struct.
                // Source could be a function node.
                let assignmentStmtSource = self.findExistingAssignmentStatement(connection.targetStruct);
                _.remove (assignmentStmtSource.getChildren()[1].getChildren()[0].getChildren(), function (child) {
                    return (child.getExpression() === targetExpression.getExpression());
                });
            } else if (_.isUndefined(sourceStruct) && !_.isUndefined(targetStruct)) {
                // Connection target is not a struct and source is a struct.
                // Target could be a function node.
                let assignmentStmtTarget = self.findExistingAssignmentStatement(connection.sourceStruct);
                _.remove (assignmentStmtTarget.getChildren()[0].getChildren(), function (child) {
                    return (child.getExpression() === sourceExpression.getExpression());
                });
            } else {
                // Connection source and target are not structs
                // Source and target could be function nodes.
                log.warn('multiple intermediate functions are not yet supported in design view');
            }
        };

        this.mapper = new TransformRender(onConnectionCallback, onDisconnectionCallback);
        this.transformOverlayDiv.style.display = 'block';

         _.forEach(self.props.model.getInput(), input => {
            self.setSource(input.getExpression(), self.predefinedStructs);
         });

         _.forEach(self.props.model.getOutput(), output => {
            self.setTarget(output.getExpression(), self.predefinedStructs);
         });

        _.forEach(this.props.model.getChildren(), statement => {
            this.createConnection(statement);
        });

        this.props.model.on('child-added', node => {
            if (BallerinaASTFactory.isAssignmentStatement(node) &&
					BallerinaASTFactory.isFunctionInvocationExpression(node.getChildren()[1].getChildren()[0])) {
                let functionInvocationExpression = node.getChildren()[1].getChildren()[0];
                let func = this.getFunctionDefinition(functionInvocationExpression);
                this.mapper.addFunction(func, node, node.getParent().removeChild.bind(node.getParent()));
            }
        });
    }

    getFunctionDefinition(functionInvocationExpression) {
        let funPackage = this.context.renderingContext.packagedScopedEnvironemnt.getPackageByName(
						functionInvocationExpression.getFullPackageName());
        let func = funPackage.getFunctionDefinitionByName(functionInvocationExpression.getFunctionName());
        return func;
    }

    createConnection(statement){
        if (BallerinaASTFactory.isAssignmentStatement(statement)){
            let leftExpressions = statement.getChildren()[0];
            let rightExpression = statement.getChildren()[1].getChildren()[0];

            if (BallerinaASTFactory.isFieldAccessExpression(rightExpression)) {
                let con = {};
                con.id = statement.id;
                let leftExpression = leftExpressions.getChildren()[0];
                con.sourceStruct = rightExpression.getChildren()[0].getVariableName();
                var complexSourceProp = this.createComplexProp(con.sourceStruct,
                                           rightExpression.getChildren()[1].getChildren());
                con.sourceType = complexSourceProp.types.reverse();
                con.sourceProperty = complexSourceProp.names.reverse();

                con.targetStruct = leftExpression.getChildren()[0].getVariableName();
                var complexTargetProp = this.createComplexProp(con.targetStruct,
                                          leftExpression.getChildren()[1].getChildren());
                con.targetType = complexTargetProp.types.reverse();
                con.targetProperty = complexTargetProp.names.reverse();
                con.isComplexMapping = false;

                self.mapper.addConnection(con);

            } else if (BallerinaASTFactory.isFunctionInvocationExpression(rightExpression)){
                let func = this.getFunctionDefinition(rightExpression);
                if (_.isUndefined(func)) {
                    alerts.error('Function definition for "' + rightExpression.getFunctionName() + '" cannot be found');
                    return;
                }
                this.mapper.addFunction(func, statement, statement.getParent().removeChild.bind(statement.getParent()));

                if (func.getParameters().length === rightExpression.getChildren().length){
                    _.forEach(func.getParameters(), (parameter, i) => {
                        // draw source struct field to function parameter connection
                        let conLeft = {};
                        conLeft.id = rightExpression.id;
                        let fieldAccessExpression = rightExpression.getChildren()[i];
                        if (BallerinaASTFactory.isFieldAccessExpression(fieldAccessExpression)) {
                            conLeft.sourceStruct = fieldAccessExpression.getChildren()[0].getVariableName();
                            var complexSourceProp = this.createComplexProp(conLeft.sourceStruct, 
                                                       fieldAccessExpression.getChildren()[1].getChildren());
                            conLeft.sourceType = complexSourceProp.types.reverse();
                            conLeft.sourceProperty = complexSourceProp.names.reverse();

                            conLeft.targetFunction = true;
                            conLeft.targetStruct = func.meta.packageName + '-' + func.getName();

                            //set id of function invocation expression to identify the function node
                            conLeft.targetId = rightExpression.getID();
                            conLeft.targetProperty = [parameter.name];
                            conLeft.targetType = [parameter.type];

                            self.mapper.addConnection(conLeft);
                        }
                    });
                } else { 
                    alerts.warn('Function inputs and mapping count does not match in "' + func.getName() + '"');
                }

                // draw function to target struct connection
                if (func.getReturnParams().length === leftExpressions.getChildren().length) {
                    _.forEach(func.getReturnParams(), (parameter, i) => {
                        let leftExpression = leftExpressions.getChildren()[i];
                        let conRight = {};
                        conRight.id = leftExpression.id;
                        conRight.sourceFunction = true;
                        conRight.sourceStruct = func.meta.packageName + '-' + func.getName();

                        //set id of function invocation expression to identify the function node
                        conRight.sourceId = rightExpression.getID();
                        conRight.sourceProperty = [func.getReturnParams()[i].name];
                        conRight.sourceType = [func.getReturnParams()[i].type];

                        conRight.targetStruct = leftExpression.getChildren()[0].getVariableName();
                        var complexTargetProp = this.createComplexProp(conRight.targetStruct,
                                                leftExpression.getChildren()[1].getChildren());
                        conRight.targetType = complexTargetProp.types.reverse();
                        conRight.targetProperty = complexTargetProp.names.reverse();
                        conRight.isComplexMapping = false;
                        self.mapper.addConnection(conRight);
                    });
                } else { 
                     alerts.warn('Function outputs and mapping count does not match in "' + func.getName() + '"');
                }
            }
        } else {
            log.error('invalid statement type in transform statement');
        }
    }

    findExistingAssignmentStatement(id){
        return _.find(self.props.model.getChildren(), function(child) {
            let exp = child.getChildren()[1].getChildren()[0];
            return (BallerinaASTFactory.isFunctionInvocationExpression(exp)) && (_.endsWith(id, exp.getID()));
        });
    }

    createComplexProp(typeName, children)
    {
        let prop = {};
        prop.names = [];
        prop.types = [];

        let propName = children[0].getBasicLiteralValue();
        let struct = _.find(self.predefinedStructs, { name:typeName});
        if (_.isUndefined(struct)) {
            alerts.error('Struct definition for variable "' + typeName + '" cannot be found');
            return;
        }
        let structProp =  _.find(struct.properties, { name:propName});
        if (_.isUndefined(structProp)) {
            alerts.error('Struct field "' + propName + '" cannot be found in variable "' + typeName + '"');
            return;
        }
        let propType = structProp.type;
        if (children.length > 1) {
            prop = self.createComplexProp(propName, children[1].getChildren());
        }
        prop.types.push(propType);
        prop.names.push(propName);
        return prop;
    }

    createType(name, predefinedStruct) {
        var struct = {};
        struct.name = name;
        struct.properties = [];
        struct.type = "struct";

        _.forEach(predefinedStruct.getVariableDefinitionStatements(), stmt => {
            var property = {};
            var structPopInfo = stmt.getLeftExpression().split(' ');
            property.name  = structPopInfo[1];
            property.type  = structPopInfo[0];

            var innerStruct = _.find(self._package.getStructDefinitions(), { _structName:property.type});
            if (innerStruct != null) {
                property.innerType = self.createType(property.name, innerStruct);
            }

            struct.properties.push(property);
        });
        self.predefinedStructs.push(struct);
        return struct;
    }

    render() {
        const { viewState, expression ,model} = this.props;
        let bBox = viewState.bBox;
        let innerZoneHeight = viewState.components['drop-zone'].h;

        // calculate the bBox for the statement
        this.statementBox = {};
        this.statementBox.h = bBox.h - innerZoneHeight;
        this.statementBox.y = bBox.y + innerZoneHeight;
        this.statementBox.w = bBox.w;
        this.statementBox.x = bBox.x;
        // we need to draw a drop box above and a statement box
        const text_x = bBox.x + (bBox.w / 2);
        const text_y = this.statementBox.y + (this.statementBox.h / 2);
        const expand_button_x = bBox.x + (bBox.w / 2) + 40;
        const expand_button_y = this.statementBox.y + (this.statementBox.h / 2) - 10;
        const drop_zone_x = bBox.x + (bBox.w - lifeLine.width)/2;
        const innerDropZoneActivated = this.state.innerDropZoneActivated;
        const innerDropZoneDropNotAllowed = this.state.innerDropZoneDropNotAllowed;
        const dropZoneClassName = ((!innerDropZoneActivated) ? 'inner-drop-zone' : 'inner-drop-zone active')
                                            + ((innerDropZoneDropNotAllowed) ? ' block' : '');

        const actionBbox = new SimpleBBox();
        const fill = this.state.innerDropZoneExist ? {} : {fill:'none'};
        const iconSize = 14;
        actionBbox.w = DesignerDefaults.actionBox.width;
        actionBbox.h = DesignerDefaults.actionBox.height;
        actionBbox.x = bBox.x + ( bBox.w - actionBbox.w) / 2;
        actionBbox.y = bBox.y + bBox.h + DesignerDefaults.actionBox.padding.top;
        let statementRectClass = 'transform-statement-rect';
        if (model.isDebugHit) {
                statementRectClass = `${statementRectClass} debug-hit`;
        }

        return (
        <g className="statement"
            onMouseOut={ this.setActionVisibility.bind(this, false) }
            onMouseOver={ this.setActionVisibility.bind(this, true)}>
            <rect x={drop_zone_x} y={bBox.y} width={lifeLine.width} height={innerZoneHeight}
                className={dropZoneClassName} {...fill}
                    onMouseOver={(e) => this.onDropZoneActivate(e)}
                    onMouseOut={(e) => this.onDropZoneDeactivate(e)}/>
            <rect x={bBox.x} y={this.statementBox.y} width={bBox.w} height={this.statementBox.h} className={statementRectClass}
                    onClick={(e) => this.onExpand()} />
            <g className="statement-body">
                <text x={text_x} y={text_y} className="transform-action" onClick={(e) =>this.onExpand()}>{expression}</text>
                    <image className="transform-action-icon" x={expand_button_x} y={expand_button_y} width={ iconSize } height={ iconSize } onClick={(e) =>this.onExpand()} xlinkHref={ ImageUtil.getSVGIconString('expand')}/>
            </g>
            <ActionBox
                bBox={ actionBbox }
                show={ this.state.active }
                onDelete={ () => this.onDelete() }
                onJumptoCodeLine={ () => this.onJumptoCodeLine() }
            />
            {		model.isBreakpoint &&
                    this.renderBreakpointIndicator()
            }
            {this.props.children}
        </g>);
    }

    setActionVisibility (show) {
        if (!this.context.dragDropManager.isOnDrag()) {
            if (show) {
                this.context.activeArbiter.readyToActivate(this);
            } else {
                this.context.activeArbiter.readyToDeactivate(this);
            }
        }
    }

    onTransformDropZoneActivate (e) {
        this.transformOverlayContentDiv = document.getElementById('transformOverlay-content');
        const dragDropManager = this.context.dragDropManager,
            dropTarget = this.props.model,
            model = this.props.model;
        if (dragDropManager.isOnDrag()) {
            if (_.isEqual(dragDropManager.getActivatedDropTarget(), dropTarget)){
                return;
            }
            dragDropManager.setActivatedDropTarget(dropTarget,
				(nodeBeingDragged) => {
					// This drop zone is for assignment statements only.
                    // Functions with atleast one return parameter is allowed to be dropped. If the dropped node
                    // is an Assignment Statement, that implies there is a return parameter . If there is no
                    // return parameter, then it is a Function Invocation Statement, which is validated with below check.
                    return model.getFactory().isAssignmentStatement(nodeBeingDragged);
                },
				() => {
                    return dropTarget.getChildren().length;
                }
            );
        }
        e.stopPropagation();
    }

    onTransformDropZoneDeactivate (e) {
        this.transformOverlayContentDiv = document.getElementById('transformOverlay-content');
        const dragDropManager = this.context.dragDropManager,
            dropTarget = this.props.model.getParent();
        if (dragDropManager.isOnDrag()) {
            if (_.isEqual(dragDropManager.getActivatedDropTarget(), dropTarget)) {
                dragDropManager.clearActivatedDropTarget();
                this.setState({innerDropZoneActivated: false, innerDropZoneDropNotAllowed: false});
            }
        }
        e.stopPropagation();
    }
    onDropZoneActivate (e) {
        const dragDropManager = this.context.dragDropManager,
            dropTarget = this.props.model.getParent(),
            model = this.props.model;
        if (dragDropManager.isOnDrag()) {
            if (_.isEqual(dragDropManager.getActivatedDropTarget(), dropTarget)){
                return;
            }
            dragDropManager.setActivatedDropTarget(dropTarget,
				(nodeBeingDragged) => {
                    // IMPORTANT: override node's default validation logic
                    // This drop zone is for statements only.
                    // Statements should only be allowed here.
                    return model.getFactory().isStatement(nodeBeingDragged);
                },
				() => {
                    return dropTarget.getIndexOfChild(model);
                }
			);
            this.setState({innerDropZoneActivated: true,
                innerDropZoneDropNotAllowed: !dragDropManager.isAtValidDropTarget()
            });
            dragDropManager.once('drop-target-changed', function(){
                this.setState({innerDropZoneActivated: false, innerDropZoneDropNotAllowed: false});
            }, this);
        }
        e.stopPropagation();
    }

    onDropZoneDeactivate (e) {
        const dragDropManager = this.context.dragDropManager,
			  dropTarget = this.props.model.getParent();
        if (dragDropManager.isOnDrag()) {
            if (_.isEqual(dragDropManager.getActivatedDropTarget(), dropTarget)) {
                dragDropManager.clearActivatedDropTarget();
                this.setState({innerDropZoneActivated: false, innerDropZoneDropNotAllowed: false});
            }
        }
        e.stopPropagation();
    }

    onArrowStartPointMouseOver (e) {
        e.target.style.fill = '#444';
        e.target.style.fillOpacity = 0.5;
        e.target.style.cursor = 'url(images/BlackHandwriting.cur), pointer';
    }

    onArrowStartPointMouseOut (e) {
        e.target.style.fill = '#444';
        e.target.style.fillOpacity = 0;
    }

    onMouseDown (e) {
        const messageManager = this.context.messageManager;
        const model = this.props.model;
        const bBox = model.getViewState().bBox;
        const statement_h = this.statementBox.h;
        const messageStartX = bBox.x +  bBox.w;
        const messageStartY = this.statementBox.y +  statement_h/2;
        let actionInvocation;
        if (ASTFactory.isAssignmentStatement(model)) {
            actionInvocation = model.getChildren()[1].getChildren()[0];
        } else if (ASTFactory.isVariableDefinitionStatement(model)) {
            actionInvocation = model.getChildren()[1];
        }
        messageManager.setSource(actionInvocation);
        messageManager.setIsOnDrag(true);
        messageManager.setMessageStart(messageStartX, messageStartY);

        messageManager.setTargetValidationCallback(function (destination) {
            return actionInvocation.messageDrawTargetAllowed(destination);
        });

        messageManager.startDrawMessage(function (source, destination) {
            source.setAttribute('_connector', destination);
        });
    }

    onMouseUp (e) {
        const messageManager = this.context.messageManager;
        messageManager.reset();
    }

    openExpressionEditor(e){
        let options = this.props.editorOptions;
        let packageScope = this.context.renderingContext.packagedScopedEnvironemnt;
        if (options) {
            new ExpressionEditor( this.statementBox , this.context.container ,
                (text) => this.onUpdate(text), options , packageScope );
        }
    }

    onUpdate(text){
    }

    loadSchemaToComboBox(comboBoxId, name) {
        $('#' + comboBoxId).append('<option value="' + name + '">' + name + '</option>');
    }

    createAccessNode(name, property) {
        var structExpression = BallerinaASTFactory.createFieldAccessExpression();
        var structPropertyHolder = BallerinaASTFactory.createFieldAccessExpression();
        var structProperty = BallerinaASTFactory.createBasicLiteralExpression({basicLiteralType:'string',
            basicLiteralValue:property});
        var structName =  BallerinaASTFactory.createVariableReferenceExpression();

        structName.setVariableName(name);
        structExpression.addChild(structName);
        structPropertyHolder.addChild(structProperty);
        structExpression.addChild(structPropertyHolder);

        return structExpression;
    }

    getStructAccessNode(name, property) {
        var structExpressions = [];

        _.forEach(property, prop => {
            structExpressions.push(self.createAccessNode(name, prop));
        });

        for (var i = structExpressions.length - 1; i > 0 ; i--) {
            structExpressions[i-1].children[1].addChild(structExpressions[i].children[1]);
        }
        return structExpressions[0];
    }

    setSource(currentSelection, predefinedStructs) {
        var sourceSelection =  _.find(predefinedStructs, { name:currentSelection});
        if (_.isUndefined(sourceSelection)){
            alerts.error('Mapping source "' + currentSelection + '" cannot be found');
            return false;
        }
        if (!sourceSelection.added) {
            if (sourceSelection.type == "struct") {
                self.mapper.addSourceType(sourceSelection);
            } else {
                self.mapper.addVariable(sourceSelection, "source");
            }
            sourceSelection.added = true;
            return true;
        } else {
            return false;
        }
    }

    setTarget(currentSelection, predefinedStructs) {
        var targetSelection = _.find(predefinedStructs, { name: currentSelection});
        if (_.isUndefined(targetSelection)){
            alerts.error('Mapping target "' + currentSelection + '" cannot be found');
            return false;
        }
        if (!targetSelection.added) {
            if (targetSelection.type == "struct") {
                self.mapper.addTargetType(targetSelection);
            } else {
                self.mapper.addVariable(targetSelection, "target");
            }
            targetSelection.added = true;
            return true;
        } else {
            return false;
        }
    }
}

TransformStatementDecorator.propTypes = {
    bBox: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
        w: PropTypes.number.isRequired,
        h: PropTypes.number.isRequired,
    }),
    model: PropTypes.instanceOf(ASTNode).isRequired,
    expression: PropTypes.string.isRequired,
};

TransformStatementDecorator.contextTypes = {
	 dragDropManager: PropTypes.instanceOf(DragDropManager).isRequired,
	 container: PropTypes.instanceOf(Object).isRequired,
	 renderingContext: PropTypes.instanceOf(Object).isRequired,
	 activeArbiter: PropTypes.instanceOf(ActiveArbiter).isRequired
};

export default TransformStatementDecorator;
