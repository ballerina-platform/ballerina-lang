import { ReactWidget } from '@theia/core/lib/browser/widgets/react-widget';
import { injectable, postConstruct, inject } from 'inversify';
import { EditorManager, TextEditor, EditorWidget, TextDocumentChangeEvent } from "@theia/editor/lib/browser";
import { DisposableCollection } from '@theia/core/lib/common/disposable';
import * as React from 'react';
import { parseContent, ParserReply } from '../common'

import '../../../../../../../lib/bundle.css';
import '../../../../../../../lib/theme.css';
import '../../../../../../../lib/less.css';


const { BalDiagram, TreeBuilder, BallerinaDiagramWrapper } = require('../../../../../../../lib/ballerina-diagram-library');


@injectable()
export class BallerinaPreviewWidget extends ReactWidget {

    protected currentAST: Object | undefined;
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

    update() : void {
        this.getAST()
                .then((parserReply: ParserReply) => {
                    if (parserReply.model) {
                        this.currentAST = TreeBuilder.build(parserReply.model);
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
    }

    protected onDocumentContentChanged(editor: TextEditor, event: TextDocumentChangeEvent): void {
        if (event.contentChanges.length > 0) {
            this.update();
        }
    }

    protected render(): React.ReactNode {
        if (!this.currentAST) {
            return <div>{'Unable to render diagram.'}</div>
        }
        return <React.Fragment>
                    <BallerinaDiagramWrapper>
                        <BalDiagram 
                            model={this.currentAST}
                            mode='action'
                            editMode={true}
                            height={300}
                            width={300}
                        />
                    </BallerinaDiagramWrapper>
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