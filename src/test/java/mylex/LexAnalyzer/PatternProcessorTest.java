package mylex.LexAnalyzer;

import mylex.LexAnalyzer.nfa.NFA;
import mylex.vo.Pattern;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class PatternProcessorTest {

    PatternProcessor patternProcessor;

    @Before
    public void setUp(){
        List<Pattern> patterns = new ArrayList<>();
        patterns.add(new Pattern("1", "(aa)?", 0));
        patterns.add(new Pattern("2", "(a|b)?", 1));
        patternProcessor = new PatternProcessor(patterns);
    }

    //测试转中缀转后缀表达式
    @Test
    public void createAnalysisTree() throws Exception {
        Assert.assertEquals("(ab|)*abb",
                patternProcessor.createAnalysisTree("(a|b)*abb"));
        Assert.assertEquals("((ab|)*)?", patternProcessor.createAnalysisTree("((a|b)*)?"));


    }

    @Test
    public void createNFAOnePattern() throws Exception {
        NFA nfa = patternProcessor.combinePatterns();
        nfa.printNFA();
    }

    @Test
    public void combinePatterns() throws Exception {
        NFA nfa = patternProcessor.combinePatterns();
        nfa.printNFA();
    }

}
