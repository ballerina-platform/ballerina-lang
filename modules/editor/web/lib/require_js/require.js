//
// This file is part of Smoothie.
//
// Copyright (C) 2013-2015 Flowy Apps GmbH <hello@flowyapps.com>
//
// Smoothie is free software: you can redistribute it and/or modify it under the
// terms of the GNU Lesser General Public License as published by the Free
// Software Foundation, either version 3 of the License, or (at your option) any
// later version.
//
// Smoothie is distributed in the hope that it will be useful, but WITHOUT ANY
// WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
// A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
// details.You should have received a copy of the GNU Lesser General Public
// License along with Smoothie.  If not, see <http://www.gnu.org/licenses/>.
//
////////////////////////////////////////////////////////////////////////////////

// INFO Standalone require()
//      This is a stripped down standalone version of Smoothie's require
//      function. If you also like to have a 'bootloader', which gives you some
//      nice hooks to execute code on different loading states of the document
//      and keeps your JavaScript completely separate from your HTML, we
//      recommend to load smoothie.js from the library's root directory.

// NOTE The load parameter points to the function, which prepares the
//      environment for each module and runs its code. Scroll down to the end of
//      the file to see the function definition.
(function(load) { 'use strict';

var SmoothieError = function(message, fileName, lineNumber) {
	this.name = "SmoothieError";
	this.message = message;
}
SmoothieError.prototype = Object.create(Error.prototype);

// NOTE Mozilla still sets the wrong fileName porperty for errors that occur
//      inside an eval call (even with sourceURL). However, the stack
//      contains the correct source, so it can be used to re-threw the error
//      with the correct fileName property.
// NOTE Re-threwing a new error object will mess up the stack trace and the
//      column number.
if (typeof (new Error()).fileName == "string") {
	self.addEventListener("error", function(evt) {
		if (evt.error instanceof Error) {
			if (pwd[0]) {
				evt.preventDefault();
				throw new evt.error.constructor(evt.error.message, pwd[0].uri, evt.error.lineNumber);
			}
			else {
				var m = evt.error.stack.match(/^[^\n@]*@([^\n]+):\d+:\d+/);
				if (m === null) {
					console.warn("Smoothie: unable to read file name from stack");
				}
				else if (evt.error.fileName != m[1]) {
					evt.preventDefault();
					throw new evt.error.constructor(evt.error.message, m[1], evt.error.lineNumber);
				}
			}
		}
	}, false);
}

// INFO Current module descriptors
//      pwd[0] contains the descriptor of the currently loaded module,
//      pwd[1] contains the descriptor its parent module and so on.

var pwd = Array();

// INFO Path parser
// NOTE Older browsers don't support the URL interface, therefore we use an
//      anchor element as parser in that case. Thes breaks web worker support,
//      but we don't care since these browser also don't support web workers.

var parser = URL ? new URL(location.href) : document.createElement('A');

// INFO Module cache
// NOTE Contains getter functions for the exports objects of all the loaded
//      modules. The getter for the module 'mymod' is name '$name' to prevent
//      collisions with predefined object properties (see note below).
//      As long as a module has not been loaded the getter is either undefined
//      or contains the module code as a function (in case the module has been
//      pre-loaded in a bundle).
// NOTE IE8 supports defineProperty only for DOM objects, therfore we use a
//      HTMLDivElement as cache in that case. This breaks web worker support,
//      but we don't care since IE8 has no web workers at all.

try {
	var cache = new Object();
	Object.defineProperty(cache, "foo", {'value':"bar",'configurable':true});
	delete cache.foo;
}
catch (e) {
	console.warn("Falling back to DOM workaround for defineProperty: "+e);
	cache = document.createElement('DIV');
}

// INFO Send lock
// NOTE Sending the request causes the event loop to continue. Therefore
//      pending AJAX load events for the same url might be executed before
//      the synchronous onLoad is called. This should be no problem, but in
//      Chrome the responseText of the sneaked in load events will be empty.
//      Therefore we have to lock the loading while executing send().   

var lock = new Object();

// INFO Smoothie options
//      The values can be set by defining a object called Smoothie. The
//      Smoothe object has to be defined before this script here is loaded
//      and changing the values in the Smoothie object will have no effect
//      afterwards!

var requirePath = self.Smoothie&&self.Smoothie.requirePath!==undefined ? self.Smoothie.requirePath.slice(0) : ['./'];
var requireCompiler = self.Smoothie&&self.Smoothie.requireCompiler!==undefined ? self.Smoothie.requireCompiler : null;

// NOTE Parse module root paths
var base = [location.origin, location.href.substr(0, location.href.lastIndexOf("/")+1)];
for (var i=0; i<requirePath.length; i++) {
	parser.href = (requirePath[i][0]=="."?base[1]:base[0])+requirePath[i];
	requirePath[i] = parser.href;
}

// NOTE Add preloaded modules to cache
for (var id in (self.Smoothie && self.Smoothie.requirePreloaded))
	cache['$'+resolve(id).id] = self.Smoothie.requirePreloaded[id].toString();

// NOTE Add module overrides to cache
for (var id in (self.Smoothie && self.Smoothie.requireOverrides))
	cache['$'+resolve(id).id] = self.Smoothie.requireOverrides[id];

// INFO Module getter
//      Takes a module identifier, resolves it and gets the module code via an
//      AJAX request from the module URI. If this was successful the code and
//      some environment variables are passed to the load function. The return
//      value is the module's `exports` object. If the cache already
//      contains an object for the module id, this object is returned directly.
// NOTE If a callback function has been passed, the AJAX request is asynchronous
//      and the mpdule exports are passed to the callback function after the
//      module has been loaded.

function require(identifier, callback, compiler) {
	if (identifier instanceof Array) {
		var modules = new Array();
		var modcount = identifier.length;
		for (var index = 0; index < identifier.length; index++) {
			(function(id, i) {
				modules.push(require(id, callback&&function(mod) {
					modules[i] = mod;
					(--modcount==0) && callback(modules);
				}, compiler));
			})(identifier[index], index);
		}
		return modules;
	}

	compiler = compiler!==undefined ? compiler : requireCompiler;
	var descriptor = resolve(identifier);
	var cacheid = '$'+descriptor.id;

	if (cache[cacheid]) {
		if (typeof cache[cacheid] === 'string')
			load(descriptor, cache, pwd, cache[cacheid]);
		// NOTE The callback should always be called asynchronously to ensure
		//      that a cached call won't differ from an uncached one.
		callback && setTimeout(function(){callback(cache[cacheid])}, 0);
		return cache[cacheid];
	}

	var request = new XMLHttpRequest();

	// NOTE IE8 doesn't support the onload event, therefore we use
	//      onreadystatechange as a fallback here. However, onreadystatechange
	//      shouldn't be used for all browsers, since at least mobile Safari
	//      seems to have an issue where onreadystatechange is called twice for
	//      readyState 4.
	callback && (request[request.onload===null?'onload':'onreadystatechange'] = onLoad);
	request.open('GET', descriptor.uri, !!callback);
	lock[cacheid] = lock[cacheid]++||1;
	request.send();
	lock[cacheid]--;
	!callback && onLoad();
	return cache[cacheid];

	function onLoad() {
		if (request.readyState != 4)
			return;
		if (request.status != 200)
			throw new SmoothieError("unable to load "+descriptor.id+" ("+request.status+" "+request.statusText+")");
		if (lock[cacheid]) {
			console.warn("module locked: "+descriptor.id);
			callback && setTimeout(onLoad, 0);
			return;
		}
		if (!cache[cacheid]) {
			var source = compiler ? compiler(request.responseText) : request.responseText;
			load(descriptor, cache, pwd, 'function(){\n'+source+'\n}');
		}
		callback && callback(cache[cacheid]);
	}
}

// INFO Module resolver
//      Takes a module identifier and resolves it to a module id and URI. Both
//      values are returned as a module descriptor, which can be passed to
//      `fetch` to load a module.

function resolve(identifier) {
	// NOTE Matches [1]:[..]/[path/to/][file][.js]
	var m = identifier.match(/^(?:([^:\/]+):)?(\.\.?)?\/?((?:.*\/)?)([^\.]+)?(\..*)?$/);
	// NOTE Matches [1]:[/path/to/]file.js
	var p = (pwd[0]?pwd[0].id:"").match(/^(?:([^:\/]+):)?(.*\/|)[^\/]*$/);
	var root = m[2] ? requirePath[p[1]?parseInt(p[1]):0] : requirePath[m[1]?parseInt(m[1]):0];
	parser.href = (m[2]?root+p[2]+m[2]+'/':root)+m[3]+(m[4]?m[4]:'index');
	var uri = parser.href+(m[5]?m[5]:'.js');
	if (uri.substr(0,root.length) != root)
		throw new SmoothieError("Relative identifier outside of module root");
	var id = (m[1]?m[1]+":":"0:")+parser.href.substr(root.length);
	return {'id':id,'uri':uri};
}

// INFO Exporting require to global scope

if (self.require !== undefined)
	throw new SmoothieError('\'require\' already defined in global scope');

try {
	Object.defineProperty(self, 'require', {'value':require});
	Object.defineProperty(self.require, 'resolve', {'value':resolve});
	Object.defineProperty(self.require, 'path', {'get':function(){return requirePath.slice(0);}});
}
catch (e) {
	// NOTE IE8 can't use defineProperty on non-DOM objects, so we have to fall
	//      back to unsave property assignments in this case.
	self.require = require;
	self.require.resolve = resolve;
	self.require.path = requirePath.slice(0);
}

})(

// INFO Module loader
//      Takes the module descriptor, the global variables and the module code,
//      sets up the module envirinment, defines the module getter in the cache
//      and evaluates the module code. If module is a bundle the code of the
//      pre-loaded modules will be stored in the cache afterwards.
// NOTE This functions is defined as an anonymous function, which is passed as
//      a parameter to the closure above to provide a clean environment (only
//      global variables, module and exports) for the loaded module. This is
//      also the reason why `source`, `pwd` & `cache` are not named parameters.
// NOTE If we would strict use mode here, the evaluated code would be forced to be
//      in strict mode, too.

function /*load*/(module/*, cache, pwd, source*/) {
	var global = self;
	var exports = new Object();
	Object.defineProperty(module, 'exports', {'get':function(){return exports;},'set':function(e){exports=e;}});
	arguments[2].unshift(module);
	Object.defineProperty(arguments[1], '$'+module.id, {'get':function(){return exports;}});
	arguments[3] = '('+arguments[3]+')();\n//# sourceURL='+module.uri;
	eval(arguments[3]);
	// NOTE Store module code in the cache if the loaded file is a bundle
	if (typeof module.id !== 'string')
		for (id in module)
			arguments[1]['$'+require.resolve(id).id] = module[id].toString();
	arguments[2].shift();
}

);
