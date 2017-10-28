package mylex.LexAnalyzer.dfa;

import mylex.vo.Pattern;

import java.util.*;

public class DFAOptimizer {

    /**
     * 保存DFA状态组的划分
     */
    private Set<Set<DFAState>> dfaStatePartition;

    /**
     * 经NFA转化后的DFA对象，但是还未优化过
     */
    private DFA dfa;

    /**
     * 输入字母表
     */
    private Set<Character> inputAlphabet;

    /**
     * 保存未优化的DFA状态集合到新DFA状态的映射
     */
    private Map<Set<DFAState>, DFAState> dfaStateMap;

    public DFAOptimizer(DFA dfa) {

        this.dfa = dfa;
        inputAlphabet = dfa.getInputAlphabet();
        dfaStateMap = new HashMap<>();

        //初始化状态的划分，分为接受状态组和非接受状态组
        dfaStatePartition = new HashSet<>();
        if (!dfa.getEndStates().isEmpty()) dfaStatePartition.add(dfa.getEndStates());
        if (!dfa.getNotEndStates().isEmpty()) dfaStatePartition.add(dfa.getNotEndStates());
    }

    /**
     * 优化DFA
     *
     * @return 优化后的DFA
     */
    public DFA constructOptimizedDFA() {

        //先将状态组划分为最小划分的组
        partitionState();

        //建立未优化的DFA状态集合到新DFA状态的映射
        int id = 0;
        //对于每一对DFA状态集合--新DFA状态，建立一对键值对
        for (Set<DFAState> stateSet : dfaStatePartition) {
            dfaStateMap.put(stateSet, new DFAState(id++, hasDFAEndState(stateSet)));
        }

        //为新的DFA状态添加边
        for (Map.Entry<Set<DFAState>, DFAState> entry : dfaStateMap.entrySet()) {
            assert !entry.getKey().isEmpty() : DFAOptimizer.class.getName() + ": DFA状态集合为空（constructOpimizedDFA）";
            //从未优化的DFA状态集合中任意抽取一个DFAState作为源状态
            DFAState srcState = entry.getKey().iterator().next();

            //对于该源状态中的输入字母表进行遍历
            for (Character c : srcState.getAlphabet()) {

                //找到这个DFA状态经c转换后到达的DFA状态所属的状态集合
                Set<DFAState> destDFAStateSet = move(srcState, c);
                assert dfaStateMap.containsKey(destDFAStateSet);

                addEdge(entry.getKey(), dfaStateMap.get(destDFAStateSet), c);
            }
        }

        //装载新的DFA
        return loadOptimizedDFA();
    }

    /**
     * 根据原DFA的状态集合到新DFA状态的映射，找到新DFA的状态集合，开始状态和结束状态集合
     *
     * @return 新的DFA
     */
    private DFA loadOptimizedDFA() {
        Set<DFAState> states = new HashSet<>();
        DFAState startState = null;
        Set<DFAState> endStates = new HashSet<>();
        Map<DFAState, List<Pattern>> endStateToPatterns = new HashMap<>();
        for (Map.Entry<Set<DFAState>, DFAState> entry : dfaStateMap.entrySet()) {
            DFAState newDFAState = entry.getValue();
            //原DFA状态集合中是否含有原DFA的起始状态，若有，则说明该新的DFA状态是新DFA的起始状态
            if (entry.getKey().contains(dfa.getStartState())) startState = newDFAState;

            //新DFA状态是否是结束状态，若是，则将该状态加入到新DFA的接受状态集合中，且将该DFAState对应的Pattern加入映射
            if (newDFAState.isEndState()) {
                endStates.add(newDFAState);
                endStateToPatterns.put(newDFAState, findMatchedPatterns(entry.getKey()));
            }

            //将每个新的DFA状态加入到新的DFA状态集合中去
            states.add(entry.getValue());
        }

        assert !states.isEmpty() && startState != null && !endStates.isEmpty() : "新DFA装载错误";
        assert endStateToPatterns.keySet().equals(endStates) : "Pattern映射中的结束状态集和DFA的结束状态集不同";
        return new DFA(states, startState, endStates, inputAlphabet, endStateToPatterns);
    }

    /**
     * 根据DFA的接受状态对应的DFA状态集合，找到对应该接受状态优先级最高的Pattern
     *
     * @param states 原DFA状态集合
     * @return 对应的Pattern集合
     */
    private List<Pattern> findMatchedPatterns(Set<DFAState> states) {
        List<Pattern> allMatchedPattern = new ArrayList<>();

        for (DFAState state : states) {
            if (state.isEndState()) allMatchedPattern.addAll(dfa.findPatternsByDFAState(state));
        }

        return allMatchedPattern;
    }

