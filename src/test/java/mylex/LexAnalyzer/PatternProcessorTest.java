package mylex.LexAnalyzer;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PatternProcessorTest {

    PatternProcessor patternProcessor;

    @Before
    public void setUp(){
        patternProcessor = new PatternProcessor();
    }

    //测试无扩展字符下直接返回结果
    @Test
    public void simplifyPattern() throws Exception {
        assertSame("joke[rs]", patternProcessor.simplifyPattern("joke[rs]"));
    }

}
