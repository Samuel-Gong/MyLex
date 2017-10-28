package mylex.LexAnalyzer;

import mylex.LexAnalyzer.dfa.DFA;
import mylex.LexAnalyzer.dfa.DFAState;
import mylex.vo.Token;

import java.util.ArrayList;
import java.util.List;

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
                tokens.add(new Token(dfa.findPatternByDFAState(curState).name, sb.toString()));

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
        tokens.add(new Token(dfa.findPatternByDFAState(curState).name, sb.toString()));

        assert tokens.size() > 0 : "解析出来的Token个数不可能为0";
        return tokens;
    }

}
