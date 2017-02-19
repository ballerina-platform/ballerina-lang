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
    expressionEditorUtil.createEditor = function (editorWrapper, wrapperClass, property, callback) {
        var propertyWrapper = $("<div/>", {
            "class": wrapperClass
        }).appendTo(editorWrapper);

        var propertyValue = _.isNil(property.getterMethod.call(property.model)) ? "" : property.getterMethod.call(property.model);
        var propertyInputValue = $("<input type='text' value=''>").appendTo(propertyWrapper);
        var hiddenSpan = $('<span style="display: none"/>').appendTo(propertyWrapper);
        hiddenSpan.css('white-space', 'pre');
        hiddenSpan.css('padding-left', '15px');
        $(propertyInputValue).css('border', '1px solid');
        $(propertyInputValue).css('padding-left', '15px');
        $(propertyInputValue).val(propertyValue);

        // returns the width in pixels needed to show a given text
        // This adds the text to the hidden span and takes its width
        // This is done so that we can measure the exact width needed for the input to show the text
        function getNecessaryWidth(text) {
            hiddenSpan.text(text + 'abc'); // Add 3 charactors so there is a buffer length
            return hiddenSpan.outerWidth();
        }

        $(propertyInputValue).css("width", getNecessaryWidth(propertyValue));

        $(propertyInputValue).on("paste keyup", function (e) {
            var width = getNecessaryWidth($(this).val());
            $(this).css("width", width);

            if(e.keyCode==13){
                // Enter pressed.
                if(_.isFunction(callback)){
                    callback();
                }
            }
        });
        $(propertyInputValue).on("change", function(){
            // Do not set the value to the model directly, instead fire the event.
            property.model.trigger('update-property-text', $(this).val(), property.key);
        });
    };
    return expressionEditorUtil;
});
