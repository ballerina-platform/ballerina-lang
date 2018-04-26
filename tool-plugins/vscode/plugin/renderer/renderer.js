const fs = require('fs');
const request = require('request-promise');
const { getParserService } = require('../serverStarter');

const RETRY_COUNT = 10;
const RETRY_WAIT = 2000;

let jsonModel;

function render (content, retries=0) {
    const parseOpts = {
        content,
        filename: 'file.bal',
        includePackageInfo: true,
        includeProgramDir: true,
        includeTree: true,
    }

    return getParserService()
    .then(({port}) => {
        return request.post({
            url: `http://127.0.0.1:${port||9091}/composer/ballerina/parser/file/validate-and-parse`,
            json: parseOpts,
        })
    })
    .then((body) => {
        if (body.model) {
            jsonModel = body.model;
        }
        return renderDiagram(jsonModel);
    })
    .catch((e) => {
        return new Promise((res, rej) => {
            if (retries > RETRY_COUNT) {
                res(renderError());
                return;
            }

            setTimeout(() => {
                console.log('Retrying rendering');
                res(render(content, retries + 1));
            }, RETRY_WAIT);
        });
    });
};

function renderDiagram(jsonModelObj) {
    const jsonModel = JSON.stringify(jsonModelObj);

    const page = `
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link rel="stylesheet" href="file://${__dirname}/resources/bundle.css" />
        <link rel="stylesheet" href="file://${__dirname}/resources/theme.css" />
        <link rel="stylesheet" href="file://${__dirname}/resources/less.css" />
        <style>
            .overlay {
                display: none;
            }
            .drop-zone.rect {
                fill-opacity: 0;
            }
            #diagram {
                overflow: scroll;
                height : 100%;
            }
            #errors .warning {
                color:rgb(230, 70, 70);
            }
            #errors {
                padding: 10px;
            }
        </style>
    </head>

    <body>
    <div class="ballerina-editor design-view-container" id="diagram">
    </div>
    </body>
    <script charset="UTF-8" src="file://${__dirname}/resources/ballerina-diagram-library.js"></script>
    <script>
        (function() {
            const json = ${jsonModel};

            if (!json) {
                drawError("Could not parse")
            }

            function drawDiagram() {
                try {
                    ballerinaDiagram.renderDiagram(document.getElementById("diagram"), json, {
                        width: window.innerWidth - 6, height: window.innerHeight
                    });
                } catch(e) {
                    drawError(e.stack);
                }
            }

            function drawError(message) {
                document.getElementById("diagram").innerHTML = \`
                <div id="errors">
                    <span class="warning fw fw-error"></span>
                    <span>\$\{message\}
                </div>
                \`;
            }
            
            window.onresize = drawDiagram;
            drawDiagram();
        }());
    </script>
    </html>
    `;

    return page;
}

function renderError() {
    return `
    <!DOCTYPE html>
    <html>
    
    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">
    </head>
    <body>
    <div>
        Could not connect to the parser service. Please try again after restarting vscode.
        <a href="command:workbench.action.reloadWindow">Restart</a>
    </div>
    </body>
    </html>
    `;
}

module.exports.render = render;
module.exports.activate = getParserService;
