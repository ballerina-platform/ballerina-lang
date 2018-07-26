import { injectable } from "inversify";
import axios from "axios";
import URI from "@theia/core/lib/common/uri";

import { PreviewHandler, RenderContentParams } from '@theia/preview/lib/browser';

interface ParserReply {
    model?: Object
}

const bundleJsContent = require('raw-loader!../../../../../../../lib/ballerina-diagram-library.txt');

@injectable()
export class BallerinaPreviewHandler implements PreviewHandler {

    readonly iconClass: string = 'ballerina-icon file-icon';
    readonly contentClass: string = 'ballerina-preview';

    canHandle(uri: URI): number {
        return uri.scheme === 'file' && uri.path.ext === '.bal' ? 500 : 0;
    }

    renderContent(params: RenderContentParams): HTMLElement {
        const content = params.content;
        const parseOpts = {
            content,
            includePackageInfo: true,
            includeProgramDir: true,
            includeTree: true,
        }
        const contentElement = document.createElement('div');
        contentElement.classList.add(this.contentClass);
        contentElement.innerHTML = '<div id="diagram" />';;

        axios.post('https://parser.playground.preprod.ballerina.io/api/parser', parseOpts,
        { 
            headers: {
                'content-type': 'application/json; charset=utf-8',
            } 
        })
        .then(response => response.data) // parses response to JSON
        .then((body: ParserReply) => {
            let jsonModel = {};
            if (body.model) {
                jsonModel = body.model;
            }
            const bundleCss = document.createElement("link");
            bundleCss.type = "text/css";
            bundleCss.rel = "stylesheet";
            bundleCss.href = "http://localhost:5000/bundle.css";

            const bundleTheme = document.createElement("link");
            bundleTheme.type = "text/css";
            bundleTheme.rel = "stylesheet";
            bundleTheme.href = "http://localhost:5000/theme.css";

            const bundleLess = document.createElement("link");
            bundleLess.type = "text/css";
            bundleLess.rel = "stylesheet";
            bundleLess.href = "http://localhost:5000/less.css";

            const bundleJs = document.createElement( 'script' );
            bundleJs.innerHTML = bundleJsContent;

            const renderScript = document.createElement( 'script' );

            document.head.appendChild(bundleCss);
            document.head.appendChild(bundleTheme);
            document.head.appendChild(bundleLess);
            contentElement.appendChild(bundleJs);
            contentElement.appendChild(renderScript);

            renderScript.innerHTML = `
                function draw() {
                    const json = ${JSON.stringify(jsonModel)};
                    const test = document.getElementById("diagram");
                    ballerinaDiagram.renderDiagram(test, json, {
                        width: 600, height: 600
                    });
                }
                draw();
            `;
            
        })
        .catch((e: Error) => {
            console.log(e);
            
        });
        return contentElement;
    }

    findElementForFragment(content: HTMLElement, link: string): HTMLElement | undefined {
        return undefined;
    }

    findElementForSourceLine(content: HTMLElement, sourceLine: number): HTMLElement | undefined {
        return undefined;
    }

    getSourceLineForOffset(content: HTMLElement, offset: number): number | undefined {
        return undefined;
    }

}
