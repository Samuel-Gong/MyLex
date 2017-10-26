package mylex.LexAnalyzer.nfa;

import java.util.*;

public class NFA {

    static char EPSILON = '\0';

    /**
     * 图中状态集合
     */
    private Set<NFAState> states;

    /**
     * 图中结束状态集合
     */
    private Set<NFAState> endStates;

    /**
     * NFA的起始状态
     */
    private NFAState startState;

    /**
     * 输入字母表
     */
    private Set<Character> inputAlphabet;


    public NFA(Set<Character> inputAlphabet){
        states = new HashSet<>();
        endStates = new HashSet<>();
        this.inputAlphabet = inputAlphabet;
    }


    /**
     * 找到能够从NFA的状态s开始只通过epsilon转换到达的NFA状态集合
     * @return  能够到达的NFA状态集合
     */
    public Set<NFAState> epsilonClosureStart(){
        return moveState(startState, NFA.EPSILON);
    }

    /**
     * 找到从T中某个NFA状态s开始只通过epsilon转换到达的NFA状态集合
     * @param T 需要转换的NFA状态集合
     * @return  能够到达的NFA状态集合
     */
    public Set<NFAState> epsilonClosureOther(Set<NFAState> T){
        return move(T, NFA.EPSILON);
    }

    /**
     * 找到从T中某个状态s出发通过标号为label的转换到达的NFA状态集合
     * @param T 需要转换的NFA状态集合
     * @param label 标号
     * @return  从NFA的状态state开始，通过label转换到达的NFA状态集合
     */
    public Set<NFAState> move(Set<NFAState> T, char label){
        Set<NFAState> NFAStateClosure = new HashSet<>();
        for(NFAState state : T){
            NFAStateClosure.addAll(moveState(state, label));
        }
        return NFAStateClosure;
    }

    public Set<NFAState> getEndStates(){
        //若为空,初始化NFA的结束状态集
        if(endStates.isEmpty()){
            for(NFAState state : states){
                if(state.isEndState()) endStates.add(state);
            }
        }
        return endStates;
    }

    public NFAState getStartState(){
        return startState;
    }

    /**
     * 返回输入字母表
     * @return  输入字母表
     */
    public Set<Character> getInputAlphabet() {
        return inputAlphabet;
    }

    /**
     * 在源状态上通过标号移动，找到能到达的NFA状态集合
     * @param srcState  源状态
     * @param label 标号
     * @return  能到达的NFA状态集合
     */
    private Set<NFAState> moveState(NFAState srcState, char label){
        Set<NFAState> stateClosure = new HashSet<>();
        for (NFAEdge edge : srcState.adjacentList) {
            if(edge.getLabel() == label){
                stateClosure.add(edge.getDestState());
            }
        }
        return stateClosure;
    }

    /**
     * 新增一条边
     * @param srcNFAState  源状态
     * @param destNFAState 目标状态
     * @param label 转化标志
     */
    private void addEdge(NFAState srcNFAState, NFAState destNFAState, char label) {
        if(!states.contains(srcNFAState)) states.add(srcNFAState);
        if(!states.contains(destNFAState)) states.add(destNFAState);
        assert (states.contains(srcNFAState) && states.contains(destNFAState)) : NFA.class.getName() + " : NFA中还未包含该状态";


        srcNFAState.adjacentList.add(new NFAEdge(destNFAState, label));
    }


}
