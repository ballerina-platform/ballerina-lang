/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the 'License'); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * 'AS IS' BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

const fs = require('fs');
const path = require('path');
const request = require('request-promise');
const chalk = require('chalk');
const { spawn } = require('child_process');
const { JSDOM } = require('jsdom');

const balDiagramLib = fs.readFileSync(path.join(global.targetPath,
    'js-files-from-web/ballerina-diagram-library.js'), { encoding: "utf-8" });
let window;

function initialize() {
    window = (new JSDOM('<div id="diagram"/>', { 
        pretendToBeVisual: true, runScripts: "dangerously",
        beforeParse(window) {
            window.Element.prototype.getComputedTextLength = function() {
                return 200;
            }
            window.Element.prototype.getSubStringLength = function() {
                return 200;
            }
        }
    })).window;

    const balLibScriptEl = window.document.createElement("script");
    balLibScriptEl.textContent = balDiagramLib;
    window.document.body.appendChild(balLibScriptEl);
    
    const testScriptEl = window.document.createElement("script");
    testScriptEl.textContent = `
        window.render = (el, jsonModel, opt) => {
            // following is done because "instanceof Array" would always give false for arrays
            // created outside of jsdom environment
            const _jsonModel = JSON.parse(JSON.stringify(jsonModel));
            window.ballerinaDiagram.renderStaticDiagram(el, _jsonModel, opt);
        }

        window.generateSource = (jsonModel) => {
            const _jsonModel = JSON.parse(JSON.stringify(jsonModel));
            const tree = window.ballerinaDiagram.TreeBuilder.build(_jsonModel);
            return tree.getSource();
        }

        window.buildTree = (jsonModel) => {
            const _jsonModel = JSON.parse(JSON.stringify(jsonModel));
            return window.ballerinaDiagram.TreeBuilder.build(_jsonModel);
        }
    `;
    window.document.body.appendChild(testScriptEl);
}

function render(model) {
    window.render(
        window.document.getElementById("diagram"),
        model, {height: 100, width: 100});
}

function generateSource(model) {
    return window.generateSource(model);
}

function buildTree(model) {
    return window.buildTree(model);
}

module.exports = {
    initialize,
    render,
    generateSource,
    buildTree,
}