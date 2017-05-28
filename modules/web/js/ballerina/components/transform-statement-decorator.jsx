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
import React from "react";
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
import ASTFactory from './../ast/ballerina-ast-factory';
import './statement-decorator.css';
import ArrowDecorator from './arrow-decorator';
import BackwardArrowDecorator from './backward-arrow-decorator';
import ExpressionEditor from 'expression_editor_utils';
import select2 from 'select2';
import TransformRender from '../../ballerina/components/transform-render';
import BallerinaASTFactory from 'ballerina/ast/ballerina-ast-factory';
import ActiveArbiter from './active-arbiter';
import ImageUtil from './image-util';

const text_offset = 50;

class TransformStatementDecorator extends React.Component {

	constructor(props, context) {
		super(props, context);
        const {dragDropManager} = context;
        dragDropManager.on('drag-start', this.startDropZones.bind(this));
        dragDropManager.on('drag-stop', this.stopDragZones.bind(this));

		this.state = {
		    innerDropZoneActivated: false,
	        innerDropZoneDropNotAllowed: false,
	        innerDropZoneExist: false,
            active: 'hidden'
		};
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
    			if(model.isBreakpoint) {
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
              '</select>' +
              '</div></div>');

          var targetContent = $('<div class="rightType">' +
              '<div class="target-view">' +
              '<select id="' + targetId + '" class="type-mapper-combo">' +
              '<option value="-1">--Select--</option>' +
              '</select>' +
              '</div></div>');

          var transformNameText = $('<p class="transform-header-text "><i class="transform-header-icon fw fw-type-converter fw-inverse"></i>Transform</p>');
          var transformHeader = $('<div id ="transformHeader" class ="transform-header"></div>');
          var transformMenuDiv = $('<div id ="transformContextMenu" class ="transformContextMenu"></div>');

          var transformOverlayContent =  $('<div id = "transformOverlay-content" class="transformOverlay-content clearfix">'+
                                                   ' <span class="close-transform">&times;</span>'+
                                              '    </div>');

          var transformOverlay = $( '<div id="transformOverlay" class="transformOverlay">'+
                                     '  </div>' );

          transformOverlayContent.append(transformHeader);
          transformHeader.append(transformNameText);
          transformOverlayContent.append(sourceContent);
          transformOverlayContent.append(targetContent);
          transformOverlay.append(transformOverlayContent);
          transformOverlayContent.append(transformMenuDiv);
          $("#tab-content-wrapper").append(transformOverlay);

          this.transformOverlayDiv = document.getElementById('transformOverlay');
          var span = document.getElementsByClassName("close-transform")[0];

          this.predefinedStructs = [];
          var transformIndex = this.props.model.parent.getIndexOfChild(this.props.model);
          _.forEach(this.props.model.parent.getVariableDefinitionStatements(), variableDefStmt => {
               var currentIndex = this.props.model.parent.getIndexOfChild(variableDefStmt);
               var structInfo = variableDefStmt.getLeftExpression().split(" ");
               //Checks struct defined before the transform statement
               if(currentIndex < transformIndex) {
                   _.forEach(this._package.getStructDefinitions(), predefinedStruct => {
                          if (structInfo[0] ==  predefinedStruct.getStructName()) {
                                var struct = self.createType(structInfo[1], predefinedStruct);
                                self.predefinedStructs.push(struct);
                                self.loadSchemaToComboBox(sourceId, struct.name);
                                self.loadSchemaToComboBox(targetId, struct.name);
                           }
                    });
               }
          });
           $(".type-mapper-combo").select2();

           $("#" + sourceId).on("select2:selecting", function (e) {
               var currentSelection = e.params.args.data.id;
               var previousSelection = $("#" + sourceId).val();
               if (currentSelection == -1) {
                   self.mapper.removeType(previousSelection);
                   self.props.model.children = [];
               } else if (currentSelection != $("#" + targetId).val()) {
                   self.mapper.removeType(previousSelection);
                   self.setSource(currentSelection, self.predefinedStructs);
                   var inputDef = BallerinaASTFactory
                                        .createVariableReferenceExpression({variableName: currentSelection});
                   self.props.model.setInput([inputDef]);
                   self.props.model.children = [];
               } else {
                   return false;
               }
           });

          $("#" + targetId).on("select2:selecting", function (e) {
               var currentSelection = e.params.args.data.id;
               var previousSelection = $("#" + targetId).val();
               if (currentSelection == -1) {
                   self.mapper.removeType(previousSelection);
                   self.props.model.children = [];
               } else if (currentSelection != $("#" + sourceId).val()) {
                    self.mapper.removeType(previousSelection);
                    self.setTarget(currentSelection, self.predefinedStructs);
                    var outDef = BallerinaASTFactory
                                        .createVariableReferenceExpression({variableName: currentSelection});
                    self.props.model.setOutput([outDef]);
                    self.props.model.children = [];
               } else {
                   return false;
               }
          });

          $(window).on('resize', function(){
               self.mapper.reposition(self.mapper);
          });

          span.onclick = function() {
               document.getElementById('transformOverlay').style.display = "none";
               $(transformOverlay).remove();
          }

          window.onclick = function(event) {
                var transformOverlayDiv = document.getElementById('transformOverlay')
               if (event.target == transformOverlayDiv) {
                   transformOverlayDiv.style.display = "none";
                   $(transformOverlay).remove();
               }
          }

           var onConnectionCallback = function(connection) {
               var assignmentStmt = BallerinaASTFactory.createAssignmentStatement();
               var leftOperand = BallerinaASTFactory.createLeftOperandExpression();
               leftOperand.addChild(self.getStructAccessNode(connection.targetStruct, connection.targetProperty));
               var rightOperand = BallerinaASTFactory.createRightOperandExpression();
               rightOperand.addChild(self.getStructAccessNode(connection.sourceStruct, connection.sourceProperty));
               assignmentStmt.addChild(leftOperand);
               assignmentStmt.addChild(rightOperand);
               self.props.model.addChild(assignmentStmt);

               return assignmentStmt.id;
           };

           var onDisconnectionCallback = function(connection) {
                var con =  _.find(self.props.model.children, { id:connection.id});
                self.props.model.removeChild(con);
            };

           this.mapper = new TransformRender(onConnectionCallback, onDisconnectionCallback);
           this.transformOverlayDiv.style.display = "block";

           if(self.props.model.getInput() != null && self.props.model.getInput().length > 0) {
                  var sourceType = self.props.model.getInput()[0].getExpression();
                  $("#" + sourceId).val(sourceType).trigger('change');
                  self.setSource(sourceType, self.predefinedStructs);
           }

           if(self.props.model.getOutput() != null && self.props.model.getOutput().length > 0) {
                  var targetType = self.props.model.getOutput()[0].getExpression();
                  $("#" + targetId).val(targetType).trigger('change');
                  self.setTarget(targetType, self.predefinedStructs);
           }

            _.forEach(this.props.model.getChildren(), assignments => {
                var con = {};
                con.id = assignments.id;
                con.sourceStruct = assignments.getChildren()[1].getChildren()[0].getChildren()[0].getVariableName();
                var complexSourceProp = self.createComplexProp(con.sourceStruct,
                assignments.getChildren()[1].getChildren()[0].getChildren()[1].getChildren());
                con.sourceType = complexSourceProp.types.reverse();
                con.sourceProperty = complexSourceProp.names.reverse();
                con.targetStruct = assignments.getChildren()[0].getChildren()[0].getChildren()[0].getVariableName();
                var complexTargetProp = self.createComplexProp(con.targetStruct,
                assignments.getChildren()[0].getChildren()[0].getChildren()[1].getChildren());
                con.targetType = complexTargetProp.types.reverse();
                con.targetProperty = complexTargetProp.names.reverse();
                con.isComplexMapping = false;
                self.mapper.addConnection(con);
            });


	}

