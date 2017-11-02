package mylex.LexAnalyzer.dfa;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DFAState {

    static Logger logger = Logger.getLogger(DFA.class.getName());

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

    /**
     * 标记是否是接受状态
     */
    private boolean isEndState;

    public DFAState(int id, boolean isEndState){
        this.adjacentList = new HashMap<>();
        isLabeled = false;
        this.id = id;
        this.isEndState = isEndState;
    }

    public boolean isEndState(){
        return isEndState;
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

    public void printDFAState() {
        System.out.println("id：" + id + "        结束状态：" + isEndState);
        for (Map.Entry<Character, DFAState> entry : adjacentList.entrySet()){
            System.out.println("符号：" + entry.getKey() +"       目的状态id：" + entry.getValue().id);
        }
    }

    public int getID(){
        return id;
    }

    /**
     * 根据label向后移动，若邻接表中有目的状态，则返回，否则返回null
     * @param label 输入符号
     * @return 目的状态
     */
    public DFAState move(char label){
        DFAState nextState = adjacentList.get(label);
        if (nextState != null) logger.info(id + " move through " + label + " to " + adjacentList.get(label).id);
        return adjacentList.get(label);
    }

    /**
     * 获取该状态下可读取的输入字符表
     *
     * @return 输入字符集合
     */
    public Set<Character> getAlphabet() {
        return adjacentList.keySet();
    }
}
