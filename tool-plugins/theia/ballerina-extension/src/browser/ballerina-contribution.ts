import { injectable, inject } from "inversify";
import { CommandContribution, CommandRegistry, MenuContribution, MenuModelRegistry, MessageService } from "@theia/core/lib/common";
import { CommonMenus } from "@theia/core/lib/browser";

export const BallerinaCommand = {
    id: 'Ballerina.command',
    label: "Shows a message"
};

@injectable()
export class BallerinaCommandContribution implements CommandContribution {

    constructor(
        @inject(MessageService) private readonly messageService: MessageService,
    ) { }

    registerCommands(registry: CommandRegistry): void {
        registry.registerCommand(BallerinaCommand, {
            execute: () => this.messageService.info('Hello World!')
        });
    }
}

@injectable()
export class BallerinaMenuContribution implements MenuContribution {

    registerMenus(menus: MenuModelRegistry): void {
        menus.registerMenuAction(CommonMenus.EDIT_FIND, {
            commandId: BallerinaCommand.id,
            label: 'Say Hello'
        });
    }
}