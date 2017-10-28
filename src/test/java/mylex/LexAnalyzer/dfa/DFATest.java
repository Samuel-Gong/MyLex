package mylex.LexAnalyzer.dfa;

import mylex.LexAnalyzer.PatternProcessor;
import org.junit.Before;
import org.junit.Test;

public class DFATest {

    DFA dfa;

    @Before
    public void setUp(){
        PatternProcessor patternProcessor = new PatternProcessor(null);
        dfa = new DFA(patternProcessor.createNFAOnePattern(patternProcessor.createAnalysisTree("a*")));
    }

    @Test
    public void dtran() throws Exception {
        dfa.Dtran();
        dfa.printDFA();
    }

}