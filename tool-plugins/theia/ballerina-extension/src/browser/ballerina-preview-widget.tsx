import axios from "axios";
import { ReactWidget } from '@theia/core/lib/browser/widgets/react-widget';
import { Widget } from '@theia/core/lib/browser/widgets/widget';
import { injectable, postConstruct, inject } from 'inversify';
import { EditorManager, TextEditor, EditorWidget, TextDocumentChangeEvent } from "@theia/editor/lib/browser";
import { DisposableCollection } from '@theia/core/lib/common/disposable';
import * as React from 'react';
import * as lsp from 'vscode-languageserver-types';
import { BALLERINA_LANGUAGE_ID } from '../common'

import { BallerinaDiagram, BallerinaDiagramChangeEvent, ParserReply } from './diagram/ballerina-diagram'; 

export function parseContent(content: String) : Promise<ParserReply> {
    const parseOpts = {
        content,
        includePackageInfo: true,
        includeProgramDir: true,
        includeTree: true,
    }
    return axios.post(
                    'https://parser.playground.preprod.ballerina.io/api/parser',
                    parseOpts,
                    { 
                        headers: {
                            'content-type': 'application/json; charset=utf-8',
                        } 
                    })
                .then(response => response.data);
}

@injectable()
export class BallerinaPreviewWidget extends ReactWidget {

    protected readonly toDisposePerCurrentEditor = new DisposableCollection();

    constructor(
        @inject(EditorManager) readonly editorManager: EditorManager
    ) {
        super();
        this.id = 'ballerina-preview-widget';
        this.title.label = 'Ballerina Interaction';
        this.title.closable = true;
        this.addClass('ballerina-preview');
    }

    @postConstruct()
    protected init(): void {
        this.update();
        this.toDispose.push(this.editorManager.onCurrentEditorChanged(this.onCurrentEditorChanged.bind(this)));
    }

    protected onResize(msg: Widget.ResizeMessage): void {
        super.onResize(msg);
        this.update();
    }

    protected onCurrentEditorChanged(editorWidget: EditorWidget | undefined): void {
        this.toDisposePerCurrentEditor.dispose();
        if (editorWidget) {
            const { editor } = editorWidget;
            this.toDisposePerCurrentEditor.push(
                editor.onDocumentContentChanged(event => this.onDocumentContentChanged(editor, event))
            );
        }
        this.update();
        const currentEditor = this.getCurrentEditor();
        if (currentEditor && currentEditor.document.languageId !== BALLERINA_LANGUAGE_ID) {
            if (this.isVisible) {
                this.hide();
            }
        } else if (this.isHidden) {
            this.show();
        }
    }

    protected onChange(evt: BallerinaDiagramChangeEvent) {
        const newContent = evt.newContent;
        const currentEditor: TextEditor | undefined = this.getCurrentEditor();
        if (currentEditor && currentEditor.document.languageId === BALLERINA_LANGUAGE_ID) {
            const endLine = currentEditor.document.lineCount;
            const endOffset = currentEditor.document.getLineContent(endLine).length;
            const startPosition = lsp.Position.create(0,0);
            const endPosition = lsp.Position.create(endLine, endOffset);
            const editOperation: lsp.TextEdit = {
                newText: newContent,
                range: lsp.Range.create(startPosition, endPosition)
            };
            currentEditor.executeEdits([editOperation])
        }
    }

    protected onDocumentContentChanged(editor: TextEditor, event: TextDocumentChangeEvent): void {
        this.update();
    }

    protected render(): React.ReactNode {
        const currentEditor = this.getCurrentEditor();
        if (!currentEditor) {
            return (
                <div className="ballerina-preview">
                    <div>
                        {'No bal file is open.'}
                    </div>
                </div>
            )
        }
        if (currentEditor && currentEditor.document.languageId !== BALLERINA_LANGUAGE_ID) {
            return (
                <div className="ballerina-preview">
                    <div>
                        {'Unsupported file type.'}
                    </div>
                </div>
            )
        }
        return <React.Fragment>
                <div className='ballerina-editor design-view-container'>
                    <BallerinaDiagram 
                        content={currentEditor.document.getText()}
                        parseContent={parseContent}
                        onChange={this.onChange.bind(this)}
                        height={this.node.clientHeight}
                        width={this.node.clientWidth}
                    />
                </div>
            </React.Fragment>;
    }

    protected getCurrentEditor(): TextEditor | undefined {
        const activeEditor = this.editorManager.currentEditor;
        if (activeEditor) {
            return activeEditor.editor;
        }
        return undefined;
    }

}