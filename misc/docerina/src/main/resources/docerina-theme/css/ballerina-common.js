function subscribeUser(email) {
    $('#subscribeUserMessage').remove("");
    if (email == "") {
        $('.cFormContainer').append('<span id="subscribeUserMessage">Please enter your email</span>');
    } else if (!isEmail(email)) {
        $('.cFormContainer').append('<span id="subscribeUserMessage">Please enter a valid email</span>');
    } else {
        $('.cFieldContainer').hide();
        $('.cButtonContainer').hide();
        $(".pdframe").html("<iframe src='https://go.pardot.com/l/142131/2018-03-26/4yl979?email=" + email + "'></iframe>");
        $('.cFormContainer').append('<span id="subscribeUserMessage" class="success">Thank you! Stay tuned for updates on Ballerina.</span>');
        $("#emailUser").val("");
    }
    return;
}

function isEmail(email) {
    var regex = /^([a-zA-Z0-9_.+-])+\@(([a-zA-Z0-9-])+\.)+([a-zA-Z0-9]{2,4})+$/;
    return regex.test(email);
}

function formatDate(date, format) {
    if (!format) {
        return moment(date, "YYYY-MM-DD").format('MMM DD, Y');
    } else {
        return moment(date, "YYYY-MM-DD").format(format);
    }
}

/*
 * Following script is adding line numbers to the ballerina code blocks in the gneerated documentation
 */
function initCodeLineNumbers() {
    $('pre > code.ballerina, pre > code.language-ballerina').each(function() {

        if ($(this).parent().find('.line-numbers-wrap').length === 0) {
            //cont the number of rows
            //Remove the new line from the end of the text
            var numberOfLines = $(this).text().replace(/\n$/, "").split(/\r\n|\r|\n/).length;
            var lines = '<div class="line-numbers-wrap">';

            //Iterate all the lines and create div elements with line number
            for (var i = 1; i <= numberOfLines; i++) {
                lines = lines + '<div class="line-number">' + i + '</div>';
            }
            lines = lines + '</div>';
            //calculate <pre> height and set it to the container
            var preHeight = numberOfLines * 18 + 20;

            $(this).parent()
                .addClass('ballerina-pre-wrapper')
                .prepend($(lines));
        }

    });
}

/*
 * Register ballerina language for highlightJS
 * Grammer: https://github.com/ballerina-platform/ballerina-lang/blob/master/compiler/ballerina-lang/src/main/resources/grammar/BallerinaLexer.g4
 */
if (typeof hljs === 'object') {
    hljs.registerLanguage('ballerina', function() {
        return {
            "k": "package import as public private native service resource function object annotation parameter transformer worker endpoint " +
                "bind xmlns returns version documentation deprecated new if else match foreach while next break fork join some all timeout " +
                "try catch finally throw return transaction abort fail onretry retries onabort oncommit lengthof with in lock untaint start await but check",
            "i": {},
            "c": [{
                "cN": "ballerinadoc",
                "b": "/\\*\\*",
                "e": "\\*/",
                "r": 0,
                "c": [{
                    "cN": "ballerinadoctag",
                    "b": "(^|\\s)@[A-Za-z]+"
                }]
            }, {
                "cN": "comment",
                "b": "//",
                "e": "$",
                "c": [{
                    "b": {}
                }, {
                    "cN": "label",
                    "b": "XXX",
                    "e": "$",
                    "eW": true,
                    "r": 0
                }]
            }, {
                "cN": "comment",
                "b": "/\\*",
                "e": "\\*/",
                "c": [{
                    "b": {}
                }, {
                    "cN": "label",
                    "b": "XXX",
                    "e": "$",
                    "eW": true,
                    "r": 0
                }, "self"]
            }, {
                "cN": "string",
                "b": "\"",
                "e": "\"",
                "i": "\\n",
                "c": [{
                    "b": "\\\\[\\s\\S]",
                    "r": 0
                }, {
                    "cN": "constant",
                    "b": "\\\\[abfnrtv]\\|\\\\x[0-9a-fA-F]*\\\\\\|%[-+# *.0-9]*[dioxXucsfeEgGp]",
                    "r": 0
                }]
            }, {
                "cN": "number",
                "b": "(\\b(0b[01_]+)|\\b0[xX][a-fA-F0-9_]+|(\\b[\\d_]+(\\.[\\d_]*)?|\\.[\\d_]+)([eE][-+]?\\d+)?)[lLfF]?",
                "r": 0
            }, {
                "cN": "annotation",
                "b": "@[A-Za-z]+"
            }, {
                "cN": "type",
                "b": "\\b(int|float|boolean|string|blob|map|jsonOptions|json|xml|table|stream|any|typedesc|type|future|var|error)",
            }]
        };
    });
}

