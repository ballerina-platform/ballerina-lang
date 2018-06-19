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
            window.ballerinaDiagram.renderDiagram(el, _jsonModel, opt);
        }

        window.generateSource = (jsonModel) => {
            const _jsonModel = JSON.parse(JSON.stringify(jsonModel));
            const tree = window.ballerinaDiagram.TreeBuilder.build(_jsonModel);
            return tree.getSource();
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

module.exports = {
    initialize,
    render,
    generateSource,
}