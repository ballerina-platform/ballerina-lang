import { Uri, ExtensionContext } from "vscode";
import { join } from "path";

export function getWebViewResourceRoot(context: ExtensionContext): Uri {
    return Uri.file(join(context.extensionPath, 'resources')).with({ scheme: 'vscode-resource' });
}

export function getNodeModulesRoot(context: ExtensionContext): Uri {
    return Uri.file(join(context.extensionPath, 'node_modules')).with({ scheme: 'vscode-resource' });
}


export function getLibraryWebViewContent(context: ExtensionContext,
        body: string, scripts: string, styles: string, bodyCss?: string, isAPIDesigner?: boolean) {

    const resourceRoot = getWebViewResourceRoot(context).toString();
    const composerResourcesRoot = process.env.COMPOSER_DEBUG === "true" 
                ? process.env.COMPOSER_DEV_HOST
                : `${resourceRoot}/composer`;
    const jsModule = isAPIDesigner ? 'apiEditor' : 'composer';

    const nodeModulesRoot = getNodeModulesRoot(context).toString();

    return `
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <meta http-equiv="X-UA-Compatible" content="IE=edge">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <link rel="stylesheet" type="text/css" href="${composerResourcesRoot}/themes/ballerina-default.min.css">
                ${process.env.COMPOSER_DEBUG === "true" ? '<script src="http://localhost:8097"></script>' : ''}
                <style>
                    ${styles}
                </style>
            </head>
            
            <body style="overflow: auto;" class="${bodyCss}">
                ${body}
                <script>
                    ${scripts}
                </script>
                <script charset="UTF-8" src="${nodeModulesRoot}/mousetrap/mousetrap.min.js"></script>
                <script charset="UTF-8" src="${resourceRoot}/utils/messaging.js"></script>
                <script charset="UTF-8" src="${resourceRoot}/utils/undo-redo.js"></script>
                <script charset="UTF-8" onload="loadedScript();" src="${composerResourcesRoot}/${jsModule}.js"></script>
            </body>
            </html>
        `;
}
