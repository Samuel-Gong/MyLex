package mylex.LexAnalyzer.nfa;

import mylex.vo.Pattern;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NFA {

    public static char EPSILON = '\0';

    /**
     * 图中状态集合
     */
    private Set<NFAState> states;

    /**
     * NFA的起始状态
     */
    private NFAState startState;

    /**
     * 图中结束状态集合
     */
    private Set<NFAState> endStates;

    /**
     * 每个NFA结束状态和对应pattern的映射
     */
    private Map<NFAState, Pattern> endStateToPattern;

    /**
     * 输入字母表
     */
    private Set<Character> inputAlphabet;

    /**
     * 基础的，只具有两个状态的NFA构造器
     *
     * @param startState 开始状态
     * @param endState   结束状态
     * @param inputChar  输入字符
     */
    public NFA(NFAState startState, NFAState endState, char inputChar) {
        states = new HashSet<>();
        endStates = new HashSet<>();
        this.startState = startState;

        states.add(this.startState);
        states.add(endState);

        assert endState.isEndState() : ": 添加的结束状态不为状态标记不为true";
        endStates.add(endState);

        //添加一条从开始状态到结束状态的边
        this.startState.addEdge(new NFAEdge(endState, inputChar));

        //输入字母表中加入该输入字符
        this.inputAlphabet = new HashSet<>();
        inputAlphabet.add(inputChar);

        endStateToPattern = new HashMap<>();
        //添加pattern
        endStateToPattern.put(endState, new Pattern("0", Character.toString(inputChar), 0));
    }

    /**
     * 通配符的NFA构造器
     *
     * @param startState   开始状态
     * @param endState     结束状态
     * @param fullAlphabet 字母表全集
     */
    public NFA(NFAState startState, NFAState endState, Set<Character> fullAlphabet) {
        states = new HashSet<>();
        endStates = new HashSet<>();
        this.startState = startState;

        states.add(this.startState);
        states.add(endState);

        assert endState.isEndState() : ": 添加的结束状态不为状态标记不为true";
        endStates.add(endState);

        inputAlphabet = fullAlphabet;

        //添加从开始状态到结束状态经过字母表中字符的边
        for (Character c : inputAlphabet) {
            startState.addEdge(new NFAEdge(endState, c));
        }

        endStateToPattern = new HashMap<>();
        //添加pattern
        endStateToPattern.put(endState, new Pattern("0", Character.toString('.'), 0));
    }

    /**
     * 根据传入参数构建一个新的NFA
     *
     * @param states            所有状态集合
     * @param startState        开始状态
     * @param endStates         结束状态集合
     * @param endStateToPattern 结束状态集合到对应pattern的映射
     * @param inputAlphabet     输入字母表
     */
    public NFA(Set<NFAState> states, NFAState startState, Set<NFAState> endStates, Map<NFAState, Pattern> endStateToPattern, Set<Character> inputAlphabet) {
        this.states = states;
        this.startState = startState;
        this.endStates = endStates;
        this.endStateToPattern = endStateToPattern;
        this.inputAlphabet = inputAlphabet;
    }

    public Set<NFAState> getStates() {
        assert !states.isEmpty() : NFA.class.getName() + ": states为空";
        return states;
    }

    public Set<NFAState> getEndStates() {
        assert endStates != null : NFA.class.getName() + ": endState为null";
        return endStates;
    }

    public NFAState getStartState() {
        assert startState != null : NFA.class.getName() + ": startState为null";
        return startState;
    }


    /*
     * 在RE转NFA中，运用Thompson算法，实现并，连接，闭包三种运算
     */

    /**
     * 两个正则表达式的并的NFA
     *
     * @param nextNFA 需要并计算的NFA
     * @param id      当前的id
     * @return 分配后的id值
     */
    public int union(NFA nextNFA, int id) {

        //更新输入字母表
        inputAlphabet.addAll(nextNFA.inputAlphabet);
        assert inputAlphabet.containsAll(nextNFA.inputAlphabet) : NFA.class.getName() + ": 新的字母表中不完全包含后继状态的字母表";

        //新增两个状态，一个开始状态，一个结束状态
        NFAState curStartState = new NFAState(id++);
        NFAState curEndState = new NFAState(id++, true);

        //添加两条从新的开始状态分别到两个NFA开始状态的边
        curStartState.addEdge(new NFAEdge(startState, NFA.EPSILON));
        curStartState.addEdge(new NFAEdge(nextNFA.startState, NFA.EPSILON));

        assert endStates != null : NFA.class.getName() + ": first的结束状态为null";
        assert nextNFA.endStates != null : NFA.class.getName() + ": second的结束状态为null";

        //添加两条分别从两个NFA的结束状态到新的结束状态的边
        simpleNFAEndStateAddEdge(new NFAEdge(curEndState, NFA.EPSILON));
        nextNFA.simpleNFAEndStateAddEdge(new NFAEdge(curEndState, NFA.EPSILON));

        //移除原来两个NFA的结束状态
        removeSimpleNFAEndState();
        nextNFA.removeSimpleNFAEndState();

        //添加新状态到状态集合中
        states.addAll(nextNFA.states);
        states.add(curStartState);
        states.add(curEndState);

        //重设开始和结束状态
        startState = curStartState;
        setSimpleNFAEndState(curEndState);

        return id;
    }

    /**
     * 两个正则表达的连接的NFA，没有新增状态，故不需要新分配id值
     *
     * @param postNFA 后继NFA
     * @return 分配后的id值
     */
    public int concat(NFA postNFA, int id) {

        //更新输入字母表
        inputAlphabet.addAll(postNFA.inputAlphabet);
        assert inputAlphabet.containsAll(postNFA.inputAlphabet) : NFA.class.getName() + ": 新的字母表中不完全包含后继状态的字母表";

        //将后继NFA的开始状态的邻接表加入到当前NFA的结束状态的邻接表中，除了通过epsilon到达自身状态的边
        for (NFAEdge edge : postNFA.startState.getAdjacentcentList()) {
            if (inputAlphabet.contains(edge.getLabel())) simpleNFAEndStateAddEdge(edge);
        }

        //添加新加入的NFA状态图的所有状态，并删除postNFA的状态，重设结束状态
        states.addAll(postNFA.states);
        states.remove(postNFA.startState);
        removeSimpleNFAEndState();
        endStates = postNFA.endStates;

        assert endStates.equals(postNFA.endStates);
        assert !states.contains(postNFA.startState);

        return id;
    }

    /**
     * 一个正则表达式的闭包的NFA
     *
     * @param id 当前的id值
     * @return 分配后的id值
     */
    public int closure(int id) {

        //新增两个状态，一个开始状态，一个结束状态
        NFAState curStartState = new NFAState(id++);
        NFAState curEndState = new NFAState(id++, true);

        //添加两条边，分别从当前的开始状态到原开始状态, 从当前开始状态到当前结束状态
        curStartState.addEdge(new NFAEdge(startState, NFA.EPSILON));
        curStartState.addEdge(new NFAEdge(curEndState, NFA.EPSILON));

        //添加两条边，分别从原结束状态到原开始状态，从原结束状态到当前结束状态
        simpleNFAEndStateAddEdge(new NFAEdge(startState, NFA.EPSILON));
        simpleNFAEndStateAddEdge(new NFAEdge(curEndState, NFA.EPSILON));

        //更新当前开始和结束状态
        startState = curStartState;
        removeSimpleNFAEndState();
        setSimpleNFAEndState(curEndState);

        assert !states.contains(startState) && !(states.containsAll(endStates)) : NFA.class.getName();
        //向状态集合中添加新的状态
        states.add(startState);
        states.add(curEndState);

        return id;
    }

    /*
     * RE转NFA中，新添加的操作
     */

    /**
     * 表示当前模式出现零次或一次，只需新增一条从开始状态到结束状态的epsilon边
     *
     * @param id 当前的id值
     * @return 分配后的id值
     */
    public int zeroOrOnce(int id) {
        NFAState endState = getSimpleNFAEndState();

        //给开始状态加一条到结束状态的epsilon边
        startState.addEdge(new NFAEdge(endState, NFA.EPSILON));

        //更新状态集合中的开始状态
        states.add(startState);
        return id;
    }

    /**
     * 表示当前模式出现一次或多次
     *
     * @param id 当前的id值
     * @return 分配后的id值
     */
    public int onceOrMany(int id) {
        //新增一条从当前结束状态到开始状态的一条epsilon边
        simpleNFAEndStateAddEdge(new NFAEdge(startState, NFA.EPSILON));

        return id;
    }

    /**
     * 表示在当前模式后面连接一个可选的NFA
     *
     * @param postNFA 后继NFA
     * @param id 当前的id值
     * @return 分配后的id值
     */
    private int concatOptional(NFA postNFA, int id) {

        //更新输入字母表
        inputAlphabet.addAll(postNFA.inputAlphabet);
        assert inputAlphabet.containsAll(postNFA.inputAlphabet) : NFA.class.getName() + ": 新的字母表中不完全包含后继状态的字母表";

        //将后继NFA的开始状态的邻接表加入到当前NFA的结束状态的邻接表中，除了通过epsilon到达自身状态的边
        for (NFAEdge edge : postNFA.startState.getAdjacentcentList()) {
            if (inputAlphabet.contains(edge.getLabel())) simpleNFAEndStateAddEdge(edge);
        }

        //因为是可选的NFA，则给当前NFA的结束状态增加一条到后继NFA结束状态的epsilon边
        simpleNFAEndStateAddEdge(new NFAEdge(postNFA.getSimpleNFAEndState(), NFA.EPSILON));

        //添加新加入的NFA状态图的所有状态，并删除postNFA的状态，重设结束状态
        states.addAll(postNFA.states);
        states.remove(postNFA.startState);
        removeSimpleNFAEndState();
        endStates = postNFA.endStates;

        assert endStates.equals(postNFA.endStates);
        assert !states.contains(postNFA.startState);

        return id;
    }

    /**
     * 当前NFA自身与自身的克隆连接
     *
     * @param times 该NFA连接的次数，包括自身，所以只与克隆连接times-1次
     * @param id    当前分配的id
     * @return 分配后的id
     */
    public int concatSelfCertainTimes(int times, int id) {
        NFA repeatNFA = cloneNFA(id);
        //id加上克隆NFA状态的大小
        id = id + repeatNFA.getStates().size();

        //如果最小次数为0，说明可以不出现，不然说明必须出现minTime次，则将剩余的minTime-1次与nfa连接
        if (times == 0) id = zeroOrOnce(id);
        else {
            for (int repeatTimeNecessary = 1; repeatTimeNecessary < times; repeatTimeNecessary++) {
                id = concat(repeatNFA, id);
                //再次克隆原NFA并给克隆的NFA重新分配id
                repeatNFA = repeatNFA.cloneNFA(id);
                id += repeatNFA.getStates().size();
            }
        }

        return id;
    }

    /**
     * 解决大括号{n,}的NFA连接情况,至少出现n次，包括自身
     *
     * @param times 至少需要连接的次数
     * @param id    当前分配的id
     * @return 分配后的id
     */
    public int concatSelfLeastTimes(int times, int id) {
        assert times >= 0 : ": {}中只能为非负整数";
        //如果至少为0次 等同于*
        if (times == 0) return closure(id);
            //如果至少为1次 等同于+
        else if (times == 1) return onceOrMany(id);
            //如果至少为n次 (n >= 2)
        else {
            NFA repeatNFA = cloneNFA(id);
            //更新id
            id = id + repeatNFA.getStates().size();

            //自身连接n-1
            id = concatSelfCertainTimes(times - 1, id);

            //保存出现n-1的NFA的结束状态
            NFAState lastEndState = getSimpleNFAEndState();

            //再将当前的NFA与重复NFA连接一次，保证出现至少n次
            id = concat(repeatNFA, id);

            //给当前的NFA的结束状态添加一条到出现n-1的NFA的结束状态的epsilon边
            simpleNFAEndStateAddEdge(new NFAEdge(lastEndState, NFA.EPSILON));
        }

        return id;
    }

    /**
     * 解决大括号{n,m}的NFA连接情况
     *
     * @param minTimes 最小次数
     * @param maxTimes 最大次数
     * @param id       当前分配的id
     * @return 分配后的id
     */
    public int concatSelfMinToMax(int minTimes, int maxTimes, int id) {
        //保存当前的NFA
        NFA repeatNFA = cloneNFA(id);
        //id加上克隆NFA状态的大小
        id += repeatNFA.getStates().size();

        //进行必要的自连接
        id = concatSelfCertainTimes(minTimes, id);

        //对于可选择的重复次数，在当前NFA后面连接一个可选的repeatNFA
        for (int reapeatTimeOptional = 1; reapeatTimeOptional < maxTimes - minTimes; reapeatTimeOptional++) {
            id = concatOptional(repeatNFA, id);
            //再次克隆原NFA并给克隆的NFA重新分配id
            repeatNFA = repeatNFA.cloneNFA(id);
            id += repeatNFA.getStates().size();
        }

        return id;
    }

    public NFAState getSimpleNFAEndState() {
        assert endStates.size() == 1 : ": 简单NFA中的结束状态结合大小不为1";
        return endStates.iterator().next();
    }

    /**
     * 对于只有一个结束状态的NFA，给结束状态添加一条边
     */
    private void simpleNFAEndStateAddEdge(NFAEdge edge) {

        assert endStates.size() == 1 : NFA.class.getName() + ": 简单NFA中的结束状态集合大小不为1";

        //给endState添加一条新边
        NFAState srcState = endStates.iterator().next();
        srcState.addEdge(edge);

        //更新结束状态集合，和状态集合中的结束状态
        endStates.add(srcState);
        states.add(srcState);
    }

    /**
     * 对于只有一个结束状态的NFA，更新里面结束状态为false，并从中结束状态集合中移除
     */
    private void removeSimpleNFAEndState() {

        assert endStates.size() == 1 : NFA.class.getName() + ": 简单NFA的结束状态集合大小不为1";

        //更新结束状态的结束标记
        NFAState endState = endStates.iterator().next();
        endState.setEndState(false);

        //更新结束状态集合，和状态结合中的唯一的结束状态
        endStates.remove(endState);
        states.add(endState);
    }

    /**
     * 对于只有一个结束状态的NFA，添加一个结束状态
     */
    private void setSimpleNFAEndState(NFAState endState) {
        endStates.add(endState);
    }


    /*
     * 在NFA转DFA中，用于寻找epsilon闭包
     */

    /**
     * 找到能够从NFA的状态s开始只通过epsilon转换到达的NFA状态集合
     *
     * @return 能够到达的NFA状态集合
     */
    public Set<NFAState> epsilonClosureStart() {
        Set<NFAState> closure = new HashSet<>();
        closure.add(startState);
        return epsilonClosureOther(closure);
    }

    /**
     * 找到从T中某个NFA状态s开始只通过epsilon转换到达的NFA状态集合
     *
     * @param T 需要转换的NFA状态集合
     * @return 能够到达的NFA状态集合
     */
    public Set<NFAState> epsilonClosureOther(Set<NFAState> T) {
        int size = T.size();
        Set<NFAState> closure = move(T, NFA.EPSILON);
        //一直往下循环，直到找到最大的闭包
        while (closure.size() != size) {
            size = closure.size();
            closure = move(closure, NFA.EPSILON);
        }
        return closure;
    }

    /**
     * 找到从T中某个状态s出发通过标号为label的转换到达的NFA状态集合
     *
     * @param T     需要转换的NFA状态集合
     * @param label 标号
     * @return 从NFA的状态state开始，通过label转换到达的NFA状态集合
     */
    public Set<NFAState> move(Set<NFAState> T, char label) {
        Set<NFAState> NFAStateClosure = new HashSet<>();
        for (NFAState state : T) {
            NFAStateClosure.addAll(moveState(state, label));
        }
        return NFAStateClosure;
    }

    /**
     * 在源状态上通过标号移动，找到能到达的NFA状态集合
     *
     * @param srcState 源状态
     * @param label    标号
     * @return 能到达的NFA状态集合
     */
    private Set<NFAState> moveState(NFAState srcState, char label) {
        return srcState.findDestStateByLabel(label);
    }

    /**
     * 返回输入字母表
     *
     * @return 输入字母表
     */
    public Set<Character> getInputAlphabet() {
        return inputAlphabet;
    }

    /**
     * 打印NFA中所有状态
     */
    public void printNFA() {
        System.out.println("开始状态:");
        startState.printNFAState();
        System.out.println("结束状态:");
        for (NFAState endState : endStates) {
            System.out.println("id: " + +endState.getID() + "     pattern:" + endStateToPattern.get(endState).regularExpression);
        }
        System.out.println("-----------------------------");
        for (NFAState state : states) {
            state.printNFAState();
        }
        System.out.println("-----------------------------");
    }

    /**
     * 根据接受状态找到对应的Pattern
     *
     * @param endState NFA的一个接受状态
     * @return
     */
    public Pattern findPatternByNFAState(NFAState endState) {
        assert endStateToPattern.keySet().contains(endState) : ": 该NFA的接受状态没有映射任何Pattern";
        return endStateToPattern.get(endState);
    }

    /**
     * 克隆一个新的NFA，新NFA的状态的id是原NFA状态的id+当前被分配的id
     *
     * @param id 现在的id
     * @return 新的NFA
     */
    private NFA cloneNFA(int id) {
        Set<NFAState> newStates = new HashSet<>();
        NFAState newStartState = null;
        Set<NFAState> newEndStates = new HashSet<>();
        Map<NFAState, Pattern> newEndStateToPattern = new HashMap<>();

        //保存从旧NFA状态到新NFA状态的映射
        Map<NFAState, NFAState> oldStateToNewState = new HashMap<>();

        for (NFAState oldState : states) {

            NFAState newState = null;

            //说明已经存在该对应该旧NFA状态的新NFA状态，就不需要再添加
            if (oldStateToNewState.containsKey(oldState)) {
                newState = oldStateToNewState.get(oldState);
            } else {
                newState = new NFAState(id++, oldState.isEndState());
                newStates.add(newState);
                oldStateToNewState.put(oldState, newState);
            }

            assert newState != null : ": 克隆时没有找到对应旧NFA状态的新NFA状态";

            //将旧状态的邻接表拷贝到新状态上
            for (NFAEdge oldEdge : oldState.getAdjacentcentList()) {
                NFAState oldDestState = oldEdge.getDestState();

                //略过目的边为本边且字符为epsilong的默认边
                if (!(oldDestState.equals(oldState) && oldEdge.getLabel() == NFA.EPSILON)) {
                    NFAState destState = null;
                    //有该旧NFA状态，添加新状态
                    if (oldStateToNewState.containsKey(oldDestState)) {
                        destState = oldStateToNewState.get(oldDestState);
                    } else {
                        destState = new NFAState(id++, oldDestState.isEndState());
                        newStates.add(destState);
                        oldStateToNewState.put(oldDestState, destState);
                    }

                    assert destState != null : ": 没有找到对应旧NFA状态新NFA状态";

                    newState.addEdge(new NFAEdge(destState, oldEdge.getLabel()));
                }
            }

            //如果是结束状态，就在结束状态集合中加入该状态，并在新的pattern映射中加入映射
            if (newState.isEndState()) {
                newEndStates.add(newState);
                newEndStateToPattern.put(newState, endStateToPattern.get(oldState));
            }

            if (oldState.equals(startState)) {
                newStartState = newState;
            }
        }

        assert newStates.size() == states.size() : ": 克隆NFA新状态集合与原NFA状态集合大小不同";
        assert newStartState != null : ": 克隆NFA的开始状态为空";
        assert newEndStates.size() == endStates.size() : ": 克隆NFA的结束状态集合与原NFA状态结束状态集合大小不同";
        assert newEndStateToPattern.size() == endStateToPattern.size() : ": 克隆NFA的Pattern映射大小与原NFA的Pattern映射大小不同";

        return new NFA(newStates, newStartState, newEndStates, newEndStateToPattern, inputAlphabet);
    }
}
