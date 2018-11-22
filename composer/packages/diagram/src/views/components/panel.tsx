
import * as React from "react";
import { FunctionViewState , SimpleBBox} from "../../view-model/index";

export const Panel: React.StatelessComponent<{ model: FunctionViewState }> = ({ model , children}) => {

    const body: SimpleBBox = model.body;

    return (
        <g className="panel">
            <g className="panel-header">
                <g className="panel-body">
                    <rect
                        x={body.x}
                        y={body.y}
                        width={body.w}
                        height={body.h}
                    />
                    {children}
                </g>
            </g>
        </g>);
};
