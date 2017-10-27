package mylex.LexAnalyzer;

import mylex.LexAnalyzer.nfa.NFA;
import mylex.LexAnalyzer.nfa.NFAState;
import mylex.vo.RegExpVO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * 将输入的每一个正则表达式，转换成NFA，并将所有的NFA合并
 */
public class PatternProcessor {

    /**
     * 记录当前分配给NFA的id
     */
    private int id;

    /**
     * 每个名称对应的正则表达式
     */
    private Map<String, RegExpVO> namePatternMap;

    public PatternProcessor(Map<String, RegExpVO> namePatternMap) {
        id = 0;
        this.namePatternMap = namePatternMap;

    }

    /**
     * 根据语法分析树的后缀表达式,构建NFA
     *
     * @param regExpPostfix 语法分析树的后缀表达式
     * @return 对应该语法分析树的后缀表达式
     */
    public NFA createNFAOnePattern(String regExpPostfix) {
        Stack<Object> stack = new Stack<>();
        for (int i = 0; i < regExpPostfix.length(); i++) {
            char c = regExpPostfix.charAt(i);
            if (isOperand(c)) {
                NFAState startState = new NFAState(id++);
                NFAState endState = new NFAState(id++, true);
                NFA nfa = new NFA(startState, endState, c);
                stack.push(nfa);
            }
            //求一个正则表达式的闭包的NFA
            if (c == '*') {
                assert stack.peek() instanceof NFA : ": 正则表达式有误";
                //求闭包，并更新id
                NFA nfa = (NFA) stack.pop();
                id = (nfa).closure(id);
                stack.push(nfa);
                continue;
            }
            //求两个正则表达式的并的NFA
            if (c == '|') {
                assert stack.peek() instanceof NFA : ": 正则表达式有误";
                NFA second = (NFA) stack.pop();
                assert stack.peek() instanceof NFA : ": 正则表达式有误";
                NFA first = (NFA) stack.pop();
                //并运算，并更新id
                id = (first.union(second, id));
                stack.push(first);
                continue;
            }
            if (c == '(') {
                stack.push(c);
                continue;
            }
            //对左括号之前的所有NFA状态做连接
            if (c == ')') {
                List<NFA> needToConcat = new ArrayList<>();
                assert !stack.empty() : ": 正则表达式有误";
                while (!stack.empty()) {
                    Object obj = stack.pop();
                    //判断是否是字符，若是字符，则必是(,说明该（）分组结束
                    if (obj instanceof Character) {
                        assert (Character) obj == '(' : "：正则表达式有误";
                        break;
                    }
                    if (obj instanceof NFA) {
                        needToConcat.add(0, (NFA) obj);
                    }
                }

                //说明（）中间没有被压栈的NFA
                if (needToConcat.isEmpty()) {
                } else stack.push(concatNFA(needToConcat));
            }
        }


        //最后将栈中剩余的所有的NFA全部连接起来
        List<NFA> needToConcat = new ArrayList<>();
        assert !stack.empty() : ": 正则表达式有误";
        while (!stack.empty()) {
            Object obj = stack.pop();

            assert obj instanceof NFA : ": 正则表达式有误";
            needToConcat.add(0, (NFA) obj);
        }
        stack.push(concatNFA(needToConcat));

        assert stack.size() == 1 : ": 正则表达式有误";

        return (NFA) stack.pop();
    }

    private NFA concatNFA(List<NFA> needToConcat) {
        assert !needToConcat.isEmpty() : "：（）之间不可能为空";
        NFA nfa = needToConcat.get(0);
        for (int i = 1; i < needToConcat.size(); i++) {
            nfa.concat(needToConcat.get(i));
        }
        return nfa;
    }

    /**
     * 给基础正则表达式构造语法分析树的后缀表达式，只有()*|等操作符
     *
     * @param regExp 需要构造语法分析树的正则表达式
     * @return
     */

    public String createAnalysisTree(String regExp) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < regExp.length(); i++) {
            char c = regExp.charAt(i);
            if (c == '(') {
                sb.append(c);
                continue;
            }
            if (c == ')') {
                sb.append(c);
                continue;
            }
            if (c == '*') {
                sb.append(c);
                continue;
            }
            if (isOperand(c)) {
                sb.append(c);
                continue;
            }
            if (c == '|') {
                //最后一个符号不可能是|，否则就是有错
                assert i < regExp.length() - 1 : "正则表达式有错";

                char nextChar = regExp.charAt(++i);
                if (isOperand(nextChar)) {
                    sb.append(nextChar);
                    assert i < regExp.length() - 1 : "正则表达式有错";
                    char nextCharOfNext = regExp.charAt(++i);
                    if (nextCharOfNext == '*') {
                        while (nextCharOfNext == '*' && i != regExp.length()) {
                            sb.append('*');
                            if (i < regExp.length() - 1) {
                                nextCharOfNext = regExp.charAt(++i);
                            }
                        }
                    } else i--;
                } else if (nextChar == '(') {
                    int left = 1;
                    StringBuilder regExpInParentheseSb = new StringBuilder();
                    regExpInParentheseSb.append('(');
                    while (left != 0 && i != regExp.length() - 1) {
                        char nextCharOfNext = regExp.charAt(++i);
                        if (nextCharOfNext == ')') {
                            regExpInParentheseSb.append(nextCharOfNext);
                            left--;
                        } else regExpInParentheseSb.append(nextCharOfNext);
                    }
                    assert left == 0 : "没有右括号匹配";
                    sb.append(createAnalysisTree(regExpInParentheseSb.toString()));
                } else {
                    //TODO
                    assert false : "该情况暂未考虑";
                }
                sb.append('|');
            }

        }
        return sb.toString();
    }

    /**
     * 判断是否是操作数
     *
     * @param c 字符
     * @return
     */
    private boolean isOperand(char c) {
        //TODO 暂且认为操作数为数字,字母和空格
        return Character.isLetterOrDigit(c) || Character.isWhitespace(c);
    }
}
