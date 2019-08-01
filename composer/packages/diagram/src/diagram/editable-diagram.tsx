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

export interface EditableDiagramProps extends CommonDiagramProps {
    docUri: string;
    langClient: IBallerinaLangClient;
}

export interface EditableDiagramState {
    ast?: CompilationUnit;
    docUri: string;
    editingEnabled: boolean;
    hasSyntaxErrors: boolean;
    error?: Error;
    width?: number;
    height?: number;
}

export class EditableDiagram extends React.Component<EditableDiagramProps, EditableDiagramState> {

    // get default context or provided context from a parent (if any)
    public static contextType = DiagramContext;

    public state: EditableDiagramState = {
        ast: undefined,
        docUri: this.props.docUri,
        editingEnabled: false,
        hasSyntaxErrors: false,
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
            docUri: "",
            fitToWidthOrHeight: true,
            height,
            langClient: this.props.langClient,
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

    public componentWillReceiveProps(nextProps: EditableDiagramProps) {
        this.updateAST(nextProps.docUri);
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
            const { langClient } = this.props;
            const serializableAST = JSON.stringify(this.state.ast, (key, value) => {
                if (key === "parent" || key === "viewState") {
                    return undefined;
                }
                return value;
            });
            langClient.astDidChange({
                ast: JSON.parse(serializableAST) as BallerinaAST,
                textDocumentIdentifier: {
                    uri: this.state.docUri
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

    public updateAST(uri: string = this.state.docUri) {
        const newState: Partial<EditableDiagramState> = {
            docUri: uri,
            error: undefined,
            hasSyntaxErrors: false
        };
        // invoke the parser and get the AST
        this.props.langClient.getAST({
            documentIdentifier: {
                uri,
            },
        }).then(({ ast, parseSuccess }) => {
            if (ast) {
                newState.ast = ast as CompilationUnit;
            } else if (parseSuccess) {
                // ast cannot be created due to syntax errors
                newState.hasSyntaxErrors = true;
            } else {
                // runtime error while parsing
                newState.error = new Error("Unable to parse " + this.props.docUri);
            }
            this.setState(newState as EditableDiagramState);
        }, (error) => {
            newState.error = error;
            this.setState(newState as EditableDiagramState);
        });
    }

    private createContext(): IDiagramContext {
        // create context contributions for the children
        const contextContributions: Partial<IDiagramContext> = {
            docUri: this.state.docUri,
            editingEnabled: this.state.editingEnabled,
            hasSyntaxErrors: this.state.hasSyntaxErrors,
            langClient: this.props.langClient,
        };

        // merge with parent (if any) or with default context
        return { ...this.context, ...contextContributions };
    }
}
