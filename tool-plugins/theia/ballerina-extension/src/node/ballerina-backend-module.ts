import { ContainerModule } from "inversify";
import { LanguageServerContribution } from "@theia/languages/lib/node";
import { BallerinaLanguageServerContribution } from './ballerina-lang-server-contribution';

export default new ContainerModule(bind => {
    bind(LanguageServerContribution).to(BallerinaLanguageServerContribution).inSingletonScope();
});
