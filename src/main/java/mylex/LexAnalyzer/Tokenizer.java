package mylex.LexAnalyzer;

import mylex.LexAnalyzer.dfa.DFA;
import mylex.LexAnalyzer.dfa.DFAState;
import mylex.LexAnalyzer.patternProcessor.PatternProcessor;
import mylex.vo.Pattern;
import mylex.vo.Token;

import java.util.*;

public class Tokenizer {

    /**
     * 经过优化后的DFA
     */
    private DFA dfa;

    public Tokenizer(DFA dfa) {
        this.dfa = dfa;
    }

    /**
     * 根据用户输入的字符串，获取其中所有的Token
     *
     * @return 字符流中的Token序列
     */
    public List<Token> getTokens(String input) {

        assert inputCharsInAlphabet(dfa, input) : ": 输入中有操作数不在DFA的输入字母表中";

        StringBuilder sb = new StringBuilder();
        List<Token> tokens = new ArrayList<>();

        //初始状态是DFA的初始状态
        DFAState curState = dfa.getStartState();

        for (int curPos = 0; curPos < input.length(); curPos++) {
            char c = input.charAt(curPos);
            DFAState nextState = curState.move(c);
            assert !curState.isEndState() || nextState != null : Tokenizer.class.getName()
                    + ": 当前状态既不是接受状态，且下一个状态为空，说明没有对应的Pattern，该输入无法解析";
            //当前状态为接受状态，且下一个状态为空，则说明当前状态即为匹配之前输入的最终状态
            if (curState.isEndState() && nextState == null) {
                tokens.add(new Token(findTopPrecedencePattern(curState, sb.toString()).name, sb.toString()));

                //清空StringBuilder
                sb.delete(0, sb.length());
                nextState = dfa.getStartState();
                curPos--;
                curState = nextState;
            }else {
                sb.append(c);
                curState = nextState;
            }
        }

        assert curState.isEndState() : ": 最后的状态不在结束状态上，该String解析错误";
        //将最后一个状态加入Toke序列中
        tokens.add(new Token(findTopPrecedencePattern(curState, sb.toString()).name, sb.toString()));

        assert tokens.size() > 0 : "解析出来的Token个数不可能为0";
        return tokens;
    }

    /**
     * 判断输入字符串中的操作数字符是否全在DFA的字母表中
     *
     * @param dfa   需要判断的DFA的输入字母表
     * @param input 输入字符串
     * @return
     */
    private boolean inputCharsInAlphabet(DFA dfa, String input) {

        Set<Character> inputCharSet = new HashSet<>();
        char inputChars[] = input.toCharArray();
        for (int i = 0; i < inputChars.length; i++) {
            if (PatternProcessor.isOperand(inputChars[i])) inputCharSet.add(inputChars[i]);
        }

        return dfa.getInputAlphabet().containsAll(inputCharSet);
    }

    /**
     * 根据获取的输入，找到匹配当前状态的优先级最高的Pattern
     *
     * @param curState 当前结束状态
     * @param input    输入字符串
     * @return
     */
    private Pattern findTopPrecedencePattern(DFAState curState, String input) {
        List<Pattern> patterns = dfa.findPatternsByDFAState(curState);

        //对所有Pattern进行优先级排序，优先级高的在前
        patterns.sort(new Comparator<Pattern>() {
            @Override
            public int compare(Pattern o1, Pattern o2) {
                return o1.precedence - o2.precedence;
            }
        });

        assert patterns.size() >= 1 : "没找到对应的Pattern集合";
        if (patterns.size() == 1) return patterns.get(0);
        else {
            //对每个pattern构造一个DFA，并对输入获取Token，选取第一个Token序列长度为1的Pattern
            LexAnalyzer lexAnalyzer = new LexAnalyzer();
            for (int i = 0; i < patterns.size(); i++) {
                List<Pattern> patternList = new ArrayList<>();
                patternList.add(patterns.get(0));
                Tokenizer tokenizer = lexAnalyzer.createTokenizer(patternList);
                if (tokenizer.checkPatternWithOneToken(tokenizer.dfa, input)) return patterns.get(i);
            }
        }

        return null;
    }

    /**
     * 验证该Pattern是否对于该输入字符串的Token序列长度为1
     *
     * @param input 输入字符串
     * @return
     */
    private boolean checkPatternWithOneToken(DFA dfa, String input) {

        if (!inputCharsInAlphabet(dfa, input)) return false;

        StringBuilder sb = new StringBuilder();

        //初始状态是DFA的初始状态
        DFAState curState = dfa.getStartState();

        for (int curPos = 0; curPos < input.length(); curPos++) {
            char c = input.charAt(curPos);
            DFAState nextState = curState.move(c);
            //当前状态既不是接受状态，且下一个状态为空，说明没有对应的Pattern，该输入无法解析
            if (!curState.isEndState() && nextState == null) return false;
            //当前状态为接受状态，且下一个状态为空，则说明当前状态即为匹配之前输入的最终状态，而后面还有需要匹配的字符，返回false
            if (curState.isEndState() && nextState == null) {
                return false;
            } else {
                sb.append(c);
                curState = nextState;
            }
        }

        //最后状态不在结束状态上，该String解析错误
        if (!curState.isEndState()) return false;

        return true;
    }

}
