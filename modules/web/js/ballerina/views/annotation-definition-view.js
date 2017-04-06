/**
 * Copyright (c) 2016-2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import $ from 'jquery';
import SVGCanvas from '../../../../../integration-tests/ui-integration-tests/js/ballerina/views/svg-canvas';
import Point from '../../../../../integration-tests/ui-integration-tests/js/ballerina/views/point';
import AnnotationDefinition from './../ast/annotation-definition';
import BallerinaASTFactory from 'ballerina/ast/ballerina-ast-factory';
import Axis from '../../../../../integration-tests/ui-integration-tests/js/ballerina/views/axis';
import Alerts from 'alerts';

/**
 * The view to represent an annotation definition which is an AST Visitor.
 * */
class AnnotationDefinitionView extends SVGCanvas {
    constructor(args) {
        super(args);

        this._annotationName = _.get(args, 'definitionName', "");
        this._attachedDefinitions = _.get(args, 'attachedDefinitions', []);
        this._annotationProperties = _.get(args, 'annotationProperties', []);
        this._parentView = _.get(args, 'parentView');
        this._viewOptions.offsetTop = _.get(args, 'viewOptionsOffsetTop', 75);
        this._viewOptions.topBottomTotalGap = _.get(args, 'viewOptionsTopBottomTotalGap', 100);
        this._viewOptions.panelIcon = _.get(args.viewOptions, 'cssClass.service_icon');
        this._viewOptions.minHeight = _.get(args, 'minHeight', 300);

        this._totalHeight = 170;

        if (_.isNil(this._model) || !(this._model instanceof AnnotationDefinition)) {
            log.error('Annotation definition is undefined or is of different type.' + this._model);
            throw 'Annotation definition is undefined or is of different type.' + this._model;
        }

        if (_.isNil(this._container)) {
            log.error('Container for annotation definition is undefined.' + this._container);
            throw 'Container for annotation definition is undefined.' + this._container;
        }
    }

    setModel(model) {
        if (!_.isNil(model) && model instanceof AnnotationDefinition) {
            this._model = model;
        } else {
            log.error('Annotation definition is undefined or is of different type.' + model);
            throw 'Annotation definition is undefined or is of different type.' + model;
        }
    }

    setContainer(container) {
        if (!_.isNil(container)) {
            this._container = container;
        } else {
            log.error('Container for annotation definition is undefined.' + container);
            throw 'Container for annotation definition is undefined.' + container;
        }
    }

    setViewOptions(viewOptions) {
        this._viewOptions = viewOptions;
    }

    getModel() {
        return this._model;
    }

    getContainer() {
        return this._container;
    }

    getViewOptions() {
        return this._viewOptions;
    }

    render(diagramRenderingContext) {
        this.setDiagramRenderingContext(diagramRenderingContext);
        this.drawAccordionCanvas(this._viewOptions, this.getModel().getID(), this.getModel().type.toLowerCase(),
            this.getModel().getAnnotationName());

        this.getPanelIcon().addClass(_.get(this._viewOptions, "cssClass.service_icon", ""));
        var currentContainer = $('#' + this.getModel().getID());
        this._container = currentContainer;
        this._package = diagramRenderingContext.getPackagedScopedEnvironment().getCurrentPackage();

        var annotationDefinitionContainerId = "annotation-definition-container___" + this._model.id;
        var annotationDefinitionContainer = $('<div id="' + annotationDefinitionContainerId +
            '" class="annotation-definition-container"><div>' +
            '<span>Annotation:</span>' +
            '<input type="text" placeholder="eg:-Param"/>' +
            '<span>attach to:</span>' +
            '<input type="text" placeholder="eg:-service,function"/>' +
            '</div>' +
            '</div>');
        currentContainer.find('svg').parent().append(annotationDefinitionContainer);
        currentContainer.find('svg').remove();
    }
}

export default AnnotationDefinitionView;