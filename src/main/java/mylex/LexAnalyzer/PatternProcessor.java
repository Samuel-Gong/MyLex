package mylex.LexAnalyzer;

import mylex.LexAnalyzer.nfa.NFA;
import mylex.LexAnalyzer.nfa.NFAEdge;
import mylex.LexAnalyzer.nfa.NFAState;
import mylex.vo.Pattern;

import java.util.*;

/**
 * 将输入的每一个正则表达式，转换成NFA，并将所有的NFA合并
 */
public class PatternProcessor {

    private static char EPSILON = '\0';

    /**
     * 记录当前分配给NFA的id
     */
    private int id;

    /**
     * NFA所有的正则表达式
     */
    private List<Pattern> patterns;

    public PatternProcessor(List<Pattern> patterns) {
        id = 0;
        this.patterns = patterns;
    }

    /**
     * 将所有pattern构建得到的NFA进行合并
     * @return 合并好的NFA，按照龙书P106的合并方式实现
     */
    public NFA combinePatterns(){

        List<NFA> nfaList = new ArrayList<>();

        //结束状态对应的正则表达式的模式
        Map<NFAState, Pattern> endStateToPattern = new HashMap<>();

        //对每个pattern构建一个NFA，添加该NFA的结束状态和pattern的键值对
        for(Pattern pattern : patterns) {
            NFA nfaOnePattern = createNFAOnePattern(createAnalysisTree(pattern.regularExpression));
            nfaList.add(nfaOnePattern);
            endStateToPattern.put(nfaOnePattern.getSimpleNFAEndState(), pattern);
        }

        //合并nfaList
        Set<NFAState> states = new HashSet<>();
        Set<NFAState> endStates = new HashSet<>();
        Set<Character> inputAlphabet = new HashSet<>();

        NFAState startState = new NFAState(id++);

        for(NFA nfa : nfaList){
            //加入到新NFA状态集合
            states.addAll(nfa.getStates());
            //加入到新NFA的结束状态集合
            assert  nfa.getEndStates().size() == 1 : PatternProcessor.class.getName() + "：简单NFA的结束状态集合大小不为1";
            endStates.addAll(nfa.getEndStates());
            //扩充新NFA的输入字母表
            inputAlphabet.addAll(nfa.getInputAlphabet());
            //给新NFA的开始状态新增一条到要连接的NFA状态的开始状态的边
            startState.addEdge(new NFAEdge(nfa.getStartState(), NFA.EPSILON));
        }

        //状态集合中加入开始状态
        states.add(startState);

        assert endStateToPattern.keySet().equals(endStates) : "Pattern映射中的结束状态集和NFA的结束状态集不同";

        return new NFA(states, startState, endStates, endStateToPattern, inputAlphabet);
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
