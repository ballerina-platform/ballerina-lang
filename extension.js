const { workspace, ExtensionContext } = require('vscode');
const { LanguageClient, LanguageClientOptions, ServerOptions } = require('vscode-languageclient');
const path = require('path');

exports.activate = function(context) {
	// The server is implemented in java
	let serverModule = context.asAbsolutePath(path.join('server-build', 'langserver.jar'));
    
	// If the extension is launched in debug mode then the debug server options are used
	// Otherwise the run options are used
	let serverOptions = {
		run: { command: 'java', args: ['-jar', serverModule] },
		debug: {
			command: 'java', 
			args: [
				'-jar',
				'-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005,quiet=y',
				serverModule,
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