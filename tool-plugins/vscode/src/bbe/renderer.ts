import { ExtendedLangClient } from '../core/extended-language-client';
import { ExtensionContext } from 'vscode';
import { getLibraryWebViewContent } from '../utils';

export function render(context: ExtensionContext, langClient: ExtendedLangClient)
    : string {

    const body = `<div id="examples" class="examples-container" />`;
    const bodyCss = "examples";
    const styles = ``;
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

    return getLibraryWebViewContent(context, body, script, styles, bodyCss);
}
