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

/**
 * Document for the given package or package related functions.
 *
 * @class Document
 * */
class Document extends React.Component {
    /**
     * Render the document.
     *
     * @return {object} documentation for the given package.
     * */
    render() {
        let source = "/docs/api/" + this.props.packageName + ".html" +
            (this.props.functionName ? '#' + this.props.functionName : '');

        return (<div className='initial-doc-background-container'>
            <iframe className='docerina-page'
                    src={source}
                    id={"doc"}
            />
        </div>);
    }
}

export default Document;