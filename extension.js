const { workspace, commands, window, ExtensionContext } = require('vscode');
const { LanguageClient, LanguageClientOptions, ServerOptions } = require('vscode-languageclient');
const path = require('path');

let oldConfig;

exports.activate = function(context) {
	// The server is implemented in java
	let serverModule = context.asAbsolutePath(path.join('server-build', 'langserver.jar'));
	const main = 'org.ballerinalang.langserver.launchers.stdio.Main';

	const config = workspace.getConfiguration('ballerina');
	oldConfig = config;
	// in windows class path seperated by ';'
	const sep = process.platform === 'win32' ? ';' : ':';
	
	if (config.sdk) {
		// sdk path is set in configurations
		serverModule = context.asAbsolutePath(path.join('server-build', 'langserver-no-bal-deps.jar'));
		serverModule += (sep + path.join(config.sdk, 'bre', 'lib', '*'));
	}
	const args = ['-cp', serverModule, main];
	// If the extension is launched in debug mode then the debug server options are used
	// Otherwise the run options are used
	let serverOptions = {
		run: { command: 'java', args },
		debug: {
			command: 'java', 
			args: [
				'-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005,quiet=y',
				...args,
			],
		},
	}
	// Options to control the language client
	let clientOptions = {
		documentSelector: [{ scheme: 'file', language: 'ballerina' }],
	}

	const forceDebug = (process.env.LSDEBUG === "true");

	let disposable = new LanguageClient('ballerina-vscode', 'Ballerina vscode lanugage client',
		serverOptions, clientOptions, forceDebug).start();

	// Push the disposable to the context's subscriptions so that the 
	// client can be deactivated on extension deactivation
	context.subscriptions.push(disposable);
}

workspace.onDidChangeConfiguration(params => {
	const newConfig = workspace.getConfiguration('ballerina');
	if (newConfig.sdk != oldConfig.sdk) {
		const msg = 'Ballerina sdk path configuration changed. Please restart vscode for changes to take effect.';
		const action = 'Restart Now';
		window.showWarningMessage(msg, action).then((selection) => {
			if (action === selection) {
				commands.executeCommand('workbench.action.reloadWindow');
			}
		});
	}

	oldConfig = newConfig;
});