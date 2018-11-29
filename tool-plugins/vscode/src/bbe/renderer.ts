import { ExtendedLangClient } from '../core/extended-language-client';
import { ExtensionContext } from 'vscode';
import { getLibraryWebViewContent } from '../utils';

export function render(context: ExtensionContext, langClient: ExtendedLangClient)
    : string {
    const body = `<div id="examples" />`;
    const script = `
            function loadedScript() {
                    function openExample(url) {
                        webViewRPCHandler.invokeRemoteMethod("openExample", [url]);
                    }
                    const langClient = getLangClient();
                    function renderSamples() {
                        ballerinaComposer.renderSamplesList(document.getElementById("examples"), openExample, langClient.getExamples, () => {});
                    }
                    renderSamples();
            }
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