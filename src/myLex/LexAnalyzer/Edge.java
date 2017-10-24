package myLex.LexAnalyzer;

public class Edge {

    /**
     * 起始状态
     */
    private State startState;

    /**
     * 结束状态
     */
    private State endState;

    /**
     * 边的值
     */
    private int value;

    public Edge(State endState) {
        startState = null;
        this.endState = endState;
        value = 0;
    }

    public Edge(State startState, State endState, int value){
        this.startState = startState;
        this.endState = endState;
        this.value = value;
    }
}
