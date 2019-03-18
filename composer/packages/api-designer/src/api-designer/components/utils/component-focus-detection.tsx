/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

import * as React from "react";

export interface OutProp {
    onClickedOut: () => void;
}

/**
 * A Wrapper component which will use the ref to
 * handle a focus out from its child component.
 */
class ComponentFocusDetector extends React.Component<OutProp, any> {

    private componentRef: any;

    constructor(props: any) {
        super(props);

        this.setComponentRef = this.setComponentRef.bind(this);
        this.handleClickOutside = this.handleClickOutside.bind(this);
    }

    public componentDidMount() {
        document.addEventListener("mousedown", this.handleClickOutside);
    }

    public componentWillUnmount() {
        document.removeEventListener("mousedown", this.handleClickOutside);
    }

    public render() {
        return <div ref={this.setComponentRef}>{this.props.children}</div>;
    }

    private handleClickOutside(event: any) {
        if (this.componentRef && !this.componentRef.contains(event.target)) {
            this.props.onClickedOut();
        }
    }

    private setComponentRef(node: any) {
        this.componentRef = node;
    }

}

export default ComponentFocusDetector;
