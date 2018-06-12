const fs = require('fs');
const path = require('path');
const request = require('request-promise');
const chalk = require('chalk');
const { spawn } = require('child_process');
const { JSDOM } = require('jsdom');

const balDiagramLib = fs.readFileSync(path.join(
    __dirname, '../../../target/js-files-from-web/ballerina-diagram-library.js'), { encoding: "utf-8" });
let window;
const port = 9091;
const parserUrl = `http://127.0.0.1:${port}/composer/ballerina/parser/file/validate-and-parse`;

function test() {
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
            const _jsonModel = JSON.parse(JSON.stringify(jsonModel));
            window.ballerinaDiagram.renderDiagram(el, _jsonModel, opt);
        }
    `;
    window.document.body.appendChild(testScriptEl);

    const testResDir = path.join(__dirname, '../resources/passing');
    const testFiles = findBalFilesInDirSync(testResDir);

    testFiles.forEach(file => {
        fs.readFile(file, 'utf8', (err, content) => {
            testContent(content, file);
        })
    });
}

function testContent(content, file) {
    const parseOpts = {
        content,
        filename: 'file.bal',
        includePackageInfo: true,
        includeProgramDir: true,
        includeTree: true,
    }

    const parseReq = request.post({
        url: parserUrl,
        json: parseOpts,
    });

    parseReq.then(body => {
        const jsonModel = body.model;
        if (!jsonModel){
            console.log(chalk.yellow('Could not parse: ' + file))
            return;
        }
        window.render(
            window.document.getElementById("diagram"), jsonModel, {height: 100, width: 100});
    }).catch((e) => {
        console.log(chalk.red(file));
        console.log(e)
    });
}

function findBalFilesInDirSync(dir, filelist) {
    const files = fs.readdirSync(dir);
    filelist = filelist || [];
    files.forEach((file) => {
        if (fs.statSync(path.join(dir, file)).isDirectory()) {
            filelist = findBalFilesInDirSync(path.join(dir, file), filelist);
        } else if (path.extname(file) === '.bal') {
            filelist.push(path.join(dir, file));
        }
    });
    return filelist;
}

test();