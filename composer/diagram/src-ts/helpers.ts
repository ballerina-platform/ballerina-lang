import { createElement } from "react";
import ReactDOM from "react-dom";
import { DiagramEditor, DiagramEditorProps } from "./DiagramEditor";

export interface StaticDiagramArgs {
    target: HTMLElement;
    diagramProps: DiagramEditorProps;
}

export const renderStaticDiagram = (args: StaticDiagramArgs) => {
    const BalDiagramElement = createElement(DiagramEditor, args.diagramProps);
    ReactDOM.render(BalDiagramElement, args.target);
};