$(document).ready(function() {

    var menu = '<div class="container">' +
        '<div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">' +
        '<nav class="navbar">' +
        '<div>' +
        '<div class="navbar-header">' +
        '<button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">' +
        '<span class="sr-only">&#9776</span>' +
        '<span class="icon-bar"></span>' +
        '<span class="icon-bar"></span>' +
        '<span class="icon-bar"></span>' +
        '</button>' +
        '<p class="navbar-brand cTagLine" href="#">Cloud Native Programming Language' +
        '<a class="cMobileLogo" href="." ><img src="/img/ballerina-logo.svg" alt="Ballerina"/></a>' +
        '</p>' +
        '</div>' +
        '<div id="navbar" class="collapse navbar-collapse">' +
        '<ul class="nav navbar-nav cTopNav">' +
        '<li class="active toctree-l1" id="learnli"><a class="cBioTopLink" href="/learn">Learn</a></li>' +
        '<li class="toctree-l1" id="philosophyli"><a class="cBioTopLink" href="/philosophy">Philosophy</a></li>' +
        '<li class="toctree-l1"><a class="cBioTopLink" href="https://central.ballerina.io/" target="_blank">Central</a></li>' +
        '<li class="toctree-l1" id="openli"><a class="cBioTopLink" href="/open-source">Open Source</a></li>' +
        '<li class="toctree-l1" id="helpli"><a class="cBioTopLink" href="/help">Help </a></li>' +
        '<li class="toctree-l1"><a class="cSerachIcon" href="#"><img src="/img/search.svg"/></a>' +
        '<div class="cSearchBoxTopMenu">' +
        '<div role="search">' +
        '<form role="form">' +
        '<div class="form-group">' +
        '<input type="text" class="form-control" placeholder="Search..." id="mkdocs-search-query" autocomplete="off">' +
        '</div>' +
        '</form>' +
        '<div id="mkdocs-search-results"></div>' +
        '</div></div>' +
        '</li>' +
        '</ul>' +
        '</div>' +
        '</div>' +
        '</nav>' +
        '</div>' +
        '</div>';

    var footer = '<div class="container">' +
        '<div class="col-xs-12 col-sm-12 col-md-3 col-lg-3 cBallerina-io-left-col cBallerinaFooterLinks">' +
        '<ul>' +
        '<li><a class="cBioFooterLink" href="/downloads">Download</a></li>' +
        '<li><a class="cBioFooterLink" href="https://github.com/ballerina-lang/ballerina/blob/master/LICENSE">Code License</a></li>' +
        '<li><a class="cBioFooterLink" href="/license-of-site">Site License</a></li>' +
        '<li><a class="cBioFooterLink" href="/terms-of-service">TERMS OF SERVICE</a></li>' +
        '<li><a class="cBioFooterLink" href="/privacy-policy">PRIVACY POLICY</a></li>' +
        '</ul>' +
        '</div>' +
        '<div class="col-xs-12 col-sm-12 col-md-3 col-lg-3 cBallerina-io-middle-col cBallerinaFooterSignUp">' +
        '<p><span>Announcement List</span><br/>' +
        '<div class="cFormContainer">' +
        '<form>' +
        '<div class="cFieldContainer">' +
        '<input maxlength="90" value="" id="emailUser" name="email" placeholder="I consent to join the email list" title="email" type="text">' +
        '</div>' +
        '<div class="cButtonContainer">' +
        '<a class="cBallerinaButtons subscribeUserForm" href="" id="subscribeUserButton"></a>' +
        '</div>' +
        '</form>' +
        '</div>' +
        '<div class="cSocialmedia">' +
        '<ul>' +
        '<li>' +
        '<a class="cBioFooterLink" href="https://github.com/ballerina-platform" target="_blank"><img src="/img/github.svg"/></a>' +
        '</li>' +
        '<li><a class="cBioFooterLink" href="https://stackoverflow.com/questions/tagged/ballerina" target="_blank"><img src="/img/stackoverflow.svg"/></a></li>' +
        '<li><a class="cBioFooterLink" href="https://twitter.com/ballerinaplat" target="_blank"><img src="/img/twitter.svg"/></a></li>' +
        '<li><a class="cBioFooterLink" href="/open-source/slack/"><img src="/img/slack.svg"/></a></li>' +
        '</ul>' +
        '<div class="pdframe"></div>' +
        '</div>' +
        '</div>' +
        '<div class="col-xs-12 col-sm-12 col-md-6 col-lg-6 cBallerina-io-right-col">' +
        '<p>In the creation of Ballerina, we were inspired by so many technologies. Thank you to all that have come before us (and forgive us if we missed one): Go, Kotlin, Java, Rust, Bootstrap, JavaScript, Jenkins, NPM, Crates, Maven, Gradle, Kubernetes, Envoy, Docker, Microsoft VS Code, Jetbrains IntelliJ, Eclipse Che, WSO2, mkdocs and GitHub.</div>' +
        '</div>';

    $('#iMainNavigation').append(menu);
    $('#iBallerinaFooter').append(footer);

    $("code").addClass('cBasicCode');
    $(".ballerina").removeClass('cBasicCode');
    $(".bash").removeClass('cBasicCode');

    $(".cRuntimeContent").addClass('cShow');

    $(".cRUNTIME").click(function() {
        $(".cRuntimeContent").addClass('cShow');
        $(".cDeploymentContent").removeClass('cShow');
        $(".cLifecycleContent").removeClass('cShow');

    });

    $(".cDEPLOYMENT").click(function() {
        $(".cRuntimeContent").removeClass('cShow');
        $(".cDeploymentContent").addClass('cShow');
        $(".cLifecycleContent").removeClass('cShow');

    });

    $(".cLIFECYCLE").click(function() {
        $(".cRuntimeContent").removeClass('cShow');
        $(".cDeploymentContent").removeClass('cShow');
        $(".cLifecycleContent").addClass('cShow');
    });

    $(".cSEQUENCEContent").addClass('cShow');

    $(".cSEQUENCE").click(function() {
        $(".cSEQUENCEContent").addClass('cShow');
        $(".cCONCURRENCYContent").removeClass('cShow');
        $(".cTYPEContent").removeClass('cShow');

    });

    $(".cCONCURRENCY").click(function() {
        $(".cSEQUENCEContent").removeClass('cShow');
        $(".cCONCURRENCYContent").addClass('cShow');
        $(".cTYPEContent").removeClass('cShow');

    });

    $(".cTYPE").click(function() {
        $(".cSEQUENCEContent").removeClass('cShow');
        $(".cCONCURRENCYContent").removeClass('cShow');
        $(".cTYPEContent").addClass('cShow');
    });

    $(".cSerachIcon").click(function() {
        $(".cSearchBoxTopMenu").toggleClass('cShowcSearchTopMenu');
        if ($(".cSearchBoxTopMenu").hasClass('cShowcSearchTopMenu')) {
            $("#mkdocs-search-query").focus()
        }
    });

    /*
     * subscribe form
     */
    $("#subscribeUserButton").click(function(event) {
        event.preventDefault();
        subscribeUser($(this).val());
    });

    $('#emailUser').on('keypress', function(event) {
        if (event.which === 13) {
            event.preventDefault();
            $(this).attr("disabled", "disabled");
            subscribeUser($(this).val());
            $(this).removeAttr("disabled");
        }
    });

    $(".cBallerina-io-packages").click(function() {
        $(".cCollaps-Menu").toggleClass('cOpenMenu');
        $(".cBallerina-io-packages").toggleClass('cOpenMenu');
        $(".cCollaps-Menu-first").removeClass('cOpenMenu');
        $(".cBallerina-io-primitive-types").removeClass('cOpenMenu');
        $(".cCollaps-Menu-second").removeClass('cOpenMenu');
        $(".cBallerina-io-x").removeClass('cOpenMenu');
    });

    $(".cBallerina-io-primitive-types").click(function() {
        $(".cCollaps-Menu-first").toggleClass('cOpenMenu');
        $(".cBallerina-io-primitive-types").toggleClass('cOpenMenu');
        $(".cCollaps-Menu").removeClass('cOpenMenu');
        $(".cBallerina-io-packages").removeClass('cOpenMenu');
        $(".cCollaps-Menu-second").removeClass('cOpenMenu');
        $(".cBallerina-io-x").removeClass('cOpenMenu');
    });

    $(".cBallerina-io-x").click(function() {
        $(".cCollaps-Menu-second").toggleClass('cOpenMenu');
        $(".cBallerina-io-x").toggleClass('cOpenMenu');
        $(".cCollaps-Menu").removeClass('cOpenMenu');
        $(".cBallerina-io-packages").removeClass('cOpenMenu');
        $(".cCollaps-Menu-first").removeClass('cOpenMenu');
        $(".cBallerina-io-primitive-types").removeClass('cOpenMenu');
    });

    initCodeLineNumbers();

    $('.cBBE-body').each(function() {
        var lineCount = 0,
            olCount = 1;

        $('.cTR', this).each(function(i, n) {
            var $codeElem = $(n).find('td.code').get(0);
            var lines = $('> td.code', n).text().replace(/\n$/, "").trim().split(/\r\n|\r|\n/);
            var numbers = [];

            $.each(lines, function(i) {
                lineCount += 1;
                numbers.push('<span class="line-number">' + lineCount + '</span>');
            });

            $("<div/>", {
                "class": "bbe-code-line-numbers",
                html: numbers.join("")
            }).prependTo($codeElem);


            if ($('.cCodeDesription > div > ol', this).length > 0) {
                var $elem = $('.cCodeDesription > div > ol', this);
                $($elem).parent().prepend('<span class="ol-number">' + olCount + '.</span>');
                olCount++;
            } else {
                olCount = 1;
            }
        });
    });

});
