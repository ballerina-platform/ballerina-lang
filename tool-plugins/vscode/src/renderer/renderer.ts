import { log } from '../logger';
import { BallerinaAST, ExtendedLangClient } from '../lang-client';
import { Uri, ExtensionContext } from 'vscode';
import { getLibraryWebViewContent } from '../utils';

const RETRY_COUNT = 5;
const RETRY_WAIT = 1000;

export function render (context: ExtensionContext, langClient: ExtendedLangClient, docUri: Uri, retries: number = 1)
        : Thenable<string | undefined> {       
    return langClient.getAST(docUri)
                .then((resp) => {
                    let stale = true;
                    if (resp.ast) {
                        stale = false;
                        return renderDiagram(context, docUri, resp.ast, stale);
                    } else {
                        if (retries > RETRY_COUNT) {
                            log('Could not render');
                            return renderError();
                        }
                        setTimeout(() => {
                            log(`Retrying rendering ${retries}/${RETRY_COUNT}\n`);
                            return render(context, langClient, docUri, retries + 1);
                        }, RETRY_WAIT);
                    }
                });
}

function renderDiagram(context: ExtensionContext, docUri: Uri, jsonModelObj: BallerinaAST, stale: boolean): string {
    const jsonModel = JSON.stringify(jsonModelObj);

    const body = `
        <div id="warning">
        </div>
        <div class="ballerina-editor design-view-container" id="diagram">
        </div>
    `;

    const styles = `
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
    `;

    const script = `
        let docUri = ${JSON.stringify(docUri.toString())};
        let json = ${jsonModel};
        let stale = ${JSON.stringify(stale)};

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

        function getEndpoints() {
            return new Promise((resolve, reject) => {
                webViewRPCHandler.invokeRemoteMethod('getEndpoints', [], (resp) => {
                    resolve(resp);
                });
            })
        }

        function parseFragment(fragment) {
            return new Promise((resolve, reject) => {
                webViewRPCHandler.invokeRemoteMethod('parseFragment', [fragment], (resp) => {
                    resolve(resp);
                });
            })
        }

        function goToSource(model) {
            const pos = model.position;
            if (pos) {
                return new Promise((resolve, reject) => {
                    webViewRPCHandler.invokeRemoteMethod(
                        'revealRange', 
                        [pos.startLine, pos.startColumn, pos.endLine, pos.endColumn], 
                        (resp) => {
                            resolve(resp);
                        }
                    );
                })
            }
        }

        function drawDiagram() {
            try {
                let width = window.innerWidth - 6;
                let height = window.innerHeight;
                console.log('rendering ' + width);
                ballerinaDiagram.renderEditableDiagram(document.getElementById("diagram"), docUri,
                    width, height, getAST, onChange, getEndpoints, parseFragment, goToSource
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
    `;

    return getLibraryWebViewContent(context, body, script, styles);
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