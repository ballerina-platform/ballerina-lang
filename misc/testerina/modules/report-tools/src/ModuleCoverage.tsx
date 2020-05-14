import React, { Component } from 'react';
import { ModuleCoverage} from './TestData';

class ModuleCoverageSummary extends Component<{ module: ModuleCoverage, modIndex: number, updateState: Function}> {
    render () {
        let data = this.props.module
        return <div className="col-sm-7 card">
            <table className="table table-striped table-borderless">
                <thead>
                <tr>
                    <th>Source File</th>
                    <th>Lines Covered</th>
                    <th>Lines Missed</th>
                    <th>Coverage Percentage</th>
                </tr>
                </thead>
                <tbody>
                {data.sourceFiles.map((sourceFile, index) => {
                    return <tr id={sourceFile.name}>
                        <td><span className="link" onClick={() => this.props.updateState("coverage", this.props.modIndex, index)}>{sourceFile.name}</span></td>
                        <td>{sourceFile.coveredLines.length}</td>
                        <td>{sourceFile.missedLines.length}</td>
                        <td>
                        <div className="progress">
                            <div className="progress-bar" style={{width: sourceFile.coveragePercentage+"%"}}>
                            {sourceFile.coveragePercentage}%</div>
                        </div>
                        </td>
                    </tr>
                })}
                </tbody>
            </table>
        </div>
        }
}

export default ModuleCoverageSummary
