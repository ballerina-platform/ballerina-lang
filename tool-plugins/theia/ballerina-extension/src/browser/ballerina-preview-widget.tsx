import { ReactWidget } from '@theia/core/lib/browser/widgets/react-widget';
import { injectable } from 'inversify';
import * as React from 'react';

@injectable()
export class BallerinaPreviewWidget extends ReactWidget {

    constructor(
        // @inject(CommandService) protected readonly commands: CommandService,
        // @inject(LabelParser) protected readonly entryService: LabelParser,
        // @inject(FrontendApplicationStateService) protected readonly applicationStateService: FrontendApplicationStateService
    ) {
        super();
        this.id = 'ballerina-preview-widget';
        this.title.label = 'Ballerina Interaction';
        this.addClass('ballerina-preview');
    }
    protected render(): React.ReactNode {
       return <div>{'My Custom Widget'}</div>;
    }
}