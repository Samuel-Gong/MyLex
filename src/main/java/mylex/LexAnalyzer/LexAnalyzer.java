package mylex.LexAnalyzer;

import mylex.LexAnalyzer.dfa.DFA;
import mylex.LexAnalyzer.dfa.DFAOptimizer;
import mylex.LexAnalyzer.nfa.NFA;
import mylex.vo.Pattern;

import java.util.List;

public class LexAnalyzer {

    /**
     * 根据传入的模式，构造一个基于优化后的DFA词法分析器
     * @param patterns 解析.l文件后的所有模式
     * @return 词法分析器
     */
    public Tokenizer createTokenizer(List<Pattern> patterns){
        PatternProcessor patternProcessor = new PatternProcessor(patterns);
        NFA nfa = patternProcessor.combinePatterns();
        DFA dfa = new DFA(nfa);
        DFAOptimizer dfaOptimizer = new DFAOptimizer(dfa.Dtran());
        return new Tokenizer(dfaOptimizer.constructOptimizedDFA());
    }

}
