import { ReactWidget } from '@theia/core/lib/browser/widgets/react-widget';
import { Widget } from '@theia/core/lib/browser/widgets/widget';
import { injectable, postConstruct, inject } from 'inversify';
import { EditorManager, TextEditor, EditorWidget, TextDocumentChangeEvent } from "@theia/editor/lib/browser";
import { DisposableCollection } from '@theia/core/lib/common/disposable';
import * as React from 'react';
import * as lsp from 'vscode-languageserver-types';
import { parseContent, ParserReply, BALLERINA_LANGUAGE_ID } from '../common'

import '../../../resources/lib/bundle.css';
import '../../../resources/lib/theme.css';
import '../../../resources/lib/less.css';

import '../../src/browser/style/preview.css';


const { BallerinaDesignView, TreeBuilder } = require('../../../resources/lib/ballerina-diagram-library');

const TREE_MODIFIED = 'tree-modified';
export interface EditModeChangeEvent {
    editMode: boolean,
}
export interface DiagramModeChangeEvent {
    mode: string,
}

export interface AST {
    on(evt: string, handler: Function): void;
    off(evt: string, handler: Function): void;
    getSource(): string;
}
@injectable()
export class BallerinaPreviewWidget extends ReactWidget {

    protected currentAST: AST | undefined;
    protected readonly toDisposePerCurrentEditor = new DisposableCollection();
    protected editMode: boolean = true;
    protected diagramMode: string = 'action';

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
        super.update();
    }

    update() : void {
        this.getAST()
                .then((parserReply: ParserReply) => {
                    if (parserReply.model) {
                        if (this.currentAST) {
                            this.currentAST.off(TREE_MODIFIED, this.onModelUpdate);
                        }
                        this.currentAST = TreeBuilder.build(parserReply.model);
                        if (this.currentAST) {
                            this.currentAST.on(TREE_MODIFIED, this.onModelUpdate.bind(this));
                        }
                        super.update();
                    }
                });
        super.update();
    }

    protected onCurrentEditorChanged(editorWidget: EditorWidget | undefined): void {
        this.currentAST = undefined;
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

    protected onModelUpdate(evt: any) {
        if (this.currentAST) {
            const newContent = this.currentAST.getSource();
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
            console.log(newContent);
        }
    }

    protected onDocumentContentChanged(editor: TextEditor, event: TextDocumentChangeEvent): void {
        if (event.contentChanges.length > 0) {
            this.update();
        }
    }

    protected render(): React.ReactNode {
        const currentEditor = this.getCurrentEditor();
        if (currentEditor && currentEditor.document.languageId !== BALLERINA_LANGUAGE_ID) {
            return (
                <div className="ballerina-preview">
                    <div>
                        {'Unsupported file type.'}
                    </div>
                </div>
            )
        }
        if (!this.currentAST) {
            return (
                <div className='spinnerContainer'>
                    <div className='fa fa-spinner fa-pulse fa-3x fa-fw' style={{ color: "grey" }}></div>
                </div>
            )
        } 
        return <React.Fragment>
                <div className='ballerina-editor design-view-container'>
                    <BallerinaDesignView 
                        model={this.currentAST}
                        mode={this.diagramMode}
                        editMode={this.editMode}
                        height={this.node.clientHeight}
                        width={this.node.clientWidth}
                        onModeChange={(evt: EditModeChangeEvent) => {
                            this.editMode = evt.editMode;
                            this.update();
                        }}
                        onCodeExpandToggle={(evt: DiagramModeChangeEvent) => {
                            this.diagramMode = evt.mode;
                            this.update();
                        }}
                    />
                </div>
            </React.Fragment>;
    }

    protected getAST(): Promise<ParserReply> {
        const currentEditor: TextEditor | undefined = this.getCurrentEditor();
        if (currentEditor) {
            return parseContent(currentEditor.document.getText());
        }
        return Promise.resolve({});
    }

    protected getCurrentEditor(): TextEditor | undefined {
        const activeEditor = this.editorManager.currentEditor;
        if (activeEditor) {
            return activeEditor.editor;
        }
        return undefined;
    }

}