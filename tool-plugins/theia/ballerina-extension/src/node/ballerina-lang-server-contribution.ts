import { injectable } from "inversify";
import { BaseLanguageServerContribution, IConnection } from "@theia/languages/lib/node";
import { BALLERINA_LANGUAGE_ID, BALLERINA_LANGUAGE_NAME } from '../common';

@injectable()
export class BallerinaLanguageServerContribution extends BaseLanguageServerContribution {

    readonly id = BALLERINA_LANGUAGE_ID;
    readonly name = BALLERINA_LANGUAGE_NAME;

    start(clientConnection: IConnection): void {
        const command = "java";
        const args: string[] = [
            '-cp',
            '/home/theia/composer/resources/language-server-stdio-launcher.jar:/home/theia/composer/resources/platform/lib/resources/composer/services/*',
            '-Dballerina.home=/home/theia/composer/resources/platform', // TODO: Read this properly
			'org.ballerinalang.langserver.launchers.stdio.Main' // TODO: Read this properly
        ];
        const serverConnection = this.createProcessStreamConnection(command, args);
        this.forward(clientConnection, serverConnection);
    }
}