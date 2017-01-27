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
define(['lodash', 'log', 'd3', './ballerina-view', './variables-view', 'ballerina/ast/ballerina-ast-factory', './canvas',
            'typeMapper'], function (_, log, d3, BallerinaView, VariablesView, BallerinaASTFactory, Canvas, TypeMapper) {
    var TypeStructDefinitionView = function (args) {
        Canvas.call(this, args);

        this._parentView = _.get(args, "parentView");
        this._viewOptions.offsetTop = _.get(args, "viewOptionsOffsetTop", 50);
        this._viewOptions.topBottomTotalGap = _.get(args, "viewOptionsTopBottomTotalGap", 100);
        //set panel icon for the type struct
        this._viewOptions.panelIcon = _.get(args.viewOptions, "cssClass.type_struct_icon");
        //set initial height for the type struct container svg
        this._totalHeight = 30;

        if (_.isNil(this._model) || !(BallerinaASTFactory.isTypeStructDefinition(this._model))) {
            log.error("Type Struct definition is undefined or is of different type." + this._model);
            throw "Type Struct definition is undefined or is of different type." + this._model;
        }

        if (_.isNil(this._container)) {
            log.error("Container for Type Struct definition is undefined." + this._container);
            throw "Container for Type Struct definition is undefined." + this._container;
        }
        this.init();
    };

    TypeStructDefinitionView.prototype = Object.create(Canvas.prototype);
    TypeStructDefinitionView.prototype.constructor = Canvas;

    TypeStructDefinitionView.prototype.init = function(){
        //Registering event listeners
        this.listenTo(this._model, 'child-removed', this.childViewRemovedCallback);
    };

    TypeStructDefinitionView.prototype.canVisitTypeStructDefinition = function (typeStructDefinition) {
        return true;
    };

    /**
     * Rendering the view of the Type struct definition.
     * @param {Object} diagramRenderingContext - the object which is carrying data required for rendering
     */
    TypeStructDefinitionView.prototype.render = function (diagramRenderingContext) {
        this._diagramRenderingContext = diagramRenderingContext;
        console.log("QQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQ");
        console.log(diagramRenderingContext);
        var struct = this._model.getSchemaPropertyObj();
        var category = this._model.getCategory();

        var mapper = new TypeMapper(this._model.getOnConnectInstance(),this._model.getOnDisconnectInstance());

        if(category == "SOURCE"){
            mapper.addSourceStruct(struct);
        }else{
            mapper.addTargetStruct(struct);
        }
    };

    TypeStructDefinitionView.prototype.getChildContainer = function () {
        return this._rootGroup;
    };

//    /**
//     * Receives attributes connected
//     * @param connection object
//     */
//    TypeStructDefinitionView.prototype.onAttributesConnect = function (connection) {
//        alert("connected");
//    };
//
//    /**
//     * Receives the attributes disconnected
//     * @param connection object
//     */
//    TypeStructDefinitionView.prototype.onAttributesDisConnect = function (connection) {
//        alert("disconnected");
//    };

    return TypeStructDefinitionView;
});