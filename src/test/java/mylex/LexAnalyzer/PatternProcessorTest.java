package mylex.LexAnalyzer;

import mylex.LexAnalyzer.nfa.NFA;
import mylex.LexAnalyzer.patternProcessor.PatternProcessor;
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
        patterns.add(new Pattern("1", "if", 0));
        patterns.add(new Pattern("2", "else", 0));
        patternProcessor = new PatternProcessor(patterns);
    }

    //测试语法分析树生成
    @Test
    public void createAnalysisTree() throws Exception {
        //基本正则表达式
        Assert.assertEquals("(ab|)*abb",
                patternProcessor.infixToPostfix("(a|b)*abb"));
        //零次或一次
        Assert.assertEquals("((ab|)*)?", patternProcessor.infixToPostfix("((a|b)*)?"));
        //一次或多次
        Assert.assertEquals("(ab|c|)+", patternProcessor.infixToPostfix("(a|b|c)+"));
        //中括号并
        Assert.assertEquals("[abc]+", patternProcessor.infixToPostfix("[abc]+"));
        //大括号重复
        Assert.assertEquals("a{3,5}", patternProcessor.infixToPostfix("a{3,5}"));
        //转译
        Assert.assertEquals("\\.", patternProcessor.infixToPostfix("\\."));
        //垂线，转译符号
        Assert.assertEquals("\\.\\\\|", patternProcessor.infixToPostfix("\\.|\\\\"));

    }

    @Test
    public void combinePatterns() throws Exception {
        List<NFA> nfaList = patternProcessor.combinePatterns();
        for (int i = 0; i < nfaList.size(); i++) {
            System.out.println("第" + i + "个NFA:");
            nfaList.get(i).printNFA();
        }
    }
}
