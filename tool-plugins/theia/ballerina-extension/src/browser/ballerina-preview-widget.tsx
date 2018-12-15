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

import '@ballerina/distribution/build/themes/ballerina-default.css';
import { ILanguageClient } from '@theia/languages/lib/browser';

@injectable()
export class BallerinaPreviewWidget extends ReactWidget {

    protected readonly toDisposePerCurrentEditor = new DisposableCollection();
    protected langClient?: BallerinaLangClient;

    constructor(
        @inject(EditorManager) readonly editorManager: EditorManager,
        @inject(LanguageClientProvider) readonly languageClientProvider: LanguageClientProvider
    ) {
        super();
        this.id = 'ballerina-preview-widget';
        this.title.label = 'Ballerina Interaction';
        this.title.closable = true;
        this.addClass('ballerina-preview');
        this.languageClientProvider.getLanguageClient(BALLERINA_LANGUAGE_ID)
            .then((langClient) => {
                this.langClient = new BallerinaLangClient(langClient as ILanguageClient);
                this.update();
            });
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
                <div className='diagram'>
                    {this.langClient && <EditableDiagram
                        mode={DiagramMode.ACTION}
                        docUri={currentEditor.document.uri}
                        zoom={1}
                        height={this.node.clientHeight}
                        width={this.node.clientWidth}
                        langClient={this.langClient as BallerinaLangClient}
                    />}
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