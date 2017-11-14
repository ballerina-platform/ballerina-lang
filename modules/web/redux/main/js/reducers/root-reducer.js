import { combineReducers } from 'redux';
import { treeReducer } from './tree-reducer';


const rootReducer = combineReducers({
    tree: treeReducer,
});

export default rootReducer;
