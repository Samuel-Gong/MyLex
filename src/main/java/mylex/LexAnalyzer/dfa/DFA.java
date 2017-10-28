package mylex.LexAnalyzer.dfa;

import mylex.LexAnalyzer.nfa.NFA;
import mylex.LexAnalyzer.nfa.NFAState;

import java.util.*;

public class DFA {

    /**
     * DFA状态集合
     */
    private Set<DFAState> states;

    /**
     * DFA的开始状态
     */
    private DFAState startState;

    /**
     * DFA结束状态集合
     */
    private Set<DFAState> endStates;

    /**
     * 输入字母表
     */
    private Set<Character> inputAlphabet;

    /**
     * NFA
     */
    private NFA nfa;

    /**
     * NFA状态集合到DFA的映射
     */
    private Map<Set<NFAState>, DFAState> dfaStateMap;

    /**
     * id
     */
    private int stateID;

    public DFA(NFA nfa){
        states = new HashSet<>();
        endStates = new HashSet<>();

        this.nfa = nfa;
        dfaStateMap = new HashMap<>();

        //初始化id
        stateID = 0;

        //DFA的字母表即是NFA的字母表
        inputAlphabet = nfa.getInputAlphabet();

        //找到表示DFA开始状态的NFA集合,并将其加入到dfaStateMap中
        Set<NFAState> nfaStates = nfa.epsilonClosureStart();
        startState = new DFAState(stateID++, hasNFAEndState(nfaStates));
        //添加初始DFA状态
        addState(nfaStates, startState);
    }

    public DFA(Set<DFAState> states, DFAState startState, Set<DFAState> endStates, Set<Character> inputAlphabet){
        this.states = states;
        this.startState = startState;
        this.endStates = endStates;
        this.inputAlphabet = inputAlphabet;
    }

    /**
     * DFA的转换函数
     */
    public void Dtran(){

        while(!allLabeled()){

            Set<NFAState> nfaStatesNotLabeled = findNFAStateNotLabeled();
            DFAState stateNotLabeled = dfaStateMap.get(nfaStatesNotLabeled);
            assert stateNotLabeled != null : DFA.class.getName() + ": 未标记的状态为空";

            //对每个输入符号label，获取新的DFAState，新状态不在状态集内，则加入状态集
            for(Character label : inputAlphabet){

                //对输入符号label，获取转换后的NFA状态集合epsilon闭包
                Set<NFAState> nfaStateSet = nfa.epsilonClosureOther(nfa.move(nfaStatesNotLabeled, label));

                //检查映射中是否已经存在以该NFA状态集合为键值的DFA状态,若不存在，添加新的键值对
                if(!dfaStateMap.containsKey(nfaStateSet)){
                    DFAState u = new DFAState(stateID++, hasNFAEndState(nfaStateSet));
                    //添加一个新的状态
                    addState(nfaStateSet, u);
                }
                //给当前状态新增一条通过label可达到状态u的边
                stateNotLabeled.addEdge(label, dfaStateMap.get(nfaStateSet));
            }

            //将映射中和DFA状态集合中，该DFA状态的标记位记为true
            updateDFAStateLabel(nfaStatesNotLabeled);
        }
    }

    /**
     * 更新映射中和DFA状态集合中，该NFA状态集合对应的DFA状态的标记位
     */
    private void updateDFAStateLabel(Set<NFAState> dfaStates) {
        DFAState stateNotLabeled = dfaStateMap.get(dfaStates);
        stateNotLabeled.setLabeled(true);
        dfaStateMap.put(dfaStates, stateNotLabeled);
        states.remove(stateNotLabeled);
        states.add(stateNotLabeled);
    }

    /**
     * 找到没被标记的DFA状态所对应的NFA状态集合
     * @return NFA状态集合
     */
    private Set<NFAState> findNFAStateNotLabeled() {
        for(Map.Entry<Set<NFAState>, DFAState> entry : dfaStateMap.entrySet()){
            if(!entry.getValue().isLabeled()) return entry.getKey();
        }
        return null;
    }

    /**
     * 判断该NFA状态集合中是否有结束状态
     * @param nfaStateSet NFA状态集合
     * @return
     */
    private boolean hasNFAEndState(Set<NFAState> nfaStateSet) {
        for(NFAState nfaState : nfaStateSet){
            if(nfaState.isEndState()) return true;
        }
        return false;
    }

    /**
     * 获取DFA的接受状态组
     * @return  DFA的接受状态集合
     */
    public Set<DFAState> getEndStates(){
        assert !endStates.isEmpty() : "DFA状态集合为空";
        return endStates;
    }

    /**
     * 获取DFA的非接受状态组
     * @return  DFA的非接受状态集合
     */
    public Set<DFAState> getNotEndStates(){
        assert !states.isEmpty() : ": DFA状态集合为空";
        assert !endStates.isEmpty() : ": DFA接受状态集合为空";

        Set<DFAState> notEndStates = states;
        notEndStates.removeAll(endStates);

        return notEndStates;
    }

    public Set<Character> getInputAlphabet(){
        return inputAlphabet;
    }

    /**
     * 添加一个新的DFA状态
     * @param nfaStateSet   该DFA状态对应的NFA状态集合
     * @param dfaState  DFA状态
     */
    private void addState(Set<NFAState> nfaStateSet, DFAState dfaState){
        assert !dfaStateMap.containsKey(nfaStateSet) : DFA.class.getName() + ": 映射中已经存在该NFA状态集合";
        dfaStateMap.put(nfaStateSet, dfaState);

        //给DFA状态集合中加入该DFA状态
        states.add(dfaState);
        //如果DFA被标记为结束状态，则该DFA状态也是结束状态
        if(dfaState.isEndState()) endStates.add(dfaState);
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

    public void printDFA(){
        System.out.println("----------------------");
        for(DFAState state : states){
            state.printDFAState();
        }
        System.out.println("----------------------");
    }

    public DFAState getStartState() {
        return startState;
    }
}
