(function(){function h(a){return function(){return this[a]}}function l(a){return function(){return a}}var m=this;
function aa(a){var b=typeof a;if("object"==b)if(a){if(a instanceof Array)return"array";if(a instanceof Object)return b;var c=Object.prototype.toString.call(a);if("[object Window]"==c)return"object";if("[object Array]"==c||"number"==typeof a.length&&"undefined"!=typeof a.splice&&"undefined"!=typeof a.propertyIsEnumerable&&!a.propertyIsEnumerable("splice"))return"array";if("[object Function]"==c||"undefined"!=typeof a.call&&"undefined"!=typeof a.propertyIsEnumerable&&!a.propertyIsEnumerable("call"))return"function"}else return"null";
else if("function"==b&&"undefined"==typeof a.call)return"object";return b}function n(a){return"string"==typeof a}function ba(a,b,c){return a.call.apply(a.bind,arguments)}function da(a,b,c){if(!a)throw Error();if(2<arguments.length){var d=Array.prototype.slice.call(arguments,2);return function(){var c=Array.prototype.slice.call(arguments);Array.prototype.unshift.apply(c,d);return a.apply(b,c)}}return function(){return a.apply(b,arguments)}}
function q(a,b,c){q=Function.prototype.bind&&-1!=Function.prototype.bind.toString().indexOf("native code")?ba:da;return q.apply(null,arguments)}function ea(a,b){var c=Array.prototype.slice.call(arguments,1);return function(){var b=c.slice();b.push.apply(b,arguments);return a.apply(this,b)}}
function r(a){var b=t;function c(){}c.prototype=b.prototype;a.u=b.prototype;a.prototype=new c;a.t=function(a,c,f){for(var g=Array(arguments.length-2),k=2;k<arguments.length;k++)g[k-2]=arguments[k];return b.prototype[c].apply(a,g)}}Function.prototype.bind=Function.prototype.bind||function(a,b){if(1<arguments.length){var c=Array.prototype.slice.call(arguments,1);c.unshift(this,a);return q.apply(null,c)}return q(this,a)};/*

 Copyright 2014 Software Freedom Conservancy

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
function u(a,b,c){this.a=a;this.b=b||1;this.d=c||1};var fa=String.prototype.trim?function(a){return a.trim()}:function(a){return a.replace(/^[\s\xa0]+|[\s\xa0]+$/g,"")};function v(a,b){return-1!=a.indexOf(b)}function ga(a,b){return a<b?-1:a>b?1:0};var w=Array.prototype,ha=w.indexOf?function(a,b,c){return w.indexOf.call(a,b,c)}:function(a,b,c){c=null==c?0:0>c?Math.max(0,a.length+c):c;if(n(a))return n(b)&&1==b.length?a.indexOf(b,c):-1;for(;c<a.length;c++)if(c in a&&a[c]===b)return c;return-1},x=w.forEach?function(a,b,c){w.forEach.call(a,b,c)}:function(a,b,c){for(var d=a.length,e=n(a)?a.split(""):a,f=0;f<d;f++)f in e&&b.call(c,e[f],f,a)},ia=w.filter?function(a,b,c){return w.filter.call(a,b,c)}:function(a,b,c){for(var d=a.length,e=[],f=0,g=n(a)?
a.split(""):a,k=0;k<d;k++)if(k in g){var p=g[k];b.call(c,p,k,a)&&(e[f++]=p)}return e},z=w.reduce?function(a,b,c,d){d&&(b=q(b,d));return w.reduce.call(a,b,c)}:function(a,b,c,d){var e=c;x(a,function(c,g){e=b.call(d,e,c,g,a)});return e},ja=w.some?function(a,b,c){return w.some.call(a,b,c)}:function(a,b,c){for(var d=a.length,e=n(a)?a.split(""):a,f=0;f<d;f++)if(f in e&&b.call(c,e[f],f,a))return!0;return!1};
function ka(a,b){var c;a:{c=a.length;for(var d=n(a)?a.split(""):a,e=0;e<c;e++)if(e in d&&b.call(void 0,d[e],e,a)){c=e;break a}c=-1}return 0>c?null:n(a)?a.charAt(c):a[c]}function la(a){return w.concat.apply(w,arguments)}function ma(a,b,c){return 2>=arguments.length?w.slice.call(a,b):w.slice.call(a,b,c)};function na(a){var b=arguments.length;if(1==b&&"array"==aa(arguments[0]))return na.apply(null,arguments[0]);for(var c={},d=0;d<b;d++)c[arguments[d]]=!0;return c};var A;a:{var oa=m.navigator;if(oa){var pa=oa.userAgent;if(pa){A=pa;break a}}A=""};function B(){return v(A,"Edge")};var qa=v(A,"Opera")||v(A,"OPR"),C=v(A,"Edge")||v(A,"Trident")||v(A,"MSIE"),ra=v(A,"Gecko")&&!(v(A.toLowerCase(),"webkit")&&!B())&&!(v(A,"Trident")||v(A,"MSIE"))&&!B(),sa=v(A.toLowerCase(),"webkit")&&!B();function ta(){var a=A;if(ra)return/rv\:([^\);]+)(\)|;)/.exec(a);if(C&&B())return/Edge\/([\d\.]+)/.exec(a);if(C)return/\b(?:MSIE|rv)[: ]([^\);]+)(\)|;)/.exec(a);if(sa)return/WebKit\/(\S+)/.exec(a)}function ua(){var a=m.document;return a?a.documentMode:void 0}
var va=function(){if(qa&&m.opera){var a=m.opera.version;return"function"==aa(a)?a():a}var a="",b=ta();b&&(a=b?b[1]:"");return C&&!B()&&(b=ua(),b>parseFloat(a))?String(b):a}(),wa={};
function xa(a){if(!wa[a]){for(var b=0,c=fa(String(va)).split("."),d=fa(String(a)).split("."),e=Math.max(c.length,d.length),f=0;0==b&&f<e;f++){var g=c[f]||"",k=d[f]||"",p=RegExp("(\\d*)(\\D*)","g"),y=RegExp("(\\d*)(\\D*)","g");do{var F=p.exec(g)||["","",""],ca=y.exec(k)||["","",""];if(0==F[0].length&&0==ca[0].length)break;b=ga(0==F[1].length?0:parseInt(F[1],10),0==ca[1].length?0:parseInt(ca[1],10))||ga(0==F[2].length,0==ca[2].length)||ga(F[2],ca[2])}while(0==b)}wa[a]=0<=b}}
function ya(a){return C&&(B()||za>=a)}var Aa=m.document,Ba=ua(),za=!Aa||!C||!Ba&&B()?void 0:Ba||("CSS1Compat"==Aa.compatMode?parseInt(va,10):5);/*

 Copyright 2014 Software Freedom Conservancy

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
var D=C&&!ya(9),Ca=C&&!ya(8);/*

 Copyright 2014 Software Freedom Conservancy

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
function E(a,b,c,d){this.a=a;this.nodeName=c;this.nodeValue=d;this.nodeType=2;this.parentNode=this.ownerElement=b}function Da(a,b){var c=Ca&&"href"==b.nodeName?a.getAttribute(b.nodeName,2):b.nodeValue;return new E(b,a,b.nodeName,c)};/*

 Copyright 2014 Software Freedom Conservancy

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
function Ea(a){this.b=a;this.a=0}function Fa(a){a=a.match(Ga);for(var b=0;b<a.length;b++)Ha.test(a[b])&&a.splice(b,1);return new Ea(a)}var Ga=RegExp("\\$?(?:(?![0-9-])[\\w-]+:)?(?![0-9-])[\\w-]+|\\/\\/|\\.\\.|::|\\d+(?:\\.\\d*)?|\\.\\d+|\"[^\"]*\"|'[^']*'|[!<>]=|\\s+|.","g"),Ha=/^\s/;function G(a,b){return a.b[a.a+(b||0)]}function H(a){return a.b[a.a++]}function Ia(a){return a.b.length<=a.a};na("area base br col command embed hr img input keygen link meta param source track wbr".split(" "));!ra&&!C||C&&ya(9)||ra&&xa("1.9.1");C&&xa("9");function Ja(a,b){if(a.contains&&1==b.nodeType)return a==b||a.contains(b);if("undefined"!=typeof a.compareDocumentPosition)return a==b||Boolean(a.compareDocumentPosition(b)&16);for(;b&&a!=b;)b=b.parentNode;return b==a}
function Ka(a,b){if(a==b)return 0;if(a.compareDocumentPosition)return a.compareDocumentPosition(b)&2?1:-1;if(C&&!ya(9)){if(9==a.nodeType)return-1;if(9==b.nodeType)return 1}if("sourceIndex"in a||a.parentNode&&"sourceIndex"in a.parentNode){var c=1==a.nodeType,d=1==b.nodeType;if(c&&d)return a.sourceIndex-b.sourceIndex;var e=a.parentNode,f=b.parentNode;return e==f?La(a,b):!c&&Ja(e,b)?-1*Ma(a,b):!d&&Ja(f,a)?Ma(b,a):(c?a.sourceIndex:e.sourceIndex)-(d?b.sourceIndex:f.sourceIndex)}d=9==a.nodeType?a:a.ownerDocument||
a.document;c=d.createRange();c.selectNode(a);c.collapse(!0);d=d.createRange();d.selectNode(b);d.collapse(!0);return c.compareBoundaryPoints(m.Range.START_TO_END,d)}function Ma(a,b){var c=a.parentNode;if(c==b)return-1;for(var d=b;d.parentNode!=c;)d=d.parentNode;return La(d,a)}function La(a,b){for(var c=b;c=c.previousSibling;)if(c==a)return-1;return 1};/*

 Copyright 2014 Software Freedom Conservancy

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
function I(a){var b=null,c=a.nodeType;1==c&&(b=a.textContent,b=void 0==b||null==b?a.innerText:b,b=void 0==b||null==b?"":b);if("string"!=typeof b)if(D&&"title"==a.nodeName.toLowerCase()&&1==c)b=a.text;else if(9==c||1==c){a=9==c?a.documentElement:a.firstChild;for(var c=0,d=[],b="";a;){do 1!=a.nodeType&&(b+=a.nodeValue),D&&"title"==a.nodeName.toLowerCase()&&(b+=a.text),d[c++]=a;while(a=a.firstChild);for(;c&&!(a=d[--c].nextSibling););}}else b=a.nodeValue;return""+b}
function J(a,b,c){if(null===b)return!0;try{if(!a.getAttribute)return!1}catch(d){return!1}Ca&&"class"==b&&(b="className");return null==c?!!a.getAttribute(b):a.getAttribute(b,2)==c}function Na(a,b,c,d,e){return(D?Oa:Pa).call(null,a,b,n(c)?c:null,n(d)?d:null,e||new K)}
function Oa(a,b,c,d,e){if(a instanceof L||8==a.b||c&&null===a.b){var f=b.all;if(!f)return e;a=Qa(a);if("*"!=a&&(f=b.getElementsByTagName(a),!f))return e;if(c){for(var g=[],k=0;b=f[k++];)J(b,c,d)&&g.push(b);f=g}for(k=0;b=f[k++];)"*"==a&&"!"==b.tagName||M(e,b);return e}Ra(a,b,c,d,e);return e}
function Pa(a,b,c,d,e){b.getElementsByName&&d&&"name"==c&&!C?(b=b.getElementsByName(d),x(b,function(b){a.a(b)&&M(e,b)})):b.getElementsByClassName&&d&&"class"==c?(b=b.getElementsByClassName(d),x(b,function(b){b.className==d&&a.a(b)&&M(e,b)})):a instanceof N?Ra(a,b,c,d,e):b.getElementsByTagName&&(b=b.getElementsByTagName(a.d()),x(b,function(a){J(a,c,d)&&M(e,a)}));return e}
function Sa(a,b,c,d,e){var f;if((a instanceof L||8==a.b||c&&null===a.b)&&(f=b.childNodes)){var g=Qa(a);if("*"!=g&&(f=ia(f,function(a){return a.tagName&&a.tagName.toLowerCase()==g}),!f))return e;c&&(f=ia(f,function(a){return J(a,c,d)}));x(f,function(a){"*"==g&&("!"==a.tagName||"*"==g&&1!=a.nodeType)||M(e,a)});return e}return Ta(a,b,c,d,e)}function Ta(a,b,c,d,e){for(b=b.firstChild;b;b=b.nextSibling)J(b,c,d)&&a.a(b)&&M(e,b);return e}
function Ra(a,b,c,d,e){for(b=b.firstChild;b;b=b.nextSibling)J(b,c,d)&&a.a(b)&&M(e,b),Ra(a,b,c,d,e)}function Qa(a){if(a instanceof N){if(8==a.b)return"!";if(null===a.b)return"*"}return a.d()};/*

 Copyright 2014 Software Freedom Conservancy

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
function K(){this.b=this.a=null;this.i=0}function Ua(a){this.d=a;this.a=this.b=null}function Va(a,b){if(!a.a)return b;if(!b.a)return a;for(var c=a.a,d=b.a,e=null,f=null,g=0;c&&d;){var f=c.d,k=d.d;f==k||f instanceof E&&k instanceof E&&f.a==k.a?(f=c,c=c.a,d=d.a):0<Ka(c.d,d.d)?(f=d,d=d.a):(f=c,c=c.a);(f.b=e)?e.a=f:a.a=f;e=f;g++}for(f=c||d;f;)f.b=e,e=e.a=f,g++,f=f.a;a.b=e;a.i=g;return a}function Wa(a,b){var c=new Ua(b);c.a=a.a;a.b?a.a.b=c:a.a=a.b=c;a.a=c;a.i++}
function M(a,b){var c=new Ua(b);c.b=a.b;a.a?a.b.a=c:a.a=a.b=c;a.b=c;a.i++}function Xa(a){return(a=a.a)?a.d:null}function Ya(a){return(a=Xa(a))?I(a):""}function O(a,b){return new Za(a,!!b)}function Za(a,b){this.d=a;this.b=(this.c=b)?a.b:a.a;this.a=null}function P(a){var b=a.b;if(null==b)return null;var c=a.a=b;a.b=a.c?b.b:b.a;return c.d};/*

 Copyright 2014 Software Freedom Conservancy

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
function $a(a){switch(a.nodeType){case 1:return ea(ab,a);case 9:return $a(a.documentElement);case 11:case 10:case 6:case 12:return bb;default:return a.parentNode?$a(a.parentNode):bb}}function bb(){return null}function ab(a,b){if(a.prefix==b)return a.namespaceURI||"http://www.w3.org/1999/xhtml";var c=a.getAttributeNode("xmlns:"+b);return c&&c.specified?c.value||null:a.parentNode&&9!=a.parentNode.nodeType?ab(a.parentNode,b):null};/*

 Copyright 2014 Software Freedom Conservancy

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
/*

 Copyright 2014 Software Freedom Conservancy

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
function t(a){this.g=a;this.b=this.e=!1;this.d=null}function Q(a){return"\n  "+a.toString().split("\n").join("\n  ")}function cb(a,b){a.e=b}function db(a,b){a.b=b}function R(a,b){var c=a.a(b);return c instanceof K?+Ya(c):+c}function S(a,b){var c=a.a(b);return c instanceof K?Ya(c):""+c}function eb(a,b){var c=a.a(b);return c instanceof K?!!c.i:!!c};/*

 Copyright 2014 Software Freedom Conservancy

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
function fb(a,b,c){t.call(this,a.g);this.c=a;this.f=b;this.k=c;this.e=b.e||c.e;this.b=b.b||c.b;this.c==gb&&(c.b||c.e||4==c.g||0==c.g||!b.d?b.b||b.e||4==b.g||0==b.g||!c.d||(this.d={name:c.d.name,l:b}):this.d={name:b.d.name,l:c})}r(fb);
function hb(a,b,c,d,e){b=b.a(d);c=c.a(d);var f;if(b instanceof K&&c instanceof K){e=O(b);for(d=P(e);d;d=P(e))for(b=O(c),f=P(b);f;f=P(b))if(a(I(d),I(f)))return!0;return!1}if(b instanceof K||c instanceof K){b instanceof K?e=b:(e=c,c=b);e=O(e);b=typeof c;for(d=P(e);d;d=P(e)){switch(b){case "number":d=+I(d);break;case "boolean":d=!!I(d);break;case "string":d=I(d);break;default:throw Error("Illegal primitive type for comparison.");}if(a(d,c))return!0}return!1}return e?"boolean"==typeof b||"boolean"==typeof c?
a(!!b,!!c):"number"==typeof b||"number"==typeof c?a(+b,+c):a(b,c):a(+b,+c)}fb.prototype.a=function(a){return this.c.j(this.f,this.k,a)};fb.prototype.toString=function(){var a="Binary Expression: "+this.c,a=a+Q(this.f);return a+=Q(this.k)};function ib(a,b,c,d){this.a=a;this.p=b;this.g=c;this.j=d}ib.prototype.toString=h("a");var jb={};function T(a,b,c,d){if(jb.hasOwnProperty(a))throw Error("Binary operator already created: "+a);a=new ib(a,b,c,d);return jb[a.toString()]=a}
T("div",6,1,function(a,b,c){return R(a,c)/R(b,c)});T("mod",6,1,function(a,b,c){return R(a,c)%R(b,c)});T("*",6,1,function(a,b,c){return R(a,c)*R(b,c)});T("+",5,1,function(a,b,c){return R(a,c)+R(b,c)});T("-",5,1,function(a,b,c){return R(a,c)-R(b,c)});T("<",4,2,function(a,b,c){return hb(function(a,b){return a<b},a,b,c)});T(">",4,2,function(a,b,c){return hb(function(a,b){return a>b},a,b,c)});T("<=",4,2,function(a,b,c){return hb(function(a,b){return a<=b},a,b,c)});
T(">=",4,2,function(a,b,c){return hb(function(a,b){return a>=b},a,b,c)});var gb=T("=",3,2,function(a,b,c){return hb(function(a,b){return a==b},a,b,c,!0)});T("!=",3,2,function(a,b,c){return hb(function(a,b){return a!=b},a,b,c,!0)});T("and",2,2,function(a,b,c){return eb(a,c)&&eb(b,c)});T("or",1,2,function(a,b,c){return eb(a,c)||eb(b,c)});/*

 Copyright 2014 Software Freedom Conservancy

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
function kb(a,b){if(b.a.length&&4!=a.g)throw Error("Primary expression must evaluate to nodeset if filter has predicate(s).");t.call(this,a.g);this.c=a;this.f=b;this.e=a.e;this.b=a.b}r(kb);kb.prototype.a=function(a){a=this.c.a(a);return lb(this.f,a)};kb.prototype.toString=function(){var a;a="Filter:"+Q(this.c);return a+=Q(this.f)};/*

 Copyright 2014 Software Freedom Conservancy

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
function mb(a,b){if(b.length<a.o)throw Error("Function "+a.h+" expects at least"+a.o+" arguments, "+b.length+" given");if(null!==a.n&&b.length>a.n)throw Error("Function "+a.h+" expects at most "+a.n+" arguments, "+b.length+" given");a.s&&x(b,function(b,d){if(4!=b.g)throw Error("Argument "+d+" to function "+a.h+" is not of type Nodeset: "+b);});t.call(this,a.g);this.f=a;this.c=b;cb(this,a.e||ja(b,function(a){return a.e}));db(this,a.r&&!b.length||a.q&&!!b.length||ja(b,function(a){return a.b}))}r(mb);
mb.prototype.a=function(a){return this.f.j.apply(null,la(a,this.c))};mb.prototype.toString=function(){var a="Function: "+this.f;if(this.c.length)var b=z(this.c,function(a,b){return a+Q(b)},"Arguments:"),a=a+Q(b);return a};function nb(a,b,c,d,e,f,g,k,p){this.h=a;this.g=b;this.e=c;this.r=d;this.q=e;this.j=f;this.o=g;this.n=void 0!==k?k:g;this.s=!!p}nb.prototype.toString=h("h");var ob={};
function U(a,b,c,d,e,f,g,k){if(ob.hasOwnProperty(a))throw Error("Function already created: "+a+".");ob[a]=new nb(a,b,c,d,!1,e,f,g,k)}U("boolean",2,!1,!1,function(a,b){return eb(b,a)},1);U("ceiling",1,!1,!1,function(a,b){return Math.ceil(R(b,a))},1);U("concat",3,!1,!1,function(a,b){var c=ma(arguments,1);return z(c,function(b,c){return b+S(c,a)},"")},2,null);U("contains",2,!1,!1,function(a,b,c){return v(S(b,a),S(c,a))},2);U("count",1,!1,!1,function(a,b){return b.a(a).i},1,1,!0);
U("false",2,!1,!1,l(!1),0);U("floor",1,!1,!1,function(a,b){return Math.floor(R(b,a))},1);U("id",4,!1,!1,function(a,b){function c(a){if(D){var b=e.all[a];if(b){if(b.nodeType&&a==b.id)return b;if(b.length)return ka(b,function(b){return a==b.id})}return null}return e.getElementById(a)}var d=a.a,e=9==d.nodeType?d:d.ownerDocument,d=S(b,a).split(/\s+/),f=[];x(d,function(a){a=c(a);!a||0<=ha(f,a)||f.push(a)});f.sort(Ka);var g=new K;x(f,function(a){M(g,a)});return g},1);U("lang",2,!1,!1,l(!1),1);
U("last",1,!0,!1,function(a){if(1!=arguments.length)throw Error("Function last expects ()");return a.d},0);U("local-name",3,!1,!0,function(a,b){var c=b?Xa(b.a(a)):a.a;return c?c.localName||c.nodeName.toLowerCase():""},0,1,!0);U("name",3,!1,!0,function(a,b){var c=b?Xa(b.a(a)):a.a;return c?c.nodeName.toLowerCase():""},0,1,!0);U("namespace-uri",3,!0,!1,l(""),0,1,!0);U("normalize-space",3,!1,!0,function(a,b){return(b?S(b,a):I(a.a)).replace(/[\s\xa0]+/g," ").replace(/^\s+|\s+$/g,"")},0,1);
U("not",2,!1,!1,function(a,b){return!eb(b,a)},1);U("number",1,!1,!0,function(a,b){return b?R(b,a):+I(a.a)},0,1);U("position",1,!0,!1,function(a){return a.b},0);U("round",1,!1,!1,function(a,b){return Math.round(R(b,a))},1);U("starts-with",2,!1,!1,function(a,b,c){b=S(b,a);a=S(c,a);return 0==b.lastIndexOf(a,0)},2);U("string",3,!1,!0,function(a,b){return b?S(b,a):I(a.a)},0,1);U("string-length",1,!1,!0,function(a,b){return(b?S(b,a):I(a.a)).length},0,1);
U("substring",3,!1,!1,function(a,b,c,d){c=R(c,a);if(isNaN(c)||Infinity==c||-Infinity==c)return"";d=d?R(d,a):Infinity;if(isNaN(d)||-Infinity===d)return"";c=Math.round(c)-1;var e=Math.max(c,0);a=S(b,a);if(Infinity==d)return a.substring(e);b=Math.round(d);return a.substring(e,c+b)},2,3);U("substring-after",3,!1,!1,function(a,b,c){b=S(b,a);a=S(c,a);c=b.indexOf(a);return-1==c?"":b.substring(c+a.length)},2);
U("substring-before",3,!1,!1,function(a,b,c){b=S(b,a);a=S(c,a);a=b.indexOf(a);return-1==a?"":b.substring(0,a)},2);U("sum",1,!1,!1,function(a,b){for(var c=O(b.a(a)),d=0,e=P(c);e;e=P(c))d+=+I(e);return d},1,1,!0);U("translate",3,!1,!1,function(a,b,c,d){b=S(b,a);c=S(c,a);var e=S(d,a);a=[];for(d=0;d<c.length;d++){var f=c.charAt(d);f in a||(a[f]=e.charAt(d))}c="";for(d=0;d<b.length;d++)f=b.charAt(d),c+=f in a?a[f]:f;return c},3);U("true",2,!1,!1,l(!0),0);/*

 Copyright 2014 Software Freedom Conservancy

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
/*

 Copyright 2014 Software Freedom Conservancy

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
function N(a,b){this.f=a;this.c=void 0!==b?b:null;this.b=null;switch(a){case "comment":this.b=8;break;case "text":this.b=3;break;case "processing-instruction":this.b=7;break;case "node":break;default:throw Error("Unexpected argument");}}function pb(a){return"comment"==a||"text"==a||"processing-instruction"==a||"node"==a}N.prototype.a=function(a){return null===this.b||this.b==a.nodeType};N.prototype.d=h("f");N.prototype.toString=function(){var a="Kind Test: "+this.f;null===this.c||(a+=Q(this.c));return a};/*

 Copyright 2014 Software Freedom Conservancy

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
function qb(a){t.call(this,3);this.c=a.substring(1,a.length-1)}r(qb);qb.prototype.a=h("c");qb.prototype.toString=function(){return"Literal: "+this.c};/*

 Copyright 2014 Software Freedom Conservancy

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
function L(a,b){this.h=a.toLowerCase();this.c=b?b.toLowerCase():"http://www.w3.org/1999/xhtml"}L.prototype.a=function(a){var b=a.nodeType;return 1!=b&&2!=b?!1:"*"!=this.h&&this.h!=a.nodeName.toLowerCase()?!1:this.c==(a.namespaceURI?a.namespaceURI.toLowerCase():"http://www.w3.org/1999/xhtml")};L.prototype.d=h("h");L.prototype.toString=function(){return"Name Test: "+("http://www.w3.org/1999/xhtml"==this.c?"":this.c+":")+this.h};/*

 Copyright 2014 Software Freedom Conservancy

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
function rb(a){t.call(this,1);this.c=a}r(rb);rb.prototype.a=h("c");rb.prototype.toString=function(){return"Number: "+this.c};/*

 Copyright 2014 Software Freedom Conservancy

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
function sb(a,b){t.call(this,a.g);this.f=a;this.c=b;this.e=a.e;this.b=a.b;if(1==this.c.length){var c=this.c[0];c.m||c.c!=tb||(c=c.k,"*"!=c.d()&&(this.d={name:c.d(),l:null}))}}r(sb);function ub(){t.call(this,4)}r(ub);ub.prototype.a=function(a){var b=new K;a=a.a;9==a.nodeType?M(b,a):M(b,a.ownerDocument);return b};ub.prototype.toString=l("Root Helper Expression");function vb(){t.call(this,4)}r(vb);vb.prototype.a=function(a){var b=new K;M(b,a.a);return b};vb.prototype.toString=l("Context Helper Expression");
function wb(a){return"/"==a||"//"==a}sb.prototype.a=function(a){var b=this.f.a(a);if(!(b instanceof K))throw Error("Filter expression must evaluate to nodeset.");a=this.c;for(var c=0,d=a.length;c<d&&b.i;c++){var e=a[c],f=O(b,e.c.a),g;if(e.e||e.c!=xb)if(e.e||e.c!=yb)for(g=P(f),b=e.a(new u(g));null!=(g=P(f));)g=e.a(new u(g)),b=Va(b,g);else g=P(f),b=e.a(new u(g));else{for(g=P(f);(b=P(f))&&(!g.contains||g.contains(b))&&b.compareDocumentPosition(g)&8;g=b);b=e.a(new u(g))}}return b};
sb.prototype.toString=function(){var a;a="Path Expression:"+Q(this.f);if(this.c.length){var b=z(this.c,function(a,b){return a+Q(b)},"Steps:");a+=Q(b)}return a};/*

 Copyright 2014 Software Freedom Conservancy

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
function zb(a,b){this.a=a;this.b=!!b}
function lb(a,b,c){for(c=c||0;c<a.a.length;c++)for(var d=a.a[c],e=O(b),f=b.i,g,k=0;g=P(e);k++){var p=a.b?f-k:k+1;g=d.a(new u(g,p,f));if("number"==typeof g)p=p==g;else if("string"==typeof g||"boolean"==typeof g)p=!!g;else if(g instanceof K)p=0<g.i;else throw Error("Predicate.evaluate returned an unexpected type.");if(!p){p=e;g=p.d;var y=p.a;if(!y)throw Error("Next must be called at least once before remove.");var F=y.b,y=y.a;F?F.a=y:g.a=y;y?y.b=F:g.b=F;g.i--;p.a=null}}return b}
zb.prototype.toString=function(){return z(this.a,function(a,b){return a+Q(b)},"Predicates:")};/*

 Copyright 2014 Software Freedom Conservancy

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
function V(a,b,c,d){t.call(this,4);this.c=a;this.k=b;this.f=c||new zb([]);this.m=!!d;b=this.f;b=0<b.a.length?b.a[0].d:null;a.b&&b&&(a=b.name,a=D?a.toLowerCase():a,this.d={name:a,l:b.l});a:{a=this.f;for(b=0;b<a.a.length;b++)if(c=a.a[b],c.e||1==c.g||0==c.g){a=!0;break a}a=!1}this.e=a}r(V);
V.prototype.a=function(a){var b=a.a,c=null,c=this.d,d=null,e=null,f=0;c&&(d=c.name,e=c.l?S(c.l,a):null,f=1);if(this.m)if(this.e||this.c!=Ab)if(a=O((new V(Bb,new N("node"))).a(a)),b=P(a))for(c=this.j(b,d,e,f);null!=(b=P(a));)c=Va(c,this.j(b,d,e,f));else c=new K;else c=Na(this.k,b,d,e),c=lb(this.f,c,f);else c=this.j(a.a,d,e,f);return c};V.prototype.j=function(a,b,c,d){a=this.c.d(this.k,a,b,c);return a=lb(this.f,a,d)};
V.prototype.toString=function(){var a;a="Step:"+Q("Operator: "+(this.m?"//":"/"));this.c.h&&(a+=Q("Axis: "+this.c));a+=Q(this.k);if(this.f.a.length){var b=z(this.f.a,function(a,b){return a+Q(b)},"Predicates:");a+=Q(b)}return a};function Cb(a,b,c,d){this.h=a;this.d=b;this.a=c;this.b=d}Cb.prototype.toString=h("h");var Db={};function W(a,b,c,d){if(Db.hasOwnProperty(a))throw Error("Axis already created: "+a);b=new Cb(a,b,c,!!d);return Db[a]=b}
W("ancestor",function(a,b){for(var c=new K,d=b;d=d.parentNode;)a.a(d)&&Wa(c,d);return c},!0);W("ancestor-or-self",function(a,b){var c=new K,d=b;do a.a(d)&&Wa(c,d);while(d=d.parentNode);return c},!0);
var tb=W("attribute",function(a,b){var c=new K,d=a.d();if("style"==d&&b.style&&D)return M(c,new E(b.style,b,"style",b.style.cssText)),c;var e=b.attributes;if(e)if(a instanceof N&&null===a.b||"*"==d)for(var d=0,f;f=e[d];d++)D?f.nodeValue&&M(c,Da(b,f)):M(c,f);else(f=e.getNamedItem(d))&&(D?f.nodeValue&&M(c,Da(b,f)):M(c,f));return c},!1),Ab=W("child",function(a,b,c,d,e){return(D?Sa:Ta).call(null,a,b,n(c)?c:null,n(d)?d:null,e||new K)},!1,!0);W("descendant",Na,!1,!0);
var Bb=W("descendant-or-self",function(a,b,c,d){var e=new K;J(b,c,d)&&a.a(b)&&M(e,b);return Na(a,b,c,d,e)},!1,!0),xb=W("following",function(a,b,c,d){var e=new K;do for(var f=b;f=f.nextSibling;)J(f,c,d)&&a.a(f)&&M(e,f),e=Na(a,f,c,d,e);while(b=b.parentNode);return e},!1,!0);W("following-sibling",function(a,b){for(var c=new K,d=b;d=d.nextSibling;)a.a(d)&&M(c,d);return c},!1);W("namespace",function(){return new K},!1);
var Eb=W("parent",function(a,b){var c=new K;if(9==b.nodeType)return c;if(2==b.nodeType)return M(c,b.ownerElement),c;var d=b.parentNode;a.a(d)&&M(c,d);return c},!1),yb=W("preceding",function(a,b,c,d){var e=new K,f=[];do f.unshift(b);while(b=b.parentNode);for(var g=1,k=f.length;g<k;g++){var p=[];for(b=f[g];b=b.previousSibling;)p.unshift(b);for(var y=0,F=p.length;y<F;y++)b=p[y],J(b,c,d)&&a.a(b)&&M(e,b),e=Na(a,b,c,d,e)}return e},!0,!0);
W("preceding-sibling",function(a,b){for(var c=new K,d=b;d=d.previousSibling;)a.a(d)&&Wa(c,d);return c},!0);var Fb=W("self",function(a,b){var c=new K;a.a(b)&&M(c,b);return c},!1);/*

 Copyright 2014 Software Freedom Conservancy

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
function Gb(a){t.call(this,1);this.c=a;this.e=a.e;this.b=a.b}r(Gb);Gb.prototype.a=function(a){return-R(this.c,a)};Gb.prototype.toString=function(){return"Unary Expression: -"+Q(this.c)};/*

 Copyright 2014 Software Freedom Conservancy

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
function Hb(a){t.call(this,4);this.c=a;cb(this,ja(this.c,function(a){return a.e}));db(this,ja(this.c,function(a){return a.b}))}r(Hb);Hb.prototype.a=function(a){var b=new K;x(this.c,function(c){c=c.a(a);if(!(c instanceof K))throw Error("Path expression must evaluate to NodeSet.");b=Va(b,c)});return b};Hb.prototype.toString=function(){return z(this.c,function(a,b){return a+Q(b)},"Union Expression:")};/*

 Copyright 2014 Software Freedom Conservancy

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
function Ib(a,b){this.a=a;this.b=b}function Jb(a){for(var b,c=[];;){X(a,"Missing right hand side of binary expression.");b=Kb(a);var d=H(a.a);if(!d)break;var e=(d=jb[d]||null)&&d.p;if(!e){a.a.a--;break}for(;c.length&&e<=c[c.length-1].p;)b=new fb(c.pop(),c.pop(),b);c.push(b,d)}for(;c.length;)b=new fb(c.pop(),c.pop(),b);return b}function X(a,b){if(Ia(a.a))throw Error(b);}function Lb(a,b){var c=H(a.a);if(c!=b)throw Error("Bad token, expected: "+b+" got: "+c);}
function Mb(a){a=H(a.a);if(")"!=a)throw Error("Bad token: "+a);}function Nb(a){a=H(a.a);if(2>a.length)throw Error("Unclosed literal string");return new qb(a)}function Ob(a){var b=H(a.a),c=b.indexOf(":");if(-1==c)return new L(b);var d=b.substring(0,c);a=a.b(d);if(!a)throw Error("Namespace prefix not declared: "+d);b=b.substr(c+1);return new L(b,a)}
function Pb(a){var b,c=[],d;if(wb(G(a.a))){b=H(a.a);d=G(a.a);if("/"==b&&(Ia(a.a)||"."!=d&&".."!=d&&"@"!=d&&"*"!=d&&!/(?![0-9])[\w]/.test(d)))return new ub;d=new ub;X(a,"Missing next location step.");b=Qb(a,b);c.push(b)}else{a:{b=G(a.a);d=b.charAt(0);switch(d){case "$":throw Error("Variable reference not allowed in HTML XPath");case "(":H(a.a);b=Jb(a);X(a,'unclosed "("');Lb(a,")");break;case '"':case "'":b=Nb(a);break;default:if(isNaN(+b))if(!pb(b)&&/(?![0-9])[\w]/.test(d)&&"("==G(a.a,1)){b=H(a.a);
b=ob[b]||null;H(a.a);for(d=[];")"!=G(a.a);){X(a,"Missing function argument list.");d.push(Jb(a));if(","!=G(a.a))break;H(a.a)}X(a,"Unclosed function argument list.");Mb(a);b=new mb(b,d)}else{b=null;break a}else b=new rb(+H(a.a))}"["==G(a.a)&&(d=new zb(Rb(a)),b=new kb(b,d))}if(b)if(wb(G(a.a)))d=b;else return b;else b=Qb(a,"/"),d=new vb,c.push(b)}for(;wb(G(a.a));)b=H(a.a),X(a,"Missing next location step."),b=Qb(a,b),c.push(b);return new sb(d,c)}
function Qb(a,b){var c,d,e;if("/"!=b&&"//"!=b)throw Error('Step op should be "/" or "//"');if("."==G(a.a))return d=new V(Fb,new N("node")),H(a.a),d;if(".."==G(a.a))return d=new V(Eb,new N("node")),H(a.a),d;var f;if("@"==G(a.a))f=tb,H(a.a),X(a,"Missing attribute name");else if("::"==G(a.a,1)){if(!/(?![0-9])[\w]/.test(G(a.a).charAt(0)))throw Error("Bad token: "+H(a.a));c=H(a.a);f=Db[c]||null;if(!f)throw Error("No axis with name: "+c);H(a.a);X(a,"Missing node name")}else f=Ab;c=G(a.a);if(/(?![0-9])[\w]/.test(c.charAt(0)))if("("==
G(a.a,1)){if(!pb(c))throw Error("Invalid node type: "+c);c=H(a.a);if(!pb(c))throw Error("Invalid type name: "+c);Lb(a,"(");X(a,"Bad nodetype");e=G(a.a).charAt(0);var g=null;if('"'==e||"'"==e)g=Nb(a);X(a,"Bad nodetype");Mb(a);c=new N(c,g)}else c=Ob(a);else if("*"==c)c=Ob(a);else throw Error("Bad token: "+H(a.a));e=new zb(Rb(a),f.a);return d||new V(f,c,e,"//"==b)}
function Rb(a){for(var b=[];"["==G(a.a);){H(a.a);X(a,"Missing predicate expression.");var c=Jb(a);b.push(c);X(a,"Unclosed predicate expression.");Lb(a,"]")}return b}function Kb(a){if("-"==G(a.a))return H(a.a),new Gb(Kb(a));var b=Pb(a);if("|"!=G(a.a))a=b;else{for(b=[b];"|"==H(a.a);)X(a,"Missing next union location path."),b.push(Pb(a));a.a.a--;a=new Hb(b)}return a};/*

 Copyright 2014 Software Freedom Conservancy

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
function Sb(a,b){if(!a.length)throw Error("Empty XPath expression.");var c=Fa(a);if(Ia(c))throw Error("Invalid XPath expression.");b?"function"==aa(b)||(b=q(b.lookupNamespaceURI,b)):b=l(null);var d=Jb(new Ib(c,b));if(!Ia(c))throw Error("Bad token: "+H(c));this.evaluate=function(a,b){var c=d.a(new u(a));return new Y(c,b)}}
function Y(a,b){if(0==b)if(a instanceof K)b=4;else if("string"==typeof a)b=2;else if("number"==typeof a)b=1;else if("boolean"==typeof a)b=3;else throw Error("Unexpected evaluation result.");if(2!=b&&1!=b&&3!=b&&!(a instanceof K))throw Error("value could not be converted to the specified type");this.resultType=b;var c;switch(b){case 2:this.stringValue=a instanceof K?Ya(a):""+a;break;case 1:this.numberValue=a instanceof K?+Ya(a):+a;break;case 3:this.booleanValue=a instanceof K?0<a.i:!!a;break;case 4:case 5:case 6:case 7:var d=
O(a);c=[];for(var e=P(d);e;e=P(d))c.push(e instanceof E?e.a:e);this.snapshotLength=a.i;this.invalidIteratorState=!1;break;case 8:case 9:d=Xa(a);this.singleNodeValue=d instanceof E?d.a:d;break;default:throw Error("Unknown XPathResult type.");}var f=0;this.iterateNext=function(){if(4!=b&&5!=b)throw Error("iterateNext called with wrong result type");return f>=c.length?null:c[f++]};this.snapshotItem=function(a){if(6!=b&&7!=b)throw Error("snapshotItem called with wrong result type");return a>=c.length||
0>a?null:c[a]}}Y.ANY_TYPE=0;Y.NUMBER_TYPE=1;Y.STRING_TYPE=2;Y.BOOLEAN_TYPE=3;Y.UNORDERED_NODE_ITERATOR_TYPE=4;Y.ORDERED_NODE_ITERATOR_TYPE=5;Y.UNORDERED_NODE_SNAPSHOT_TYPE=6;Y.ORDERED_NODE_SNAPSHOT_TYPE=7;Y.ANY_UNORDERED_NODE_TYPE=8;Y.FIRST_ORDERED_NODE_TYPE=9;function Tb(a){this.lookupNamespaceURI=$a(a)}
function Ub(a){a=a||m;var b=a.document;b.evaluate||(a.XPathResult=Y,b.evaluate=function(a,b,e,f){return(new Sb(a,e)).evaluate(b,f)},b.createExpression=function(a,b){return new Sb(a,b)},b.createNSResolver=function(a){return new Tb(a)})}var Vb=["wgxpath","install"],Z=m;Vb[0]in Z||!Z.execScript||Z.execScript("var "+Vb[0]);for(var Wb;Vb.length&&(Wb=Vb.shift());)Vb.length||void 0===Ub?Z[Wb]?Z=Z[Wb]:Z=Z[Wb]={}:Z[Wb]=Ub;})()

// Export for Node.js.
module.exports.install = wgxpath.install;
module.exports.XPathResultType = {
  ANY_TYPE: 0,
  NUMBER_TYPE: 1,
  STRING_TYPE: 2,
  BOOLEAN_TYPE: 3,
  UNORDERED_NODE_ITERATOR_TYPE: 4,
  ORDERED_NODE_ITERATOR_TYPE: 5,
  UNORDERED_NODE_SNAPSHOT_TYPE: 6,
  ORDERED_NODE_SNAPSHOT_TYPE: 7,
  ANY_UNORDERED_NODE_TYPE: 8,
  FIRST_ORDERED_NODE_TYPE: 9
};

