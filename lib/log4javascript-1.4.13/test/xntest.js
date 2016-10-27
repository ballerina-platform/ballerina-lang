// Next three methods are primarily for IE5, which is missing them
if (!Array.prototype.push) {
	Array.prototype.push = function() {
		for (var i = 0; i < arguments.length; i++){
				this[this.length] = arguments[i];
		}
		return this.length;
	};
}

if (!Array.prototype.shift) {
	Array.prototype.shift = function() {
		if (this.length > 0) {
			var firstItem = this[0];
			for (var i = 0; i < this.length - 1; i++) {
				this[i] = this[i + 1];
			}
			this.length = this.length - 1;
			return firstItem;
		}
	};
}

if (!Function.prototype.apply) {
	Function.prototype.apply = function(obj, args) {
		var methodName = "__apply__";
		if (typeof obj[methodName] != "undefined") {
			methodName += (String(Math.random())).substr(2);
		}
		obj[methodName] = this;

		var argsStrings = new Array(args.length);
		for (var i = 0; i < args.length; i++) {
			argsStrings[i] = "args[" + i + "]";
		}
		var script = "obj." + methodName + "(" + argsStrings.join(",") + ")";
		var returnValue = eval(script);
		delete obj[methodName];
		return returnValue;
	};
}

/* -------------------------------------------------------------------------- */

var xn = new Object();

