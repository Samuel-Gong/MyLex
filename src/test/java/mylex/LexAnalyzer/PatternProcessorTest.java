package mylex.LexAnalyzer;

import mylex.LexAnalyzer.nfa.NFA;
import mylex.vo.RegExpVO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

public class PatternProcessorTest {

    PatternProcessor patternProcessor;

    @Before
    public void setUp(){
        //TODO
        Map<String, RegExpVO> map = null;
        patternProcessor = new PatternProcessor(map);
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
    }

    @Test
    public void createNFAOnePattern() throws Exception {
        NFA nfa = patternProcessor.createNFAOnePattern(patternProcessor.createAnalysisTree("a*"));
        nfa.printNFA();
    }

}
