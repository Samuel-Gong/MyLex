package mylex.LexAnalyzer.nfa;

public class NFAEdge {

    /**
     * 目标状态
     */
    private NFAState destState;

    /**
     * 边的值
     */
    private char label;

    public NFAEdge(NFAState destState, char label) {
        this.destState = destState;
        this.label = label;
    }

    public char getLabel() {
        return label;
    }

    public void setLabel(char label) {
        this.label = label;
    }

    public NFAState getDestState() {
        return destState;
    }

    public void setDestState(NFAState destState) {
        this.destState = destState;
    }

}
