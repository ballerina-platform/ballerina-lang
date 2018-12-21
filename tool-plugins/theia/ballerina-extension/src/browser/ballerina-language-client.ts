import { GetASTParams, GetASTResponse, ASTDidChangeParams, ASTDidChangeResponse,
        BallerinaExampleListParams, BallerinaExampleListResponse, GetBallerinaProjectParams,
        BallerinaProject, GoToSourceParams, RevealRangeParams, IBallerinaLangClient 
    } from "@ballerina/lang-service/lib/src/client/model";
import { InitializeParams, InitializeResult,  } from "@ballerina/lang-service/node_modules/vscode-languageserver-protocol/lib/protocol";
import { 
        BallerinaSourceFragment, BallerinaASTNode, BallerinaEndpoint 
    } from "@ballerina/lang-service/lib/src/client/ast-models";
import { ILanguageClient, Position, Range } from "@theia/languages/lib/browser";
import { TextEditor } from "@theia/editor/lib/browser/editor";
import { EditorManager } from "@theia/editor/lib/browser/editor-manager";
import { EditorWidget } from "@theia/editor/lib/browser";
import URI from "@theia/core/lib/common/uri";


export class BallerinaLangClient implements IBallerinaLangClient {

    isInitialized: boolean = true;

    constructor(private langClient: ILanguageClient,
        private editorManager: EditorManager) {
    }

    public init(params?: InitializeParams): Thenable<InitializeResult> {
        return Promise.resolve(this.langClient.initializeResult as InitializeResult);
    }

    public getAST(params: GetASTParams): Thenable<GetASTResponse> {
        return this.langClient.sendRequest<GetASTResponse>("ballerinaDocument/ast", params);
    }

    public astDidChange(params: ASTDidChangeParams): Thenable<ASTDidChangeResponse> {
        return this.langClient.sendRequest("ballerinaDocument/astDidChange", params);
    }

    public fetchExamples(params: BallerinaExampleListParams = {}): Thenable<BallerinaExampleListResponse> {
        return this.langClient.sendRequest("ballerinaExample/list", params);
    }

    public parseFragment(params: BallerinaSourceFragment): Thenable<BallerinaASTNode> {
        return this.langClient.sendRequest("ballerinaFragment/ast", params).then((resp: any) => resp.ast);
    }

    public getEndpoints(): Thenable<BallerinaEndpoint[]> {
        return this.langClient.sendRequest("ballerinaSymbol/endpoints", {})
                    .then((resp: any) => resp.endpoints);
    }

    public getBallerinaProject(params: GetBallerinaProjectParams): Thenable<BallerinaProject> {
        return this.langClient.sendRequest("ballerinaDocument/project", params);
    }

    public goToSource(params: GoToSourceParams): void {
       // TODO
    }

    public revealRange(params: RevealRangeParams): void {
        const revealRangeInEditor = (editor: TextEditor) => {
            const { start, end } = params.range;
            const startPosition = Position.create(start.line - 1, start.character - 1);
            const endPosition = Position.create(end.line - 1, end.character - 1);
            const range = Range.create(startPosition, endPosition);
            editor.revealRange(range);
            editor.selection = range;
        };
        const activeEditorWidget = this.editorManager.currentEditor;
        const visibleEditorWidgets = this.editorManager.all;
        const findByDocUri = (widget: EditorWidget) => widget.editor.document.uri.toString() 
                                === params.textDocumentIdentifier.uri;
        const foundVisibleEditorWidget = visibleEditorWidgets.find(findByDocUri);

        if (activeEditorWidget && findByDocUri(activeEditorWidget)) {
            revealRangeInEditor(activeEditorWidget.editor);
        } else if (foundVisibleEditorWidget) {
            revealRangeInEditor(foundVisibleEditorWidget.editor);
        } else {
            this.editorManager.open(new URI(params.textDocumentIdentifier.uri), {
                widgetOptions: {
                    area: "main"
                }
            }).then((openedEditor) => {
                revealRangeInEditor(openedEditor.editor);
            });
        }
    }

    public close(): void {
        // TODO
    }
}