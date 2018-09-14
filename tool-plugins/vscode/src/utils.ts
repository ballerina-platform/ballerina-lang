import { Uri, ExtensionContext } from "vscode";
import { join } from "path";

export function getWebViewResourceRoot(context: ExtensionContext): Uri {
    return (process.env.DIAGRAM_DEBUG === "true") ? 
                Uri.parse("http://localhost:8081")
                : Uri.file(join(context.extensionPath, 'resources', 'diagram')).with({ scheme: 'vscode-resource' });
}

export function getLibraryWebViewContent(context: ExtensionContext,
        body: string, scripts: string, styles: string) {
    const resourceRoot = getWebViewResourceRoot(context).toString();
    return `
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link rel="stylesheet" href="${resourceRoot}/bundle.css" />
        <link rel="stylesheet" href="${resourceRoot}/theme.css" />
        <link rel="stylesheet" href="${resourceRoot}/less.css" />
        ${process.env.DIAGRAM_DEBUG === "true" ? '<script src="http://localhost:8097"></script>' : ''}
        <style>
            ${styles}
        </style>
    </head>
    
    <body style="overflow-y: scroll;">
        ${body}
    </body>
    <script charset="UTF-8" src="${resourceRoot}/ballerina-diagram-library.js"></script>
    <script>
        (function() {
            ${scripts}
        }());
    </script>
    </html>
    `;
}