	createComplexProp(typeName, children)
    {
        var prop = {};
        prop.names = [];
        prop.types = [];

        if (children.length == 1) {
            var propName = children[0].getBasicLiteralValue();
            var struct = _.find(self.predefinedStructs, { name:typeName});
            var propType =  _.find(struct.properties, { name:propName}).type;
            prop.types.push(propType);
            prop.names.push(propName);
        } else {
            var propName = children[0].getBasicLiteralValue();
            var struct = _.find(self.predefinedStructs, { name:typeName});
            var propType =  _.find(struct.properties, { name:propName}).type;
            prop = self.createComplexProp(propName, children[1].getChildren());
            prop.types.push(propType);
            prop.names.push(propName);
        }

        return prop;
    }
	createType(name, predefinedStruct) {
        var struct = {};
        struct.name = name;
        struct.properties = [];

        _.forEach(predefinedStruct.getVariableDefinitionStatements(), stmt => {
             var property = {};
             var structPopInfo = stmt.getLeftExpression().split(" ");
             property.name  = structPopInfo[1];
             property.type  = structPopInfo[0];

             var innerStruct = _.find(self._package.getStructDefinitions(), { _structName:property.type});
             if (innerStruct != null) {
                 property.innerType = self.createType(property.type, innerStruct);
             }

             struct.properties.push(property);
        });

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
    		let arrowStart = { x: 0, y: 0 };
    		let arrowEnd = { x: 0, y: 0 };
    		let backArrowStart = { x: 0, y: 0 };
    		let backArrowEnd = { x: 0, y: 0 };
    		const dropZoneClassName = ((!innerDropZoneActivated) ? "inner-drop-zone" : "inner-drop-zone active")
    											+ ((innerDropZoneDropNotAllowed) ? " block" : "");

    		const arrowStartPointX = bBox.getRight();
    		const arrowStartPointY = this.statementBox.y + this.statementBox.h/2;
    		const radius = 10;

    		let actionInvocation;
    		let isActionInvocation = false;
    		let connector;
    		if (ASTFactory.isAssignmentStatement(model)) {
    			actionInvocation = model.getChildren()[1].getChildren()[0];
    		} else if (ASTFactory.isVariableDefinitionStatement(model)) {
    			actionInvocation = model.getChildren()[1];
    		}

    		if (actionInvocation) {
    			isActionInvocation = !_.isNil(actionInvocation) && ASTFactory.isActionInvocationExpression(actionInvocation);
    			if (!_.isNil(actionInvocation._connector)) {
    				connector = actionInvocation._connector;
    				arrowStart.x = this.statementBox.x + this.statementBox.w;
    				arrowStart.y = this.statementBox.y + this.statementBox.h/3;
    				arrowEnd.x = connector.getViewState().bBox.x + connector.getViewState().bBox.w/2;
    				arrowEnd.y = arrowStart.y;
    				backArrowStart.x = arrowEnd.x;
    				backArrowStart.y = this.statementBox.y + (2 * this.statementBox.h/3);
    				backArrowEnd.x = arrowStart.x;
    				backArrowEnd.y = backArrowStart.y;
    			}
    		}
    		const actionBbox = new SimpleBBox();
            const fill = this.state.innerDropZoneExist ? {} : {fill:'none'};
            const iconSize = 14;
    		actionBbox.w = DesignerDefaults.actionBox.width;
    		actionBbox.h = DesignerDefaults.actionBox.height;
    		actionBbox.x = bBox.x + ( bBox.w - actionBbox.w) / 2;
    		actionBbox.y = bBox.y + bBox.h + DesignerDefaults.actionBox.padding.top;
    		let statementRectClass = "transform-statement-rect";
    		if(model.isDebugHit) {
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
    							 <image className="transform-action-icon" x={expand_button_x} y={expand_button_y} width={ iconSize } height={ iconSize } onClick={(e) =>this.onExpand()} xlinkHref={ ImageUtil.getSVGIconString("expand")}/>
    						</g>
						<ActionBox
                            bBox={ actionBbox }
                            show={ this.state.active }
                            onDelete={ () => this.onDelete() }
                            onJumptoCodeLine={ () => this.onJumptoCodeLine() }
						/>

						{isActionInvocation &&
							<g>
								<circle cx={arrowStartPointX}
								cy={arrowStartPointY}
								r={radius}
								fill="#444"
								fillOpacity={0}
								onMouseOver={(e) => this.onArrowStartPointMouseOver(e)}
								onMouseOut={(e) => this.onArrowStartPointMouseOut(e)}
								onMouseDown={(e) => this.onMouseDown(e)}
								onMouseUp={(e) => this.onMouseUp(e)}/>
								{connector && <ArrowDecorator start={arrowStart} end={arrowEnd} enable={true}/>}
								{connector && <BackwardArrowDecorator start={backArrowStart} end={backArrowEnd} enable={true}/>}
							</g>
						}
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

	onDropZoneActivate (e) {
			const dragDropManager = this.context.dragDropManager,
						dropTarget = this.props.model.getParent(),
						model = this.props.model;
			if(dragDropManager.isOnDrag()) {
					if(_.isEqual(dragDropManager.getActivatedDropTarget(), dropTarget)){
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
	}

	onDropZoneDeactivate (e) {
			const dragDropManager = this.context.dragDropManager,
						dropTarget = this.props.model.getParent();
			if(dragDropManager.isOnDrag()){
					if(_.isEqual(dragDropManager.getActivatedDropTarget(), dropTarget)){
							dragDropManager.clearActivatedDropTarget();
							this.setState({innerDropZoneActivated: false, innerDropZoneDropNotAllowed: false});
					}
			}
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
    			source.setAttribute('_connector', destination)
    		});
    	}

    	onMouseUp (e) {
    		const messageManager = this.context.messageManager;
    		messageManager.reset();
    	}

    	openExpressionEditor(e){
    		let options = this.props.editorOptions;
    		let packageScope = this.context.renderingContext.packagedScopedEnvironemnt;
    		if(options){
    			new ExpressionEditor( this.statementBox , this.context.container ,
    				(text) => this.onUpdate(text), options , packageScope );
    		}
    	}

    	onUpdate(text){
    	}

    loadSchemaToComboBox(comboBoxId, name) {
        $("#" + comboBoxId).append('<option value="' + name + '">' + name + '</option>');
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
        self.mapper.addSourceType(sourceSelection);
    }

    setTarget(currentSelection, predefinedStructs) {
        var targetSelection = _.find(predefinedStructs, { name: currentSelection});
        self.mapper.addTargetType(targetSelection);
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
	 messageManager: PropTypes.instanceOf(MessageManager).isRequired,
	 container: PropTypes.instanceOf(Object).isRequired,
	 renderingContext: PropTypes.instanceOf(Object).isRequired,
	 activeArbiter: PropTypes.instanceOf(ActiveArbiter).isRequired
};


export default TransformStatementDecorator;
