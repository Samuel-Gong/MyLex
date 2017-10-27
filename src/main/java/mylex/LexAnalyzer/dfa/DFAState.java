package mylex.LexAnalyzer.dfa;

import java.util.HashMap;
import java.util.Map;

public class DFAState {

    /**
     * 由DFA的特性可知，由源状态出发，唯一一条边到达唯一一个状态，故利用map实现的邻接表
     */
    private Map<Character, DFAState> adjacentList;

    /**
     * 标注该DFAState的id
     */
    private int id;

    /**
     * 是否被标记
     */
    private boolean isLabeled;

    public DFAState(int id){
        this.adjacentList = new HashMap<>();
        isLabeled = false;
        this.id = id;
    }

    public boolean isLabeled() {
        return isLabeled;
    }

    /**
     * 判断两个DFAState是否相同
     * 若两个DFAState的id相同，则说明两个DFAState相同
     * @param obj   另一个DFAState对象
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        return id == ((DFAState)obj).id;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = result * 31 + id;
        return result;
    }

    public void setLabeled(boolean labeled) {
        isLabeled = labeled;
    }


    public Map<Character, DFAState> getAdjacentList() {
        return adjacentList;
    }

    public void addEdge(char label, DFAState destState){
        adjacentList.put(label, destState);
    }
}
