package mylex.LexAnalyzer.dfa;

import mylex.LexAnalyzer.PatternProcessor;
import mylex.LexAnalyzer.nfa.NFA;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DFAOptimizerTest {

    DFAOptimizer dfaOptimizer;

    @Before
    public void setUp(){
        PatternProcessor patternProcessor = new PatternProcessor(null);
        NFA nfa = patternProcessor.createNFAOnePattern(patternProcessor.createAnalysisTree("a*"));
        DFA dfa = new DFA(nfa);
        dfa.Dtran();
        dfaOptimizer = new DFAOptimizer(dfa);
    }

    @Test
    public void constructOptimizedDFA() throws Exception {
        DFA newDFA = dfaOptimizer.constructOptimizedDFA();
        newDFA.printDFA();
    }

}