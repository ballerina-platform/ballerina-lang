const vscode = require('vscode');
const fs = require('fs');
const { render, deactivate, activate } = require('./renderer');

class DiagramProvider {

    constructor() {
        this._onDidChange = new vscode.EventEmitter();
        this.onDidChange = this._onDidChange.event;
    }

    update(uri) {
        if (!vscode.window.activeTextEditor) {
            return;
        }
        this._onDidChange.fire(uri);
    }

    provideTextDocumentContent(uri) {
        const editor = vscode.window.activeTextEditor;
        if(!editor) {
            return "";
        }

        const text = render(editor.document.getText());
        return text;
    }

    deactivate() {
        return deactivate();
    }

    activate() {
        return activate();
    }
}

module.exports = DiagramProvider;