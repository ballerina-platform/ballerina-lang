/**
 * Copyright 2015 Tim Down.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


var log4javascript_stub=(function(){var log4javascript;function ff(){return function(){};}
function copy(obj,props){for(var i in props){obj[i]=props[i];}}
var f=ff();var Logger=ff();copy(Logger.prototype,{setLevel:f,getLevel:f,trace:f,debug:f,info:f,warn:f,error:f,fatal:f,isEnabledFor:f,isTraceEnabled:f,isDebugEnabled:f,isInfoEnabled:f,isWarnEnabled:f,isErrorEnabled:f,isFatalEnabled:f});var getLogger=function(){return new Logger();};function Log4JavaScript(){}
log4javascript=new Log4JavaScript();log4javascript={isStub:true,version:"1.4.13",edition:"log4javascript_lite",setEnabled:f,isEnabled:f,setShowStackTraces:f,getDefaultLogger:getLogger,getLogger:getLogger,getNullLogger:getLogger,Level:ff(),LoggingEvent:ff(),Appender:ff()};log4javascript.LoggingEvent.prototype={getThrowableStrRep:f,getCombinedMessages:f};log4javascript.Level.prototype={toString:f,equals:f,isGreaterOrEqual:f};var level=new log4javascript.Level();copy(log4javascript.Level,{ALL:level,TRACE:level,DEBUG:level,INFO:level,WARN:level,ERROR:level,FATAL:level,OFF:level});log4javascript.Appender.prototype.append=f;return log4javascript;})();if(typeof window.log4javascript=="undefined"){var log4javascript=log4javascript_stub;}