    /**
     * 判断该DFA状态集合中是否含有DFA的接受状态
     *
     * @param stateSet DFA状态集合
     * @return
     */
    private boolean hasDFAEndState(Set<DFAState> stateSet) {
        for (DFAState dfaState : stateSet) {
            if (dfaState.isEndState()) return true;
        }
        return false;
    }

    /**
     * 根据DFA状态集合找到新的DFA状态，然后加上一条到另一新DFA状态的边
     *
     * @param key       dfaStateMap的键值
     * @param destState 目的DFA状态（优化后的）
     * @param label     转化字符
     */
    private void addEdge(Set<DFAState> key, DFAState destState, char label) {
        dfaStateMap.get(key).addEdge(label, destState);
    }

    /**
     * 将状态组划分为不能再分割的状态组
     */
    private void partitionState() {
        Set<Set<DFAState>> newPartition = new HashSet<>();
        newPartition.addAll(dfaStatePartition);
        while (canPartition(newPartition)) {
            for (Set<DFAState> dfaStateSet : newPartition) {
                if (!allStatesInSameSet(dfaStateSet)) {

                    //先根据一个状态找出与这个状态在一组的状态，合并为一个状态
                    Set<DFAState> stateInSameSet = findStateInSameSet(dfaStateSet);
                    dfaStatePartition.add(stateInSameSet);

                    //找出和先前的状态不在同一组的状态
                    Set<DFAState> stateNotInSameSet = dfaStateSet;
                    stateNotInSameSet.removeAll(stateInSameSet);

                    //删除划分的组
                    dfaStatePartition.remove(dfaStateSet);

                    dfaStatePartition.add(stateNotInSameSet);
                }
            }
            newPartition.clear();
            newPartition.addAll(dfaStatePartition);
        }
    }

    /**
     * 判断当前的划分组还能不能划分
     *
     * @return
     */
    private boolean canPartition(Set<Set<DFAState>> dfaStatePartition) {
        for (Set<DFAState> dfaStateSet : dfaStatePartition) {
            if(!allStatesInSameSet(dfaStateSet)) return true;
        }
        return false;
    }

    /**
     * 取集合中的一个状态，找到与这个状态相同的其它状态，并将他们划分为一组
     *
     * @param dfaStateSet
     * @return
     */
    private Set<DFAState> findStateInSameSet(Set<DFAState> dfaStateSet) {
        Set<DFAState> stateInSameSet = new HashSet<>();
        Iterator<DFAState> iterator = dfaStateSet.iterator();
        DFAState chosenState = iterator.next();
        //现将该状态加入，该状态可能为分组中的唯一状态
        stateInSameSet.add(chosenState);
        while (iterator.hasNext()) {
            DFAState nextState = iterator.next();
            if (isInSameSet(chosenState, nextState)) stateInSameSet.add(nextState);
        }
        return stateInSameSet;
    }

    /**
     * 检查两个状态是否同构
     *
     * @param chosenState 选择的DFA状态
     * @param nextState   与之比较的DFA状态
     * @return
     */
    private boolean isInSameSet(DFAState chosenState, DFAState nextState) {
        //首先输入字符表得相同
        if (chosenState.getAlphabet().equals(nextState.getAlphabet())) {
            Set<Character> inputAlphabet = chosenState.getAlphabet();
            //对于输入字符表的每个输入，转换后到达的DFA状态集合都应该相同
            for (Character c : inputAlphabet) {
                if (!move(chosenState, c).equals(move(nextState, c))) return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 检查该组中的每两个状态对于同一个输入字符表中的字符是否转化到同一个DFA状态集合中
     *
     * @param dfaStateSet 需要检查的DFA状态集合
     * @return
     */
    private boolean allStatesInSameSet(Set<DFAState> dfaStateSet) {
        assert !dfaStateSet.isEmpty() : "DFA状态组为空了";
        Iterator<DFAState> iterator = dfaStateSet.iterator();
        DFAState chosenState = iterator.next();
        while (iterator.hasNext()) {
            if (!isInSameSet(chosenState, iterator.next())) return false;
        }
        return true;
    }

    /**
     * 找到srcState经label转换后到达的状态位于哪个DFA状态集合，
     *
     * @param srcState 源状态
     * @param label    输入字符
     * @return destState所处的DFA状态集合
     */
    private Set<DFAState> move(DFAState srcState, char label) {
        DFAState destState = srcState.getAdjacentList().get(label);
        if (destState == null) return new HashSet<>();
        for (Set<DFAState> dfaStateSet : dfaStatePartition) {
            if (dfaStateSet.contains(destState)) return dfaStateSet;
        }
        return new HashSet<>();
    }
}
