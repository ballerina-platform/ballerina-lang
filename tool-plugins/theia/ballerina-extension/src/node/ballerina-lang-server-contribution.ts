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
            '/Users/kavithlokuhewage/ls/language-server-stdio-launcher-0.980.2-SNAPSHOT-jar-with-non-ballerina-dependencies.jar:/Library/Ballerina/ballerina-0.980.0/lib/resources/composer/services/*',
            '-Dballerina.home=/Library/Ballerina/ballerina-0.980.0', // TODO: Read this properly
			'org.ballerinalang.langserver.launchers.stdio.Main' // TODO: Read this properly
        ];
        const serverConnection = this.createProcessStreamConnection(command, args);
        this.forward(clientConnection, serverConnection);
    }
}