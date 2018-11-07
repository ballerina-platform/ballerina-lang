import React from "react";

export interface DiagramEditorProps {
    width?: number;
}

interface DiagramEditorState {

}

export class DiagramEditor extends React.Component<DiagramEditorProps, DiagramEditorState> {
    
    render() {
        return <div>{'Diagram Editor'}</div>
    }
}