import React, { Component } from 'react';
import { ModuleStatus, TestData} from './TestData';

import {ReactComponent as TotalIcon} from './images/test.svg';
import {ReactComponent as PassedIcon} from "./images/success.svg";
import {ReactComponent as FailedIcon} from "./images/failed.svg";
import {ReactComponent as SkippedIcon} from "./images/skipped.svg";

type ModuleSummaryViewProps = {
    data: TestData,
    index: number
}

export function ModuleSummaryView ({data, index} : ModuleSummaryViewProps) {
    let isCoverageAvailable = data.moduleCoverage.length > 0
    let coverage = null
    if (isCoverageAvailable) {
        coverage = <div className="col-sm-3 card summary-card progress-card">
            <h6>Code Coverage: {data.moduleCoverage[index].coveragePercentage}%</h6>
            <div className="progress">
                <div className="progress-bar" style={{width: data.moduleCoverage[index].coveragePercentage+"%"}}>{data.moduleCoverage[index].coveragePercentage}%</div>
            </div>
        </div>
      }
    return <div className="row">
        <div className="col card summary-card total">
            <div className="row">
                <div className="col-sm-4"><TotalIcon className="icon total" /></div>
                <div className="col-sm-8"><h3>{data.moduleStatus[index].totalTests}</h3><small>Total tests</small></div>
            </div>
        </div>
        <div className="col card summary-card passed white-font">
        <div className="row">
                <div className="col-sm-5"><PassedIcon className="icon" /><br/><small>Passed</small></div>
                <div className="col-sm-7"><h1>{data.moduleStatus[index].passed}/<small>{data.moduleStatus[index].totalTests}</small></h1></div>
            </div>
        </div>
        <div className="col card summary-card failed white-font">
        <div className="row">
                <div className="col-sm-5"><FailedIcon className="icon" /><br/><small>Failed</small></div>
                <div className="col-sm-7"><h1>{data.moduleStatus[index].failed}/<small>{data.moduleStatus[index].totalTests}</small></h1></div>
            </div>
        </div>
        <div className="col card summary-card skipped white-font">
        <div className="row">
                <div className="col-sm-5"><SkippedIcon className="icon" /><br/><small>Skipped</small></div>
                <div className="col-sm-7"><h1>{data.moduleStatus[index].skipped}/<small>{data.moduleStatus[index].totalTests}</small></h1></div>
            </div>
        </div>
        {coverage}
    </div>;
     
}

class ModuleStatusSummary extends Component<{ module: ModuleStatus}> {
    render () {
        let data = this.props.module
        return <div className="col card">
            <table className="table table-striped table-borderless">
                <thead>
                <tr>
                        <th>Test Name</th>
                        <th>Status</th>
                    </tr>
                </thead>
                <tbody>
                    {data.tests.map(function(test, index) {
                    let failure_reason = null
                    if (test.status === "FAILURE") {
                        failure_reason =<tr className={test.status}><td className="small" colSpan={2}>{test.failureMessage}</td></tr>
                        
                    }
                    return [<tr className={test.status}  id={test.name}>
                        <td>{test.name}</td>
                        <td>{test.status}</td>
                    </tr>,failure_reason]
                })}
                </tbody>
            </table>
        </div>;
        }
}

export default ModuleStatusSummary
