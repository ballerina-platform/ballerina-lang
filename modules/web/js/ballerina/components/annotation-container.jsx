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

class AnnotationContainer extends React.Component {
    constructor(props) {
        super(props);
        this.state = {annotationSelect: false , text: ''};        
    }

    render() {
        let bBox = this.props.model.bBox;
        let style = {
            position: 'absolute',
            top: bBox.y,
            left: bBox.x,
            width: bBox.w,
            height: bBox.h            
        };


        let annotations = getComponentForNodeArray(this.props.model.annotations);
        return <div style={style} className="annotation-container">
                    {annotations}
                    { this.state.annotationSelect ? 
                        <input autoFocus type="text" defaultValue="another" onBlur={this.onBlurHandle.bind(this)}/> 
                        :
                        <div className="annotation-add"  
                            onClick={this.addAnnotation.bind(this)}> + Add Annotation</div>
                    }
               </div>;
    }

    addAnnotation(e){
        this.setState({annotationSelect: true, text: ""});
    }

    onBlurHandle(e){
        this.setState({annotationSelect: false, text: ""});
    }
}

export default AnnotationContainer;