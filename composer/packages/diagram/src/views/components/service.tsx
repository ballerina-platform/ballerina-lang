
import { Service as ServiceNode } from "@ballerina/ast-model";
import { getCodePoint } from "@ballerina/font";
import * as React from "react";
import { DiagramConfig } from "../../config/default";
import { DiagramUtils } from "../../diagram/diagram-utils";
import { ViewState } from "../../view-model/index";
import { SourceLinkedLabel } from "./source-linked-label";

const config: DiagramConfig = DiagramUtils.getConfig();

export const Service: React.StatelessComponent<{
    model: ServiceNode
}> = ({
    model
}) => {
        const resources: React.ReactNode[] = [];
        const serviceTitle = { x: 0, y: 0, className: "panel-group-title" };
        const serviceIcon = { x: 0, y: 0, className: "panel-group-title panel-group-title-icon" };

        const viewState: ViewState = model.viewState;

        serviceTitle.y = serviceIcon.y = viewState.bBox.y + (config.panelGroup.header.height / 2);
        serviceTitle.x = viewState.bBox.x + config.panelGroup.title.margin.left;
        serviceIcon.x = viewState.bBox.x;

        resources.push(DiagramUtils.getComponents(model.resources));
        return (
            <g className="service">
                <g className="panel-group-header" >
                    <SourceLinkedLabel {...serviceTitle} text={model.name.value} target={model} />
                    <text {...serviceIcon}>{getCodePoint("service")}</text>
                </g>
                {resources}
            </g>);
    };
