import '../../../../../../../lib/bundle.css';
import '../../../../../../../lib/theme.css';
import '../../../../../../../lib/less.css';

import { injectable } from "inversify";
import axios from "axios";
import URI from "@theia/core/lib/common/uri";

import { PreviewHandler, RenderContentParams } from '@theia/preview/lib/browser';

interface ParserReply {
    model?: Object
}

const { renderDiagram } = require('../../../../../../../lib/ballerina-diagram-library');

let lastRenderedAST: Object | undefined = undefined;

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
        contentElement.classList.add('ballerina-editor');
        contentElement.classList.add('design-view-container');

        if (lastRenderedAST) {
            renderDiagram(contentElement, lastRenderedAST, {
                width: 600, height: 600
            });
        }

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
            renderDiagram(contentElement, jsonModel, {
                width: 600, height: 600
            });
            lastRenderedAST = jsonModel;
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
