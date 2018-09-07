import { ExtendedLangClient, BallerinaExampleCategory } from '../lang-client';
import { Uri } from 'vscode';

export function render (langClient: ExtendedLangClient, resourceRoot: Uri)
        : Thenable<string | undefined> {
    return langClient.fetchExamples()
            .then((resp) => {
              return Promise.resolve(renderExamples(resp.samples, resourceRoot));
            });
}

function renderExamples(examples: Array<BallerinaExampleCategory>, resourceRoot: Uri): string {
    const page = `
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link rel="stylesheet" href="${resourceRoot.toString()}/bundle.css" />
        <link rel="stylesheet" href="${resourceRoot.toString()}/theme.css" />
        <link rel="stylesheet" href="${resourceRoot.toString()}/less.css" />
    </head>
    <body>
        <div id="examples" />
    </body>
    <script charset="UTF-8" src="${resourceRoot.toString()}/ballerina-diagram-library.js"></script>
    <script>
        (function() {
            const vscode = acquireVsCodeApi();
            const examples = ${JSON.stringify(examples)};
            function openExample(url) {
                vscode.postMessage({
                    command: 'openExample',
                    url: JSON.stringify(url)
                });
            }
            ballerinaDiagram.renderSamplesList(document.getElementById("examples"), examples, openExample, () => {});
        }());
    </script>
    </html>
    `;

    return page;
}