import { Uri, ExtensionContext } from "vscode";
import { join } from "path";

export function getWebViewResourceRoot(context: ExtensionContext): Uri {
    return Uri.file(join(context.extensionPath, 'resources')).with({ scheme: 'vscode-resource' });
}

export function getLibraryWebViewContent(context: ExtensionContext,
        body: string, scripts: string, styles: string) {
    const resourceRoot = getWebViewResourceRoot(context).toString();
    const diagramResourceRoot = process.env.DIAGRAM_DEBUG === "true" 
                ? 'http://localhost:8081'
                : `${resourceRoot}/diagram`;
    return `
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link rel="stylesheet" href="${diagramResourceRoot}/bundle.css" />
        <link rel="stylesheet" href="${diagramResourceRoot}/theme.css" />
        <link rel="stylesheet" href="${diagramResourceRoot}/less.css" />
        ${process.env.DIAGRAM_DEBUG === "true" ? '<script src="http://localhost:8097"></script>' : ''}
        <style>
            ${styles}
        </style>
    </head>
    
    <body style="overflow-y: scroll;">
        ${body}
        <script charset="UTF-8" src="${resourceRoot}/utils/messaging.js"></script>
        <script charset="UTF-8" src="${diagramResourceRoot}/ballerina-diagram-library.js"></script>
        <script>
            (function() {
                ${scripts}
            }());
        </script>
    </body>
    </html>
    `;
}