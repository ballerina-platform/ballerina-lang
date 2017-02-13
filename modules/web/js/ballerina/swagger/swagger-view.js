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
define(['log', 'lodash', 'jquery', 'event_channel', './swagger-holder'],
   function(log, _, $, EventChannel, SwaggerHolder) {

       /**
        * @class SwaggerView
        * @augments EventChannel
        * @constructor
        * @class SwaggerView  Wraps the Swagger Editor for swagger view
        * @param {Object} args - Rendering args for the view
        * @param {String} args.container - selector for div element to render ace editor
        * @param {String} [args.content] - initial content for the editor
        */
       var SwaggerView = function (args) {
           this._options = args;
           if(!_.has(args, 'container')){
               log.error('container is not specified for rendering swagger view.')
           }
           if(!_.has(args, 'backend')){
               log.error('backend is not specified for rendering swagger view.')
           }
           this._container = _.get(args, 'container');
           this._content = _.get(args, 'content');
           this._backend = _.get(args, 'backend');
           this._generatedNodeTree = "";
           this._clean = true;
       };

       var initSwaggerEditor = function(self, content){
           self._swaggerHolder = new SwaggerHolder(function (content) {
               if(content && content!="null" && self._generatedSource){
                   var response = self._backend.call("convert-swagger", "POST", {
                       "name": "CalculatorService",
                       "description": "null",
                       "swaggerDefinition": content,
                       "ballerinaDefinition": self._generatedSource
                   }, [{name: "expectedType", value: "ballerina"}]);

                   if(!response.error && !response.errorMessage){
                       self._generatedNodeTree = JSON.parse(response.ballerinaDefinition);
                       self._clean = false;
                   }
               }
           });

           self._swaggerHolder.setSwaggerAsText(content);
           var swaggerEditor = $(self._container).find('div.swaggerEditor');
           swaggerEditor.html('<iframe class="se-iframe" width=100% height="100%"></iframe>');
           swaggerEditor.find('iframe.se-iframe').attr("src", swaggerEditor.data("editor-url"));
           var swaggerEditorWindow = $(self._container).find('div.swaggerEditor').find('iframe.se-iframe')[0].contentWindow;
           self._swaggerEditorWindow = swaggerEditorWindow;
           swaggerEditor.ready(function () {
               if (swaggerEditorWindow.setSwaggerHolder) {
                   swaggerEditorWindow.setSwaggerHolder(self._swaggerHolder);
                   swaggerEditorWindow.updateSwaggerEditor();
               } else {
                   swaggerEditorWindow.onEditorLoad = function () {
                       swaggerEditorWindow.setSwaggerHolder(self._swaggerHolder);
                       swaggerEditorWindow.updateSwaggerEditor();
                   };
               }
           });
       };

       SwaggerView.prototype = Object.create(EventChannel.prototype);
       SwaggerView.prototype.constructor = SwaggerView;

       SwaggerView.prototype.render = function () {
           initSwaggerEditor(this, "{swagger: '2.0', info: {version: '1.0.0', title: 'Swagger Resource'}, paths: {}}");
       };

       /**
        * Set the content of swagger editor.
        * @param {String} content - content for the editor.
        *
        */
       SwaggerView.prototype.setContent = function(content){
           this._generatedSource = content;
           var generatedSwagger = "{swagger: '2.0', info: {version: '1.0.0', title: 'Swagger Resource'}, paths: {}}";
           if (content) {
               var response = this._backend.call("convert-ballerina", "POST", {
                   "name": "CalculatorService",
                   "description": "null",
                   "swaggerDefinition": "null",
                   "ballerinaDefinition": content
               }, [{name: "expectedType", value: "ballerina"}]);

               if (!response.error && !response.errorMessage) {
                   generatedSwagger = response.swaggerDefinition;
               }
           }

           this._swaggerHolder.setSwaggerAsText(generatedSwagger);
           this._swaggerEditorWindow.updateSwaggerEditor();
       };

       SwaggerView.prototype.getContent = function(){
           return this._generatedNodeTree;
       };

       SwaggerView.prototype.show = function(){
           $(this._container).show();
       };

       SwaggerView.prototype.hide = function(){
           $(this._container).hide();
       };

       SwaggerView.prototype.isVisible = function(){
           return  $(this._container).is(':visible')
       };

       SwaggerView.prototype.isClean = function(){
           return this._clean;
       };

       SwaggerView.prototype.markClean = function(){
           this._clean = true;
       };

       return SwaggerView;
   });