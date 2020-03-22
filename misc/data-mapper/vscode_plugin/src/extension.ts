// The module 'vscode' contains the VS Code extensibility API
// Import the module and reference it with the alias vscode in your code below
import * as vscode from 'vscode';
import { execSync } from 'child_process';
import { fileURLToPath } from 'url';

// this method is called when your extension is activated
// your extension is activated the very first time the command is executed
export function activate(context: vscode.ExtensionContext) {

	// Use the console to output diagnostic information (console.log) and errors (console.error)
	// This line of code will only be executed once when your extension is activated
	console.log('Congratulations, your extension "Ballerina AI" is now active!');

	// The command has been defined in the package.json file
	// Now provide the implementation of the command with registerCommand
	// The commandId parameter must match the command field in package.json
	let disposable = vscode.commands.registerCommand('extension.installRecordMappingFunctionality', () => {
		// The code you place here will be executed every time your command is executed

		const { exec } = require('child_process');
		exec('sh ~/.vscode/extensions/ballerina.ballerinaai-0.0.1/shell_scripts/classpathscript.sh', (err: any, stdout: any, stderr: any) => {
		if (err) {
			//some err occurred
			console.error(err)
		} else {
			// the *entire* stdout and stderr (buffered)
			console.log(`stdout: ${stdout}`);
			console.log(`stderr: ${stderr}`);
		}
		});

		vscode.window.showInformationMessage('Successfully installed the plugin! Quit VSCode and reopen');

	});

	let disposablenew = vscode.commands.registerCommand('extension.setupAIserviceURL', () => {
		
		
		const { exec } = require('child_process');
		const aiServiceUrlParam = vscode.workspace.getConfiguration().get("BallerinaAI.AI_SERVICE_URL");
		
		let executableCommand="sh ~/.vscode/extensions/ballerina.ballerinaai-0.0.1/shell_scripts/paramscript.sh " + aiServiceUrlParam;

		exec(executableCommand, (err: any, stdout: any, stderr: any) => {
		if (err) {
			//some err occurred
			console.error(err)
		} else {
			// the *entire* stdout and stderr (buffered)
			console.log(`stdout: ${stdout}`);
			console.log(`stderr: ${stderr}`);
		}
		});

		vscode.window.showInformationMessage('Successfully set the AI Service URL! Quit VSCode and reopen');
		
	});

	context.subscriptions.push(disposable);
	context.subscriptions.push(disposablenew);
}

// this method is called when your extension is deactivated
export function deactivate() {}
