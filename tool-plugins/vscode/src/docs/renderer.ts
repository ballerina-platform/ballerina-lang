import { ExtendedLangClient } from '../core/extended-language-client';
import { ExtensionContext } from 'vscode';
import { getLibraryWebViewContent } from '../utils';

export function render(context: ExtensionContext, langClient: ExtendedLangClient)
    : string {

    const body = `<div id="ballerina-documentation" class="documentation-container" />`;
    const bodyCss = "documentation";
    const styles = ``;
    const script = `
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

    return getLibraryWebViewContent(context, body, script, styles, bodyCss);
}
