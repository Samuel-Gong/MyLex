package mylex.LexAnalyzer;

import mylex.LexAnalyzer.dfa.DFA;
import mylex.LexAnalyzer.dfa.DFAOptimizer;
import mylex.LexAnalyzer.nfa.NFA;
import mylex.LexAnalyzer.patternProcessor.PatternProcessor;
import mylex.vo.Pattern;
import mylex.vo.Token;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TokenizerTest {

    Tokenizer tokenizer;
    List<DFA> dfaList;

    @Before
    public void setUp(){
        List<Pattern> patterns = new ArrayList<>();
        patterns.add(new Pattern("if", "if", 1));
        patterns.add(new Pattern("else", "else", 1));

        PatternProcessor patternProcessor = new PatternProcessor(patterns);
        List<NFA> nfaList = patternProcessor.combinePatterns();
        dfaList = new ArrayList<>();
        for (int i = 0; i < nfaList.size(); i++) {
            DFAOptimizer dfaOptimizer = new DFAOptimizer(new DFA(nfaList.get(i)).Dtran());
            dfaList.add(dfaOptimizer.constructOptimizedDFA());
        }
        tokenizer = new Tokenizer(dfaList);
    }

    @Test
    public void getTokens() throws Exception {
        for (int i = 0; i < dfaList.size(); i++) {
            dfaList.get(i).printDFA();
        }
        List<Token> tokens = tokenizer.getTokens("ifelse");
        Token first = tokens.get(0);
        Token second = tokens.get(1);
        Assert.assertEquals("if", first.getName());
        Assert.assertEquals("else", second.getName());
    }

}