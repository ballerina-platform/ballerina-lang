import { ASTUtil, CompilationUnit, Disposable } from "@ballerina/ast-model";
import { BallerinaAST, IBallerinaLangClient  } from "@ballerina/lang-service";
import debounce from "lodash.debounce";
import React from "react";
import { CommonDiagramProps, Diagram } from "./diagram";
import { DiagramContext, IDiagramContext } from "./diagram-context";
import { DiagramErrorBoundary } from "./diagram-error-boundary";
import { DiagramErrorDialog } from "./error-dialog";
import { Loader } from "./loader";

const resizeDelay = 200;

export interface EdiatableDiagramProps extends CommonDiagramProps {
    docUri: string;
    langClient: IBallerinaLangClient;
}

export interface EditableDiagramState {
    ast?: CompilationUnit;
    editingEnabled: boolean;
    error?: Error;
    width?: number;
    height?: number;
}

export class EditableDiagram extends React.Component<EdiatableDiagramProps, EditableDiagramState> {

    // get default context or provided context from a parent (if any)
    public static contextType = DiagramContext;

    public state: EditableDiagramState = {
        ast: undefined,
        editingEnabled: false,
        height: this.props.height,
        width: this.props.width
    };

    private wrapperRef = React.createRef<HTMLDivElement>();

    private disposables: Disposable[] = [];

    public render() {
        const { mode, zoom } = this.props;
        const { ast, error, height, width } = this.state;

        // create props for the diagram
        const diagramProps = {
            ast,
            height,
            mode,
            width,
            zoom,
        };

        return <DiagramContext.Provider value={this.createContext()}>
                    <DiagramErrorBoundary>
                        <div
                            className="diagram-wrapper"
                            style={{ width: "100%", height: "100%" }}
                            ref={this.wrapperRef}
                        >
                            {!ast && !error && <Loader />}
                            {ast && <Diagram {...diagramProps} />}
                            {error && <DiagramErrorDialog error={error as Error} />}
                        </div>
                    </DiagramErrorBoundary>
            </DiagramContext.Provider>;
    }

    public componentWillReceiveProps(nextProps: EdiatableDiagramProps) {
        if (this.props.docUri !== nextProps.docUri) {
            this.updateAST(nextProps.docUri);
        }
    }

    public componentDidMount(): void {
        const wrapperDiv = this.wrapperRef.current;
        const parentOffset = wrapperDiv ? wrapperDiv.offsetParent : undefined;
        if (parentOffset) {
            this.setState({
                height: parentOffset.clientHeight,
                width: parentOffset.clientWidth
            });
        }
        if (!this.props.langClient.isInitialized) {
            this.props.langClient.init()
                .then(() => this.updateAST());
        } else {
            this.updateAST();
        }
        if (window) {
            window.onresize = debounce(() => {
                if (parentOffset) {
                    this.setState({
                        height: parentOffset.clientHeight,
                        width: parentOffset.clientWidth
                    });
                }
            }, resizeDelay);
        }
        const disposable = ASTUtil.onTreeModified((tree) => {
            this.forceUpdate();
            const { langClient, docUri } = this.props;
            langClient.astDidChange({
                ast: tree as BallerinaAST,
                textDocumentIdentifier: {
                    uri: docUri
                },
            });
        });
        this.disposables.push(disposable);
    }

    public componentWillUnmount() {
        this.disposables.forEach((disposable) => {
            disposable.dispose();
        });
    }

    public updateAST(uri: string = this.props.docUri) {
        // invoke the parser and get the AST
        this.props.langClient.getAST({
            documentIdentifier: {
                uri,
            },
        }).then((resp) => {
            if (resp.ast) {
                this.setState({
                    ast: resp.ast as CompilationUnit,
                    error: undefined
                });
            } else {
                this.setState({
                    error: new Error("Unable to parse " + this.props.docUri)
                });
            }
        }, (error) => {
           this.setState({ error });
        });
    }

    private createContext(): IDiagramContext {
        // create context contributions for the children
        const contextContributions: Partial<IDiagramContext> = {
            docUri: this.props.docUri,
            editingEnabled: this.state.editingEnabled,
            langClient: this.props.langClient,
            toggleEditing: () => {
                this.setState({
                    editingEnabled: !this.state.editingEnabled,
                });
            }
        };

        // merge with parent (if any) or with default context
        return { ...this.context, ...contextContributions };
    }
}
