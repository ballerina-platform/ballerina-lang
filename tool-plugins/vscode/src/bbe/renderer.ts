import { ExtendedLangClient } from '../core/extended-language-client';
import { ExtensionContext } from 'vscode';
import { getLibraryWebViewContent } from '../utils';

export function render(context: ExtensionContext, langClient: ExtendedLangClient)
    : string {
    const body = `<div id="examples" />`;
    const script = `
        function getSamples() {
            return Promise.resolve(examples);
        }
        function getExamples() {
            return new Promise((resolve, reject) => {
                webViewRPCHandler.invokeRemoteMethod('getExamples', [], (resp) => {
                    resolve(resp.samples);
                });
            })
        }
        function openExample(url) {
            vscode.postMessage({
                command: 'openExample',
                url: JSON.stringify(url)
            });
        }
        function renderSamples() {
            ballerinaComposer.renderSamplesList(document.getElementById("examples"), openExample, getExamples, () => {});
        }
        renderSamples();
        renderSamples();
        
        `;
    const styles = `
        body.vscode-dark {
            background-color: #1e1e1e;
        }
        body.vscode-light {
            background-color: white;
        }
    `;

    return getLibraryWebViewContent(context, body, script, styles);
}