
import { Service as ServiceNode } from "@ballerina/ast-model";
import * as React from "react";
import { DiagramUtils } from "../../diagram/diagram-utils";

export const Service: React.StatelessComponent<{
        model: ServiceNode
    }> = ({
        model
    }) => {
        const resources: React.ReactNode[] = [];
        resources.push(DiagramUtils.getComponents(model.resources));
        return (
            <g className="service">
                {resources}
            </g>);
    };
