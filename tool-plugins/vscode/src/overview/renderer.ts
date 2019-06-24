import { ExtendedLangClient } from '../core/extended-language-client';
import { ExtensionContext } from 'vscode';
import { getLibraryWebViewContent, getComposerWebViewOptions } from '../utils';

export function render (context: ExtensionContext, langClient: ExtendedLangClient, sourceRoot: string, retries: number = 1)
        : string {       
   return renderDiagram(context, sourceRoot);
}

function renderDiagram(context: ExtensionContext, sourceRoot: string): string {
    const body = `
        <div class="ballerina-editor design-view-container" id="diagram"></div>
    `;

    const bodyCss = "diagram";

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

    const scripts = `
        function loadedScript() {
            window.langclient = getLangClient();
            let sourceRoot = ${JSON.stringify(sourceRoot)};
            function drawDiagram() {
                try {
                    let width = window.innerWidth - 6;
                    let height = window.innerHeight;
                    let zoom = 1;
                    const options = {
                        target: document.getElementById("diagram"),
                        editorProps: {
                            docUri: sourceRoot,
                            width,
                            height,
                            zoom,
                            langClient: getLangClient()
                        }
                    };
                    const diagram = ballerinaComposer.renderOverview(options);
                    webViewRPCHandler.addMethod("updateAST", (args) => {
                        diagram.updateAST();
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
            enableUndoRedo();
        }
    `;
    debugger;

    return getLibraryWebViewContent({...getComposerWebViewOptions(), body, scripts, styles, bodyCss});
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
