
import { ASTUtil, Service as ServiceNode } from "@ballerina/ast-model";
import * as React from "react";
import { DiagramConfig } from "../../config/default";
import { DiagramUtils } from "../../diagram/diagram-utils";
import { DiagramContext } from "../../diagram/index";
import { getCodePoint } from "../../utils";
import { SimpleBBox, ViewState } from "../../view-model/index";
import { EditableSVGText } from "./editable-svg-text";
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

        serviceTitle.y = serviceIcon.y = viewState.bBox.y + (config.panelHeading.height / 2);
        serviceTitle.x = viewState.bBox.x + config.panelGroup.title.margin.left;
        serviceIcon.x = viewState.bBox.x;

        const editableTitle = new SimpleBBox();
        editableTitle.x = serviceTitle.x;
        editableTitle.y = viewState.bBox.y;
        editableTitle.w = DiagramUtils.getTextWidth(model.name.value).w;
        editableTitle.h = config.panelHeading.height;

        resources.push(DiagramUtils.getComponents(model.resources));
        return (
            <g className="service">
                <g className="panel-group-header" >
                    <DiagramContext.Consumer>
                        {({ editingEnabled, hasSyntaxErrors }) => {
                            return (
                                <React.Fragment>
                                    {(editingEnabled && !hasSyntaxErrors) &&
                                        <EditableSVGText
                                            bBox={editableTitle}
                                            value={model.name.value}
                                            className="panel-title"
                                            onChange={(newValue) => {
                                                if (model) {
                                                    ASTUtil.renameNode(model, newValue);
                                                }
                                            }}
                                        />
                                    }
                                    {(!editingEnabled || hasSyntaxErrors) &&
                                        <SourceLinkedLabel
                                            {...serviceTitle}
                                            text={model.name.value}
                                            target={model}
                                            className="panel-title"
                                        />
                                    }
                                </React.Fragment>
                            );
                        }}
                    </DiagramContext.Consumer>
                    <text {...serviceIcon}>{getCodePoint("service")}</text>
                </g>
                {resources}
            </g>);
    };
