package mylex.LexAnalyzer.nfa;

import java.util.*;

public class NFA {

    static char EPSILON = '\0';

    /**
     * 图中状态集合
     */
    private Set<NFAState> states;

    /**
     * 图中结束状态集合,由Tompson得到的结束状态始终只有一个
     */
    private NFAState endState;

    /**
     * NFA的起始状态
     */
    private NFAState startState;

    /**
     * 输入字母表
     */
    private Set<Character> inputAlphabet;

    /**
     * 基础的，只具有两个状态的NFA构造器
     * @param startState 开始状态
     * @param endState 结束状态
     * @param inputChar 输入字符
     */
    public NFA(NFAState startState, NFAState endState, char inputChar){
        states = new HashSet<>();
        this.startState = startState;
        this.endState = endState;
        states.add(this.startState);
        states.add(this.endState);

        //添加一条从
        startState.addEdge(new NFAEdge(endState, inputChar));

        //输入字母表中加入该输入字符
        this.inputAlphabet = new HashSet<>();
        inputAlphabet.add(inputChar);
    }

    public NFAState getEndState(){
        assert endState != null : NFA.class.getName() + ": endState为null";
        return endState;
    }

    public NFAState getStartState(){
        assert startState != null : NFA.class.getName() + ": startState为null";
        return startState;
    }


    /*
     * 在RE转NFA中，运用Thompson算法，实现并，连接，闭包三种运算
     */

    /**
     * 两个正则表达式的并的NFA
     * @param nextNFA 需要并计算的NFA
     * @param id 当前的id
     * @return 分配后的id值
     */
    public int union(NFA nextNFA, int id){

        //更新输入字母表
        inputAlphabet.addAll(nextNFA.inputAlphabet);
        assert inputAlphabet.containsAll(nextNFA.inputAlphabet) : NFA.class.getName() + ": 新的字母表中不完全包含后继状态的字母表";

        //新增两个状态，一个开始状态，一个结束状态
        NFAState curStartState = new NFAState(id++);
        NFAState curEndState = new NFAState(id++, true);

        //添加两条从新的开始状态分别到两个NFA开始状态的边
        curStartState.addEdge(new NFAEdge(startState, NFA.EPSILON));
        curStartState.addEdge(new NFAEdge(nextNFA.startState, NFA.EPSILON));

        assert endState != null : NFA.class.getName() + ": first的结束状态为null";
        assert nextNFA.endState != null : NFA.class.getName() + ": second的结束状态为null";

        //添加两条分别从两个NFA的结束状态到新的结束状态的边
        endState.addEdge(new NFAEdge(curEndState, NFA.EPSILON));
        nextNFA.endState.addEdge(new NFAEdge(curEndState, NFA.EPSILON));

        //取消原来两个NFA的结束状态
        endState.setEndState(false);
        nextNFA.endState.setEndState(false);

        //添加新状态到状态集合中
        states.addAll(nextNFA.states);
        states.add(curStartState);
        states.add(curEndState);

        //重设开始和结束状态
        startState = curStartState;
        endState = curEndState;

        return id;
    }

    /**
     * 两个正则表达式的连接的NFA，没有新增状态，故不需要新分配id值
     * @param postNFA 后继NFA
     */
    public void concat(NFA postNFA){

        //更新输入字母表
        inputAlphabet.addAll(postNFA.inputAlphabet);
        assert inputAlphabet.containsAll(postNFA.inputAlphabet) : NFA.class.getName() + ": 新的字母表中不完全包含后继状态的字母表";

        //将后继NFA的开始状态的邻接表加入到当前NFA的结束状态的邻接表中，除了通过epsilon到达自身状态的边
        for(NFAEdge edge : postNFA.startState.getAdjacentcentList()){
            if(inputAlphabet.contains(edge.getLabel())) endState.addEdge(edge);
        }

        //添加新加入的NFA状态图的所有状态，并删除postNFA的状态，重设结束状态
        states.addAll(postNFA.states);
        states.remove(postNFA.startState);
        endState.setEndState(false);
        endState = postNFA.endState;

        assert endState.equals(postNFA.endState);
        assert !states.contains(postNFA.startState);
    }

    /**
     * 一个正则表达式的闭包的NFA
     * @param id 当前的id值
     * @return 分配后的id值
     */
    public int closure(int id){

        //新增两个状态，一个开始状态，一个结束状态
        NFAState curStartState = new NFAState(id++);
        NFAState curEndState = new NFAState(id++, true);

        //添加两条边，分别从当前的开始状态到原开始状态, 从当前开始状态到当前结束状态
        curStartState.addEdge(new NFAEdge(startState, NFA.EPSILON));
        curStartState.addEdge(new NFAEdge(curEndState, NFA.EPSILON));

        //添加两条边，分别从原结束状态到原开始状态，从原结束状态到当前结束状态
        endState.addEdge(new NFAEdge(startState, NFA.EPSILON));
        endState.addEdge(new NFAEdge(curEndState, NFA.EPSILON));

        //更新当前开始和结束状态
        startState = curStartState;
        endState = curEndState;

        assert !states.contains(startState) && !(states.contains(endState)) : NFA.class.getName();
        //向状态集合中添加新的状态
        states.add(startState);
        states.add(endState);

        return id;
    }


    /*
     * 在NFA转DFA中，用于寻找epsilon闭包
     */

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

    /**
     * 在源状态上通过标号移动，找到能到达的NFA状态集合
     * @param srcState  源状态
     * @param label 标号
     * @return  能到达的NFA状态集合
     */
    private Set<NFAState> moveState(NFAState srcState, char label){
        return srcState.findDestStateByLabel(label);
    }

    /**
     * 返回输入字母表
     * @return  输入字母表
     */
    public Set<Character> getInputAlphabet() {
        return inputAlphabet;
    }

    /**
     * 打印NFA中所有状态
     */
    public void printNFA(){
        System.out.println("开始状态:");
        startState.printNFAState();
        System.out.println("结束状态:");
        endState.printNFAState();
        System.out.println("-----------------------------");
        for(NFAState state : states){
            state.printNFAState();
        }
        System.out.println("-----------------------------");
    }

    public int getStatesNum(){
        return states.size();
    }

}
