package mylex.LexAnalyzer.dfa;

import mylex.LexAnalyzer.nfa.NFA;
import mylex.LexAnalyzer.patternProcessor.PatternProcessor;
import mylex.vo.Pattern;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class DFAOptimizerTest {

    DFAOptimizer dfaOptimizer;

    @Before
    public void setUp(){
        List<Pattern> patterns = new ArrayList<>();
        patterns.add(new Pattern("1", "a{0,1}", 0));
        patterns.add(new Pattern("2", "b{2}", 1));
        PatternProcessor patternProcessor = new PatternProcessor(patterns);

        NFA nfa = patternProcessor.combinePatterns();
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