import { createElement } from "react";
import ReactDOM from "react-dom";
import { Diagram, DiagramProps } from "./diagram";

export interface StaticDiagramArgs {
    target: HTMLElement;
    diagramProps: DiagramProps;
}

export const renderStaticDiagram = (args: StaticDiagramArgs) => {
    const BalDiagramElement = createElement(Diagram, args.diagramProps);
    ReactDOM.render(BalDiagramElement, args.target);
};
