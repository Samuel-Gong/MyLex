package mylex.LexAnalyzer;

import mylex.LexAnalyzer.nfa.NFA;
import mylex.LexAnalyzer.nfa.NFAEdge;
import mylex.LexAnalyzer.nfa.NFAState;
import mylex.lexFileParser.RegexpCharType;
import mylex.vo.Pattern;

import java.util.*;

/**
 * 将输入的每一个正则表达式，转换成NFA，并将所有的NFA合并
 */
public class PatternProcessor {

    /**
     * 字母表全集
     */
    private Set<Character> fullAlphabet;

    /**
     * 记录当前分配给NFA的id
     */
    private int id;

    /**
     * NFA所有的正则表达式
     */
    private List<Pattern> patterns;

    /**
     * 正则表达式后缀表达式的栈，保存后缀表达式以及产生的NFA
     */
    private Stack<Object> regExpPostfixStack;

    public PatternProcessor(List<Pattern> patterns) {
        id = 0;
        this.patterns = patterns;
        regExpPostfixStack = new Stack<>();

        fullAlphabet = new HashSet<>(RegexpCharType.names());
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
        for (int i = 0; i < regExpPostfix.length(); i++) {
            char c = regExpPostfix.charAt(i);
            if (isOperand(c)) {
                createSimpleNFA(c);
            }
            //求一个正则表达式的闭包的NFA
            if (c == '*') {
                meetStar();
                continue;
            }
            //求两个正则表达式的并的NFA
            if (c == '|') {
                meetVerticalBar();
                continue;
            }
            //支持模式零次或一次出现
            if (c == '?') {
                meetQuestionMark();
                continue;
            }
            //支持模式一次或多次出现
            if (c == '+') {
                meetPlus();
                continue;
            }
            //转译处理
            if (c == '\\') {
                assert i < regExpPostfix.length() - 1 : ": 正则表达式有误";
                char cNeedToTransfer = regExpPostfix.charAt(++i);
                assert RegexpCharType.isOperator(cNeedToTransfer) : ": \\后面需要跟一个操作符";
                //将需要转译的字符c
                transfer(cNeedToTransfer);
                continue;
            }
            //通配符处理
            if (c == '.') {
                meetPeriod();
                continue;
            }

            /*
             * 括号处理
             */

            //小括号里面的NFA进行连接
            if (c == '(') {
                regExpPostfixStack.push(c);
                continue;
            }
            //对左括号之前的所有NFA状态做连接
            if (c == ')') {
                meetRightParenthsis();
                continue;
            }

            //中括号里面的NFA进行union
            if (c == '[') {
                regExpPostfixStack.push(c);
                continue;
            }
            //对左右括号之间的所有NFA状态做并
            if (c == ']') {
                meetRightBracket();
                continue;
            }

            //对左右大括号之间的表达式进行解析，找到前面一个NFA，对其进行重复连接, 大括号里分三种情况
            //1. {n} n是一个非负整数。匹配确定的n次
            //2. {n,} n是一个非负整数。至少匹配n次
            //3. {n,m} m和n均为非负整数，其中n<=m。最少匹配n次且最多匹配m次
            if (c == '{') {
                while (i < regExpPostfix.length() && c != '}') {
                    regExpPostfixStack.push(c);
                    c = regExpPostfix.charAt(++i);
                }
                assert c == '}' : ": {没有匹配的}";
                meetRightBrace();
            }
        }

        //最后将栈中剩余的所有的NFA全部连接起来
        List<NFA> needToConcat = new ArrayList<>();
        assert !regExpPostfixStack.empty() : ": 正则表达式有误";
        while (!regExpPostfixStack.empty()) {
            Object obj = regExpPostfixStack.pop();

            assert obj instanceof NFA : ": 正则表达式有误";
            needToConcat.add(0, (NFA) obj);
        }
        regExpPostfixStack.push(concatNFA(needToConcat));

        assert regExpPostfixStack.size() == 1 : ": 正则表达式有误";

        return (NFA) regExpPostfixStack.pop();
    }

    /*
     * 对后缀表达式到NFA的解析
     */

