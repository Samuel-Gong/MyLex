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
        patterns.add(new Pattern("1", "abb", 0));
        patternProcessor = new PatternProcessor(patterns);
    }

    //测试转中缀转后缀表达式
    @Test
    public void createAnalysisTree() throws Exception {
        Assert.assertEquals("(ab|)(cdel)|",
                patternProcessor.createAnalysisTree("(a|b)|(cdel)"));
        Assert.assertEquals("((ab|)*(cd|)|)*",
                patternProcessor.createAnalysisTree("((a|b)*|(c|d))*"));
        Assert.assertEquals("((a b|)*(cd|)|)*",
                patternProcessor.createAnalysisTree("((a |b)*|(c|d))*"));
        Assert.assertEquals("(ab|)*abb",
                patternProcessor.createAnalysisTree("(a|b)*abb"));

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
