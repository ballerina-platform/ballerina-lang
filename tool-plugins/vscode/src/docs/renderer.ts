import { Uri, ExtensionContext } from 'vscode';
import * as path from 'path';

export function render(context: ExtensionContext): string {
    let composerJsSrc;
    if (process.env.COMPOSER_DEBUG === "true") {
        const { extensionPath } = context;
        const onDiskPath = Uri.file(path.join(extensionPath, 'resources', 'composer', 'composer.js'));
        composerJsSrc = onDiskPath.with({ scheme: 'vscode-resource' });
    } else {
        composerJsSrc = 'http://localhost:9000/composer.js';
    }

    return `
        <!doctype html>
        <html lang="en">
        <head>
            <meta charset="utf-8">
        </head>
        <body>
            <div id="ballerina-documentation"/>
            <script>
                let astJson;
                const el = document.getElementById("ballerina-documentation");
                window.addEventListener('message', event => {
                    switch (event.data.command) {
                        case 'update':
                            astJson = event.data.json;
                            if (ballerinaComposer) {
                                ballerinaComposer.renderDocuments(astJson, el);
                            }
                            break;
                    }
                });

                function loadedScript() {
                    if(astJson) {
                        ballerinaComposer.renderDocuments(astJson, el);
                    }
                }
            </script>
            <script onload="loadedScript();" src="${composerJsSrc}"></script>
        </body>
        </html> 
    `;
}