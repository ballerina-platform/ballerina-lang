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
            '-Dballerina.home=/Library/Ballerina/ballerina-0.980.0', // TODO: Read this properly
			'-jar',
			'/Users/kavithlokuhewage/ls/ballerina-ls-with-deps.jar' // TODO: Read this properly
        ];
        const serverConnection = this.createProcessStreamConnection(command, args);
        this.forward(clientConnection, serverConnection);
    }
}