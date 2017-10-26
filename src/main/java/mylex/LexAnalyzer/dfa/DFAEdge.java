package mylex.LexAnalyzer.dfa;

public class DFAEdge {

    /**
     * 目标状态
     */
    private DFAState destState;

    /**
     * 边的标号
     */
    private char label;

    public DFAEdge(DFAState destState, char label){
        this.destState = destState;
        this.label = label;
    }

    public DFAState getDestState() {
        return destState;
    }
}
