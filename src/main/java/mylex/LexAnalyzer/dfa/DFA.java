package mylex.LexAnalyzer.dfa;

import mylex.LexAnalyzer.nfa.NFA;
import mylex.LexAnalyzer.nfa.NFAState;

import java.util.*;

public class DFA {

    /**
     * 保存NFA状态集合到DFAState的映射
     */
    private Map<Set<NFAState>, DFAState> dfaStateMap;

    /**
     * DFA状态集合
     */
    private Set<DFAState> states;

    /**
     * DFA结束状态集合
     */
    private Set<DFAState> endStates;

    /**
     * NFA
     */
    private NFA nfa;

    /**
     * 输入字母表
     */
    private Set<Character> inputAlphabet;

    /**
     * 维护一个stateID，向DFA状态集合加入DFA状态时，同时给DFA分配一个id
     */
    private int stateID;

    /**
     * DFA的开始状态
     */
    private DFAState startState;

    public DFA(NFA nfa){
        dfaStateMap = new HashMap<>();
        states = new HashSet<>();
        endStates = new HashSet<>();
        this.nfa = nfa;
        inputAlphabet = nfa.getInputAlphabet();
        stateID = 0;

        //找到表示DFA开始状态的NFA集合,并将其加入到dfaStateMap中
        startState = new DFAState(stateID++);
        addState(nfa.epsilonClosureStart(), startState);
    }

    public void Dtran(){
        while(!allLabeled()){
            DFAState stateNotLabeled = findStateNotLabeled();
            assert stateNotLabeled != null : DFA.class.getName() + ": 未标记的状态为空";

            //给未标记状态加上标记
            stateNotLabeled.setLabeled(true);

            //对每个输入符号label，获取新的DFAState，新状态不在状态集内，则加入状态集
            for(Character label : inputAlphabet){

                //对输入符号label，获取转换后的NFA状态集合epsilon闭包
                Set<NFAState> nfaStateSet = nfa.epsilonClosureOther(nfa.move(getNFAStateSetByDFAState(stateNotLabeled), label));

                if(dfaStateMap.containsKey(nfaStateSet)){
                    System.out.println(DFA.class.getName() + ": 该NFA状态集合已经存在，即对应DFA状态已存在");
                }
                else{
                    DFAState u = new DFAState(stateID++);
                    addState(nfaStateSet, u);
                    //给当前状态新增一条通过label可达到状态u的边
                    stateNotLabeled.addEdge(label, u);
                }
            }

            //替换原来集合中的未标记的状态
            states.add(stateNotLabeled);
        }
    }

    /**
     * 获取DFA的接受状态组
     * @return  DFA的接受状态集合
     */
    public Set<DFAState> getEndStates(){
        //若DFA接受状态组为空，则初始化
        if (endStates.isEmpty()){
            //对于每一对NFA状态集合--DFAState， 检查NFA状态集合中是否含有NFA的结束状态
            //若是，则将对应的DFAState加入到DFA的接受状态组中
            for(Map.Entry<Set<NFAState>, DFAState> entry : dfaStateMap.entrySet()){
                for(NFAState nfaState : entry.getKey()){
                    if(nfaState.isEndState()){
                        endStates.add(entry.getValue());
                        break;
                    }
                }
            }
        }
        return endStates;
    }

    /**
     * 获取DFA的非接受状态组
     * @return  DFA的非接受状态集合
     */
    public Set<DFAState> getNotEndStates(){
        Set<DFAState> notEndStates = states;
        states.removeAll(endStates);
        //检查
        for(DFAState state : endStates){
            assert !notEndStates.contains(state) : DFA.class.getName() + ": 非接受状态组中包含了接受状态";
        }
        return notEndStates;
    }

    public Set<Character> getInputAlphabet(){
        return inputAlphabet;
    }

    /**
     * 根据传入的DFAState获取映射中对应的NFA状态集合
     * @param state DFA状态
     * @return NFA状态集合
     */
    private Set<NFAState> getNFAStateSetByDFAState(DFAState state){
        assert states.contains(state);
        for (Map.Entry<Set<NFAState>, DFAState> entry : dfaStateMap.entrySet()){
            if (entry.getValue().equals(state)) return entry.getKey();
        }
        return null;
    }

    /**
     * 添加一个新的DFA状态
     * @param nfaStateSet   该DFA状态对应的NFA状态集合
     * @param dfaState  DFA状态
     */
    private void addState(Set<NFAState> nfaStateSet, DFAState dfaState){
        assert !dfaStateMap.containsKey(nfaStateSet) : DFA.class.getName() + ": 映射中已经存在该NFA状态集合";
        dfaStateMap.put(nfaStateSet, dfaState);
        states.add(dfaState);
        //如果NFA状态集中包含结束状态，则该DFA状态也是结束状态
        if(containsEndState(nfaStateSet)) endStates.add(dfaState);
    }

    /**
     * 检查该NFA状态集中是否存在结束状态
     * @param nfaStateSet
     * @return  存在，布尔值
     */
    private boolean containsEndState(Set<NFAState> nfaStateSet) {
        for(NFAState nfaState : nfaStateSet){
            if(nfaState.isEndState()) return true;
        }
        return false;
    }

    /**
     * 检查是否所有的Dstates都已经被标记
     * @return 是否都被标记的布尔值
     */
    private boolean allLabeled() {
        for (DFAState dfaState : states){
            if (!dfaState.isLabeled()) return false;
        }
        return true;
    }

    /**
     * 查找未被标记的状态
     * @return  未被标记的状态
     */
    private DFAState findStateNotLabeled(){
        for (DFAState state : states){
            if(!state.isLabeled()) return state;
        }
        return null;
    }

}
