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
import _ from 'lodash';
import log from 'log';
import StatementVisitor from './../visitors/statement-visitor';
import * as d3 from 'd3';
import D3Utils from 'd3utils';
import Point from './point';
import BBox from './bounding-box';
import ExpressionEditor from 'expression_editor_utils';
import DebugManager from 'debugger/debug-manager';
import $ from 'jquery';

/**
 * A common class which consists functions of moving or resizing views.
 * @class BallerinaStatementView
 * @extends StatementVisitor
 */
class BallerinaStatementView extends StatementVisitor {
    /**
     * @param {object} args - Constructor arguments
     * @constructor
     */
    constructor(args) {
        super(args);
        this._parent = _.get(args, 'parent');
        this._model = _.get(args, 'model');
        this._container = _.get(args, 'container');
        this._viewOptions = _.get(args, 'viewOptions');
        this.toolPalette = _.get(args, 'toolPalette');
        this.messageManager = _.get(args, 'messageManager');
        this._statementGroup = undefined;
        this._childrenViewsList = [];
        if (!_.has(args, 'topCenter')) {
            log.warn('topCenter has not defined. Default top center will be created');
        }
        this._topCenter = _.has(args, 'topCenter') ? _.get(args, 'topCenter').clone() : new Point(0,0);
        this._bottomCenter = _.has(args, 'bottomCenter') ? _.get(args, 'bottomCenter').clone() : new Point(0,0);
        this._boundingBox = new  BBox();
        this._isEditControlsActive = false;
        var self = this;
        this._topCenter.on('moved', function(offset){
            self._bottomCenter(offset.dx, offset.dy);
        });
        this.init();
    }

    init() {
        //Registering event listeners
        this._parent.on('changeStatementMetricsEvent', this.changeMetricsCallback, this);
        this._model.on('child-removed', this.childRemovedCallback, this);
        this._model.on('before-remove', this.onBeforeModelRemove, this);
    }

    /**
     * Remove statement view callback
     */
    onBeforeModelRemove() {
        d3.select('#_' +this._model.id).remove();
        // resize the bounding box in order to the other objects to resize
        this.getBoundingBox().h(0).w(0);
    }

    /**
     * Child remove callback
     */
    childRemovedCallback() {
    }

    setParent(parent) {
        this._parent = parent;
    }

    getParent() {
        return this._parent;
    }

    getStatementGroup() {
        return this._statementGroup;
    }

    /**
     * Sets the statement group of this view.
     * @param statementGroup {object} statement group to be set
     */
    setStatementGroup(statementGroup) {
        this._statementGroup = statementGroup;
    }

    getChildrenViewsList() {
        return this._childrenViewsList;
    }

    changeWidth() {
    }

    changeHeight() {
    }

    changeMetricsCallback() {
    }

    getDiagramRenderingContext() {
        return this._diagramRenderingContext;
    }

