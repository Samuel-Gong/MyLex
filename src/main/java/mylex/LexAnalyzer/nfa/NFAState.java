package mylex.LexAnalyzer.nfa;

import java.util.ArrayList;
import java.util.List;

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
    List<NFAEdge> adjacentList;

    public NFAState(int id){
        this.id = id;
        isEndState = false;

        setAdjacentList();
    }

    public NFAState(int id, boolean isEndState) {
        this(id);
        this.isEndState = isEndState;
        setAdjacentList();
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

    public boolean isEndState() {
        return isEndState;
    }

    private void setAdjacentList() {
        adjacentList = new ArrayList<>();
        //新建状态的时候新建一条通过epsilon到自己的边
        adjacentList.add(new NFAEdge(this, NFA.EPSILON));
    }
}
