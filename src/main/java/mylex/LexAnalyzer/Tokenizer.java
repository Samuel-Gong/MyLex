package mylex.LexAnalyzer;

import mylex.LexAnalyzer.dfa.DFA;
import mylex.LexAnalyzer.dfa.DFAState;
import mylex.vo.Token;

import java.util.*;

public class Tokenizer {

    /**
     * 经过优化后的DFA
     */
    private List<DFA> dfaList;

    public Tokenizer(List<DFA> dfaList) {
        this.dfaList = dfaList;
    }

    /**
     * 根据用户输入的字符串，获取其中所有的Token
     *
     * @return 字符流中的Token序列
     */
    public List<Token> getTokens(String input) {

        StringBuilder sb = new StringBuilder();
        List<Token> tokens = new ArrayList<>();

        //保存当前每个DFA移动的状态
        Map<Integer, DFAState> curStatesMap = new TreeMap<>(new MyComparator());
        for (int i = 0; i < dfaList.size(); i++) {
            curStatesMap.put(i, dfaList.get(i).getStartState());
        }
        //保存对于每个DFA的当前所处状态的下一个状态
        Map<Integer, DFAState> nextStatesMap = new TreeMap<>(new MyComparator());
        for (int curPos = 0; curPos < input.length(); curPos++) {
            char c = input.charAt(curPos);
            for (Map.Entry<Integer, DFAState> entry : curStatesMap.entrySet()) {
                DFAState curState = entry.getValue();
                int key = entry.getKey();
                if (curState != null) {
                    DFAState nextState = curState.move(c);
                    //如果nextState为空，说明对于该输入，此DFA没有对应的接受状态
                    if (nextState == null) {
                        if (nextStatesMap.get(key) != null) nextStatesMap.remove(key);
                    } else nextStatesMap.put(key, nextState);
                }
            }

            //检查下一个状态映射是否为空，说明当前状态映射中的第一个不为空的状态为此词素的接受状态
            if (nextStatesMap.isEmpty()) {
                for (int i = 0; i < curStatesMap.size(); i++) {
                    DFAState curState = curStatesMap.get(i);
                    if (curState != null && curState.isEndState()) {
                        //当前状态为最长匹配，优先级最高的接受状态
                        tokens.add(new Token(dfaList.get(i).getPattern().name, sb.toString()));

                        //清空StringBuilder
                        sb.delete(0, sb.length());
                        curPos--;

                        //初始化当前状态映射
                        for (int j = 0; j < dfaList.size(); j++) {
                            curStatesMap.put(j, dfaList.get(j).getStartState());
                        }

                        break;
                    }
                }
            }
            //不为空，则当前词素加上字符c
            else {
                sb.append(c);
                //更新当前状态映射
                curStatesMap = new TreeMap<>(new MyComparator());
                curStatesMap.putAll(nextStatesMap);
            }
        }

        //将最后匹配的字符串序列加入到当中Token序列中
        DFAState endState = null;
        for (Map.Entry<Integer, DFAState> entry : curStatesMap.entrySet()) {
            DFAState curState = entry.getValue();
            int key = entry.getKey();
            if (curState != null && curState.isEndState()) {
                tokens.add(new Token(dfaList.get(key).getPattern().name, sb.toString()));
                endState = curState;
            }
        }

        assert endState != null : ": 最后的状态不在结束状态上" + sb.toString() + "无法解析";

        assert tokens.size() > 0 : "解析出来的Token个数不可能为0";
        return tokens;
    }
}

class MyComparator implements Comparator<Integer> {
    @Override
    public int compare(Integer o1, Integer o2) {
        return o1 - o2;
    }
}
