import React from "react";
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
}
