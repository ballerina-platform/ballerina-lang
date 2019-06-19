
import { ASTUtil, Function as BalFunction } from "@ballerina/ast-model";
import * as React from "react";
import { DiagramConfig } from "../../config/default";
import { DiagramContext } from "../../diagram/diagram-context";
import { DiagramUtils } from "../../diagram/diagram-utils";
import { getCodePoint } from "../../utils";
import { FunctionViewState, SimpleBBox } from "../../view-model/index";
import { EditableSVGText } from "./editable-svg-text";
import { SourceLinkedLabel } from "./source-linked-label";

const config: DiagramConfig = DiagramUtils.getConfig();

export const Panel: React.StatelessComponent<{
        model: FunctionViewState,
        title: string,
        icon: string,
        astModel?: BalFunction,
    }> = ({
        model,
        title,
        children,
        icon,
        astModel
    }) => {
        const body: SimpleBBox = model.body;
        const header: SimpleBBox = model.header;

        const headerCenter = header.y + (model.header.h / 2);

        const titleTextBBox = new SimpleBBox();
        titleTextBBox.h = header.h;
        titleTextBBox.w = header.w;
        titleTextBBox.x = header.x + config.panelHeading.title.margin.left;
        titleTextBBox.y = header.y;

        return (
            <g className="panel">
                <g className="panel-header">
                    <rect
                        x={header.x}
                        y={header.y}
                        width={header.w}
                        height={header.h}
                    />
                    <text
                        x={header.x + config.panelHeading.padding.left}
                        y={headerCenter}
                        className="panel-icon"
                    >
                        {getCodePoint(icon)}
                    </text>
                    <DiagramContext.Consumer>
                        {({ editingEnabled }) => {
                            return <React.Fragment>
                                {(editingEnabled || !astModel) &&
                                        <EditableSVGText
                                            bBox={titleTextBBox}
                                            value={title}
                                            className="panel-title"
                                            onChange={(newValue) => {
                                                if (astModel) {
                                                    ASTUtil.renameNode(astModel, newValue);
                                                }
                                            }}
                                />}
                                {(!editingEnabled && astModel) && <SourceLinkedLabel
                                                x={titleTextBBox.x}
                                                y={titleTextBBox.y + titleTextBBox.h / 2}
                                                className="panel-title"
                                                target={astModel}
                                                text={title}
                                />}
                            </React.Fragment>;
                        }}
                    </DiagramContext.Consumer>
                </g>
                <g className="panel-body">
                    <rect
                        x={body.x}
                        y={body.y}
                        width={body.w}
                        height={body.h}
                    />
                    {children}
                </g>

            </g>);
    };
