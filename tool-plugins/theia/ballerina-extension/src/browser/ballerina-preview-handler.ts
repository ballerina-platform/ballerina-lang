import '../../../resources/lib/bundle.css';
import '../../../resources/lib/theme.css';
import '../../../resources/lib/less.css';

import { injectable } from "inversify";
import URI from "@theia/core/lib/common/uri";

import { PreviewHandler, RenderContentParams } from '@theia/preview/lib/browser';

import { ParserReply, parseContent } from '../common';

const { renderEditableDiagram } = require('../../../resources/lib/ballerina-diagram-library');

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

        const contentElement = document.createElement('div');
        contentElement.classList.add(this.contentClass);
        contentElement.classList.add('ballerina-editor');
        contentElement.classList.add('design-view-container');

        if (lastRenderedAST) {
            renderEditableDiagram(contentElement, lastRenderedAST, {
                width: 600, height: 600, mode: 'default'
            });
        }
        parseContent(content)
            .then((body: ParserReply) => {
                let jsonModel = {};
                if (body.model) {
                    jsonModel = body.model;
                }
                renderEditableDiagram(contentElement, jsonModel, {
                    width: 600, height: 600, mode: 'default'
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
