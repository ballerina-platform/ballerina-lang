import { ReactWidget } from '@theia/core/lib/browser/widgets/react-widget';
import { Widget } from '@theia/core/lib/browser/widgets/widget';
import { injectable, postConstruct, inject } from 'inversify';
import { EditorManager, TextEditor, EditorWidget, TextDocumentChangeEvent } from "@theia/editor/lib/browser";
import { DisposableCollection } from '@theia/core/lib/common/disposable';
import * as React from 'react';
import { BALLERINA_LANGUAGE_ID } from '../common'

import { EditableDiagram, DiagramMode } from '@ballerina/diagram'; 
import { BallerinaLangClient } from './ballerina-language-client';
import { LanguageClientProvider } from '@theia/languages/lib/browser/language-client-provider';

import '@ballerina/font/build/font/font-ballerina.css';
import '@ballerina/distribution/build/themes/ballerina-default.css';
import { ILanguageClient } from '@theia/languages/lib/browser';

@injectable()
export class BallerinaPreviewWidget extends ReactWidget {

    protected readonly toDisposePerCurrentEditor = new DisposableCollection();
    protected langClient?: BallerinaLangClient;
    protected currentEditor?: EditorWidget;

    constructor(
        @inject(EditorManager) readonly editorManager: EditorManager,
        @inject(LanguageClientProvider) readonly languageClientProvider: LanguageClientProvider
    ) {
        super();
        this.id = 'ballerina-preview-widget';
        this.title.label = 'Ballerina Diagram';
        this.title.closable = true;
        this.addClass('ballerina-preview');
        // this is a workaround to fix styles until ballerina theme is fixed
        const body = document.getElementsByTagName("body").item(0)
        if (body) {
            body.classList.add("diagram");
        }
        this.languageClientProvider.getLanguageClient(BALLERINA_LANGUAGE_ID)
            .then((langClient) => {
                this.langClient = new BallerinaLangClient(langClient as ILanguageClient, this.editorManager);
                this.update();
            });
    }

    @postConstruct()
    protected init(): void {
        this.update();
        this.toDispose.push(this.editorManager.onCurrentEditorChanged(this.onCurrentEditorChanged.bind(this)));
        this.toDispose.push(this.editorManager.onCreated(this.onCurrentEditorChanged.bind(this)));
        this.toDispose.push(this.editorManager.onActiveEditorChanged(this.onCurrentEditorChanged.bind(this)));
    }

    protected onResize(msg: Widget.ResizeMessage): void {
        super.onResize(msg);
        this.update();
    }

    protected onCurrentEditorChanged(editorWidget: EditorWidget | undefined): void {
        this.toDisposePerCurrentEditor.dispose();
        const currentEditor = editorWidget
                    || this.editorManager.currentEditor
                    || this.editorManager.activeEditor
                    || this.editorManager.all.find(e => e.isVisible);
        this.currentEditor = currentEditor;  
        if (currentEditor) {
            const { editor } = currentEditor;
            this.toDisposePerCurrentEditor.push(
                editor.onDocumentContentChanged(event => this.onDocumentContentChanged(editor, event)),
            );
            this.update();
            if (editor && editor.document.languageId !== BALLERINA_LANGUAGE_ID) {
                if (this.isVisible) {
                    this.hide();
                }
            } else if (this.isHidden) {
                this.show();
            }
        } else {
            this.update();
        }
    }

    protected onDocumentContentChanged(editor: TextEditor, event: TextDocumentChangeEvent): void {
        this.update();
    }

    protected render(): React.ReactNode {
        const { currentEditor } = this;
        if (!currentEditor) {
            return (
                <div className="ballerina-preview">
                    <div>
                        {'No bal file is open.'}
                    </div>
                </div>
            )
        }
        const { editor } = currentEditor;
        if (editor && editor.document.languageId !== BALLERINA_LANGUAGE_ID) {
            return (
                <div className="ballerina-preview">
                    <div>
                        {'Unsupported file type.'}
                    </div>
                </div>
            )
        }
        return <React.Fragment>
                <div className='diagram'>
                    {this.langClient && <EditableDiagram
                        mode={DiagramMode.ACTION}
                        docUri={editor.document.uri}
                        zoom={1}
                        height={this.node.clientHeight}
                        width={this.node.clientWidth}
                        langClient={this.langClient as BallerinaLangClient}
                    />}
                </div>
            </React.Fragment>;
    }
}