import { ExtendedLangClient } from '../core/extended-language-client';
import { ExtensionContext } from 'vscode';
import { getLibraryWebViewContent, WebViewOptions, getComposerWebViewOptions } from '../utils';

export function render(context: ExtensionContext, langClient: ExtendedLangClient)
    : string {

    const body = `
        <div id="ballerina-documentation" class="documentation-container" >
            <i class="fw fw-loader fw-spin fw-3x root-loader"></i>
        </div>
    `;
    const bodyCss = "documentation";
    const styles = "";
    const scripts = `
        const el = document.getElementById("ballerina-documentation");
        window.addEventListener('message', event => {
            switch (event.data.command) {
                case 'update':
                    const astJson = event.data.json;
                    if (window.ballerinaComposer) {
                        ballerinaComposer.renderDocPreview(astJson, el);
                    }
                    break;
                case 'scroll':
                    const anchor = event.data.anchor;
                    location.href = "#" + anchor;
                    break;
            }
        });

        function loadedScript() {
            vscode.postMessage({message: "loaded-doc-preview"});
        }
    `;

    const webViewOptions: WebViewOptions = {
        ...getComposerWebViewOptions(),
        body, scripts, styles, bodyCss
    };
    
    return getLibraryWebViewContent(webViewOptions);
}
