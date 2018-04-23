const vscode = require('vscode');
const fs = require('fs');
const path = require('path');
const { render, activate } = require('./renderer');

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

    activate() {
        return activate();
    }
}

class StaticProvider {
    provideTextDocumentContent(uri) {
        return require(`.${uri.path}`);
    }
}

module.exports = {
    DiagramProvider,
    StaticProvider,
};