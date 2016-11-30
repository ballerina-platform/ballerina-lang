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
define(['d3', 'lodash', 'backbone', 'jquery', 'ace/ace'], function(d3, _, Backbone, $, ace) {

    var SourceView = Backbone.View.extend(
        /** @lends SourceView.prototype */
        {
            /**
             * @augments SourceView.View
             * @constructs
             * @class SourceView Represents the sourceview
             * @param {Object} options Rendering options for the view
             */
            initialize: function (options) {
                this.options = options;
                this.editor = ace.edit(options.sourceContainer);
                this.mainEditor = this.editor;
                //Avoiding ace warning
                this.mainEditor.$blockScrolling = Infinity;
                this.mainEditor.setTheme("ace/theme/twilight");
                this.mainEditor.session.setMode("ace/mode/ballerina");
                // var langTools = ace.require("ace/ext/language_tools");
                this.mainEditor.setOptions({
                    enableBasicAutoCompletion:true
                });
                this.mainEditor.setBehavioursEnabled(true);
                //bind auto complete to key press
                this.mainEditor.commands.on("afterExec", function(e){
                    if (e.command.name == "insertstring"&&/^[\w.]$/.test(e.args)) {
                        this.mainEditor.execCommand("startAutocomplete");
                    }
                });
            },
            
            render: function (options) {
                var container = this.options.sourceContainer;
                this.mainEditor.session.setValue(options.source);
                $(container).show();
            }
        });

    return SourceView;
});

