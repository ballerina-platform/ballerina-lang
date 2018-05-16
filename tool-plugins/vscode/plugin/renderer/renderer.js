const request = require('request-promise');
const { getParserService } = require('../serverStarter');
const log = require('../logger');

const RETRY_COUNT = 5;
const RETRY_WAIT = 1000;

let jsonModel;


function render (content, retries=1) {
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
        let stale = true;
        if (body.model) {
            stale = false;
            jsonModel = body.model;
        }
        return renderDiagram(jsonModel, stale);
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

function renderDiagram(jsonModelObj, stale) {
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
    <script charset="UTF-8" src="file://${__dirname}/resources/ballerina-diagram-library.js"></script>
    <script>
        (function() {
            const json = ${jsonModel};
            const stale = ${JSON.stringify(stale)};

            if (stale) {
                showWarning('Cannot update design view due to syntax errors.')
            }

            if (!json) {
                return;
            }

            function drawDiagram() {
                try {
                    ballerinaDiagram.renderDiagram(document.getElementById("diagram"), json, {
                        width: window.innerWidth - 6, height: window.innerHeight
                    });
                    console.log('Successfully rendered')
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
module.exports.activate = () => {
    return getParserService();
}
