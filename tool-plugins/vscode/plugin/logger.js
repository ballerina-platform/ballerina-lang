const { window, workspace } = require('vscode');
let outputChannel;
const logLevelDebug = workspace.getConfiguration('ballerina').get('debugLog') === true;

if (logLevelDebug) {
    outputChannel = window.createOutputChannel("Ballerina");
}

module.exports = function log(value) {
    if (!value.endsWith('\n')) {
        value+='\n'
    }
    console.log(value);
    if (outputChannel) {
        outputChannel.append(value);
    }
}