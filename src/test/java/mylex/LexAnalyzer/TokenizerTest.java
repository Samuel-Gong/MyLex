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

    @Before
    public void setUp(){
        List<Pattern> patterns = new ArrayList<>();
        patterns.add(new Pattern("aLoop" , "a*", 1));

        PatternProcessor patternProcessor = new PatternProcessor(patterns);
        NFA nfa = patternProcessor.combinePatterns();
        DFA dfa = new DFA(nfa);
        DFAOptimizer dfaOptimizer = new DFAOptimizer(dfa.Dtran());
        tokenizer = new Tokenizer(dfaOptimizer.constructOptimizedDFA());
    }

    @Test
    public void getTokens() throws Exception {
        List<Token> tokens = tokenizer.getTokens("aaaaaaaaaa");
        Token first = tokens.get(0);
        Assert.assertEquals("aLoop", first.getName());
        Assert.assertEquals("aaaaaaaaaa", first.getValue());
    }

}