    renderEditView(editableProperties, statementGroup, viewOptions) {
        var self = this;

        // Get the bounding box of the if else view.
        var statementBoundingBox = self.getBoundingBox();

        // Calculating width for edit and delete button.
        var propertyButtonPaneRectWidth = viewOptions.actionButton.width * 3;

        // Creating an SVG group for the edit and delete buttons.
        var propertyButtonPaneGroup = D3Utils.group(statementGroup);

        // Delete button pane group
        var deleteButtonPaneGroup = D3Utils.group(statementGroup);

        // Adding svg definitions needed for styling delete button.
        var svgDefinitions = deleteButtonPaneGroup.append('defs');

        var deleteButtonPattern = svgDefinitions.append('pattern')
            .attr('id', 'statementDeleteIcon')
            .attr('width', '100%')
            .attr('height', '100%');

        deleteButtonPattern.append('image')
            .attr('xlink:href', 'images/delete.svg')
            .attr('x', (viewOptions.actionButton.width) - (36 / 2))
            .attr('y', (viewOptions.actionButton.height / 2) - (14 / 2))
            .attr('width', '14')
            .attr('height', '14');

        var addBreakpointButtonPattern = svgDefinitions.append('pattern')
            .attr('id', 'addBreakpointIcon')
            .attr('width', '100%')
            .attr('height', '100%');

        addBreakpointButtonPattern.append('image')
            .attr('xlink:href', 'images/debug-point.svg')
            .attr('x', (viewOptions.actionButton.width) - (36 / 2))
            .attr('y', (viewOptions.actionButton.height / 2) - (14 / 2))
            .attr('width', '14')
            .attr('height', '14');

        addBreakpointButtonPattern.append('image')
            .attr('xlink:href', 'images/debug-point.svg')
            .attr('x', (viewOptions.actionButton.width) - (36 / 2))
            .attr('y', (viewOptions.actionButton.height / 2) - (14 / 2))
            .attr('width', '14')
            .attr('height', '14');

        var jumpButtonPattern = svgDefinitions.append('pattern')
            .attr('id', 'statementJumpIcon')
            .attr('width', '100%')
            .attr('height', '100%');

        jumpButtonPattern.append('image')
            .attr('xlink:href', 'images/code-design.svg')
            .attr('x', (viewOptions.actionButton.width) - (36 / 2))
            .attr('y', (viewOptions.actionButton.height / 2) - (14 / 2))
            .attr('width', '14')
            .attr('height', '14');

        // Bottom center point.

        var centerPointX = statementBoundingBox.x()+ (statementBoundingBox.w() / 2);
        var centerPointY = statementBoundingBox.y()+ statementBoundingBox.h();

        var smallArrowPoints =
            // Bottom point of the polygon.
            ' ' + centerPointX + ',' + centerPointY +
                // Left point of the polygon
            ' ' + (centerPointX - 3) + ',' + (centerPointY + 3) +
                // Right point of the polygon.
            ' ' + (centerPointX + 3) + ',' + (centerPointY + 3);

        var smallArrow = D3Utils.polygon(smallArrowPoints, statementGroup);

        // Creating the action button pane border.
        var propertyButtonPaneRect = D3Utils.rect(centerPointX - (propertyButtonPaneRectWidth / 2), centerPointY + 3,
            propertyButtonPaneRectWidth, viewOptions.actionButton.height, 0, 0, deleteButtonPaneGroup)
            .classed(viewOptions.actionButton.wrapper.class, true);

        // Not allowing to click background elements.
        $(propertyButtonPaneRect.node()).click(function(event){
            event.stopPropagation();
        });

        // Creating the edit action button.
        var deleteButtonRect = D3Utils.rect(centerPointX - (propertyButtonPaneRectWidth / 2), centerPointY + 3,
            propertyButtonPaneRectWidth / 2, viewOptions.actionButton.height, 0, 0, deleteButtonPaneGroup)
            .classed(viewOptions.actionButton.class, true).classed(viewOptions.actionButton.deleteClass, true);

        // Creating the add breakpoint action button.
        var toggleBreakpointButtonRect = D3Utils.rect(centerPointX + viewOptions.actionButton.width - (propertyButtonPaneRectWidth / 2), centerPointY + 3,
            propertyButtonPaneRectWidth / 2, viewOptions.actionButton.height, 0, 0, deleteButtonPaneGroup)
            .classed(viewOptions.actionButton.class, true).classed(viewOptions.actionButton.breakpointClass, true);

        // Creating the edit action button.
        var jumpButtonRect = D3Utils.rect(centerPointX + viewOptions.actionButton.width * 2 - (propertyButtonPaneRectWidth / 2), centerPointY + 3,
            propertyButtonPaneRectWidth / 2, viewOptions.actionButton.height, 0, 0, deleteButtonPaneGroup)
            .classed(viewOptions.actionButton.class, true).classed(viewOptions.actionButton.jumpClass, true);

        // show a remove breakpoint icon if there is a breakpoint already
        var fileName = self.getDiagramRenderingContext().ballerinaFileEditor._file.getName();
        var lineNumber = self._model.getLineNumber();
        var hasActiveBreakPoint = DebugManager.hasBreakPoint(lineNumber, fileName);
        toggleBreakpointButtonRect.classed(viewOptions.actionButton.breakpointActiveClass, hasActiveBreakPoint);

        if (_.size(editableProperties) > 0) {
            // 175 is the width set in css
            var editPaneLocation = this._getEditPaneLocation();
            var propertyPaneWrapper = $('<div/>', {
                class: viewOptions.propertyForm.wrapper.class,
                css: {
                    'width': (statementBoundingBox.w() + 1), // Making the text box bit bigger than the statement box
                    'height': 32 // Height for the expression editor box.
                },
                click: function (event) {
                    event.stopPropagation();
                }
            }).offset({
                top: editPaneLocation.y(), // Adding the textbox bit bigger than the statement box.
                left: editPaneLocation.x()
            }).appendTo(propertyButtonPaneGroup.node().ownerSVGElement.parentElement);


            // Div which contains the form for the properties.
            var propertyPaneBody = $('<div/>', {
                'class': viewOptions.propertyForm.body.class
            }).appendTo(propertyPaneWrapper);

            this.expressionEditor = new ExpressionEditor(propertyPaneBody, viewOptions.propertyForm.body.property.wrapper,
                editableProperties, function () {
                    self.trigger('edit-mode-disabled');
            });
        }

        $(deleteButtonRect.node()).click(function(event){
            event.stopPropagation();
            self._model.remove();
            self.trigger('edit-mode-disabled');
            $(statementGroup).remove();
        });

        $(jumpButtonRect.node()).click(function(event){
            //@todo implement
        });

        $(toggleBreakpointButtonRect.node()).click(function(event){
            // TODO: handle line number  is not defined for new nodes
            event.stopPropagation();
            // Hiding property button pane.
            self.trigger('edit-mode-disabled');
            var fileName = self.getDiagramRenderingContext().ballerinaFileEditor._file.getName();
            var lineNumber = self._model.getLineNumber();
            if(DebugManager.hasBreakPoint(lineNumber, fileName)) {
                DebugManager.removeBreakPoint(lineNumber, fileName);
            } else {
                DebugManager.addBreakPoint(lineNumber, fileName);
            }
            $(this).toggleClass('active');
        });

        $(jumpButtonRect.node()).click(function(event){
            event.stopPropagation();
        });

        this._isEditControlsActive = true;

        this.once('edit-mode-disabled', () => {
            this.expressionEditor.distroy();
            $(propertyPaneWrapper).remove();
            $(propertyButtonPaneGroup.node()).remove();
            $(deleteButtonPaneGroup.node()).remove();
            $(smallArrow.node()).remove();
            this._isEditControlsActive = false;
        });

    }

