import React, { Component } from 'react';
import { SourceFile} from './TestData';


import {Prism as SyntaxHighlighter} from 'react-syntax-highlighter';
import { vs } from 'react-syntax-highlighter/dist/cjs/styles/prism';

class FileCoverage extends Component<{ moduleName: string, modIndex: number, file: SourceFile, updateState: Function}> {
    render () {
        let moduleName = this.props.moduleName;
        let modIndex = this.props.modIndex
        let sourceFile = this.props.file
        const COVERED = sourceFile.coveredLines;
        const MISSED = sourceFile.missedLines;
        let code = sourceFile.sourceCode
        let codeSnippet
        return <div className="module-content">
            <div className="title row">
            <span className="back-arrow report" onClick={() => this.props.updateState("module", modIndex)}>&#60;</span>
            <h5 id={moduleName}>{moduleName}/{sourceFile.name}</h5>
            </div>
            <div className="col card coverage-content">
                <table className="table table-borderless">
                    <thead>
                        <th>{sourceFile.name}</th>
                    </thead>
                    <tbody>
                        <td>
                        <SyntaxHighlighter
                        style={vs}
                        customStyle={{border: 0, margin:0, padding:0, overflow:'hidden'}}
                        wrapLines={true}
                        showLineNumbers={true}
                        lineProps={lineNumber => {
                        let style : any = { display: 'block' };
                        if (COVERED.includes(lineNumber)) {
                            style.backgroundColor = '#dbffdb';
                        } else if (MISSED.includes(lineNumber)) {
                            style.backgroundColor = '#ffecec';
                        }
                            return { style };
                        }}
                        lineNumberContainerProps={{"class" : "lineNumbers"}}
                    >
                    {code}
                    </SyntaxHighlighter>
                    {codeSnippet}
                        </td>
                    </tbody>
                </table>
            </div>
        </div>
        }
}

export default FileCoverage
