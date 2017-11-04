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
    List<DFA> dfaList;

    @Before
    public void setUp(){
        List<Pattern> patterns = new ArrayList<>();
        patterns.add(new Pattern("1", "if", 0));
        patterns.add(new Pattern("2", "else", 1));
        PatternProcessor patternProcessor = new PatternProcessor(patterns);

        List<NFA> nfaList = patternProcessor.combinePatterns();
        dfaList = new ArrayList<>();
        for (int i = 0; i < nfaList.size(); i++) {
            dfaList.add(new DFA(nfaList.get(i)));
        }
    }

    @Test
    public void constructOptimizedDFA() throws Exception {
        for (int i = 0; i < dfaList.size(); i++) {
            dfaOptimizer = new DFAOptimizer(dfaList.get(i).Dtran());
            DFA newDFA = dfaOptimizer.constructOptimizedDFA();
            newDFA.printDFA();
        }
    }
}