import { LanguageClient } from 'vscode-languageclient';
import { log } from '../logger';
import { getAST } from './utils';
import { BallerinaAST } from './diagram';
import { PathLike } from 'fs';

const RETRY_COUNT = 5;
const RETRY_WAIT = 1000;

export function render (docUri: PathLike, langClient: LanguageClient, resourceRoot: PathLike, retries: number = 1)
        : Thenable<string | undefined> {
    return getAST(langClient, docUri)
        .then((resp) => {
            let stale = true;
            if (resp.ast) {
                stale = false;
                return renderDiagram(docUri, resp.ast, resourceRoot, stale);
            } else {
                if (retries > RETRY_COUNT) {
                    log('Could not render');
                    return renderError();
                }
                setTimeout(() => {
                    log(`Retrying rendering ${retries}/${RETRY_COUNT}\n`);
                    return render(docUri, langClient, resourceRoot, retries + 1);
                }, RETRY_WAIT);
            }
        });
}

function renderDiagram(docUri: PathLike, jsonModelObj: BallerinaAST,
                resourceRoot: PathLike, stale: boolean): string {
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
            let docUri = ${JSON.stringify(docUri)};
            let json = ${jsonModel};
            let stale = ${JSON.stringify(stale)};

            const vscode = acquireVsCodeApi();

            // Handle the message inside the webview
            window.addEventListener('message', event => {

                const message = event.data; // The JSON data our extension sent

                switch (message.command) {
                    case 'update':
                        json = message.json;
                        docUri = message.docUri;
                        stale = message.stale;
                        drawDiagram();
                        break;
                }
            });

            if (stale) {
                showWarning('Cannot update design view due to syntax errors.')
            }

            if (!json) {
                return;
            }

            function getAST(docUri) {
                return Promise.resolve({ model: json });
            }

            function onChange(evt) {
                vscode.postMessage({
                    command: 'astModified',
                    ast: JSON.stringify(evt.newAST, (key, value) => {
                        currentKey = key;
                        if (key === 'parent' || key === 'viewState' || key === '_events'|| key === 'id') {
                            return undefined;
                        }
                        return value;
                    })
                })
            }

            function drawDiagram() {
                try {
                    let width = window.innerWidth - 6;
                    let height = window.innerHeight;
                    console.log('rendering ' + width);
                    ballerinaDiagram.renderEditableDiagram(document.getElementById("diagram"), docUri,
                        width, height, getAST, onChange
                    );
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