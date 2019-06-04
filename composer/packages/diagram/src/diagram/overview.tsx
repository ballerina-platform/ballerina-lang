import React from "react";
import { Diagram } from "./diagram";

export class Overview extends React.Component {
    public render() {
        const { modules } = this.props;
        return (
            <div className="ui text container" style={{padding: "1em 0em"}}>
            <div className="ui relaxed divided list">
                {Object.keys(modules).map((moduleName) => {
                    const module = modules[moduleName];
                    return (
                        <div className="item" key={module}>
                            <div className="content">
                                <a className="header">{module.name}</a>
                                {Object.keys(module.compilationUnits).map((cUnitName) => {
                                    const cUnit = module.compilationUnits[cUnitName];
                                    const ast = cUnit.ast;
                                    return <Diagram ast={ast} zoom={1} height={100} width={800}></Diagram>;
                                })}
                            </div>
                        </div>
                    );
                })}
            </div>
            </div>
        );
    }
}