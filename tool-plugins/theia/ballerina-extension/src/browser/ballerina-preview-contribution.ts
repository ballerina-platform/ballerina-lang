/********************************************************************************
 * Copyright (C) 2017 TypeFox and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 ********************************************************************************/

import { injectable } from "inversify";
import { AbstractViewContribution, FrontendApplicationContribution, FrontendApplication } from '@theia/core/lib/browser';
import { BallerinaPreviewWidget } from './ballerina-preview-widget';

export const BALLERINA_PREVIEW_WIDGET_FACTORY_ID = 'ballerina-preview';

@injectable()
export class BallerinaPreviewContribution extends AbstractViewContribution<BallerinaPreviewWidget> implements FrontendApplicationContribution {

    constructor() {
        super({
            widgetId: BALLERINA_PREVIEW_WIDGET_FACTORY_ID,
            widgetName: 'BallerinaPreview',
            defaultWidgetOptions: {
                area: 'right',
                rank: 100
            },
            toggleCommandId: 'ballerinaPreview:toggle',
            toggleKeybinding: 'ctrlcmd+shift+y'
        });
    }

    async initializeLayout(app: FrontendApplication): Promise<void> {
        await this.openView();
    }
}
