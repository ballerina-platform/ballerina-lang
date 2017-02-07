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
           this._container = _.get(args, 'container');
           this._content = _.get(args, 'content');
       };

       var initSwaggerEditor = function(self, content){
           self._swaggerHolder = new SwaggerHolder();
           parent.SwaggerHolder = self._swaggerHolder;// Allowing Swagger Editor to inject this swagger holder
           var swaggerEditor = $(self._container).find('div.swaggerEditor');
           swaggerEditor.html('<iframe class="se-iframe" width=100% height="100%"></iframe>');
           swaggerEditor.find('iframe.se-iframe').attr("src", swaggerEditor.data("editor-url"));
           swaggerEditor.ready(function () {
               self._swaggerHolder.setSwaggerAsText(content);
               self._swaggerHolder.getEditor().updateSwaggerEditor();
           });
       };

       SwaggerView.prototype = Object.create(EventChannel.prototype);
       SwaggerView.prototype.constructor = SwaggerView;

       SwaggerView.prototype.render = function () {
           initSwaggerEditor(this, {swagger: '2.0', info: {version: '1.0.0', title: 'Swagger Resource'}, paths: {}});
       };

       /**
        * Set the content of swagger editor.
        * @param {String} content - content for the editor.
        *
        */
       SwaggerView.prototype.setContent = function(content){
           if (!this._swaggerHolder.getEditor()) {
               initSwaggerEditor(this, content);
           } else {
               this._swaggerHolder.setSwaggerAsText(content);
               this._swaggerHolder.getEditor().updateSwaggerEditor();
           }
       };

       SwaggerView.prototype.getContent = function(){
           return this._swaggerHolder.getSwagger();
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

       return SwaggerView;
   });