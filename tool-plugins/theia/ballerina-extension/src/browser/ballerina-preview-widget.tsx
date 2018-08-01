import { ReactWidget } from '@theia/core/lib/browser/widgets/react-widget';
import { injectable, postConstruct } from 'inversify';
import * as React from 'react';

@injectable()
export class BallerinaPreviewWidget extends ReactWidget {

    constructor() {
        super();
        this.id = 'ballerina-preview-widget';
        this.title.label = 'Ballerina Interaction';
        this.title.closable = true;
        this.addClass('ballerina-preview');
    }

    @postConstruct()
    protected init(): void {
        this.update();
    }

    protected render(): React.ReactNode {
       return <React.Fragment><div>{'My Custom Widget'}</div></React.Fragment>;
    }

}