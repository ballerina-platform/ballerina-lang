import { ExtendedLangClient } from '../core/extended-language-client';
import { ExtensionContext } from 'vscode';
import { getLibraryWebViewContent, WebViewOptions, getComposerWebViewOptions } from '../utils';

export function render(context: ExtensionContext, langClient: ExtendedLangClient)
    : string {

    const body = `<div id="examples" class="examples-container" />`;
    const bodyCss = "examples";
    const styles = ``;
    const scripts = `
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

        const webViewOptions: WebViewOptions = {
            ...getComposerWebViewOptions(),
            body, scripts, styles, bodyCss
        };
        
        return getLibraryWebViewContent(webViewOptions);
}

