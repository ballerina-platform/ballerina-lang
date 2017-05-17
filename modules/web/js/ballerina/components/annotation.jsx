/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import React from 'react';
import ReactDOM from 'react-dom';
import PropTypes from 'prop-types';
import {getComponentForNodeArray} from './utils';

class Annotation extends React.Component {
    constructor(props) {
        super(props);
        this.state = { selected: false };
    }

    render() {
        let model = this.props.model;
        let entries = getComponentForNodeArray(model.children);
        let children = [];

        let selectedClass = (this.state.selected)? "active-annotation":"";
        let removeIcon = (this.state.selected)? <div className="annotation-remove" onClick={this.deleteAnnotation.bind(this)}><i className="fw fw-delete"></i></div>:<span></span>;

        let key = <td className="annotation-key">{removeIcon}<span className="package-name">@{model.getPackageName()}</span>:<span className="identifire">{model.getIdentifier() + " { "}</span></td>;
        if(entries.length == 0){
            children.push(<tr key="1" >{key}<td><span className="annotation-close">{" }"}</span></td></tr>);
        }else if(entries.length == 1){
            children.push(<tr key="1" >{key}<td><span>{entries}</span><span className="annotation-close">{" } "}</span></td></tr>);
        }else{
            children.push(<tr key="0">{key}<td><span>{entries[0]}</span></td></tr>);
            for (let i = 1; i < entries.length; i++) { 
                let last = (i == (entries.length - 1))? " }" : "";
                children.push(<tr  key={i} ><td className="annotation-key"><span>{""}</span></td><td>{entries[i]}<span className="annotation-close">{last}</span></td></tr>);
            }
        }


        return <table className={ "annotation-table " + selectedClass} onMouseEnter={ this.onMouseOver.bind(this) }
                      onMouseLeave={this.onMouseOut.bind(this)} >
                <tbody>
                    {children}
                </tbody>
            </table>;
    }


    onMouseOver(){
        this.setState({ selected: true });
    }

    onMouseOut(){
        this.setState({ selected: false });
    }

    deleteAnnotation(){
        this.props.model.parent.removeChild(this.props.model);
    }
}

export default Annotation;