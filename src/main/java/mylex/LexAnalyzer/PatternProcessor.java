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
     *
     * @return 合并好的NFA，按照龙书P106的合并方式实现
     */
    public NFA combinePatterns() {

        List<NFA> nfaList = new ArrayList<>();

        //结束状态对应的正则表达式的模式
        Map<NFAState, Pattern> endStateToPattern = new HashMap<>();

        //对每个pattern构建一个NFA，添加该NFA的结束状态和pattern的键值对
        for (Pattern pattern : patterns) {
            NFA nfaOnePattern = createNFAOnePattern(createAnalysisTree(pattern.regularExpression));
            nfaList.add(nfaOnePattern);
            endStateToPattern.put(nfaOnePattern.getSimpleNFAEndState(), pattern);
        }

        //合并nfaList
        Set<NFAState> states = new HashSet<>();
        Set<NFAState> endStates = new HashSet<>();
        Set<Character> inputAlphabet = new HashSet<>();

        NFAState startState = new NFAState(id++);

        for (NFA nfa : nfaList) {
            //加入到新NFA状态集合
            states.addAll(nfa.getStates());
            //加入到新NFA的结束状态集合
            assert nfa.getEndStates().size() == 1 : PatternProcessor.class.getName() + "：简单NFA的结束状态集合大小不为1";
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
                id = nfa.closure(id);
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
            //支持模式零次或一次出现
            if (c == '?') {
                assert stack.peek() instanceof NFA : ": 正则表达式有误";
                NFA nfa = (NFA) stack.pop();
                id = nfa.zeroOrOnce(id);
                stack.push(nfa);
            }
            //支持模式一次或多次出现
            if (c == '+') {
                assert stack.peek() instanceof NFA : ": 正则表达式有误";
                NFA nfa = (NFA) stack.pop();
                id = nfa.onceOrMany(id);
                stack.push(nfa);
                continue;
            }

            /*
             * 括号处理
             */

            //小括号里面的NFA进行连接
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
                        assert (Character) obj == '(' : "：没有找到匹配的(";
                        break;
                    }
                    if (obj instanceof NFA) {
                        needToConcat.add(0, (NFA) obj);
                    }
                }

                //如果（）中间有被压栈的NFA
                if (!needToConcat.isEmpty()) stack.push(concatNFA(needToConcat));
            }

            //中括号里面的NFA进行union
            if (c == '[') {
                stack.push(c);
                continue;
            }
            //对右括号之前的所有NFA状态做并
            if (c == ']') {
                List<NFA> needToUnion = new ArrayList<>();
                assert !stack.empty() : ": 正则表达式有误";
                while (!stack.empty()) {
                    Object obj = stack.pop();
                    //判断是否是字符，若是字符，则必是(,说明该（）分组结束
                    if (obj instanceof Character) {
                        assert (Character) obj == '[' : "：没有找到匹配的[";
                        break;
                    }
                    if (obj instanceof NFA) {
                        needToUnion.add(0, (NFA) obj);
                    }
                }

                //说明[]中间有被压栈的NFA
                if (!needToUnion.isEmpty()) stack.push(unionNFA(needToUnion));
            }

            //大括号检查，一旦找到左扩号，就开始读取数字,找到前面一个NFA，对其进行重复连接
            if (c == '{') {

                int minTime = 0;
                char cInBrace = regExpPostfix.charAt(++i);
                assert Character.isDigit(cInBrace) : ": {}之间不能含有除数字外的其它操作数";
                while (Character.isDigit(cInBrace)) {
                    int num = cInBrace - '0';
                    minTime = minTime * 10 + num;
                    cInBrace = regExpPostfix.charAt(++i);
                }

                //验证{}中间有逗号两个数字之间的逗号
                assert cInBrace == ',' : ": {}中间没有逗号分割";

                int maxTime = 0;
                cInBrace = regExpPostfix.charAt(++i);
                assert Character.isDigit(cInBrace) : ": {}之间不能含有除数字外的其它操作数";
                while (Character.isDigit(cInBrace)) {
                    int num = cInBrace - '0';
                    maxTime = maxTime * 10 + num;
                    cInBrace = regExpPostfix.charAt(++i);
                }

                //验证最后一个非操作数字符是}
                assert cInBrace == '}' : ": 没有匹配的}";

                assert minTime >= 0 && maxTime > 0 : ": 大括号内的数字不能小于0";
                assert minTime <= maxTime : ": 大括号内的左边数字需小于等于右边数字";

                assert stack.peek() instanceof NFA : ": 正则表达式有误";
                NFA nfa = (NFA) stack.pop();
                NFA repeatNFA = nfa.cloneNFA(id);
                //id加上克隆NFA状态的大小
                id = id + repeatNFA.getStates().size();

                //如果最小次数为0，说明可以不出现，不然说明必须出现minTime次，则将剩余的minTime-1次与nfa连接
                if (minTime == 0) id = nfa.zeroOrOnce(id);
                else {
                    for (int repeatTimeNecessary = 1; repeatTimeNecessary < minTime; repeatTimeNecessary++) {
                        id = nfa.concat(repeatNFA, id);
                        //再次克隆原NFA并给克隆的NFA重新分配id
                        repeatNFA = repeatNFA.cloneNFA(id);
                        id += repeatNFA.getStates().size();
                    }
                }

                //对于可选择的重复次数，在当前NFA后面连接一个可选的repeatNFA
                for (int reapeatTimeOptional = 0; reapeatTimeOptional < maxTime - minTime; reapeatTimeOptional++) {
                    id = nfa.concatOptional(repeatNFA, id);
                    //再次克隆原NFA并给克隆的NFA重新分配id
                    repeatNFA = repeatNFA.cloneNFA(id);
                    id += repeatNFA.getStates().size();
                }

                stack.push(nfa);
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

    /**
     * 对传入的NFA集合采用并操作
     *
     * @param needToUnion NFA集合
     * @return 最终得到的NFA
     */
    private NFA unionNFA(List<NFA> needToUnion) {
        assert !needToUnion.isEmpty() : " []之间不可能为空";
        if (needToUnion.size() == 1) return needToUnion.get(0);
        NFA nfa = needToUnion.get(0);
        for (int i = 1; i < needToUnion.size(); i++) {
            id = nfa.union(needToUnion.get(i), id);
        }
        return nfa;
    }

    /**
     * 对传入NFA集合采用连接操作
     *
     * @param needToConcat NFA集合
     * @return 最终得到的NFA
     */
    private NFA concatNFA(List<NFA> needToConcat) {
        assert !needToConcat.isEmpty() : "：（）之间不可能为空";
        if (needToConcat.size() == 1) return needToConcat.get(0);
        NFA nfa = needToConcat.get(0);
        for (int i = 1; i < needToConcat.size(); i++) {
            id = nfa.concat(needToConcat.get(i), id);
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
            if (isOperand(c)) {
                sb.append(c);
                continue;
            }
            if (c == '?') {
                sb.append(c);
                continue;
            }
            if (c == '+') {
                sb.append(c);
                continue;
            }
            //左右小括号直接加到字符串末尾
            if (c == '(' || c == ')') {
                sb.append(c);
                continue;
            }
            //左右中括号直接加到字符串末尾
            if (c == '[' || c == ']') {
                sb.append(c);
                continue;
            }
            //左右大括号直接加到字符串末尾，大括号中的','也加到字符串末尾
            if (c == '{' || c == '}' || c == ',') {
                sb.append(c);
                continue;
            }
            if (c == '*') {
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
    public static boolean isOperand(char c) {
        //TODO 暂且认为操作数为数字,字母和空格
        return Character.isLetterOrDigit(c) || Character.isWhitespace(c);
    }
}
