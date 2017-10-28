package mylex.LexAnalyzer.dfa;

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

    public DFAOptimizer(DFA dfa){

        this.dfa = dfa;
        inputAlphabet = dfa.getInputAlphabet();
        dfaStateMap = new HashMap<>();

        //初始化状态的划分，分为接受状态组和非接受状态组
        dfaStatePartition = new HashSet<>();
        if(!dfa.getEndStates().isEmpty()) dfaStatePartition.add(dfa.getEndStates());
        if(!dfa.getNotEndStates().isEmpty()) dfaStatePartition.add(dfa.getNotEndStates());
    }

    /**
     * 优化DFA
     * @return 优化后的DFA
     */
    public DFA constructOptimizedDFA(){

        //先将状态组划分为最小划分的组
        dfaStatePartition = partitionState(dfaStatePartition);

        //建立未优化的DFA状态集合到新DFA状态的映射
        int id = 0;
        //对于每一对DFA状态集合--新DFA状态，建立一对键值对
        for (Set<DFAState> stateSet : dfaStatePartition){
            dfaStateMap.put(stateSet, new DFAState(id++, hasDFAEndState(stateSet)));
        }

        //为新的DFA状态添加边
        for (Map.Entry<Set<DFAState>, DFAState> entry : dfaStateMap.entrySet()){
            assert !entry.getKey().isEmpty() : DFAOptimizer.class.getName() + ": DFA状态集合为空（constructOpimizedDFA）";
            //从未优化的DFA状态集合中任意抽取一个DFAState作为源状态
            DFAState srcState = entry.getKey().iterator().next();
            //对于该源状态中的邻接表进行遍历
            for(Map.Entry<Character, DFAState> srcStateEntry : srcState.getAdjacentList().entrySet()){
                //对邻接表中的每一对Charcter--DFAState键值, 找到原来的DFAState所属的DFA状态集合
                for(Set<DFAState> dfaStateSet : dfaStatePartition){
                    if(dfaStateSet.contains(entry.getValue())){
                        //根据找到的DFA状态集合，在dfaStateMap中找到对应的新的DFA状态，并为该源状态添加Charcter--新DFAState的边
                        addEdge(entry.getKey(), dfaStateMap.get(dfaStateSet), srcStateEntry.getKey());
                        continue;
                    }
                }
            }
        }

        //装载新的DFA
        return loadOptimizedDFA();
    }

    /**
     * 根据原DFA的状态集合到新DFA状态的映射，找到新DFA的状态集合，开始状态和结束状态集合
     * @return 新的DFA
     */
    private DFA loadOptimizedDFA() {
        Set<DFAState> states = new HashSet<>();
        DFAState startState = null;
        Set<DFAState> endStates = new HashSet<>();
        for(Map.Entry<Set<DFAState>, DFAState> entry : dfaStateMap.entrySet()){
            DFAState newDFAState = entry.getValue();
            //原DFA状态集合中是否含有原DFA的起始状态，若有，则说明该新的DFA状态是新DFA的起始状态
            if(entry.getKey().contains(dfa.getStartState())) startState = newDFAState;
            //新DFA状态是否是结束状态，若是，则将该状态加入到新DFA的接受状态集合中
            if(newDFAState.isEndState()) endStates.add(newDFAState);
            //将每个新的DFA状态加入到新的DFA状态集合中去
            states.add(entry.getValue());
        }

        assert !states.isEmpty() && startState != null && !endStates.isEmpty() : "新DFA装载错误";
        return new DFA(states, startState, endStates, inputAlphabet);
    }

    /**
     * 判断该DFA状态集合中是否含有DFA的接受状态
     * @param stateSet DFA状态集合
     * @return
     */
    private boolean hasDFAEndState(Set<DFAState> stateSet) {
        for(DFAState dfaState : stateSet){
            if(dfaState.isEndState()) return true;
        }
        return false;
    }

    /**
     * 根据DFA状态集合找到新的DFA状态，然后加上一条到另一新DFA状态的边
     * @param key   dfaStateMap的键值
     * @param destState 目的DFA状态（优化后的）
     * @param label 转化字符
     */
    private void addEdge(Set<DFAState> key, DFAState destState, char label){
        dfaStateMap.get(key).addEdge(label, destState);
    }

    /**
     * 将状态组划分为不能再分割的状态组
     * @return
     */
    private Set<Set<DFAState>> partitionState(Set<Set<DFAState>> partitions){
        while(canPartition(partitions)){
            for(Set<DFAState> dfaStateSet : partitions){
                //将该集合从当前划分中移除
                partitions.remove(dfaStateSet);

                Set<Set<DFAState>> partition = new HashSet<>();
                partition.add(dfaStateSet);

                //递归调用该方法
                partitions.addAll(partitionState(partition));
            }
        }
        return partitions;
    }

    /**
     * 检查该划分中的每个组是否还能再划分
     * @return
     */
    private boolean canPartition(Set<Set<DFAState>> statePartition) {
        for(Set<DFAState> dfaStateSet : statePartition){
            if(!allStatesInSameSet(dfaStateSet)) return true;
        }
        return false;
    }

    /**
     * 检查该组中的每两个状态对于同一个输入字符表中的字符是否转化到同一个DFA状态集合中
     * @param dfaStateSet   需要检查的DFA状态集合
     * @return
     */
    private boolean allStatesInSameSet(Set<DFAState> dfaStateSet){
        for(Character label : inputAlphabet){
            //先从Set中取一个DFA状态，变换后得到所属DFA状态集合，用于比较
            DFAState srcState = dfaStateSet.iterator().next();
            Set<DFAState> destDFAStateSet = move(srcState, label);
            for(DFAState state : dfaStateSet){
                Set<DFAState> anotherDestDFAStateSet = move(state, label);
                assert destDFAStateSet != null && anotherDestDFAStateSet != null : DFAOptimizer.class.getName() + ": DFA集合为空了";
                //比较两个集合是否相等，若不相等则说明该集合还可以被划分
                if(!destDFAStateSet.equals(anotherDestDFAStateSet)) return false;
            }
        }
        return true;
    }

    /**
     * 找到srcState经label转换后到达的状态位于哪个DFA状态集合，
     * @param srcState  源状态
     * @param label 输入字符
     * @return  destState所处的DFA状态集合
     */
    private Set<DFAState> move(DFAState srcState, char label){
        DFAState destState = srcState.getAdjacentList().get(label);
        if (destState == null) return null;
        for (Set<DFAState> dfaStateSet : dfaStatePartition){
            if(dfaStateSet.contains(destState)) return dfaStateSet;
        }
        return null;
    }
}
