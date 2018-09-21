import { ExtendedLangClient } from '../lang-client';
import { ExtensionContext } from 'vscode';
import { getLibraryWebViewContent } from '../utils';

export function render(context: ExtensionContext, langClient: ExtendedLangClient)
    : Thenable<string | undefined> {
    return langClient.fetchExamples()
        .then((resp) => {
            const body = `<div id="examples" />`;
            const script = `
                const examples = ${JSON.stringify(resp.samples)};
                function openExample(url) {
                    vscode.postMessage({
                        command: 'openExample',
                        url: JSON.stringify(url)
                    });
                }
                ballerinaDiagram.renderSamplesList(document.getElementById("examples"), examples, openExample, () => {});
                `;
            const styles = `
                body.vscode-dark {
                    background-color: #1e1e1e;
                }
                body.vscode-light {
                    background-color: white;
                }
            `;

            return Promise.resolve(getLibraryWebViewContent(context, body, script, styles));
        });
}