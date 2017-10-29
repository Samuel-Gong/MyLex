package mylex.LexAnalyzer.dfa;

import mylex.LexAnalyzer.PatternProcessor;
import mylex.vo.Pattern;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class DFATest {

    DFA dfa;

    @Before
    public void setUp(){
        List<Pattern> patterns = new ArrayList<>();
        patterns.add(new Pattern("1", "(aa)?", 0));
        patterns.add(new Pattern("2", "(a|b)?", 1));
        PatternProcessor patternProcessor = new PatternProcessor(patterns);

        dfa = new DFA(patternProcessor.combinePatterns());
    }

    @Test
    public void dtran() throws Exception {
        dfa.Dtran();
        dfa.printDFA();
    }

}