    /**
     * 直接生成一个简单的NFA，开始状态由字符c连接到结束状态
     *
     * @param c 传入的字符
     */
    private void createSimpleNFA(char c) {
        NFAState startState = new NFAState(id++);
        NFAState endState = new NFAState(id++, true);
        NFA nfa = new NFA(startState, endState, c);
        regExpPostfixStack.push(nfa);
    }

    /**
     * 正则表达式的后缀表达式中遇见*，求取栈顶NFA的闭包
     */
    private void meetStar() {
        assert regExpPostfixStack.peek() instanceof NFA : ": 正则表达式有误";
        //求闭包，并更新id
        NFA nfa = (NFA) regExpPostfixStack.pop();
        id = nfa.closure(id);
        regExpPostfixStack.push(nfa);
    }

    /**
     * 正则表达式后缀表达式中遇见|，求取栈顶两个NFA的并
     */
    private void meetVerticalBar() {
        assert regExpPostfixStack.peek() instanceof NFA : ": 正则表达式有误";
        NFA second = (NFA) regExpPostfixStack.pop();
        assert regExpPostfixStack.peek() instanceof NFA : ": 正则表达式有误";
        NFA first = (NFA) regExpPostfixStack.pop();
        //并运算，并更新id
        id = (first.union(second, id));
        regExpPostfixStack.push(first);
    }

    /**
     * 后缀表达式中遇见?，修改栈顶NFA，支持该NFA模式的零次或一次出现
     */
    private void meetQuestionMark() {
        assert regExpPostfixStack.peek() instanceof NFA : ": 正则表达式有误";
        NFA nfa = (NFA) regExpPostfixStack.pop();
        id = nfa.zeroOrOnce(id);
        regExpPostfixStack.push(nfa);
    }

    /**
     * 后缀表达式中遇见+，修改栈顶NFA，支持该NFA模式的一次或多次出现
     */
    private void meetPlus() {
        assert regExpPostfixStack.peek() instanceof NFA : ": 正则表达式有误";
        NFA nfa = (NFA) regExpPostfixStack.pop();
        id = nfa.onceOrMany(id);
        regExpPostfixStack.push(nfa);
    }

    /**
     * 词法分析树遇见\，实现转译
     */
    private void transfer(char c) {
        assert regExpPostfixStack.peek() instanceof Character : ": 正则表达式有误";
        assert RegexpCharType.isOperator(c) : ": \\后面应该接一个操作符实现转译";
        createSimpleNFA(c);
    }

    /**
     * 词法分析树遇见通配符.，生成一个NFA，开始状态到结束状态由字符表中所有字符的边连接起来
     */
    private void meetPeriod() {
        NFAState startState = new NFAState(id++);
        NFAState endState = new NFAState(id++, true);
        NFA nfa = new NFA(startState, endState, fullAlphabet);
        regExpPostfixStack.push(nfa);
    }

