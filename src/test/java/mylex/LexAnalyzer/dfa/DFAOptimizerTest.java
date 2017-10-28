package mylex.LexAnalyzer.dfa;

import mylex.LexAnalyzer.PatternProcessor;
import mylex.LexAnalyzer.nfa.NFA;
import mylex.vo.Pattern;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class DFAOptimizerTest {

    DFAOptimizer dfaOptimizer;

    @Before
    public void setUp(){
        List<Pattern> patterns = new ArrayList<>();
        patterns.add(new Pattern("Aloop", "a*", 0));
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