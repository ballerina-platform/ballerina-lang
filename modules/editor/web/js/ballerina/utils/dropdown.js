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
define(['lodash', 'jquery'], function (_, $) {

    /**
     * Creates a bootstrap dropdown with events and stubs.
     *
     * @param {Object} args - Arguments for creating the view.
     * @param {Object} [args.class={}] - CSS classes.
     * @param {string} [args.class.mainWrapper=""] - The CSS class for the main wrapper of the dropdown.
     * @param {string} [args.class.button=""] - The CSS class for the select element(button).
     * @param {Object[]} [args.items=[]] - Items of the dropdown.
     * @param {string} [args.items=[].key] - The key of a dropdown element.
     * @param {string} [args.items=[].value] - The value of a dropdown element.
     * @param {string} [args.defaultValue=""] - The default selected value.
     * @param {string} [args.emptyValue=""] - Value to be shown when empty.
     * @param {function} [args.onSelectCallBackFunction] - A call back function when an item is selected.
     * @param {function} [args.onDropdownOpen] - A call back function when the dropdown is opened.
     * @param {function} [args.onDropdownClosed] - A call back function when the dropdown is closed.
     * @constructor
     */
    var Dropdown = function (args) {
        this.args = args;
        this.onSelectCallBackFunction = _.get(args, "onSelectCallBackFunction");
        this.onDropdownOpen = _.get(args, "onDropdownOpen");
        this.onDropdownClosed = _.get(args, "onDropdownClosed");
        this.dropdownMainWrapper = $("<div/>").addClass("input-group-btn").addClass(_.get(args, "class.mainWrapper", ""));
        this.dropdownButton = $("<button/>").addClass("btn").addClass("btn-default")
            .addClass(_.get(args, "class.button", "")).appendTo(this.dropdownMainWrapper);
        this.dropdownButtonText = $("<span/>").appendTo(this.dropdownButton);

        $("<i/>").addClass("icon-caret").addClass("fw").addClass("fw-down").addClass("icon-caret")
            .addClass("pull-right").appendTo(this.dropdownButton);
        this.dropdownItemWrapper = $("<ul class='dropdown-menu' role='menu'/>").appendTo(this.dropdownMainWrapper);

        var self = this;

        // Creating dropdown elements.
        _.forEach(_.get(args, "items"), function (item) {
            var dropdownItem = $("<li><a href='#' data-key='" + item.key + "'>" + item.value.trim() + "</a><li/>");
            dropdownItem.appendTo(self.dropdownItemWrapper);
            dropdownItem.click(function () {
                $(self.dropdownMainWrapper).removeClass("open");
                if (!_.isNil(self.onDropdownClosed)) {
                    self.onDropdownClosed();
                }

                if (!_.isNil(self.onSelectCallBackFunction)) {
                    self.onSelectCallBackFunction(item.key, item.value.trim());
                }

                self.setSelectedValue(item.value.trim());
            });

            // Custom type ahead
            $(dropdownItem).find("a").keydown(function (e) {
                var enteredKey = e.which || e.charCode || e.keyCode;
                // Disabling enter key
                if (enteredKey == 13) {
                    $(self.dropdownButton).focus();
                }
            });

            $(self.dropdownButton).removeClass("disabled");
        });

        // Adding the "open" class to the main wrapper when the dropdown button is clicked. If its already open, then
        // remove the "open" class.
        $(self.dropdownButton).click(function (e) {
            self.openDropdown();
            e.stopPropagation();
        });

        // Adding onlick listener for dropdown items.
        $(self.dropdownItemWrapper).find("a").each(function () {
            $(this).click(function () {
                // Setting the selected value.
                self.setSelectedValue($(this).text().trim());
                // Closing the dropdown.
                $(self.dropdownMainWrapper).removeClass("open");
                if (!_.isNil(self.onDropdownClosed)) {
                    self.onDropdownClosed();
                }
            });
        });

        if (this.getAllItems().length > 0) {
            // Setting the default selected value.
            self.setSelectedValue(_.get(self.args, "defaultValue", ""));
        } else {
            // Setting the empty string value if no items are available in the dropdown.
            self.setSelectedValue(_.get(self.args, "emptyValue", ""));
        }

        // Closing the pop-up if when clicked outside of the dropdown.
        $(window).click(function () {
            if ($(self.dropdownMainWrapper).hasClass("open")) {
                $(self.dropdownMainWrapper).removeClass("open");
                if (!_.isNil(self.onDropdownClosed)) {
                    self.onDropdownClosed();
                }
            }
        });

        // Open dropdown when enter is clicked.
        $(this.dropdownButton).keypress(function (e) {
            var enteredKey = e.which || e.charCode || e.keyCode;
            // Disabling enter key
            if (enteredKey == 13) {
                $(this).click();
                e.stopPropagation();
            }
        });

        // Custom type ahead.
        $(self.dropdownItemWrapper).keypress(function (e) {
            if ($(self.dropdownMainWrapper).hasClass("open")) {
                // get the key that was pressed
                var key = String.fromCharCode(e.which);
                self.dropdownItemWrapper.find("li").each(function (idx, item) {
                    if ($(item).text().charAt(0) == key) {
                        $(item).find("a").trigger('focus');
                        return false;
                    }
                });
            }
        });
    };

    Dropdown.prototype.constructor = Dropdown;

    /**
     * Gets the main element which the dropdown is created upon.
     * @return {HTMLElement} - The html wrapper.
     */
    Dropdown.prototype.getElement = function () {
        return this.dropdownMainWrapper;
    };

    /**
     * Gets the selected value of the dropdown.
     * @return {string|undefined} - The selected value. Undefined if nothing is selected.
     */
    Dropdown.prototype.getSelectedValue = function () {
        var selectedItem = this.dropdownButton.text();
        if (selectedItem === _.get(this.args, "emptyValue", "")) {
            return undefined;
        } else {
            return this.dropdownButton.text();
        }
    };

    /**
     * Removes an item from the dropdown.
     * @param {string} itemName - The value of the item.
     */
    Dropdown.prototype.removeItem = function (itemName) {
        var self = this;
        $(self.dropdownItemWrapper).find("li a:contains('" + itemName + "')").remove();

        if ($(self.dropdownItemWrapper).find("li").length == 0) {
            self.setSelectedValue(_.get(self.args, "emptyValue", ""));
        }
    };

    /**
     * Adds an item to the dropdown.
     * @param {Object} item - The item to be added.
     * @param {string} item.key - The key of the item.
     * @param {string} item.value - The value of the item.
     */
    Dropdown.prototype.addItem = function (item) {
        var self = this;
        var dropdownItem = $("<li><a href='#' data-key='" + item.key + "'>" + item.value.trim() + "</a></li>")
            .appendTo(self.dropdownItemWrapper);
        dropdownItem.click(function () {
            $(self.dropdownMainWrapper).removeClass("open");
            if (!_.isNil(self.onDropdownClosed)) {
                self.onDropdownClosed();
            }

            if (!_.isNil(self.onSelectCallBackFunction)) {
                self.onSelectCallBackFunction(item.key, item.value.trim());
            }

            self.setSelectedValue(item.value.trim());

            $(this.dropdownButton).removeClass("disabled");
        });
    };

    /**
     * Adds multiple items to the dropdown.
     * @param {Object[]} items - Array of items.
     * @param {string} items[].key - The key of the item.
     * @param {string} items[].value - The value of the item.
     */
    Dropdown.prototype.addItems = function (items) {
        var self = this;
        _.forEach(items, function (item) {
            var dropdownItem = $("<li><a href='#' data-key='" + item.key + "'>" + item.value.trim() + "</a></li>")
                .appendTo(self.dropdownItemWrapper);
            dropdownItem.click(function () {
                $(self.dropdownMainWrapper).removeClass("open");
                if (!_.isNil(self.onDropdownClosed)) {
                    self.onDropdownClosed();
                }

                if (!_.isNil(self.onSelectCallBackFunction)) {
                    self.onSelectCallBackFunction(item.key, item.value.trim());
                }

                self.setSelectedValue(item.value.trim());

                $(this.dropdownButton).removeClass("disabled");
            });
        });
    };

    /**
     * Gets all the items of the dropdown as a string array.
     * @return {Object[]} - Array of items.
     */
    Dropdown.prototype.getAllItems = function () {
        var self = this;
        var items = [];
        $(self.dropdownItemWrapper).find("li a").each(function () {
            items.push({key: $(this).data("key"), value: $(this).text()});
        });

        return items;
    };

    /**
     * Removes all the items of the dropdown.
     */
    Dropdown.prototype.removeAllItems = function () {
        var self = this;
        $(self.dropdownItemWrapper).find("li").remove();
        self.setSelectedValue(_.get(self.args, "emptyValue", ""));

        $(this.dropdownButton).addClass("disabled");
    };

    /**
     * Sets the selected value of the dropdown.
     * @param {string} value - The value for the selected item.
     */
    Dropdown.prototype.setSelectedValue = function (value) {
        this.dropdownButtonText.text(value);
    };

    /**
     * Set focused item.
     * @param {string} itemName - The name of the item.
     */
    Dropdown.prototype.focusOnItem = function (itemName) {
        // Removing selected items.
        this.dropdownItemWrapper.find("li").removeClass("active");

        // Setting selected item.
        if (_.isUndefined(itemName)) {
            this.dropdownItemWrapper.find("li:eq(1) a").trigger('focus');
        } else {
            this.dropdownItemWrapper.find("li").each(function () {
                if (_.isEqual($(this).text(), itemName)) {
                    $(this).addClass("active");
                    $(this).find("a").trigger('focus');
                }
            });
        }
    };

    /**
     * Opens the dropdown.
     */
    Dropdown.prototype.openDropdown = function() {
        var self = this;
        if ($(self.dropdownMainWrapper).hasClass("open")) {
            $(self.dropdownMainWrapper).removeClass("open");
            if (!_.isNil(self.onDropdownClosed)) {
                self.onDropdownClosed();
            }
        } else {
            var selectVal = self.getSelectedValue();
            if (!_.isNil(self.onDropdownOpen)) {
                self.onDropdownOpen();
            }
            if ($(self.dropdownMainWrapper).find("li").length != 0) {
                $(self.dropdownButton).removeClass("disabled");
                $(self.dropdownMainWrapper).addClass("open");
                self.setSelectedValue(selectVal);
                self.focusOnItem(selectVal);
            }
        }
    };

    return Dropdown;
});