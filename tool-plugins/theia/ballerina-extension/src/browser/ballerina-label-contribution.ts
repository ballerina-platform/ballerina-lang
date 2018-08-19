import { LabelProviderContribution } from "@theia/core/lib/browser/label-provider";
import URI from '@theia/core/lib/common/uri';
import { BALLERINA_EXTENSION } from '../common';
import { MaybePromise } from '@theia/core/lib/common';
import { injectable } from 'inversify';

@injectable()
export class BallerinaLabelProviderContribution implements LabelProviderContribution {

    canHandle(element: object): number {
        if (element instanceof URI && element.path.ext === BALLERINA_EXTENSION) {
            return 30;
        } else {
            return -1;
        }
    }

    /**
     * returns an icon class for the given element.
     */
    getIcon(element: URI): MaybePromise<string> {
        return 'java-icon';
    }

    /**
     * returns a short name for the given element.
     */
    getName(element: URI): string {
        return element.displayName;
    }

    /**
     * returns a long name for the given element.
     */
    getLongName(element: URI): string {
        return element.path.toString();
    }
}
