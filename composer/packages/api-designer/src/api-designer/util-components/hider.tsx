/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

export interface HideComponentProps {
    hideOn?: number;
    callback: () => void;
}

export interface HideComponentState {
    visible: boolean;
    defaultTime: number;
    timer: any;
}

/**
 * A util component which will hide all child components when the povided time is over.
 */
class HideComponent extends React.Component<HideComponentProps, HideComponentState> {

    constructor(props: HideComponentProps) {
        super(props);

        this.state = {
            defaultTime: 1000,
            timer: null,
            visible: false
        };
    }

    public componentWillReceiveProps(nextProps: any) {
        if (nextProps.children !== this.props.children) {
            this.setHideTimer();
            this.setState({
                visible: true
            });
        }
    }

    public componentDidMount() {
        this.setHideTimer();
    }

    public setHideTimer() {
        if (this.state.timer != null) {
            clearTimeout(this.state.timer);
        }

        const timeObj = setTimeout(() => {
            this.setState({
                timer: null,
                visible: false
            });
            this.props.callback();
        }, this.props.hideOn);

        this.setState({
            timer: timeObj
        });
    }

    public render() {
        const { visible } = this.state;
        return (
            <React.Fragment>
                {visible &&
                    <div className="hide-component">{this.props.children}</div>
                }
            </React.Fragment>
        );
    }
}

export default HideComponent;
