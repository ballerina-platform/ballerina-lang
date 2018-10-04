import { ExtendedLangClient } from '../lang-client';
import { Uri, ExtensionContext } from 'vscode';
import { getLibraryWebViewContent } from '../utils';

export function apiEditorRender(context: ExtensionContext, langClient: ExtendedLangClient, docUri: Uri, selectedService: string, retries: number = 1) : string {
    const body = `
        <div class='api-container'>
            <div class='message'></div>
            <div class='api-visualizer' id='api-visualizer'></div>
        </div>
    `;

    const styles = `
        .api-container {
            padding: 1em;
        }
    `;

    const script = `
        let docUri = ${JSON.stringify(docUri.toString())};
        let selectedService = ${JSON.stringify(selectedService.toString())};

        debugger;

        function getSwaggerJson(docUri, serviceName) {
            return new Promise((resolve, reject) => {
                webViewRPCHandler.invokeRemoteMethod('getSwaggerDef', [docUri, serviceName], (resp) => {
                    resolve(resp);
                });
            })
        }

        function onDidJsonChange(event, changedObj, oasJson) {
            webViewRPCHandler.invokeRemoteMethod('onOasChange', [JSON.stringify(oasJson)], (resp) => {
                console.log(resp);
            });
        }

        function drawAPIEditor() {
            getSwaggerJson(docUri, selectedService).then((response)=>{
                try {
                    let width = window.innerWidth - 6;
                    let height = window.innerHeight;
                    console.log(JSON.stringify(response.ballerinaOASJson));
                    ballerinaDiagram.renderBallerinaApiEditor(
                        document.getElementById("api-visualizer"), JSON.stringify(response.ballerinaOASJson), onDidJsonChange);
                } catch (e) {
                    console.log(e.stack);
                }
            })
        }

        drawAPIEditor();
        drawAPIEditor();

    `;

    return getLibraryWebViewContent(context, body, script, styles);
}