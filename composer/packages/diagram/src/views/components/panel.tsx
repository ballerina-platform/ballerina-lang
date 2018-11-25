
import { getCodePoint } from "@ballerina/font";
import * as React from "react";
import { DiagramConfig } from "../../config/default";
import { DiagramUtils } from "../../diagram/diagram-utils";
import { FunctionViewState, SimpleBBox } from "../../view-model/index";
import { EditableSVGText } from "./editable-svg-text";

const config: DiagramConfig = DiagramUtils.getConfig();

export const Panel: React.StatelessComponent<{
        model: FunctionViewState,
        title: string,
        icon: string
    }> = ({
        model,
        title,
        children,
        icon
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
                    <EditableSVGText
                        bBox={titleTextBBox}
                        value={title}
                        className="panel-title"
                    />
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