    /**
     * 后缀表达式中遇见)，对相应的左括号之前的所有NFA做连接操作
     */
    private void meetRightParenthsis() {
        List<NFA> needToConcat = new ArrayList<>();
        assert !regExpPostfixStack.empty() : ": 正则表达式有误";
        while (!regExpPostfixStack.empty()) {
            Object obj = regExpPostfixStack.pop();
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
        if (!needToConcat.isEmpty()) regExpPostfixStack.push(concatNFA(needToConcat));
    }

    /**
     * 遇见右中括号，对左中括号之前的所有NFA进行并操作
     */
    private void meetRightBracket() {
        List<NFA> needToUnion = new ArrayList<>();
        assert !regExpPostfixStack.empty() : ": 正则表达式有误";
        while (!regExpPostfixStack.empty()) {
            Object obj = regExpPostfixStack.pop();
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
        if (!needToUnion.isEmpty()) regExpPostfixStack.push(unionNFA(needToUnion));
    }

    /**
     * 遇见左大括号，对左边大括号到右边大括号之间表达式解析后，对栈顶NFA进行重复连接
     */
    private void meetRightBrace() {
        assert !regExpPostfixStack.empty() : ": 正则表达式有误";

        int num = 0;
        int minTime = 0;
        int maxTime = 0;
        int count = 0;
        //标志{}内的三种状态, 1:{n} 2:{n,} 3:{n,m}, 初始化为1
        int situation = 1;
        Object obj = null;
        while (!regExpPostfixStack.empty()) {
            obj = regExpPostfixStack.pop();
            //验证是否是字符，可能是数字也可能是,还可能是{
            assert obj instanceof Character : ": 正则表达式有误";
            if ((Character) obj == '{') break;
                //{}中间有，说明是{n,}或者{n,m}这两种情况
            else if ((Character) obj == ',') {
                maxTime = num;
                num = 0;    //num置零
                //判断是{n,} 还是{n,m}
                if (count == 0) situation = 2;
                else {
                    situation = 3;
                    count = 0;
                }
            } else {
                assert Character.isDigit((Character) obj) : "{}中不能有其它的操作数";
                num = num + ((Character) obj - '0') * (int) Math.pow(10, count);
                count++;
            }
        }

        assert (Character) obj == '{' : ": }没有匹配的{";
        minTime = num;
        assert minTime >= 0 : ": {}中的整数为非负整数";

        //{n}
        if (situation == 1) {
            assert regExpPostfixStack.peek() instanceof NFA : ": 正则表达式有误";
            NFA nfa = (NFA) regExpPostfixStack.pop();
            id = nfa.concatSelfCertainTimes(minTime, id);
            //压回栈中
            regExpPostfixStack.push(nfa);
        }
        //{n,}
        else if (situation == 2) {
            assert regExpPostfixStack.peek() instanceof NFA : ": 正则表达式有误";
            NFA nfa = (NFA) regExpPostfixStack.pop();
            id = nfa.concatSelfLeastTimes(minTime, id);
            //压回栈中
            regExpPostfixStack.push(nfa);
        }
        //{n,m}
        else {
            assert maxTime >= 0 : ": {}中的整数为非负整数";
            assert minTime <= maxTime : ": 大括号内的左边数字需小于等于右边数字";
            assert regExpPostfixStack.peek() instanceof NFA : ": 正则表达式有误";
            NFA nfa = (NFA) regExpPostfixStack.pop();

            id = nfa.concatSelfMinToMax(minTime, maxTime, id);

            //压回栈中
            regExpPostfixStack.push(nfa);
        }
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
            if (c == '\\') {
                assert i < regExp.length() - 1 : ": \\后面没有其它操作符了";
                c = regExp.charAt(++i);
                assert RegexpCharType.isOperator(c) : ": \\后面需要接一个操作符实现转译";

                //先添加转译符，再添加操作符
                sb.append('\\');
                sb.append(c);
                continue;
            }
            if (c == '.') {
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
                }
                //后面接左小括号
                else if (nextChar == '(') {
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
                }
                //后面接左中括号
                else if (nextChar == '[') {
                    int left = 1;
                    StringBuilder regExpInParentheseSb = new StringBuilder();
                    regExpInParentheseSb.append('[');
                    while (left != 0 && i != regExp.length() - 1) {
                        char nextCharOfNext = regExp.charAt(++i);
                        if (nextCharOfNext == ']') {
                            regExpInParentheseSb.append(nextCharOfNext);
                            left--;
                        } else regExpInParentheseSb.append(nextCharOfNext);
                    }
                    assert left == 0 : "没有右括号匹配";
                    sb.append(createAnalysisTree(regExpInParentheseSb.toString()));
                }
                //后面接转译符号
                else if (nextChar == '\\') {
                    sb.append('\\');
                    assert i < regExp.length() - 1 : ": 正则表达式有误";
                    nextChar = regExp.charAt(++i);
                    assert RegexpCharType.isOperator(nextChar) : ": \\后面需要接一个操作符来实现转译";
                    sb.append(nextChar);
                } else {
                    assert false : ": 正则表达式有误";
                }
                sb.append('|');
            }

        }
        return sb.toString();
    }

    /**
     * 判断是否是支持的操作数
     *
     * @param c 字符
     * @return
     */
    public static boolean isOperand(char c) {
        return RegexpCharType.isOperand(c);
    }
}