    _getEditPaneLocation() {
        var statementBoundingBox = this.getBoundingBox();
        return new Point((statementBoundingBox.x() - 1), (statementBoundingBox.y() - 1));
    }

    _createPropertyPane(args) {
        var viewOptions = _.get(args, 'viewOptions', {});
        var statementGroup = _.get(args, 'statementGroup', null);
        var editableProperties = _.get(args, 'editableProperties', []);

        viewOptions.actionButton = _.get(args, 'viewOptions.actionButton', {});
        viewOptions.actionButton.class = _.get(args, 'actionButton.class', 'property-pane-action-button');
        viewOptions.actionButton.wrapper = _.get(args, 'actionButton.wrapper', {});
        viewOptions.actionButton.wrapper.class = _.get(args, 'actionButton.wrapper.class', 'property-pane-action-button-wrapper');
        viewOptions.actionButton.disableClass = _.get(args, 'viewOptions.actionButton.disableClass', 'property-pane-action-button-disable');
        viewOptions.actionButton.deleteClass = _.get(args, 'viewOptions.actionButton.deleteClass', 'property-pane-action-button-delete');
        viewOptions.actionButton.jumpClass = _.get(args, 'viewOptions.actionButton.jumpClass', 'property-pane-action-button-jump');
        viewOptions.actionButton.breakpointClass = _.get(args, 'viewOptions.actionButton.breakpointClass', 'property-pane-action-button-breakpoint');
        viewOptions.actionButton.breakpointActiveClass = _.get(args, 'viewOptions.actionButton.breakpointActiveClass', 'active');

        viewOptions.actionButton.width = _.get(args, 'viewOptions.action.button.width', 22);
        viewOptions.actionButton.height = _.get(args, 'viewOptions.action.button.height', 22);

        viewOptions.propertyForm = _.get(args, 'propertyForm', {});
        viewOptions.propertyForm.wrapper = _.get(args, 'propertyForm.wrapper', {});
        viewOptions.propertyForm.wrapper.class = _.get(args, 'propertyForm.wrapper', 'expression-editor-form-wrapper');
        viewOptions.propertyForm.heading = _.get(args, 'propertyForm.heading', {});
        viewOptions.propertyForm.body = _.get(args, 'propertyForm.body', {});
        viewOptions.propertyForm.body.class = _.get(args, 'propertyForm.body.class', 'expression-editor-form-body');
        viewOptions.propertyForm.body.property = _.get(args, 'propertyForm.body.property', {});
        viewOptions.propertyForm.body.property.wrapper = _.get(args, 'propertyForm.body.property.wrapper',
            'expression-editor-form-body-property-wrapper');

        var self = this;
        $(statementGroup.node()).click(function (event) {
            // make current active editors close
            $(window).click();

            self.trigger('edit-mode-enabled');
            event.stopPropagation();
            $(window).click(function () {
                self.trigger('edit-mode-disabled');
                $(this).unbind('click');
            });
        });

        this.on('edit-mode-enabled', function() {
            // already in edit mode
            if(self._isEditControlsActive){
                return;
            }
            self.renderEditView(editableProperties, statementGroup, viewOptions);
        });
    }

