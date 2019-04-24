import { WebViewMethod, WebViewRPCMessage } from './model';
import { Webview, Position, Range, Selection, window, Uri, TextEditor, ViewColumn, commands } from 'vscode';
import { ExtendedLangClient } from 'src/core/extended-language-client';

const getLangClientMethods = (langClient: ExtendedLangClient): WebViewMethod[] => {
    return [{
        methodName: 'getAST',
        handler: (args: any[]) => {
            return langClient.onReady().then(() => {
                return langClient.getAST(args[0]);
            });
        }
    },
    {
        methodName: 'astDidChange',
        handler: (args: any[]) => {
            return langClient.onReady().then(() => {
                return langClient.triggerASTDidChange(args[0], args[1]);
            });
        }
    },
    {
        methodName: 'getEndpoints',
        handler: (args: any[]) => {
            return langClient.onReady().then(() => {
                return langClient.getEndpoints();
            });
        }
    },
    {
        methodName: 'parseFragment',
        handler: (args: any[]) => {
            return langClient.onReady().then(() => {
                return langClient.parseFragment({
                    enclosingScope: args[0].enclosingScope,
                    expectedNodeType: args[0].expectedNodeType,
                    source: args[0].source
                });
            });
        }
    },
    {
        methodName: 'revealRange',
        handler: (args: any[]) => {
            const params = JSON.parse(args[0]);
            const revealRangeInEditor = (editor: TextEditor) => {
                const { start, end } = params.range;
                const startPosition = new Position(start.line - 1, start.character - 1);
                const endPosition = new Position(end.line - 1, end.character - 1);
                editor.revealRange(new Range(startPosition, endPosition));
                editor.selection = new Selection(startPosition, endPosition);
            };
            const activeTextEditor = window.activeTextEditor;
            const visibleTextEditors = window.visibleTextEditors;
            const findByDocUri = (editor: TextEditor) => editor.document.uri.toString() 
                                    === params.textDocumentIdentifier.uri;
            const foundVisibleEditor = visibleTextEditors.find(findByDocUri);

            if (activeTextEditor && findByDocUri(activeTextEditor)) {
                revealRangeInEditor(activeTextEditor);
            } else if (foundVisibleEditor) {
                revealRangeInEditor(foundVisibleEditor);            
                return Promise.resolve();   
            } else {
                return window.showTextDocument(Uri.parse(params.textDocumentIdentifier.uri)
                    ,{
                        viewColumn: ViewColumn.One
                    })
                    .then((textEditor) => {
                        revealRangeInEditor(textEditor);
                    });
            }
        }
    },
    {
        methodName: 'goToSource',
        handler: (args: any[]) => {
            const activeEditor = window.activeTextEditor;
            if (activeEditor) {
                // TODO
            }
            return Promise.resolve();
        }
    },
    {
        methodName: 'getExamples',
        handler: (args: any[]) => {
            return langClient.onReady().then(() => {
                return langClient.fetchExamples();
            });
        }
    },
    {
        methodName: 'getDefinitionPosition',
        handler: (args: any[]) => {
            return langClient.getDefinitionPosition(args[0]);
        }
    }
    ];
};

const undoRedoMethods = [{
        methodName: 'undo',
        handler: (args: any[]) => {
            commands.executeCommand('workbench.action.focusPreviousGroup')
                .then(() => {
                    commands.executeCommand('undo');
                });
        }
    },
    {
        methodName: 'redo',
        handler: (args: any[]) => {
            commands.executeCommand('workbench.action.focusPreviousGroup')
                .then(() => {
                    commands.executeCommand('redo');
                });
           
        }
    }
];

export class WebViewRPCHandler {

    private _sequence: number = 1;
    private _callbacks: Map<number, Function> = new Map();

    constructor(public methods: Array<WebViewMethod>, public webView: Webview){
        webView.onDidReceiveMessage(this._onRemoteMessage.bind(this));
    }

    private _getMethod(methodName: string) {
        return this.methods.find(method => (method.methodName === methodName));
    }

    private _onRemoteMessage(msg: WebViewRPCMessage) {
        if (msg.id !== undefined) {
            // this is a request from remote
            const method = this._getMethod(msg.methodName);
            if (method) {
                method.handler(msg.arguments || [])
                    .then((response: Thenable<any>) => {
                        this.webView.postMessage({
                            originId: msg.id,
                            response,
                        });
                    });
            }
        } else if (msg.originId !== undefined) {
            // this is a response from remote to one of our requests
            const callback = this._callbacks.get(msg.originId);
            if (callback) {
                callback(msg.response);
                this._callbacks.delete(msg.originId);
            }
        }
    }

    invokeRemoteMethod(methodName: string, args: any[] = [], callback: Function) {
        const msg = {
            id: this._sequence,
            methodName,
            arguments: args,
        };
        this._callbacks.set(this._sequence, callback);
        this.webView.postMessage(msg);
        this._sequence++;
    }

    static create(
        webView: Webview,
        langClient: ExtendedLangClient,
        methods: Array<WebViewMethod> = [])
            : WebViewRPCHandler {
        return new WebViewRPCHandler(
            [...methods, ...getLangClientMethods(langClient), ...undoRedoMethods],
            webView);
    }

    dispose() {
        // TODO unregister event handlers
    }
}
