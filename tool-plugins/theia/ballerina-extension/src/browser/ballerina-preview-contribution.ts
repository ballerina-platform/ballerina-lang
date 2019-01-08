import { injectable } from "inversify";
import { AbstractViewContribution } from '@theia/core/lib/browser';
import { BallerinaPreviewWidget } from './ballerina-preview-widget';

export const BALLERINA_PREVIEW_WIDGET_FACTORY_ID = 'ballerina-diagram';

@injectable()
export class BallerinaPreviewContribution extends AbstractViewContribution<BallerinaPreviewWidget> {

    constructor() {
        super({
            widgetId: BALLERINA_PREVIEW_WIDGET_FACTORY_ID,
            widgetName: 'Ballerina Diagram',
            defaultWidgetOptions: {
                area: 'right',
                rank: 100,
            },
            toggleCommandId: 'ballerinaDiagram:toggle',
            toggleKeybinding: 'ctrlcmd+shift+y'
        });
    }
}
