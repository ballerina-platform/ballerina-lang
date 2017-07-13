const config = {
    container: '#page-content',
    welcome: {
        container: '#welcome-container',
        cssClass: {
            parent: 'initial-background-container',
            outer: 'initial-welcome-container',
            heading: 'heading-welcome-container',
            headingTitle: 'welcome-title',
            body: 'body-welcome-container',
            bodyTitle: 'welcome-body-title',
            headingIcon: 'welcome-icon',
            buttonNew: ' btn btn-block new-welcome-button',
            buttonOpen: ' btn btn-block open-welcome-button',
            samples: 'welcome-samples',
            headingTop: 'top-heading',
            btnWrap1: 'btn-wrap',
        },
        samples: [
            {
                name: 'Echo Service',
                isFile: true,
                path: '/samples/echoService/echoService.bal',
                image: 'preview_echoService',
            },
            {
                name: 'Hello World',
                isFile: true,
                path: '/samples/helloWorld/helloWorld.bal',
                image: 'preview_helloWorld',
            },
            {
                name: 'Passthrough Service',
                isFile: false,
                folder: '/samples/passthroughService/passthroughservice',
                path: '/samples/passthroughService/passthroughservice/samples/nyseStockQuoteService.bal',
                image: 'preview_passthroughService',
            },
            {
                name: 'Service Chaining',
                isFile: false,
                folder: '/samples/serviceChaining/servicechaining',
                path: '/samples/serviceChaining/servicechaining/samples/ATMLocatorService.bal',
                image: 'preview_servicechaining',
            },
            {
                name: 'JMS',
                isFile: false,
                folder: '/samples/jms/',
                path: '/samples/jms/jmsReceiver.bal',
                image: 'preview_jms',
            },
            {
                name: 'Restful Service',
                isFile: false,
                folder: '/samples/restfulService/restfulservice',
                path: '/samples/restfulService/restfulservice/samples/productsService.bal',
                image: 'preview_restfulService',
            },
            {
                name: 'Routing Services',
                isFile: false,
                folder: '/samples/routingServices',
                path: '/samples/routingServices/samples/contentBasedRoutingService.bal',
                image: 'preview_routingServices',
            },
            {
                name: 'Websocket',
                isFile: false,
                folder: '/samples/websocket',
                path: '/samples/websocket/echoServer/server/websocketEchoServer.bal',
                image: 'preview_websocket',
            }],
    },
    docs: {
        container: ".docs",
    },
    // you can overide service urls by uncommenting the following.
    // if the following are not set they will be taken automatically from the composer file server.
    /* services: {
        workspace:  {
            endpoint: "http://localhost:8289/service/workspace"
        },
        packages:  {
            endpoint: "http://localhost:8289/ballerina/editor/packages"
        },
        swagger:  {
            endpoint: "http://localhost:8289/service/swagger/"
        },
        parser:  {
            endpoint: "http://localhost:8289/ballerina/model/content"
        },
        validator:  {
            endpoint: "http://localhost:8289/ballerina/validate"
        },
        launcher: {
            endpoint: "ws://localhost:8290/launch"
        }
    },
    balHome: '',*/
    alerts: {
        container: '#alerts-container',
        cssClass: {
        },
    },
    tool_bar: {
        container: '',
    },
    menu_bar: {
        container: '#menu-bar-container',
        menu_group: {
            menu_item: {
                cssClass: {
                    label: 'menu-label pull-left',
                    shortcut: 'shortcut-label pull-right',
                    active: 'menu-item-enabled',
                    inactive: 'menu-item-disabled',
                },
            },
            cssClass: {
                group: 'menu-group file-menu-group',
                menu: 'dropdown-menu file-dropdown-menu',
                toggle: 'dropdown-toggle',
            },
        },
        cssClass: {
            menu_bar: 'dropdown-menu file-dropdown-menu',
        },
        help_urls: {
            user_guide_url: 'http://ballerinalang.org/docs/user-guide/0.8/',
            report_issue_url: 'https://github.com/ballerinalang/composer/issues/',
        },
    },
    breadcrumbs: {
        container: '#breadcrumb-container',
        cssClass: {
            active: 'active',
            list: 'breadcrumb pull-left',
            item: 'breadcrumb-item',
        },
    },
    tab_controller: {
        container: '#tabs-container',
        headers: {
            // relative selector within container for tab controller
            container: '.tab-headers',
            cssClass: {
                list: 'nav nav-tabs nav-tabs-bar',
                item: '',
                active: 'active',
            },
        },
        tabs: {
            // relative selector within container for tab controller
            container: '.tab-content',
            tab: {
                template: '#tab-template',
                cssClass: {
                    tab: 'tab-pane',
                    tab_active: 'active',
                    tab_close_btn: 'close closeTab pull-right',
                },
                ballerina_editor: {
                    design_view: {
                        // relative selector within container for a tab
                        container: '.design-view-container',
                        canvas_container: '.canvas-container',
                        new_drop_timeout: 3000,
                        tool_palette: {
                            // relative selector within container for design view
                            container: '.tool-palette-container',
                            search_bar: {
                                cssClass: {
                                    search_box: 'search-bar',
                                    search_icon: 'fw fw-search searchIcon',
                                    search_input: 'search-input',
                                },
                            },
                            toolGroup: {
                                tool: {
                                    containment_element: '#tabs-container',
                                    cssClass: {
                                        dragContainer: 'tool-drag-container',
                                        disabledIconContainer: 'disabled-icon-container',
                                        disabledIcon: 'fw fw-lg fw-block tool-disabled-icon',
                                    },
                                },
                            },
                        },
                    },
                    preview: {
                        // relative selector within container for a tab
                        container: '.preview-container',
                    },
                    source_view: {
                        // relative selector within container for a tab
                        container: '.source-view-container',
                        theme: 'ace/theme/twilight',
                        font_size: '14px',
                        scroll_margin: '20',
                        mode: 'ace/mode/ballerina',
                    },
                    swagger_view: {
                        // relative selector within container for a tab
                        container: '.swagger-view-container',
                        theme: 'ace/theme/tomorrow_night',
                        font_size: '12pt',
                        scroll_margin: '20',
                        mode: 'ace/mode/ballerina',
                    },
                    controls: {
                        view_source_btn: '.view-source-btn',
                        view_design_btn: '.view-design-btn',
                    },
                    cssClass: {
                        text_editor_class: 'text-editor',
                        outer_box: 'outer-box',
                        svg_container: 'svg-container',
                        outer_div: 'panel panel-default container-outer-div',
                        panel_title: 'panel-title',
                        panel_icon: 'panel-icon',
                        service_icon: 'fw fw-service',
                        annotation_icon: 'fw fw-annotation',
                        struct_icon: 'fw fw-struct',
                        connector_icon: 'fw fw-connector',
                        function_icon: 'fw fw-function',
                        main_function_icon: 'fw fw-main-function',
                        title_link: 'collapsed canvas-title',
                        panel_right_icon: 'fw fw-up pull-right right-icon-clickable collapser hoverable',
                        head_div: 'canvas-heading',
                        body_div: 'panel-collapse collapse',
                        canvas: 'panel-body collapse in',
                        design_view_drop: 'design-view-hover',
                        canvas_container: 'canvas-container',
                        canvas_top_controls_container: 'canvas-top-controls-container',
                        canvas_top_control_package_define: 'package-definition-wrapper',
                        canvas_top_control_packages_import: 'package-imports-wrapper',
                        canvas_top_control_constants_define: 'constants-definition-wrapper',
                        panel_delete_icon: 'fw fw-delete pull-right right-icon-clickable delete-icon hoverable',
                        panel_annotation_icon: 'fw fw-annotation pull-right right-icon-clickable hoverable',
                        panel_args_icon: 'fw fw-import pull-right right-icon-clickable hoverable',
                        type_mapper_icon: 'fw fw-type-converter',
                        type_struct_icon: 'fw fw-dgm-service fw-inverse',
                        canvas_heading_new: 'canvas-heading-new',
                    },
                    notifications: {
                        container: '#notification-container',
                    },
                    backend: {
                        url: 'http://localhost:8289/ballerina/model/content',
                    },
                    dialog_boxes: {
                        parser_error: '#parserErrorModel',
                    },
                },
            },
        },

    },
    workspace_explorer: {
        container: '.sidebar-left',
        activateBtn: '.workspace-explorer-activate-btn',
        separator: '.sidebar-left-separator',
        containerToAdjust: '.right-container',
        command: {
            id: 'toggle-file-explorer',
            shortcuts: {
                mac: {
                    key: 'command+shift+e',
                    label: '\u2318\u21E7E',
                },
                other: {
                    key: 'ctrl+shift+e',
                    label: 'Ctrl+Shift+E',
                },
            },
        },
        leftOffset: 40,
        separatorOffset: 5,
        defaultWidth: 290,
        resizeLimits: {
            minX: 200,
            maxX: 800,
        },
        containerId: 'workspace-explorer',
        cssClass: {
            container: 'workspace-explorer-container tab-pane',
            openFolderButton: 'btn  btn-default open-folder-button',
        },
    },
    notifications: {
        container: '#notification-container',
    },
    dialog: {
        container: 'body',
    },
    about_dialog: {
        selector: '#modalAbout',
    },
    settings_dialog: {
        selector: '#modalSettings',
        submit_button: '#saveSettingsButton',
    },
    open_folder_dialog: {
        modal_selector: '#open-folder-modal',
        tree_container: '.file-tree',
        errors_container: '.errors-container',
        location_input: '.location-input',
        submit_button: '.open-button',
    },
    debugger: {
        container: '.sidebar-left',
        activateBtn: '.debugger-activate-btn',
        separator: '.sidebar-left-separator',
        containerToAdjust: '.right-container',
        leftOffset: 40,
        separatorOffset: 5,
        defaultWidth: 290,
        resizeLimits: {
            minX: 200,
            maxX: 800,
        },
        containerId: 'debugger',
        cssClass: {
            container: 'debugger-container tab-pane',
        },
        command: {
            id: 'toggle-debugger',
            shortcuts: {
                mac: {
                    key: 'command+shift+d',
                    label: '\u2318\u21E7d',
                },
                other: {
                    key: 'ctrl+shift+d',
                    label: 'Ctrl+Shift+d',
                },
            },
        },
        toolbarShortcuts: [
            {
                id: 'StepOver',
                shortcuts: {
                    mac: {
                        key: 'alt+o',
                        label: 'alt+o',
                    },
                    other: {
                        key: 'alt+o',
                        label: 'alt+o',
                    },
                },
            },
            {
                id: 'Resume',
                shortcuts: {
                    mac: {
                        key: 'alt+r',
                        label: 'alt+r',
                    },
                    other: {
                        key: 'alt+r',
                        label: 'alt+r',
                    },
                },
            },
            {
                id: 'StepIn',
                shortcuts: {
                    mac: {
                        key: 'alt+i',
                        label: 'alt+i',
                    },
                    other: {
                        key: 'alt+i',
                        label: 'alt+i',
                    },
                },
            },
            {
                id: 'StepOut',
                shortcuts: {
                    mac: {
                        key: 'alt+u',
                        label: 'alt+u',
                    },
                    other: {
                        key: 'alt+u',
                        label: 'alt+u',
                    },
                },
            },
            {
                id: 'Stop',
                shortcuts: {
                    mac: {
                        key: 'alt+p',
                        label: 'alt+p',
                    },
                    other: {
                        key: 'alt+p',
                        label: 'alt+p',
                    },
                },
            },
        ],
    },
    launcher: {
        container: '.sidebar-left',
        activateBtn: '.launcher-activate-btn',
        separator: '.sidebar-left-separator',
        containerToAdjust: '.right-container',
        leftOffset: 40,
        separatorOffset: 5,
        defaultWidth: 290,
        resizeLimits: {
            minX: 200,
            maxX: 800,
        },
        containerId: 'launcher',
        cssClass: {
            container: 'launcher-container tab-pane',
        },
        command: {
            id: 'toggle-launcher',
            shortcuts: {
                mac: {
                    key: 'command+shift+r',
                    label: '\u2318\u21E7r',
                },
                other: {
                    key: 'ctrl+shift+r',
                    label: 'Ctrl+Shift+r',
                },
            },
        },
    },
};

export default config;
