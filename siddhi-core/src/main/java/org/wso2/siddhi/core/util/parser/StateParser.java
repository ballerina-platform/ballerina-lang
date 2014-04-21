/*
*  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.siddhi.core.util.parser;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.query.processor.handler.HandlerProcessor;
import org.wso2.siddhi.core.query.processor.handler.InnerHandlerProcessor;
import org.wso2.siddhi.core.query.processor.handler.pattern.PatternHandlerProcessor;
import org.wso2.siddhi.core.query.processor.handler.sequence.SequenceHandlerProcessor;
import org.wso2.siddhi.core.query.statemachine.State;
import org.wso2.siddhi.core.query.statemachine.pattern.AndPatternState;
import org.wso2.siddhi.core.query.statemachine.pattern.CountPatternState;
import org.wso2.siddhi.core.query.statemachine.pattern.LogicPatternState;
import org.wso2.siddhi.core.query.statemachine.pattern.OrPatternState;
import org.wso2.siddhi.core.query.statemachine.pattern.PatternState;
import org.wso2.siddhi.core.query.statemachine.sequence.CountSequenceState;
import org.wso2.siddhi.core.query.statemachine.sequence.OrSequenceState;
import org.wso2.siddhi.core.query.statemachine.sequence.SequenceState;
import org.wso2.siddhi.core.util.statemachine.AndUtilState;
import org.wso2.siddhi.core.util.statemachine.CountUtilState;
import org.wso2.siddhi.core.util.statemachine.LogicUtilState;
import org.wso2.siddhi.core.util.statemachine.OrUtilState;
import org.wso2.siddhi.core.util.statemachine.StateNumber;
import org.wso2.siddhi.core.util.statemachine.UtilState;
import org.wso2.siddhi.query.api.condition.ConditionValidator;
import org.wso2.siddhi.query.api.query.input.BasicStream;
import org.wso2.siddhi.query.api.query.input.handler.Filter;
import org.wso2.siddhi.query.api.query.input.pattern.PatternStream;
import org.wso2.siddhi.query.api.query.input.pattern.element.CountElement;
import org.wso2.siddhi.query.api.query.input.pattern.element.FollowedByElement;
import org.wso2.siddhi.query.api.query.input.pattern.element.LogicalElement;
import org.wso2.siddhi.query.api.query.input.pattern.element.PatternElement;
import org.wso2.siddhi.query.api.query.input.sequence.element.NextElement;
import org.wso2.siddhi.query.api.query.input.sequence.element.OrElement;
import org.wso2.siddhi.query.api.query.input.sequence.element.RegexElement;
import org.wso2.siddhi.query.api.query.input.sequence.element.SequenceElement;
import org.wso2.siddhi.query.api.query.selection.Selector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StateParser {
    static final Logger log = Logger.getLogger(StateParser.class);

    //Pattern

    public static List<PatternState> convertToPatternStateList(List<UtilState> stateList) {
        List<PatternState> patternStateList = new ArrayList<PatternState>(stateList.size());
        for (int i = 0, stateListSize = stateList.size(); i < stateListSize; i++) {
            UtilState state = stateList.get(i);
            PatternState patternState;
            if (state instanceof AndUtilState) {
                patternState = (new AndPatternState(state.getStateNumber(), state.getTransformedStream()));
            } else if (state instanceof OrUtilState) {
                patternState = (new OrPatternState(state.getStateNumber(), state.getTransformedStream()));
            } else if (state instanceof CountUtilState) {
                patternState = (new CountPatternState(state.getStateNumber(), state.getTransformedStream(), ((CountUtilState) state).getMin(), ((CountUtilState) state).getMax()));
            } else {
                patternState = (new PatternState(state.getStateNumber(), state.getTransformedStream()));
            }
            patternState.setStateNumber(i);
            patternState.setFirst(state.isFirst());
            patternState.setLast(state.isLast());
            patternStateList.add(patternState);
        }
        for (int i = 0, stateListSize = stateList.size(); i < stateListSize; i++) {
            UtilState state = stateList.get(i);
            PatternState patternState = patternStateList.get(i);
            if (state.getNext() != -1) {
                patternState.setNextState(patternStateList.get(state.getNext()));
            }
            if (state.getNextEvery() != -1) {
                patternState.setNextEveryState(patternStateList.get(state.getNextEvery()));
            }
            if (state instanceof LogicUtilState) {
                ((LogicPatternState) patternStateList.get(i)).setPartnerState(patternStateList.get(((LogicUtilState) state).getPartnerNumber()));
            }
        }
        return patternStateList;
    }


    public static List<UtilState> identifyStates(PatternElement patternElement) {
        List<UtilState> stateList = identifyStates(patternElement, new ArrayList<UtilState>(), new StateNumber(0), new ArrayList<Integer>(), true);
        //setting first state
        UtilState firstState = stateList.get(0);
        firstState.setFirst(true);
        if (firstState instanceof LogicUtilState) {
            stateList.get(((LogicUtilState) firstState).getPartnerNumber()).setFirst(true);
        }
        //setting last state
        UtilState lastState = stateList.get(stateList.size() - 1);
        lastState.setLast(true);
        if (lastState instanceof LogicUtilState) {
            stateList.get(((LogicUtilState) lastState).getPartnerNumber()).setLast(true);
        }


        return stateList;
    }


    private static List<UtilState> identifyStates(PatternElement patternElement, List<UtilState> stateList,
                                                  StateNumber stateNumber, List<Integer> perv,
                                                  boolean topLevel) {

        if (patternElement instanceof BasicStream) {
            stateList.add(new UtilState(stateNumber.getNumber(), ((BasicStream) patternElement)));
            addStateAsNext(stateList, stateNumber, perv);
            stateNumber.increment();
            perv.clear();
            perv.add(stateNumber.getNumber() - 1);
        } else if (patternElement instanceof LogicalElement) {
            if (((LogicalElement) patternElement).getType() == LogicalElement.Type.OR) {
                stateList.add(new OrUtilState(stateNumber.getNumber(), ((LogicalElement) patternElement).getTransformedStream1(), stateNumber.getNumber() + 1));
                addStateAsNext(stateList, stateNumber, perv);
                stateNumber.increment();
                stateList.add(new OrUtilState(stateNumber.getNumber(), ((LogicalElement) patternElement).getTransformedStream2(), stateNumber.getNumber() - 1));
                //addStateAsNext(stateList, stateNumber, perv);
                stateNumber.increment();
            } else {//AND
                stateList.add(new AndUtilState(stateNumber.getNumber(), ((LogicalElement) patternElement).getTransformedStream1(), stateNumber.getNumber() + 1));
                addStateAsNext(stateList, stateNumber, perv);
                stateNumber.increment();
                stateList.add(new AndUtilState(stateNumber.getNumber(), ((LogicalElement) patternElement).getTransformedStream2(), stateNumber.getNumber() - 1));
                //addStateAsNext(stateList, stateNumber, perv);
                stateNumber.increment();
            }
            perv.clear();
            perv.add(stateNumber.getNumber() - 1);
            perv.add(stateNumber.getNumber() - 2);
        } else if (patternElement instanceof CountElement) {
            stateList.add(new CountUtilState(stateNumber.getNumber(), ((CountElement) patternElement).getTransformedStream(), ((CountElement) patternElement).getMinCount(), ((CountElement) patternElement).getMaxCount()));
            addStateAsNext(stateList, stateNumber, perv);
            stateNumber.increment();
            perv.clear();
            perv.add(stateNumber.getNumber() - 1);
        } else if (patternElement instanceof FollowedByElement) {
            identifyStates(((FollowedByElement) patternElement).getPatternElement(), stateList, stateNumber, perv, topLevel);
            identifyStates(((FollowedByElement) patternElement).getFollowedByPatternElement(), stateList, stateNumber, perv, topLevel);
        } else if (patternElement instanceof PatternStream) {
            int firstEveryStateNumber = stateNumber.getNumber();
            if (!topLevel) {
                log.error("Every inside Every is not allowed !!");
            }
            identifyStates(((PatternStream) patternElement).getPatternElement(), stateList, stateNumber, perv, false);

            UtilState lastState = stateList.get(stateList.size() - 1);
            UtilState lastStatePartner = null;
            if (lastState instanceof LogicUtilState) {
                lastStatePartner = stateList.get(((LogicUtilState) lastState).getPartnerNumber());
            }
//            if (stateList.get(firstEveryStateNumber) instanceof LogicState) {
//                lastState.setNextEvery(firstEveryStateNumber);
////                lastState.addNextEvery(firstEveryStateNumber + 1);
//                if (lastStatePartner != null) {
//                    lastStatePartner.setNextEvery(firstEveryStateNumber);
////                    lastStatePartner.addNext(firstEveryStateNumber + 1);
//                }
//            } else {
            lastState.setNextEvery(firstEveryStateNumber);
            if (lastStatePartner != null) {
                lastStatePartner.setNextEvery(firstEveryStateNumber);
            }
//            }
        } else {
            log.error("Error! " + patternElement);
        }

        return stateList;
    }

    //Sequence

    public static List<SequenceState> convertToSequenceStateList(List<UtilState> stateList) {
        List<SequenceState> sequenceStateList = new ArrayList<SequenceState>(stateList.size());
        for (int i = 0, stateListSize = stateList.size(); i < stateListSize; i++) {
            UtilState state = stateList.get(i);
            SequenceState sequenceState;
            if (state instanceof OrUtilState) {
                sequenceState = (new OrSequenceState(state.getStateNumber(), state.getTransformedStream()));
            } else if (state instanceof CountUtilState) {
                sequenceState = (new CountSequenceState(state.getStateNumber(), state.getTransformedStream(), ((CountUtilState) state).getMin(), ((CountUtilState) state).getMax()));
            } else {
                sequenceState = (new SequenceState(state.getStateNumber(), state.getTransformedStream()));
            }
            sequenceState.setStateNumber(i);
            sequenceState.setFirst(state.isFirst());
            sequenceState.setLast(state.isLast());
            sequenceStateList.add(sequenceState);
        }
        for (int i = 0, stateListSize = stateList.size(); i < stateListSize; i++) {
            UtilState state = stateList.get(i);
            SequenceState sequenceState = sequenceStateList.get(i);
            if (state.getNext() != -1) {
                sequenceState.setNextState(sequenceStateList.get(state.getNext()));
            }

            if (state instanceof OrUtilState) {
                ((OrSequenceState) sequenceStateList.get(i)).setPartnerState(sequenceStateList.get(((LogicUtilState) state).getPartnerNumber()));
            }
        }
        return sequenceStateList;
    }


    public static List<UtilState> identifyStates(SequenceElement sequenceElement) {
        List<UtilState> stateList = identifyStates(sequenceElement, new ArrayList<UtilState>(), new StateNumber(0), new ArrayList<Integer>());
        //setting first state
        UtilState firstState = stateList.get(0);
        firstState.setFirst(true);
        if (firstState instanceof LogicUtilState) {
            stateList.get(((LogicUtilState) firstState).getPartnerNumber()).setFirst(true);
        }
        //setting last state
        UtilState lastState = stateList.get(stateList.size() - 1);
        lastState.setLast(true);
        if (lastState instanceof LogicUtilState) {
            stateList.get(((LogicUtilState) lastState).getPartnerNumber()).setLast(true);
        }
        return stateList;
    }


    private static List<UtilState> identifyStates(SequenceElement sequenceElement,
                                                  List<UtilState> stateList,
                                                  StateNumber stateNumber, List<Integer> perv) {

        if (sequenceElement instanceof BasicStream) {
            stateList.add(new UtilState(stateNumber.getNumber(), ((BasicStream) sequenceElement)));
            addStateAsNext(stateList, stateNumber, perv);
            stateNumber.increment();
            perv.clear();
            perv.add(stateNumber.getNumber() - 1);
        } else if (sequenceElement instanceof OrElement) {
            stateList.add(new OrUtilState(stateNumber.getNumber(), ((OrElement) sequenceElement).getTransformedStream1(), stateNumber.getNumber() + 1));
            addStateAsNext(stateList, stateNumber, perv);
            stateNumber.increment();
            stateList.add(new OrUtilState(stateNumber.getNumber(), ((OrElement) sequenceElement).getTransformedStream2(), stateNumber.getNumber() - 1));
            //addStateAsNext(stateList, stateNumber, perv);
            stateNumber.increment();

            perv.clear();
            perv.add(stateNumber.getNumber() - 1);
            perv.add(stateNumber.getNumber() - 2);
        } else if (sequenceElement instanceof RegexElement) {
            stateList.add(new CountUtilState(stateNumber.getNumber(), ((RegexElement) sequenceElement).getTransformedStream(), ((RegexElement) sequenceElement).getMinCount(), ((RegexElement) sequenceElement).getMaxCount()));
            addStateAsNext(stateList, stateNumber, perv);
            stateNumber.increment();
            perv.clear();
            perv.add(stateNumber.getNumber() - 1);
        } else if (sequenceElement instanceof NextElement) {
            identifyStates(((NextElement) sequenceElement).getSequenceElement(), stateList, stateNumber, perv);
            identifyStates(((NextElement) sequenceElement).getNextSequenceElement(), stateList, stateNumber, perv);
        } else {
            log.error("Error! " + sequenceElement);
        }

        return stateList;
    }


    public static void populateMemoryOptimizationParameters(List<? extends State> stateList, Selector selector,
                                                            List<HandlerProcessor> handlerProcessorList) {

        Set<String> dependencyVarSet = selector.getDependencySet();
        Set<String> dropCandidateSet = new HashSet<String>();

        Map<String, Integer> referenceSourceIdMap = new HashMap<String, Integer>();

        for (int i = stateList.size() - 1; i >= 0; i--) {
            referenceSourceIdMap.put(stateList.get(i).getTransformedStream().getStreamReferenceId(), i);
        }

        // setup the candidates to be dropped
        dropCandidateSet.addAll(referenceSourceIdMap.keySet());
        dropCandidateSet.removeAll(dependencyVarSet);

        for (HandlerProcessor handlerProcessor : handlerProcessorList) {

            // process input streams
            for (int i = stateList.size() - 1; i >= 0; i--) {
                InnerHandlerProcessor innerHandlerProcessor;
                if (handlerProcessor instanceof SequenceHandlerProcessor) {
                    innerHandlerProcessor = findInnerHandlerProcessorForState(((SequenceHandlerProcessor) handlerProcessor).getSequenceInnerHandlerProcessorList(), i);
                } else {
                    innerHandlerProcessor = findInnerHandlerProcessorForState(((PatternHandlerProcessor) handlerProcessor).getPatternInnerHandlerProcessorList(), i);

                }
                if (innerHandlerProcessor == null) {
                    continue;
                }

                Filter filter = stateList.get(i).getTransformedStream().getFilter();
                if (filter == null) {
                    continue;
                }
                Set<String> filterDependencyVarSet = (ConditionValidator.getDependencySet(filter.getFilterCondition()));

                filterDependencyVarSet.retainAll(dropCandidateSet);
                int[] processedEventsToBeDropped = null;
                if (!filterDependencyVarSet.isEmpty()) {
                    processedEventsToBeDropped = new int[filterDependencyVarSet.size()];
                    int j = 0;
                    for (String var : filterDependencyVarSet) {
                        processedEventsToBeDropped[j++] = (referenceSourceIdMap.get(var));
                    }

                    dependencyVarSet.addAll(filterDependencyVarSet);
                    dropCandidateSet.removeAll(filterDependencyVarSet);
                }
                innerHandlerProcessor.setProcessedEventsToBeDropped(processedEventsToBeDropped);
            }
        }

    }

    private static InnerHandlerProcessor findInnerHandlerProcessorForState(List<? extends InnerHandlerProcessor> innerHandlerProcessorList,
                                                                           int stateNo) {
        for (InnerHandlerProcessor innerHandlerProcessor : innerHandlerProcessorList) {
            if (innerHandlerProcessor.getCurrentStateNumber() == stateNo) {
                return innerHandlerProcessor;
            }
        }
        return null;
    }

    private static void addStateAsNext(List<UtilState> stateList, StateNumber stateNumber,
                                       List<Integer> perv) {
        for (int prevState : perv) {
            stateList.get(prevState).setNext(stateNumber.getNumber());
        }
    }
}
