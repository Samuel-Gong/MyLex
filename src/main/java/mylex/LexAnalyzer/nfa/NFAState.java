package mylex.LexAnalyzer.nfa;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NFAState {

    /**
     * 标注该状态为第几个状态
     */
    private int id;

    /**
     * 是否是结束状态
     */
    private boolean isEndState;

    /**
     * 邻接表
     */
    private List<NFAEdge> adjacentList;

    public NFAState(int id){
        this.id = id;
        isEndState = false;

        initAdjacentList();
    }

    public NFAState(int id, boolean isEndState) {
        this(id);
        this.isEndState = isEndState;
        initAdjacentList();
    }

    public boolean isEndState() {
        return isEndState;
    }

    /**
     * 向邻接表中添加一条鞭
     * @param edge
     */
    public void addEdge(NFAEdge edge){
        adjacentList.add(edge);
    }

    /**
     * 找到能通过label到达的NFA状态集合
     * @param label 标记
     * @return 能到达的NFA状态集合
     */
    public Set<NFAState> findDestStateByLabel(char label) {
        Set<NFAState> stateClosure = new HashSet<>();
        for (NFAEdge edge : adjacentList) {
            if(edge.getLabel() == label){
                stateClosure.add(edge.getDestState());
            }
        }
        return stateClosure;
    }

    public void setEndState(boolean endState) {
        isEndState = endState;
    }

    public List<NFAEdge> getAdjacentcentList(){
        return adjacentList;
    }

    @Override
    public boolean equals(Object obj) {
        return this.id == ((NFAState) obj).id;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = result * 31 + id;
        return result;
    }

    /**
     * 初始化邻接表
     */
    private void initAdjacentList() {
        adjacentList = new ArrayList<>();
        //新建状态的时候新建一条通过epsilon到自己的边
        adjacentList.add(new NFAEdge(this, NFA.EPSILON));
    }
}
