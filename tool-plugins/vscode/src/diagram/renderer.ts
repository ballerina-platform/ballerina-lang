import { ExtendedLangClient } from '../core/extended-language-client';
import { Uri, ExtensionContext } from 'vscode';
import { getLibraryWebViewContent } from '../utils';

export function render (context: ExtensionContext, langClient: ExtendedLangClient, docUri: Uri, retries: number = 1)
        : string {       
   return renderDiagram(context, docUri);
}

function renderDiagram(context: ExtensionContext, docUri: Uri): string {
    
    const body = `
        <div id="warning">
        </div>
        <div class="ballerina-editor design-view-container" id="diagram">
        </div>
    `;

    const styles = `
        body {
            background: #f1f1f1;
        }
        .overlay {
            display: none;
        }
        .drop-zone.rect {
            fill-opacity: 0;
        }
        #diagram {
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
        function loadedScript() {
            let docUri = ${JSON.stringify(docUri.toString())};
            function drawDiagram() {
                try {
                    let width = window.innerWidth - 6;
                    let height = window.innerHeight;
                    const options = {
                        target: document.getElementById("diagram"),
                        editorProps: {
                            docUri,
                            width,
                            height,
                            langClient: getLangClient()
                        }
                    };
                    const diagram = ballerinaComposer.renderDiagramEditor(options);
                    webViewRPCHandler.addMethod("updateAST", (args) => {
                        console.log(args);
                        diagram.updateAST(args[0]);
                        return Promise.resolve({});
                    });
                } catch(e) {
                    console.log(e.stack);
                    drawError('Oops. Something went wrong. ' + e.message);
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
            drawDiagram();
        }
    `;

    return getLibraryWebViewContent(context, body, script, styles);
}

export function renderError() {
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