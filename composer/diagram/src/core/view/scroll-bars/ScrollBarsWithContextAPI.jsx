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
 *
 */

import React from 'react';
import PropTypes from 'prop-types';
import { Scrollbars } from 'react-custom-scrollbars';

/**
 * Wraps custom scroll bars and provide scrolling API through context.
 *
 */
export default class ScrollBarsWithContextAPI extends React.Component {
    /**
     * @inheritdoc
     */
    constructor(...args) {
        super(...args);
        this.scrollBarRef = undefined;
    }

    /**
     * @inheritdoc
     */
    getChildContext() {
        return {
            scroller: {
                scrollTop: (top) => {
                    if (this.scrollBarRef) {
                        this.scrollBarRef.scrollTop(top);
                    }
                },
                scrollLeft: (left) => {
                    if (this.scrollBarRef) {
                        this.scrollBarRef.scrollLeft(left);
                    }
                },
                scrollToElement: (element) => {
                    if (this.scrollBarRef) {
                        if (!(element instanceof HTMLElement)) {
                            throw Error('Argument should be a dom instance');
                        }
                        const { offsetTop, offsetLeft } = element;
                        this.scrollBarRef.scrollTop(offsetTop);
                        this.scrollBarRef.scrollLeft(offsetLeft);
                    }
                },
                getScrollWidth: () => {
                    return this.scrollBarRef ? this.scrollBarRef.getScrollWidth() : undefined;
                },
                getScrollHeight: () => {
                    return this.scrollBarRef ? this.scrollBarRef.getScrollHeight() : undefined;
                },
                isTopVisible: (top) => {
                    if (this.scrollBarRef) {
                        const { clientHeight, scrollTop } = this.scrollBarRef.getValues();
                        return (clientHeight + scrollTop) > top;
                    }
                    return false;
                },
                isLeftVisible: (left) => {
                    if (this.scrollBarRef) {
                        const { clientWidth, scrollLeft } = this.scrollBarRef.getValues();
                        return (clientWidth + scrollLeft) > left;
                    }
                    return false;
                },
                isElementVisible: (element) => {
                    if (this.scrollBarRef) {
                        if (!(element instanceof HTMLElement)) {
                            throw Error('Argument should be a dom instance');
                        }
                        const { clientHeight, scrollTop, clientWidth, scrollLeft } = this.scrollBarRef.getValues();
                        const { offsetTop, offsetHeight, offsetLeft, offsetWidth } = element;
                        return (clientWidth + scrollLeft) >= (offsetLeft + offsetWidth)
                                    && (clientHeight + scrollTop) >= (offsetTop + offsetHeight)
                                    && (clientHeight + scrollTop) >= (offsetTop + offsetHeight)
                                    && (scrollTop - offsetTop < 0);
                    }
                    return false;
                },
                getValues: () => {
                    return this.scrollBarRef ? this.scrollBarRef.getValues() : undefined;
                },
            },
        };
    }

    /**
     * @inheritdoc
     */
    render() {
        return (
            <Scrollbars
                {...this.props}
                ref={(ref) => {
                    this.scrollBarRef = ref;
                }}
            >
                {this.props.children}
            </Scrollbars>
        );
    }
}

ScrollBarsWithContextAPI.childContextTypes = {
    scroller: PropTypes.shape({
        scrollTop: PropTypes.func.isRequired,
        scrollLeft: PropTypes.func.isRequired,
        scrollToElement: PropTypes.func.isRequired,
        getScrollWidth: PropTypes.func.isRequired,
        getScrollHeight: PropTypes.func.isRequired,
        isTopVisible: PropTypes.func.isRequired,
        isLeftVisible: PropTypes.func.isRequired,
        isElementVisible: PropTypes.func.isRequired,
        getValues: PropTypes.func.isRequired,
    }).isRequired,
};

