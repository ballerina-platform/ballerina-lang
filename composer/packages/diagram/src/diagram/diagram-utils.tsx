import React from "react";
import { DiagramConfig } from "../config/default";
import * as components from "../views/index";

export class DiagramUtils {

    public static getComponents(nodeArray: any) {
        const children: any = [];
        nodeArray.forEach((node: any) => {
            const ChildComp = (components as any)[node.kind];
            if (!ChildComp) { return; }

            children.push(<ChildComp model={node} />);
        });
        return children;
    }

    /**
     * Get diagram config
     */
    public static getConfig() {
        return DiagramConfig;
    }
}
