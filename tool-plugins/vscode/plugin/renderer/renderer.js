const request = require('request-promise');
const log = require('../logger');

const RETRY_COUNT = 5;
const RETRY_WAIT = 1000;

let jsonModel;

function render (content, langClient, resourceRoot, retries=1) {
    const parseOpts = {
        content,
        filename: 'file.bal',
        includePackageInfo: true,
        includeProgramDir: true,
        includeTree: true,
    }
    return langClient.sendRequest("ballerinaParser/parseContent", parseOpts)
        .then((body) => {
            let stale = true;
            if (body.model) {
                stale = false;
                jsonModel = body.model;
            }
            return renderDiagram(content, jsonModel, resourceRoot, stale);
        })
        .catch((e) => {
            log(`Error in parser service`);
            return new Promise((res, rej) => {
                if (retries > RETRY_COUNT) {
                    log.append('Could not render');
                    res(renderError());
                    return;
                }

                setTimeout(() => {
                    log(`Retrying rendering ${retries}/${RETRY_COUNT}\n`);
                    res(render(content, retries + 1));
                }, RETRY_WAIT);
            });
        });
};

function renderDiagram(content, jsonModelObj, resourceRoot, stale) {
    const jsonModel = JSON.stringify(jsonModelObj);

    const page = `
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link rel="stylesheet" href="${resourceRoot}/bundle.css" />
        <link rel="stylesheet" href="${resourceRoot}/theme.css" />
        <link rel="stylesheet" href="${resourceRoot}/less.css" />
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
            #errors {
                display: table;
                width: 100%;
                height: 100%;
            }
            #errors span { 
                display: table-cell;
                vertical-align: middle;
                text-align: center;
            }
            #warning {
                position: absolute;
                top: 15px;
                position: absolute;
                overflow: hidden;
                height: 25px;
                vertical-align: bottom;
                text-align: center;
                color: rgb(255, 90, 30);
                width: 100%;
            }
            #warning p {
                line-height: 25px;
            }
        </style>
    </head>

    <body>
    <div id="warning">
    </div>
    <div class="ballerina-editor design-view-container" id="diagram">
    </div>
    </body>
    <script charset="UTF-8" src="${resourceRoot}/ballerina-diagram-library.js"></script>
    <script>
        (function() {
            const content = ${JSON.stringify(content)};
            const json = ${jsonModel};
            const stale = ${JSON.stringify(stale)};

            if (stale) {
                showWarning('Cannot update design view due to syntax errors.')
            }

            if (!json) {
                return;
            }

            function parseContent(contenst) {
                console.log('parsing ' + JSON.stringify(json));
                return Promise.resolve({ model: json });
            }

            function onChange(newContent) {
                console.log(newContent);
            }

            function drawDiagram() {
                try {
                    let width = window.innerWidth - 6;
                    let height = window.innerHeight;
                    console.log('rendering ' + width);
                    ballerinaDiagram.renderEditableDiagram(document.getElementById("diagram"), content,
                        width, height , parseContent, onChange
                    );
                    // ballerinaDiagram.renderStaticDiagram(document.getElementById("diagram"), json,
                    //     { width, height } 
                    // );
                    console.log('Successfully rendered');
                } catch(e) {
                    console.log(e.stack);
                    drawError('Oops. Something went wrong.');
                }
            }

            function drawError(message) {
                document.getElementById("diagram").innerHTML = \`
                <div id="errors">
                    <span>\$\{message\}</span>
                </div>
                \`;
            }

            function showWarning(message) {
                document.getElementById("warning").innerHTML = \`
                    <p><span class="fw fw-warning"></span> \$\{message\}</p>
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