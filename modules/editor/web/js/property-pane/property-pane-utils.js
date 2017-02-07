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
define(['require', 'lodash', 'jquery'], function (require, _, $) {


    var PropertyPaneUtils = {};

    PropertyPaneUtils.createPropertyForm = function (formWrapper, wrapperClass, editableProperties) {
        _.forEach(editableProperties, function (editableProperty) {
            var propertyWrapper = $("<div/>", {
                "class": wrapperClass
            }).appendTo(formWrapper);
            switch (editableProperty.propertyType) {
                case "text":
                    createTextBox(editableProperty, propertyWrapper);
                    break;
                case "checkbox":
                    createCheckBox(editableProperty, propertyWrapper);
                    break;
                case "dropdown":
                    createDropdown(editableProperty, propertyWrapper);
                    break;
                default:
                    log.error("Unknown property type found when creating property editor.");
                    throw "Unknown property type found when creating property editor.";
            }
        });

        function createTextBox(property, propertyWrapper) {
            $("<span>" + property.key + " :<span/>").appendTo(propertyWrapper);
            var propertyValue = _.isNil(property.getterMethod.call(property.model)) ? "" : property.getterMethod.call(property.model);
            var propertyInputValue = $("<input type='text' value='" + propertyValue + "'>").appendTo(propertyWrapper);
            $(propertyInputValue).on("change paste keyup", function () {
                property.setterMethod.call(property.model, $(this).val());
                var textValToUpdate = (($(this).val().length) > 11 ? ($(this).val().substring(0,11) + '...') : $(this).val());
                property.model.trigger('update-property-text', textValToUpdate, property.key);
            });
        }

        function createCheckBox(property, propertyWrapper) {
            var isChecked = property.getterMethod.call(property.model) ? "checked" : "";
            var propertyInputValue = $("<input type='checkbox' " + property.key + " " + isChecked + "/>").appendTo(propertyWrapper);
            $(propertyInputValue).change(function () {
                property.setterMethod.call($(this).val());
            });
        }

        function createDropdown(property, propertyWrapper) {
            var propertyTitle = $("<span>" + property.key + "<span/>").appendTo(propertyWrapper);
            var propertyDropDownValue = $("<select />");
            for (var val in data) {
                $("<option />", {value: val, text: data[val]}).appendTo(propertyDropDownValue);
            }

            $(propertyDropDownValue).appendTo(propertyWrapper);
        }
    };

    return PropertyPaneUtils;
});