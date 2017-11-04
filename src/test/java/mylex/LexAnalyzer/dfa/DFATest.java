package mylex.LexAnalyzer.dfa;

import mylex.LexAnalyzer.nfa.NFA;
import mylex.LexAnalyzer.patternProcessor.PatternProcessor;
import mylex.vo.Pattern;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class DFATest {

    List<DFA> dfaList;

    @Before
    public void setUp(){
        List<Pattern> patterns = new ArrayList<>();
//        patterns.add(new Pattern("1", "(aa)?", 0));
//        patterns.add(new Pattern("2", "(a|b)?", 1));
        patterns.add(new Pattern("1", "if", 1));
        patterns.add(new Pattern("2", "else", 2));
        PatternProcessor patternProcessor = new PatternProcessor(patterns);

        List<NFA> nfaList = patternProcessor.combinePatterns();
        dfaList = new ArrayList<>();
        for (int i = 0; i < nfaList.size(); i++) {
            dfaList.add(new DFA(nfaList.get(i)));
        }
    }

    @Test
    public void dtran() throws Exception {
        for (int i = 0; i < dfaList.size(); i++) {
            System.out.println("第" + i + "个DFA");
            DFA dfa = dfaList.get(i);
            dfa.Dtran();
            dfa.printDFA();
        }
    }

}