import { Uri, ExtensionContext } from "vscode";
import { join } from "path";

export function getWebViewResourceRoot(context: ExtensionContext): Uri {
    return Uri.file(join(context.extensionPath, 'resources')).with({ scheme: 'vscode-resource' });
}

export function getLibraryWebViewContent(context: ExtensionContext,
        body: string, scripts: string, styles: string) {
    const resourceRoot = getWebViewResourceRoot(context).toString();
    const composerResourcesRoot = process.env.COMPOSER_DEBUG === "true" 
                ? process.env.COMPOSER_DEV_HOST
                : `${resourceRoot}/composer`;
    return `
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        ${process.env.COMPOSER_DEBUG === "true" ? '<script src="http://localhost:8097"></script>' : ''}
        <style>
            ${styles}
        </style>
    </head>
    
    <body style="overflow-y: scroll;">
        ${body}
        <script charset="UTF-8" src="${resourceRoot}/utils/messaging.js"></script>
        <script charset="UTF-8" src="${composerResourcesRoot}/composer.js"></script>
        <script>
            ${scripts}
        </script>
    </body>
    </html>
    `;
}
