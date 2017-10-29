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
//        patterns.add(new Pattern("1", "(aa)?", 0));
//        patterns.add(new Pattern("2", "(a|b)?", 1));
        patterns.add(new Pattern("1", "[ab]+", 0));
        patternProcessor = new PatternProcessor(patterns);
    }

    //测试转中缀转后缀表达式
    @Test
    public void createAnalysisTree() throws Exception {
        //基本正则表达式测试
        Assert.assertEquals("(ab|)*abb",
                patternProcessor.createAnalysisTree("(a|b)*abb"));
        //零次或一次操作测试
        Assert.assertEquals("((ab|)*)?", patternProcessor.createAnalysisTree("((a|b)*)?"));
        //一次或多次操作测试
        Assert.assertEquals("(ab|c|)+", patternProcessor.createAnalysisTree("(a|b|c)+"));
        //中括号并的操作测试
        Assert.assertEquals("[abc]+", patternProcessor.createAnalysisTree("[abc]+"));

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
