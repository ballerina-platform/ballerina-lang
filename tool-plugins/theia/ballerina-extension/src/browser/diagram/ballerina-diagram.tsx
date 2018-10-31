import * as React from 'react';

import '../../../../resources/lib/bundle.css';
import '../../../../resources/lib/theme.css';
import '../../../../resources/lib/less.css';

import '../../../src/browser/style/preview.css';

const { BallerinaDesignView, TreeBuilder } = require('../../../../resources/lib/ballerina-diagram-library');

const TREE_MODIFIED = 'tree-modified';

export interface EditModeToggleEvent {
    editMode: boolean,
}

export interface DiagramModeChangeEvent {
    mode: string,
}

export interface BallerinaAST {
    on(evt: string, handler: Function): void;
    off(evt: string, handler: Function): void;
    getSource(): string;
}

export interface ParserReply {
    model?: BallerinaAST
}

export interface BallerinaDiagramChangeEvent {
    newContent: string
}

export interface BallerinaDiagramProps {
    parseContent (content: string) : Promise<ParserReply>;
    onChange(evt: BallerinaDiagramChangeEvent) : void;
    content: string;
    width: number;
    height: number;
}

interface BallerinaDiagramState {
    content: string;
    currentAST: BallerinaAST | undefined;
    editMode: boolean,
    diagramMode: string 
}

export class BallerinaDiagram extends React.Component<BallerinaDiagramProps, BallerinaDiagramState> {
    constructor(props: BallerinaDiagramProps) {
        super(props);
        this.state = {
            content: this.props.content,
            currentAST: undefined,
            editMode: true,
            diagramMode: 'action'
        }
    }

    componentDidMount() {
        this.updateDiagram(this.state.content);
    }

    componentWillReceiveProps(nextProps: BallerinaDiagramProps) {
        this.updateDiagram(nextProps.content);
    }

    updateDiagram(content: string) : void {
        this.props.parseContent(content)
                .then((parserReply: ParserReply) => {
                    const { currentAST } = this.state;
                    if (parserReply.model) {
                        if (currentAST) {
                            currentAST.off(TREE_MODIFIED, this.onModelUpdate);
                        }
                        const newAST = TreeBuilder.build(parserReply.model);
                        if (newAST) {
                            newAST.on(TREE_MODIFIED, this.onModelUpdate.bind(this));
                        }
                        this.setState({
                            currentAST: newAST,
                            content
                        });
                    }
                });
        this.forceUpdate();
    }

    protected onModelUpdate(evt: any) {
        const { currentAST } = this.state;
        if (currentAST) {
            const newContent = currentAST.getSource();
            this.setState({
                content: newContent
            });
            this.props.onChange({ newContent });
        }
    }

    render(): React.ReactNode {
        const { currentAST, diagramMode, editMode } = this.state;
        const { width, height } = this.props;
        if (!currentAST) {
            return (
                <div className='spinnerContainer'>
                    <div className='fa fa-spinner fa-pulse fa-3x fa-fw' style={{ color: "grey" }}></div>
                </div>
            )
        } 
        return <React.Fragment>
                <div className='ballerina-editor design-view-container'>
                    <BallerinaDesignView 
                        model={currentAST}
                        mode={diagramMode}
                        editMode={editMode}
                        height={height}
                        width={width}
                        onModeChange={(evt: EditModeToggleEvent) => {
                            this.setState({
                                editMode: evt.editMode
                            });
                        }}
                        onCodeExpandToggle={(evt: DiagramModeChangeEvent) => {
                            this.setState({
                                diagramMode: evt.mode
                            });
                        }}
                    />
                </div>
            </React.Fragment>;
    }
}