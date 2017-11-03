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
        patterns.add(new Pattern("1", "a{0,1}", 0));
        patterns.add(new Pattern("2", "b{2}", 0));
        patternProcessor = new PatternProcessor(patterns);
    }

    //测试语法分析树生成
    @Test
    public void createAnalysisTree() throws Exception {
        //基本正则表达式
        Assert.assertEquals("(ab|)*abb",
                patternProcessor.createAnalysisTree("(a|b)*abb"));
        //零次或一次
        Assert.assertEquals("((ab|)*)?", patternProcessor.createAnalysisTree("((a|b)*)?"));
        //一次或多次
        Assert.assertEquals("(ab|c|)+", patternProcessor.createAnalysisTree("(a|b|c)+"));
        //中括号并
        Assert.assertEquals("[abc]+", patternProcessor.createAnalysisTree("[abc]+"));
        //大括号重复
        Assert.assertEquals("a{3,5}", patternProcessor.createAnalysisTree("a{3,5}"));
        //转译
        Assert.assertEquals(".\\", patternProcessor.createAnalysisTree("\\."));

    }

    @Test
    public void combinePatterns() throws Exception {
        NFA nfa = patternProcessor.combinePatterns();
        nfa.printNFA();
    }

}
