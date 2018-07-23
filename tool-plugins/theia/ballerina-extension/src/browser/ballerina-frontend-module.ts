/**
 * Generated using theia-extension-generator
 */

import { BallerinaCommandContribution, BallerinaMenuContribution } from './ballerina-contribution';
import {
    CommandContribution,
    MenuContribution
} from "@theia/core/lib/common";
import { LanguageGrammarDefinitionContribution } from "@theia/monaco/lib/browser/textmate";
import { BallerinaGrammarContribution } from "./ballerina-grammar-contribution";

import { ContainerModule } from "inversify";

export default new ContainerModule(bind => {
    // add your contribution bindings here
    
    bind(CommandContribution).to(BallerinaCommandContribution);
    bind(MenuContribution).to(BallerinaMenuContribution);
    bind(LanguageGrammarDefinitionContribution).to(BallerinaGrammarContribution).inSingletonScope();
});