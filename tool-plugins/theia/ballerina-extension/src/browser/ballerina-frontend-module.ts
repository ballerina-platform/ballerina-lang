/**
 * Generated using theia-extension-generator
 */

import { BallerinaCommandContribution, BallerinaMenuContribution } from './ballerina-contribution';
import {
    CommandContribution,
    MenuContribution
} from "@theia/core/lib/common";
import { LanguageClientContribution } from "@theia/languages/lib/browser";
import { LanguageGrammarDefinitionContribution } from "@theia/monaco/lib/browser/textmate";
import { BallerinaGrammarContribution } from "./ballerina-grammar-contribution";
import { BallerinaLanguageClientContribution } from './ballerina-language-client-contribution';

import { ContainerModule } from "inversify";

export default new ContainerModule(bind => {
    // add your contribution bindings here
    bind(CommandContribution).to(BallerinaCommandContribution);
    bind(MenuContribution).to(BallerinaMenuContribution);
    bind(LanguageGrammarDefinitionContribution).to(BallerinaGrammarContribution).inSingletonScope();
    bind(LanguageClientContribution).to(BallerinaLanguageClientContribution).inSingletonScope();
});