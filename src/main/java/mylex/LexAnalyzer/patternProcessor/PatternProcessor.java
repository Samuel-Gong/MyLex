package mylex.LexAnalyzer.patternProcessor;

import mylex.LexAnalyzer.nfa.NFA;
import mylex.LexAnalyzer.nfa.NFAState;
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
     * 对所有pattern分别构建一个NFA
     *
     * @return 所有Pattern的NFA集合
     */
    public List<NFA> combinePatterns() {

        List<NFA> nfaList = new ArrayList<>();

        //对每个pattern构建一个NFA，添加该NFA的结束状态和pattern的键值对
        for (Pattern pattern : patterns) {
            NFA nfaOnePattern = createNFAOnePattern(infixToPostfix(pattern.regularExpression), pattern);
            nfaList.add(nfaOnePattern);
        }

        return nfaList;
    }

    /**
     * 根据语法分析树的后缀表达式,构建NFA
     *
     * @param regExpPostfix 语法分析树的后缀表达式
     * @return 对应该语法分析树的后缀表达式
     */
    public NFA createNFAOnePattern(String regExpPostfix, Pattern pattern) {
        for (int i = 0; i < regExpPostfix.length(); i++) {
            char c = regExpPostfix.charAt(i);
            if (isOperand(c)) {
                regExpPostfixStack.push(createSimpleNFA(c));
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
            // 遇到连字符，先push，在遇到后中括号后再处理
            if (c == '-') {
                regExpPostfixStack.push(c);
                continue;
            }
            //转译处理
            if (c == '\\') {
                assert i < regExpPostfix.length() - 1 : ": 正则表达式有误";
                char cNeedToTransfer = regExpPostfix.charAt(++i);
                //操作符需要转译，直接传入操作符
                if (RegexpCharType.isOperator(cNeedToTransfer)) transfer(cNeedToTransfer);
                else {
                    assert cNeedToTransfer == 'n' || cNeedToTransfer == 't' :
                            ": \\后面需要跟一个操作符或n或t(\\\n表示换行符，\\\\t表示制表符)";
                    if (cNeedToTransfer == 'n') transfer('\n');
                    if (cNeedToTransfer == 't') transfer('\t');
                }
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

        NFA nfa = (NFA) regExpPostfixStack.pop();
        nfa.setPattern(pattern);

        return nfa;
    }

    /*
     * 对后缀表达式到NFA的解析
     */

    /**
     * 直接生成一个简单的NFA，开始状态由字符c连接到结束状态
     *
     * @param c 传入的字符
     */
    private NFA createSimpleNFA(char c) {
        NFAState startState = new NFAState(id++);
        NFAState endState = new NFAState(id++, true);
        NFA nfa = new NFA(startState, endState, c);
        return nfa;
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
        regExpPostfixStack.push(createSimpleNFA(c));
    }

    /**
     * 词法分析树遇见通配符.，生成一个NFA，开始状态到结束状态由字符表中所有字符的边连接起来
     */
    private void meetPeriod() {
        NFAState startState = new NFAState(id++);
        NFAState endState = new NFAState(id++, true);
        Set<Character> fullAlphabetWithOutLineBreak = new HashSet<>();
        fullAlphabetWithOutLineBreak.addAll(fullAlphabet);
        fullAlphabetWithOutLineBreak.remove('\n');
        NFA nfa = new NFA(startState, endState, fullAlphabetWithOutLineBreak);
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
     * 遇见右中括号，对左中括号之前的所有NFA进行并操作,中括号当中可能含有-
     */
    private void meetRightBracket() {
        List<NFA> needToUnion = new ArrayList<>();
        assert !regExpPostfixStack.empty() : ": 正则表达式有误";
        while (!regExpPostfixStack.empty()) {
            Object obj = regExpPostfixStack.pop();
            //判断是否是字符，若是字符，则必是(,说明该（）分组结束
            if (obj instanceof Character) {
                //遇到连字符
                if ((Character) obj == '-') {
                    //拿到连字符前面一个NFA
                    assert needToUnion.size() > 0 : ": 连字符后面需要有一个数字或字母";
                    NFA postNFA = needToUnion.get(0);
                    assert !regExpPostfixStack.empty() && regExpPostfixStack.peek() instanceof NFA : ": 连字符前面需要有一个数字或字母";
                    NFA preNFA = (NFA) regExpPostfixStack.pop();

                    Set<Character> presInputAlphabet = preNFA.getInputAlphabet();
                    Set<Character> postInputAlphabet = postNFA.getInputAlphabet();

                    assert presInputAlphabet.size() == 1 && postInputAlphabet.size() == 1 : ": 正则表达式有误";

                    //获取连字符的左右两个字符
                    char preChar = presInputAlphabet.iterator().next();
                    char postChar = postInputAlphabet.iterator().next();

                    assert (Character.isDigit(preChar) && Character.isDigit(postChar)) ||
                            (Character.isUpperCase(preChar) && Character.isUpperCase(postChar)) ||
                            (Character.isLowerCase(preChar) && Character.isLowerCase(postChar)) :
                            ": 连字符左右两个字符应同为数字或大写字母或小写字母";
                    assert preChar <= postChar : ": 连字符左边的字符应该小于等于右边的字符";

                    //从连字符左边的字符的后一个字符开始，一直加到连字符右边的字符
                    for (int i = postChar - preChar - 1; i >= 1; i--) {
                        needToUnion.add(0, createSimpleNFA((char) (preChar + i)));
                    }

                    needToUnion.add(0, preNFA);

                }
                //不然遇到左中括号，跳出循环
                else {
                    assert (Character) obj == '[' : "：没有找到匹配的[";
                    break;
                }
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
     * 构造正则表达式的后缀表达式
     *
     * @param regExp 中缀的正则表达式
     * @return
     */

    public String infixToPostfix(String regExp) {
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
            //连字符
            if (c == '-') {
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
                    sb.append(infixToPostfix(regExpInParentheseSb.toString()));
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
                    sb.append(infixToPostfix(regExpInParentheseSb.toString()));
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
            } else {
                assert false : ": 没有考虑此情况";
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
