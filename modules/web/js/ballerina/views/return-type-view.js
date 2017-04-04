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
define(['lodash', 'jquery', 'log', 'alerts', './ballerina-view'],
    function (_, $, log, Alerts, BallerinaView) {

        /**
         * Creates the return type pane.
         * @param {Object} args - Arguments for creating the view.
         * @param {Object} args.activatorElement - The variable button which activates to show the pane.
         * @param {ServiceDefinition} args.model - The service definition model.
         * @param {Object} args.paneAppendElement - The element to which the pane should be appended to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @augments BallerinaView
         */
        var ReturnTypeView = function (args) {
            BallerinaView.call(this, args);

            this._returnTypeWrapper = undefined;
            this._deleteButton = undefined;
        };

        ReturnTypeView.prototype = Object.create(BallerinaView.prototype);
        ReturnTypeView.prototype.constructor = ReturnTypeView;

        /**
         * @inheritDoc
         * Implements the view for a return type.
         */
        ReturnTypeView.prototype.render = function (diagramRenderContext) {
            this.setDiagramRenderingContext(diagramRenderContext);

            var self = this;

            // Creating a wrapper for the return type.
            var returnTypeWrapper = $("<div/>", {
                id: this.getModel().getID(),
                class: "return-types-detail-wrapper",
                text: this.getModel().getArgumentAsString()
            }).data("model", this.getModel()).appendTo(this.getContainer());

            this._returnTypeWrapper = returnTypeWrapper.get(0);

            var deleteIcon = $("<i class='fw fw-cancel return-types-detail-close-wrapper'></i>");

            this._deleteButton = deleteIcon.get(0);

            $(deleteIcon).appendTo(returnTypeWrapper);

            // Removes the value of the argument in the model and rebind the arguments to the arguments view.
            $(deleteIcon).click(function () {
                $(returnTypeWrapper).remove();
                self.getParent().removeReturnType(self.getModel().getID());
            });
        };

        /**
         * Render the editing view of a return type.
         */
        ReturnTypeView.prototype.renderEditView = function (diagramRenderingContext) {
            var self = this;

            $(this._returnTypeWrapper).empty();

            var returnTypeEditWrapper = $("<div/>",{
                click: function(e) {e.stopPropagation();}
            }).appendTo(this._returnTypeWrapper);

            // Creating the return type dropdown.
            var returnTypeDropdown = $("<select/>").appendTo(returnTypeEditWrapper);

            $(returnTypeDropdown).select2({
                tags: true,
                selectOnClose: true,
                data : self._getTypeDropdownValues(),
                query: function (query) {
                    var data = {results: []};
                    if (!_.isNil(query.term)) {
                        _.forEach(self._getTypeDropdownValues(), function (item) {
                            if (item.text.toUpperCase().indexOf(query.term.toUpperCase()) >= 0) {
                                data.results.push(item);
                            }
                        });
                    } else {
                        data.results = self._getTypeDropdownValues();
                    }
                    query.callback(data);
                }
            });

            $(returnTypeDropdown).on("select2:open", function() {
                $(".select2-search__field").attr("placeholder", "Search");
            });

            // Setting selected value
            $(returnTypeDropdown).val(this.getModel().getType()).change();

            // Setting a default value for @PathParam and updating model when changed.
            $(returnTypeDropdown).change(function () {
                self.getModel().setType($(this).val());
            });

            if (!_.isUndefined(this.getModel().getIdentifier())) {
                // Return type identifier text box.
                var returnTypeNameInput = $("<input/>", {
                    type: "text",
                    "placeholder": "m",
                    val: this.getModel().getIdentifier()
                }).keypress(function (e) {
                    // Updating return type identifier of the model on typing.
                    var enteredKey = e.which || e.charCode || e.keyCode;
                    var newIdentifier = $(this).val() + String.fromCharCode(enteredKey);
                    try {
                        self.getModel().setIdentifier(newIdentifier);
                    } catch (error) {
                        Alerts.error(error);
                        e.stopPropagation();
                        return false;
                    }
                }).keyup(function(){
                    self.getModel().setIdentifier($(this).val());
                }).appendTo(returnTypeEditWrapper);
            }

            $(this._deleteButton).appendTo(returnTypeEditWrapper);

            // Removes the value of the argument in the model and rebind the arguments to the arguments view.
            $(this._deleteButton).click(function () {
                $(returnTypeEditWrapper).remove();
                self.getParent().removeReturnType(self.getModel().getID());
            });
        };

        /**
         * Returns an object array with support types.
         * @return {Object[]} Object array as supported data types.
         */
        ReturnTypeView.prototype._getTypeDropdownValues = function() {
            var dropdownData = [];
            // Adding items to the type dropdown.
            var bTypes = this.getDiagramRenderingContext().getEnvironment().getTypes();
            _.forEach(bTypes, function (bType) {
                dropdownData.push({id: bType, text: bType});
            });

            return dropdownData;
        };

        ReturnTypeView.prototype.getReturnTypeWrapper = function () {
            return this._returnTypeWrapper;
        };

        ReturnTypeView.prototype.getDeleteButton = function () {
            return this._deleteButton;
        };

        return ReturnTypeView;
    });
