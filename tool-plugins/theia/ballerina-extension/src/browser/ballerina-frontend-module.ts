/**
 * Generated using theia-extension-generator
 */

import { BallerinaCommandContribution, BallerinaMenuContribution } from './ballerina-contribution';
import {
    CommandContribution,
    MenuContribution
} from "@theia/core/lib/common";
import { bindViewContribution, FrontendApplicationContribution, WidgetFactory } from "@theia/core/lib/browser";
import { LanguageClientContribution } from "@theia/languages/lib/browser";
import { LanguageGrammarDefinitionContribution } from "@theia/monaco/lib/browser/textmate";
import { BallerinaGrammarContribution } from "./ballerina-grammar-contribution";
import { BallerinaLanguageClientContribution } from './ballerina-language-client-contribution';
import { PreviewHandler } from '@theia/preview/lib/browser';
import { BallerinaPreviewHandler } from './ballerina-preview-handler';
import { BALLERINA_PREVIEW_WIDGET_FACTORY_ID, BallerinaPreviewContribution} from './ballerina-preview-contribution';
import { BallerinaPreviewWidget } from './ballerina-preview-widget';

import { ContainerModule } from "inversify";

export default new ContainerModule(bind => {
    bindViewContribution(bind, BallerinaPreviewContribution);
    bind(FrontendApplicationContribution).toService(BallerinaPreviewContribution);

    bind(BallerinaPreviewWidget).toSelf();
    bind(WidgetFactory).toDynamicValue(context => ({
        id: BALLERINA_PREVIEW_WIDGET_FACTORY_ID,
        createWidget: () => context.container.get<BallerinaPreviewWidget>(BallerinaPreviewWidget)
    })).inSingletonScope();

    bind(LanguageClientContribution).to(BallerinaLanguageClientContribution).inSingletonScope();
    bind(CommandContribution).to(BallerinaCommandContribution);
    bind(MenuContribution).to(BallerinaMenuContribution);
    bind(LanguageGrammarDefinitionContribution).to(BallerinaGrammarContribution).inSingletonScope();
    bind(BallerinaPreviewHandler).toSelf().inSingletonScope();
    bind(PreviewHandler).toService(BallerinaPreviewHandler);
});