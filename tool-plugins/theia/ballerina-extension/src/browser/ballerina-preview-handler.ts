import { injectable } from "inversify";
import URI from "@theia/core/lib/common/uri";

import { PreviewHandler, RenderContentParams } from '@theia/preview/lib/browser';

@injectable()
export class BallerinaPreviewHandler implements PreviewHandler {

    readonly iconClass: string = 'ballerina-icon file-icon';
    readonly contentClass: string = 'ballerina-preview';

    canHandle(uri: URI): number {
        return uri.scheme === 'file' && uri.path.ext === '.bal' ? 500 : 0;
    }

    renderContent(params: RenderContentParams): HTMLElement {
        const content = params.content;
        const renderedContent = content; // TODO Render diagram here
        const contentElement = document.createElement('div');
        contentElement.classList.add(this.contentClass);
        contentElement.innerHTML = renderedContent;
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
