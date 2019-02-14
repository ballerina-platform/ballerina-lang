
import { ASTUtil, ObjectType, TypeDefinition as TypeDefinitionNode } from "@ballerina/ast-model";
import * as React from "react";
import { DiagramConfig } from "../../config/default";
import { DiagramUtils } from "../../diagram/diagram-utils";
import { getCodePoint } from "../../utils";
import { ViewState } from "../../view-model/index";

const config: DiagramConfig = DiagramUtils.getConfig();

export const TypeDefinition: React.StatelessComponent<{
    model: TypeDefinitionNode
}> = ({
    model
}) => {
        if (model.service || !ASTUtil.isValidObjectType(model)) { return <g />; }
        const objectType = model.typeNode as ObjectType;
        const resources: React.ReactNode[] = [];
        const serviceTitle = { x: 0, y: 0, className: "panel-group-title" };
        const serviceIcon = { x: 0, y: 0, className: "panel-group-title panel-group-title-icon" };

        const viewState: ViewState = model.viewState;

        serviceTitle.y = serviceIcon.y = viewState.bBox.y + (config.panelGroup.header.height / 2);
        serviceTitle.x = viewState.bBox.x + config.panelGroup.title.margin.left;
        serviceIcon.x = viewState.bBox.x;

        resources.push(DiagramUtils.getComponents(objectType.functions));
        return (
            <g className="object">
                <g className="panel-group-header" >
                    <text {...serviceTitle}>{model.name.value}</text>
                    <text {...serviceIcon}>{getCodePoint("object")}</text>
                </g>
                {resources}
            </g>);
    };
