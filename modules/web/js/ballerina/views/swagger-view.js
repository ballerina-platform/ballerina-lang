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
import log from 'log';
import _ from 'lodash';
import $ from 'jquery';
import EventChannel from 'event_channel';
import YAML from 'js-yaml';
import BallerinaASTDeserializer from './../ast/ballerina-ast-deserializer';

/**
 * Wraps the Swagger Editor for swagger view
 * @class SwaggerView
 * @extends EventChannel
 */
class SwaggerView extends EventChannel {

    /**
     * Constructor for SwaggerView
     * @param {Object} args - Rendering args for the view
     * @param {String} args.container - selector for div element to render ace editor
     * @param {String} [args.content] - initial content for the editor
     * @constructor
     */
    constructor(args) {
        super();
        this._options = args;
        if(!_.has(args, 'container')){
            log.error('container is not specified for rendering swagger view.');
        }
        if(!_.has(args, 'backend')){
            log.error('backend is not specified for rendering swagger view.');
        }
        this._container = _.get(args, 'container');
        this._content = _.get(args, 'content');
        this._backend = _.get(args, 'backend');
        this.deserializer = BallerinaASTDeserializer;
    }

    render() {
        initSwaggerEditor(this, '{swagger: \'2.0\', info: {version: \'1.0.0\', title: \'Swagger Resource\'}, paths: {}}');
    }

    /**
     * Set the content of swagger editor.
     * @param {String} content - content for the editor.
     *
     */
    setContent(content) {
        this._generatedSource = content;
        var generatedSwagger = '{swagger: \'2.0\', info: {version: \'1.0.0\', title: \'Swagger Resource\'}, paths: {}}';
        if (content) {
            var response = this._backend.call('convert-ballerina', 'POST', {
                'name': 'CalculatorService',
                'description': 'null',
                'swaggerDefinition': 'null',
                'ballerinaDefinition': content
            }, [{name: 'expectedType', value: 'ballerina'}]);

            if (!response.error && !response.message) {
                if(response.swaggerDefinition){
                    generatedSwagger = response.swaggerDefinition;
                } else {
                    throw new Error('Swagger needs at least one service');
                }
            } else {
                throw new Error('Cannot switch to swagger view due to parser error');
            }
        }
        if (!this._swaggerEditorWindow.setSwaggerEditorValue) {
            throw new Error('Couldn\'t hookup swagger editor view. Please retry!');
        }
        this._swaggerEditorWindow.setSwaggerEditorValue(YAML.safeDump(YAML.safeLoad(generatedSwagger)));
    }

    /**
     * Set the default node tree.
     * @param {Object} root root node.
     *
     */
    setNodeTree(root) {
        this._generatedNodeTree = root;
    }

    getContent() {
        var content = this._swaggerEditorWindow.getSwaggerEditorValue();
        if (content && content !== 'null' && this._generatedSource) {
            var response = this._backend.call('convert-swagger', 'POST', {
                'name': 'CalculatorService',
                'description': 'null',
                'swaggerDefinition': content,
                'ballerinaDefinition': this._generatedSource
            }, [{name: 'expectedType', value: 'ballerina'}]);

            if (!response.error && !response.errorMessage) {
                try {
                    this._generatedNodeTree = this.deserializer.getASTModel(JSON.parse(response.ballerinaDefinition));
                } catch (err) {
                    log.error('Invalid response received for swagger-to-ballerina conversion : \''
                              + response.ballerinaDefinition + '\'');
                }
            }
        }
        return this._generatedNodeTree;
    }

    show() {
        $(this._container).show();
    }

    hide() {
        $(this._container).hide();
    }

    isVisible() {
        return  $(this._container).is(':visible');
    }
}

var initSwaggerEditor = function(self, content){
    var swaggerEditor = $(self._container).find('div.swaggerEditor');
    swaggerEditor.html('<iframe class="se-iframe" width=100% height="100%"></iframe>');
    swaggerEditor.find('iframe.se-iframe').attr('src', swaggerEditor.data('editor-url'));
    var swaggerEditorWindow = $(self._container).find('div.swaggerEditor').find('iframe.se-iframe')[0].contentWindow;
    self._swaggerEditorWindow = swaggerEditorWindow;
    swaggerEditor.ready(function () {
        if (swaggerEditorWindow.setSwaggerEditorValue) {
            swaggerEditorWindow.setSwaggerEditorValue(YAML.safeDump(YAML.safeLoad(content)));
        } else {
            swaggerEditorWindow.onEditorLoad = function () {
                swaggerEditorWindow.setSwaggerEditorValue(YAML.safeDump(YAML.safeLoad(content)));
            };
        }
    });
};

export default SwaggerView;
