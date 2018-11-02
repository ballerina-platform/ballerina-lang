
import { injectable, inject } from "inversify";
import { BaseLanguageClientContribution, Workspace, Languages, LanguageClientFactory } from '@theia/languages/lib/browser';
import { BALLERINA_LANGUAGE_ID, BALLERINA_LANGUAGE_NAME } from '../common';

@injectable()
export class BallerinaLanguageClientContribution extends BaseLanguageClientContribution {

    readonly id = BALLERINA_LANGUAGE_ID;
    readonly name = BALLERINA_LANGUAGE_NAME;

    constructor(
        @inject(Workspace) protected readonly workspace: Workspace,
        @inject(Languages) protected readonly languages: Languages,
		@inject(LanguageClientFactory) protected readonly languageClientFactory: LanguageClientFactory,
    ) {
		super(workspace, languages, languageClientFactory);
    }

    protected get globPatterns() {
        return [
            '**/*.bal'
        ];
	}
}

