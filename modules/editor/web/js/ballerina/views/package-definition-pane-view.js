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
define(['require', 'lodash', 'jquery', 'log', './../ast/package-definition',
    'ballerina/ast/ballerina-ast-factory'],
    function (require, _, $, log, PackageDefinition, BallerinaASTFactory) {

        /**
         * Creates a new instance for a package definition pane view.
         * @param args - Arguments for creating the package definition pane.
         * @param {BallerinaASTRoot} args.model - The Ballerina AST root model.
         * @param {HTMLElement} args.paneAppendElement - The element to which the pane to be appended.
         * @param {BallerinaFileEditor} args.view - The ballerina filed editor view.
         * @constructor
         */
        var PackageDefinitionPaneView = function(args) {
            this._model = _.get(args, "model");
            this._paneAppendElement = _.get(args, "paneAppendElement");
            this._viewOfModel = _.get(args, "view");
            this._container = _.get(args, "container");
        };

        PackageDefinitionPaneView.prototype.constructor = PackageDefinitionPaneView;

        /**
         * Creates the pane view of the package definition.
         */
        PackageDefinitionPaneView.prototype.render = function() {

            var currentASTRoot = this._model;
            var currentContainer = this._container;

            var packageWrapper = $("<div/>", {
                                    class: "package-definition-main-wrapper"
                                    }).insertBefore($(currentContainer).find('.constant-definition-main-wrapper'));
            
            // Creating package button.
            var packageDefinitionsButton = $("<div class='package-name-btn'></div>")
                                                .appendTo(packageWrapper);

            var collpaser = $("<div class='package-add-icon-wrapper'/>").appendTo(packageWrapper);

            $("<i class='fw fw-right'></i>").appendTo(collpaser);

            var packageButtonIcon = $("<span class='' data-toggle='tooltip' title='Package Name' data-placement='bottom'>Package</span>")
                .appendTo(packageDefinitionsButton).tooltip();

            var packageDefinitionsMainWrapper = $("<span class='package-pane'/>")
                                                    .appendTo(packageDefinitionsButton);

            $("<span class='package-name-wrapper'>" +
                "<input type='text' autocomplete='off' id='package-name-input'></span>")
                .appendTo(packageDefinitionsMainWrapper);

            var packageInput = packageWrapper.find('#package-name-input');

            //get the value from source
            packageInput.val((!_.isUndefined(currentASTRoot.getPackageName())) ?
                currentASTRoot.getPackageName() : "");

            //set the value to source
            packageInput.on("change", function () {
                currentASTRoot.setPackageName($(this).val());
            });

            //handle click event on package-btn
            $(packageButtonIcon).click(function (e) {
                $(packageDefinitionsMainWrapper).toggle();
            });
            

        }

        return PackageDefinitionPaneView;
    });