    /**
     * create view to indicate a debug point in view.  call showDebugIndicator/hideDebugIndicator to toggle this
     * indicator
    */
    _createDebugIndicator(args) {
        var self = this;
        var model = this._model;
        var viewOptions = _.get(args, 'viewOptions', {});
        var statementGroup = _.get(args, 'statementGroup', null);

        viewOptions.breakpointIndicator = _.get(args, 'viewOptions.breakpointIndicator', {});
        viewOptions.breakpointIndicator.width = _.get(args, 'viewOptions.breakpoint.width', 22);
        viewOptions.breakpointIndicator.height = _.get(args, 'viewOptions.breakpoint.height', 22);
        viewOptions.breakpointIndicator.class = _.get(args, 'breakpointIndicator.class', 'statement-view-breakpoint-indicator');

        var debugIndicatorGroup = D3Utils.group(statementGroup);

        // Adding svg definitions needed for styling delete button.
        var svgDefinitions = debugIndicatorGroup.append('defs');

        var debugIndicatorPattern = svgDefinitions.append('pattern')
            .attr('id', 'debugIcon')
            .attr('width', '100%')
            .attr('height', '100%');

        debugIndicatorPattern.append('image')
            .attr('xlink:href', 'images/debug-point.svg')
            .attr('x', (viewOptions.breakpointIndicator.width) - (36 / 2))
            .attr('y', (viewOptions.breakpointIndicator.height / 2) - (14 / 2))
            .attr('width', '14')
            .attr('height', '14');

        var removeBreakpoint = svgDefinitions.append('pattern')
             .attr('id', 'debugRemoveIcon')
             .attr('width', '100%')
             .attr('height', '100%');

        removeBreakpoint.append('image')
             .attr('xlink:href', 'images/debug-point-remove.svg')
             .attr('x', (viewOptions.breakpointIndicator.width) - (36 / 2))
             .attr('y', (viewOptions.breakpointIndicator.height / 2) - (14 / 2))
             .attr('width', '14')
             .attr('height', '14');

        var statementBoundingBox = this.getBoundingBox();
        var pointX = statementBoundingBox.x() + statementBoundingBox.w() - viewOptions.breakpointIndicator.width +
            (viewOptions.breakpointIndicator.width /2);
        var pointY = statementBoundingBox.y() - (viewOptions.breakpointIndicator.height/2);

        var removeBreakpointButton = D3Utils.rect(pointX, pointY,
            viewOptions.breakpointIndicator.width, viewOptions.breakpointIndicator.height, 0, 0, debugIndicatorGroup)
            .classed(viewOptions.breakpointIndicator.class, true).classed(viewOptions.breakpointIndicator.class, true);

        $(removeBreakpointButton.node()).click(function(event){
            event.stopPropagation();
            var fileName = self.getDiagramRenderingContext().ballerinaFileEditor._file.getName();
            DebugManager.removeBreakPoint(model.getLineNumber(), fileName);
        });

        this._debugIndicator = removeBreakpointButton;

        this.getBoundingBox().on('left-edge-moved', function(dx) {
            removeBreakpointButton.attr('x', parseFloat(removeBreakpointButton.attr('x')) + dx);
        });

        this.getBoundingBox().on('top-edge-moved', function (dy) {
            removeBreakpointButton.attr('y', parseFloat(removeBreakpointButton.attr('y')) + dy);
        });

        // resume state on rerendering
        var file = self.getDiagramRenderingContext().ballerinaFileEditor._file.getName();
        var hasBreakPoint = DebugManager.hasBreakPoint(model.getLineNumber(), file);
        if(hasBreakPoint) {
            this.showDebugIndicator();
        } else {
            this.hideDebugIndicator();
        }
    }

    showDebugIndicator() {
        $(this._debugIndicator.node()).show();
    }

    hideDebugIndicator() {
        $(this._debugIndicator.node()).hide();
    }

    getTopCenter() {
        return this._topCenter;
    }

    getViewOptions() {
        return this._viewOptions;
    }

    /**
     * Returns the bounding box of this view.
     * @return {BBox}
     */
    getBoundingBox() {
        return this._boundingBox;
    }

    repositionStatement(options) {
        this.getBoundingBox().y(this.getBoundingBox().y() + options.dy);
        this.getStatementGroup().attr('transform', ('translate(0,' + options.dy + ')'));
    }

    /**
     * Set the diagram rendering context
     * @param {object} diagramRenderingContext
     */
    setDiagramRenderingContext(diagramRenderingContext) {
        this._diagramRenderingContext = diagramRenderingContext;
    }

    /**
     * Validate the node type on focus out of the statement's expression editor
     */
    validateNode() {

    }
}

export default BallerinaStatementView;
