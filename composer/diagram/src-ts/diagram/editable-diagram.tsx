import { BallerinaAST, BallerinaASTNode, BallerinaEndpoint, BallerinaSourceFragment } from "@ballerina/ast-model";
import { ASTDidChangeParams, ASTDidChangeResponse, GetASTParams,
    GetASTResponse } from "@ballerina/lang-service";
import React from "react";
import { CommonDiagramProps, Diagram } from "./diagram";
import { DiagramContext, IDiagramContext } from "./diagram-context";

export interface DiagramLangClient {
    getAST(params: GetASTParams): Thenable<GetASTResponse>;
    astDidChange(params: ASTDidChangeParams): Thenable<ASTDidChangeResponse>;
    parseFragment(params: BallerinaSourceFragment): Thenable<BallerinaASTNode>;
    getEndpoints(): Thenable<BallerinaEndpoint[]>;
    goToSource(line: number, column: number): void;
}

export interface EdiatableDiagramProps extends CommonDiagramProps {
    docUri: string;
    langClient: DiagramLangClient;
}

export interface EditableDiagramState {
    ast?: BallerinaAST;
    editingEnabled: boolean;
}

export class EditableDiagram extends React.Component<EdiatableDiagramProps, EditableDiagramState> {

    // get default context or provided context from a parent (if any)
    private static contextType = DiagramContext;

    public state = {
        ast: undefined,
        editingEnabled: true,
    };

    public render() {
        const { height, width, mode, zoom } = this.props;
        const { ast } = this.state;

        // create props for the diagram
        const diagramProps = { height, width, mode, zoom, ast };

        return <DiagramContext.Provider value={this.createContext()}>
                <Diagram {...diagramProps} />
            </DiagramContext.Provider>;
    }

    public componentDidMount(): void {
        // invoke the parser and get the AST
        this.props.langClient.getAST({
            documentIdentifier: {
                uri: this.props.docUri,
            },
        }).then((resp) => {
            if (resp.ast) {
               this.setState({
                   ast: resp.ast,
               });
            }
        });
    }

    private createContext(): IDiagramContext {
        // create context contributions for the children
        const contextContributions = {
            editingEnabled: this.state.editingEnabled,
            toggleEditing: () => {
                this.setState({
                    editingEnabled: !this.state.editingEnabled,
                });
            },
        };

        // merge with parent (if any) or with default context
        return { ...this.context, ...contextContributions };
    }
}
