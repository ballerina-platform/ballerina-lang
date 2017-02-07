/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
define(['require', 'lodash', 'jquery'], function (require, _, $) {
    var expressionEditorUtil = {};
    expressionEditorUtil.createEditor = function (editorWrapper, wrapperClass, property) {
        var propertyWrapper = $("<div/>", {
            "class": wrapperClass
        }).appendTo(editorWrapper);

        var widthMultiFactor = 8; // Factor used to calculate what should be the width of textbox according to the text.
        var propertyValue = _.isNil(property.getterMethod.call(property.model)) ? "" : property.getterMethod.call(property.model);
        var propertyInputValue = $("<input type='text' value=''>").appendTo(propertyWrapper);
        $(propertyInputValue).css('border', '1px solid');
        $(propertyInputValue).focus();
        $(propertyInputValue).val(propertyValue);
        $(propertyInputValue).css("width", ((propertyValue.length + 1) * widthMultiFactor) + "px");
        $(propertyInputValue).on("change paste keyup", function () {
            $(this).css("width", (($(this).val().length + 1) * widthMultiFactor) + "px");
            // Do not set the value to the model directly, instead fire the event.
            property.model.trigger('update-property-text', $(this).val(), property.key);
        });
    };
    return expressionEditorUtil;
});