(function() {
	// Utility functions

	// Event listeners
	var getListenersPropertyName = function(eventName) {
		return "__listeners__" + eventName;
	};

	var addEventListener = function(node, eventName, listener, useCapture) {
		useCapture = Boolean(useCapture);
		if (node.addEventListener) {
			node.addEventListener(eventName, listener, useCapture);
		} else if (node.attachEvent) {
			node.attachEvent("on" + eventName, listener);
		} else {
			var propertyName = getListenersPropertyName(eventName);
			if (!node[propertyName]) {
				node[propertyName] = new Array();

				// Set event handler
				node["on" + eventName] = function(evt) {
					evt = module.getEvent(evt);
					var listenersPropertyName = getListenersPropertyName(eventName);

					// Clone the array of listeners to leave the original untouched
					var listeners = cloneArray(this[listenersPropertyName]);
					var currentListener;

					// Call each listener in turn
					while (currentListener = listeners.shift()) {
						currentListener.call(this, evt);
					}
				};
			}
			node[propertyName].push(listener);
		}
	};

	// Clones an array
	var cloneArray = function(arr) {
		var clonedArray = [];
		for (var i = 0; i < arr.length; i++) {
			clonedArray[i] = arr[i];
		}
		return clonedArray;
	}

	var isFunction = function(f) {
		if (!f){ return false; }
		return (f instanceof Function || typeof f == "function");
	};

	// CSS Utilities
	
	function array_contains(arr, val) {
		for (var i = 0, len = arr.length; i < len; i++) {
			if (arr[i] === val) {
				return true;
			}
		}
		return false;
	}

	function addClass(el, cssClass) {
		if (!hasClass(el, cssClass)) {
			if (el.className) {
				el.className += " " + cssClass;
			} else {
				el.className = cssClass;
			}
		}
	}

	function hasClass(el, cssClass) {
		if (el.className) {
			var classNames = el.className.split(" ");
			return array_contains(classNames, cssClass);
		}
		return false;
	}

	function removeClass(el, cssClass) {
		if (hasClass(el, cssClass)) {
			// Rebuild the className property
			var existingClasses = el.className.split(" ");
			var newClasses = [];
			for (var i = 0; i < existingClasses.length; i++) {
				if (existingClasses[i] != cssClass) {
					newClasses[newClasses.length] = existingClasses[i];
				}
			}
			el.className = newClasses.join(" ");
		}
	}

	function replaceClass(el, newCssClass, oldCssClass) {
		removeClass(el, oldCssClass);
		addClass(el, newCssClass);
	}

	function getExceptionStringRep(ex) {
		if (ex) {
			var exStr = "Exception: ";
			if (ex.message) {
				exStr += ex.message;
			} else if (ex.description) {
				exStr += ex.description;
			}
			if (ex.lineNumber) {
				exStr += " on line number " + ex.lineNumber;
			}
			if (ex.fileName) {
				exStr += " in file " + ex.fileName;
			}
			return exStr;
		}
		return null;
	}


	/* ---------------------------------------------------------------------- */

	/* Configure the test logger try to use FireBug */
	var log, error;
	if (window["console"] && typeof console.log == "function") {
		log = function() {
			if (xn.test.enableTestDebug) {
				console.log.apply(console, arguments);
			}
		};
		error = function() {
			if (xn.test.enableTestDebug) {
				console.error.apply(console, arguments);
			}
		};
	} else {
		log = function() {};
	}

	/* Set up something to report to */

	var initialized = false;
	var container;
	var progressBarContainer, progressBar, overallSummaryText;
	var currentTest = null;
	var suites = [];
	var totalTestCount = 0;
	var currentTestIndex = 0;
	var testFailed = false;
	var testsPassedCount = 0;
	var startTime;
	
	var log4javascriptEnabled = false;
	
	var nextSuiteIndex = 0;
	
	function runNextSuite() {
		if (nextSuiteIndex < suites.length) {
			suites[nextSuiteIndex++].run();
		}
	}
	
	var init = function() {
		if (initialized) { return true; }
		
		container = document.createElement("div");
		
		// Create the overall progress bar
		progressBarContainer = container.appendChild(document.createElement("div"));
		progressBarContainer.className = "xn_test_progressbar_container xn_test_overallprogressbar_container";
		progressBar = progressBarContainer.appendChild(document.createElement("div"));
		progressBar.className = "success";

		document.body.appendChild(container);

		var h1 = progressBar.appendChild(document.createElement("h1"));
		overallSummaryText = h1.appendChild(document.createTextNode(""));

		initialized = true;
		
		// Set up logging
		log4javascriptEnabled = !!log4javascript && xn.test.enable_log4javascript;
		
		function TestLogAppender() {}
		
		if (log4javascriptEnabled) {
			TestLogAppender.prototype = new log4javascript.Appender();
			TestLogAppender.prototype.layout = new log4javascript.PatternLayout("%d{HH:mm:ss,SSS} %-5p %m");
			TestLogAppender.prototype.append = function(loggingEvent) {
				var formattedMessage = this.getLayout().format(loggingEvent);
				if (this.getLayout().ignoresThrowable()) {
					formattedMessage += loggingEvent.getThrowableStrRep();
				}
				currentTest.addLogMessage(formattedMessage);
			};
			
			var appender = new TestLogAppender();
			appender.setThreshold(log4javascript.Level.ALL);
			log4javascript.getRootLogger().addAppender(appender);
			log4javascript.getRootLogger().setLevel(log4javascript.Level.ALL);
		}

		startTime = new Date();

		// First, build each suite
		for (var i = 0; i < suites.length; i++) {
			suites[i].build();
			totalTestCount += suites[i].tests.length;
		}
		
		// Now run each suite
		runNextSuite();
	};
	
	function updateProgressBar() {
		progressBar.style.width = "" + parseInt(100 * (currentTestIndex) / totalTestCount) + "%";
		var s = (totalTestCount === 1) ? "" : "s";
		var timeTaken = new Date().getTime() - startTime.getTime();
		overallSummaryText.nodeValue = "" + testsPassedCount + " of " + totalTestCount + " test" + s + " passed in " + timeTaken + "ms";
	}

	addEventListener(window, "load", init);

	/* ---------------------------------------------------------------------- */

	/* Test Suite */
	var Suite = function(name, callback, hideSuccessful) {
		this.name = name;
		this.callback = callback;
		this.hideSuccessful = hideSuccessful;
		this.tests = [];
		this.log = log;
		this.error = error;
		this.expanded = true;
		suites.push(this);
	}

	Suite.prototype.test = function(name, callback, setUp, tearDown) {
		this.log("adding a test named " + name)
		var t = new Test(name, callback, this, setUp, tearDown);
		this.tests.push(t);
	};

	Suite.prototype.build = function() {
		// Build the elements used by the suite
		var suite = this;
		this.testFailed = false;
		this.container = document.createElement("div");
		this.container.className = "xn_test_suite_container";

		var heading = document.createElement("h2");
		this.expander = document.createElement("span");
		this.expander.className = "xn_test_expander";
		this.expander.onclick = function() {
			if (suite.expanded) {
				suite.collapse();
			} else {
				suite.expand();
			}
		};
		heading.appendChild(this.expander);
		
		this.headingTextNode = document.createTextNode(this.name);
		heading.appendChild(this.headingTextNode);
		this.container.appendChild(heading);

		this.reportContainer = document.createElement("dl");
		this.container.appendChild(this.reportContainer);

		this.progressBarContainer = document.createElement("div");
		this.progressBarContainer.className = "xn_test_progressbar_container";
		this.progressBar = document.createElement("div");
		this.progressBar.className = "success";
		this.progressBar.innerHTML = "&nbsp;";
		this.progressBarContainer.appendChild(this.progressBar);
		this.reportContainer.appendChild(this.progressBarContainer);

		this.expand();

		container.appendChild(this.container);

		// invoke callback to build the tests
		this.callback.apply(this, [this]);
	};

	Suite.prototype.run = function() {
		this.log("running suite '%s'", this.name)
		this.startTime = new Date();

		// now run the first test
		this._currentIndex = 0;
		this.runNextTest();
	};

	Suite.prototype.updateProgressBar = function() {
		// Update progress bar
		this.progressBar.style.width = "" + parseInt(100 * (this._currentIndex) / this.tests.length) + "%";
		//log(this._currentIndex + ", " + this.tests.length + ", " + progressBar.style.width + ", " + progressBar.className);
	};

	Suite.prototype.expand = function() {
		this.expander.innerHTML = "-";
		replaceClass(this.reportContainer, "xn_test_expanded", "xn_test_collapsed");
		this.expanded = true;
	};

	Suite.prototype.collapse = function() {
		this.expander.innerHTML = "+";
		replaceClass(this.reportContainer, "xn_test_collapsed", "xn_test_expanded");
		this.expanded = false;
	};

	Suite.prototype.finish = function(timeTaken) {
		var newClass = this.testFailed ? "xn_test_suite_failure" : "xn_test_suite_success";
		var oldClass = this.testFailed ? "xn_test_suite_success" : "xn_test_suite_failure";
		replaceClass(this.container, newClass, oldClass);

		this.headingTextNode.nodeValue += " (" + timeTaken + "ms)";

		if (this.hideSuccessful && !this.testFailed) {
			this.collapse();
		}
		runNextSuite();
	};

	/**
	 * Works recursively with external state (the next index)
	 * so that we can handle async tests differently
	 */
	Suite.prototype.runNextTest = function() {
		if (this._currentIndex == this.tests.length) {
			// finished!
			var timeTaken = new Date().getTime() - this.startTime.getTime();

			this.finish(timeTaken);
			return;
		}

		var suite = this;
		var t = this.tests[this._currentIndex++];
		currentTestIndex++;

		if (isFunction(suite.setUp)) {
			suite.setUp.apply(suite, [t]);
		}
		if (isFunction(t.setUp)) {
			t.setUp.apply(t, [t]);
		}

		t._run();
		
		function afterTest() {
			if (isFunction(suite.tearDown)) {
				suite.tearDown.apply(suite, [t]);
			}
			if (isFunction(t.tearDown)) {
				t.tearDown.apply(t, [t]);
			}
			suite.log("finished test [%s]", t.name);
			updateProgressBar();
			suite.updateProgressBar();
			suite.runNextTest();
		}
		
		if (t.isAsync) {
			t.whenFinished = afterTest;
		} else {
			setTimeout(afterTest, 1);
		}
	};

	Suite.prototype.reportSuccess = function() {
	};

	/* ---------------------------------------------------------------------- */
	/**
	 * Create a new test
	 */
	var Test = function(name, callback, suite, setUp, tearDown) {
		this.name = name;
		this.callback = callback;
		this.suite = suite;
		this.setUp = setUp;
		this.tearDown = tearDown;
		this.log = log;
		this.error = error;
		this.assertCount = 0;
		this.logMessages = [];
		this.logExpanded = false;
	};

	/**
	 * Default success reporter, please override
	 */
	Test.prototype.reportSuccess = function(name, timeTaken) {
		/* default success reporting handler */
		this.reportHeading = document.createElement("dt");
		var text = this.name + " passed in " + timeTaken + "ms";
		
		this.reportHeading.appendChild(document.createTextNode(text));

		this.reportHeading.className = "success";
		var dd = document.createElement("dd");
		dd.className = "success";

		this.suite.reportContainer.appendChild(this.reportHeading);
		this.suite.reportContainer.appendChild(dd);
		this.createLogReport();
	};

	/**
	 * Cause the test to immediately fail
	 */
	Test.prototype.reportFailure = function(name, msg, ex) {
		this.suite.testFailed = true;
		this.suite.progressBar.className = "failure";
		progressBar.className = "failure";
		this.reportHeading = document.createElement("dt");
		this.reportHeading.className = "failure";
		var text = document.createTextNode(this.name);
		this.reportHeading.appendChild(text);

		var dd = document.createElement("dd");
		dd.appendChild(document.createTextNode(msg));
		dd.className = "failure";

		this.suite.reportContainer.appendChild(this.reportHeading);
		this.suite.reportContainer.appendChild(dd);
		if (ex && ex.stack) {
			var stackTraceContainer = this.suite.reportContainer.appendChild(document.createElement("code"));
			stackTraceContainer.className = "xn_test_stacktrace";
			stackTraceContainer.innerHTML = ex.stack.replace(/\r/g, "\n").replace(/\n{1,2}/g, "<br />");
		}
		this.createLogReport();
	};
	
	Test.prototype.createLogReport = function() {
		if (this.logMessages.length > 0) {
			this.reportHeading.appendChild(document.createTextNode(" ("));
			var logToggler = this.reportHeading.appendChild(document.createElement("a"));
			logToggler.href = "#";
			logToggler.innerHTML = "show log";
			var test = this;
			
			logToggler.onclick = function() {
				if (test.logExpanded) {
					test.hideLogReport();
					this.innerHTML = "show log";
					test.logExpanded = false;
				} else {
					test.showLogReport();
					this.innerHTML = "hide log";
					test.logExpanded = true;
				}
				return false;
			};

			this.reportHeading.appendChild(document.createTextNode(")"));
			
			// Create log report
			this.logReport = this.suite.reportContainer.appendChild(document.createElement("pre"));
			this.logReport.style.display = "none";
			this.logReport.className = "xn_test_log_report";
			var logMessageDiv;
			for (var i = 0, len = this.logMessages.length; i < len; i++) {
				logMessageDiv = this.logReport.appendChild(document.createElement("div"));
				logMessageDiv.appendChild(document.createTextNode(this.logMessages[i]));
			}
		}
	};

	Test.prototype.showLogReport = function() {
		this.logReport.style.display = "inline-block";
	};
		
	Test.prototype.hideLogReport = function() {
		this.logReport.style.display = "none";
	};

	Test.prototype.async = function(timeout, callback) {
		timeout = timeout || 250;
		var self = this;
		var timedOutFunc = function() {
			if (!self.completed) {
				var message = (typeof callback === "undefined") ?
							"Asynchronous test timed out" : callback(self);
				self.fail(message);
			}
		}
		var timer = setTimeout(function () { timedOutFunc.apply(self, []); }, timeout)
		this.isAsync = true;
	};

	/**
	 * Run the test
	 */
	Test.prototype._run = function() {
		this.log("starting test [%s]", this.name);
		this.startTime = new Date();
		currentTest = this;
		try {
			this.callback(this);
			if (!this.completed && !this.isAsync) {
				this.succeed();
			}
		} catch (e) {
			this.log("test [%s] threw exception [%s]", this.name, e);
			var s = (this.assertCount === 1) ? "" : "s";
			this.fail("Exception thrown after " + this.assertCount + " successful assertion" + s + ": " + getExceptionStringRep(e), e);
		}
	};

	/**
	 * Cause the test to immediately succeed
	 */
	Test.prototype.succeed = function() {
		if (this.completed) { return false; }
		// this.log("test [%s] succeeded", this.name);
		this.completed = true;
		var timeTaken = new Date().getTime() - this.startTime.getTime();
		testsPassedCount++;
		this.reportSuccess(this.name, timeTaken);
		if (this.whenFinished) {
			this.whenFinished();
		}
	};

	Test.prototype.fail = function(msg, ex)	{
		if (typeof msg != "string") {
			msg = getExceptionStringRep(msg);
		}
		if (this.completed) { return false; }
		this.completed = true;
		// this.log("test [%s] failed", this.name);
		this.reportFailure(this.name, msg, ex);
		if (this.whenFinished) {
			this.whenFinished();
		}
	};
	
	Test.prototype.addLogMessage = function(logMessage) {
		this.logMessages.push(logMessage);
	};

	/* assertions */
	var displayStringForValue = function(obj) {
		if (obj === null) {
			return "null";
		} else if (typeof obj === "undefined") {
			return "undefined";
		}
		return obj.toString();
	};

	var assert = function(args, expectedArgsCount, testFunction, defaultComment) {
		this.assertCount++;
		var comment = defaultComment;
		var i;
		var success;
		var values = [];
		if (args.length == expectedArgsCount) {
			for (i = 0; i < args.length; i++) {
				values[i] = args[i];
			}
		} else if (args.length == expectedArgsCount + 1) {
			comment = args[0];
			for (i = 1; i < args.length; i++) {
				values[i - 1] = args[i];
			}
		} else {
			throw new Error("Invalid number of arguments passed to assert function");
		}
		success = testFunction(values);
		if (!success) {
			var regex = /\{([0-9]+)\}/;
			while (regex.test(comment)) {
				comment = comment.replace(regex, displayStringForValue(values[parseInt(RegExp.$1)]));
			}
			this.fail("Test failed on assertion " + this.assertCount + ": " + comment);
		}
	};

	var testNull = function(values) {
		return (values[0] === null);
	};

	Test.prototype.assertNull = function() {
		assert.apply(this, [arguments, 1, testNull, "Expected to be null but was {0}"]);
	}

	var testNotNull = function(values) {
		return (values[0] !== null);
	};

	Test.prototype.assertNotNull = function() {
		assert.apply(this, [arguments, 1, testNotNull, "Expected not to be null but was {0}"]);
	}

	var testBoolean = function(values) {
		return (Boolean(values[0]));
	};

	Test.prototype.assert = function() {
		assert.apply(this, [arguments, 1, testBoolean, "Expected not to be equivalent to false"]);
	};

	var testTrue = function(values) {
		return (values[0] === true);
	};

	Test.prototype.assertTrue = function() {
		assert.apply(this, [arguments, 1, testTrue, "Expected to be true but was {0}"]);
	};

	Test.prototype.assert = function() {
		assert.apply(this, [arguments, 1, testTrue, "Expected to be true but was {0}"]);
	};

	var testFalse = function(values) {
		return (values[0] === false);
	};

	Test.prototype.assertFalse = function() {
		assert.apply(this, [arguments, 1, testFalse, "Expected to be false but was {0}"]);
	}

	var testEquivalent = function(values) {
		return (values[0] === values[1]);
	};

	Test.prototype.assertEquivalent = function() {
		assert.apply(this, [arguments, 2, testEquivalent, "Expected to be equal but values were {0} and {1}"]);
	}

	var testNotEquivalent = function(values) {
		return (values[0] !== values[1]);
	};

	Test.prototype.assertNotEquivalent = function() {
		assert.apply(this, [arguments, 2, testNotEquivalent, "Expected to be not equal but values were {0} and {1}"]);
	}

	var testEquals = function(values) {
		return (values[0] == values[1]);
	};

	Test.prototype.assertEquals = function() {
		assert.apply(this, [arguments, 2, testEquals, "Expected to be equal but values were {0} and {1}"]);
	}

	var testNotEquals = function(values) {
		return (values[0] != values[1]);
	};

	Test.prototype.assertNotEquals = function() {
		assert.apply(this, [arguments, 2, testNotEquals, "Expected to be not equal but values were {0} and {1}"]);
	}

	var testRegexMatches = function(values) {
		return (values[0].test(values[1]));
	};

	Test.prototype.assertRegexMatches = function() {
		assert.apply(this, [arguments, 2, testRegexMatches, "Expected regex {0} to match value {1} but it didn't"]);
	}

	Test.prototype.assertError = function(f, errorType) {
		try {
			f();
			this.fail("Expected error to be thrown");
		} catch (e) {
			if (errorType && (!(e instanceof errorType))) {
				this.fail("Expected error of type " + errorType + " to be thrown but error thrown was " + e);
			}
		}
	};

	/**
	 * Execute a synchronous test
	 */
	xn.test = function(name, callback) {
		xn.test.suite("Anonymous", function(s) {
			s.test(name, callback);
		});
	}

	/**
	 * Create a test suite with a given name
	 */
	xn.test.suite = function(name, callback, hideSuccessful) {
		var s = new Suite(name, callback, hideSuccessful);
	}
})();