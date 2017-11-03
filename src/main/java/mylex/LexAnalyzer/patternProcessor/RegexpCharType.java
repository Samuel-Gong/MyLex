package mylex.LexAnalyzer.patternProcessor;

import java.util.ArrayList;
import java.util.List;

public enum RegexpCharType {

    /**
     * 操作数
     */
    SPACE(RegexpCharKind.OPERAND, ' '),
    EXCLAMATION(RegexpCharKind.OPERAND, '!'),
    POUND(RegexpCharKind.OPERAND, '#'),
    DOLLAR(RegexpCharKind.OPERAND, '$'),    //TODO 看是否实现匹配一行末尾
    PERCENT(RegexpCharKind.OPERAND, '%'),
    AND(RegexpCharKind.OPERAND, '&'),
    SINGLE_QUATE(RegexpCharKind.OPERAND, '\''),
    SLASH(RegexpCharKind.OPERAND, '/'),     //TODO 后面是否做斜杠向后匹配
    /**
     * 数字
     */
    ZERO(RegexpCharKind.OPERAND, '0'),
    ONE(RegexpCharKind.OPERAND, '1'),
    TWO(RegexpCharKind.OPERAND, '2'),
    THREE(RegexpCharKind.OPERAND, '3'),
    FOUR(RegexpCharKind.OPERAND, '4'),
    FIVE(RegexpCharKind.OPERAND, '5'),
    SIX(RegexpCharKind.OPERAND, '6'),
    SEVEN(RegexpCharKind.OPERAND, '7'),
    EIGHT(RegexpCharKind.OPERAND, '8'),
    NINE(RegexpCharKind.OPERAND, '9'),

    COLON(RegexpCharKind.OPERAND, ':'),
    SEMICOLON(RegexpCharKind.OPERAND, ';'),
    LESS(RegexpCharKind.OPERAND, '<'),
    EQUAL(RegexpCharKind.OPERAND, '='),
    MORE(RegexpCharKind.OPERAND, '>'),
    AT(RegexpCharKind.OPERAND, '@'),

    /**
     * 字母
     */
    A(RegexpCharKind.OPERAND, 'a'),
    C_A(RegexpCharKind.OPERAND, 'A'),
    B(RegexpCharKind.OPERAND, 'b'),
    C_B(RegexpCharKind.OPERAND, 'B'),
    C(RegexpCharKind.OPERAND, 'c'),
    C_C(RegexpCharKind.OPERAND, 'C'),
    D(RegexpCharKind.OPERAND, 'd'),
    C_D(RegexpCharKind.OPERAND, 'D'),
    E(RegexpCharKind.OPERAND, 'e'),
    C_E(RegexpCharKind.OPERAND, 'E'),
    F(RegexpCharKind.OPERAND, 'f'),
    C_F(RegexpCharKind.OPERAND, 'F'),
    G(RegexpCharKind.OPERAND, 'g'),
    C_G(RegexpCharKind.OPERAND, 'G'),
    H(RegexpCharKind.OPERAND, 'h'),
    C_H(RegexpCharKind.OPERAND, 'H'),
    I(RegexpCharKind.OPERAND, 'i'),
    C_I(RegexpCharKind.OPERAND, 'I'),
    J(RegexpCharKind.OPERAND, 'j'),
    C_J(RegexpCharKind.OPERAND, 'J'),
    K(RegexpCharKind.OPERAND, 'k'),
    C_K(RegexpCharKind.OPERAND, 'K'),
    L(RegexpCharKind.OPERAND, 'l'),
    C_L(RegexpCharKind.OPERAND, 'L'),
    M(RegexpCharKind.OPERAND, 'm'),
    C_M(RegexpCharKind.OPERAND, 'M'),
    N(RegexpCharKind.OPERAND, 'n'),
    C_N(RegexpCharKind.OPERAND, 'N'),
    O(RegexpCharKind.OPERAND, 'o'),
    C_O(RegexpCharKind.OPERAND, 'O'),
    P(RegexpCharKind.OPERAND, 'p'),
    C_P(RegexpCharKind.OPERAND, 'P'),
    Q(RegexpCharKind.OPERAND, 'q'),
    C_Q(RegexpCharKind.OPERAND, 'Q'),
    R(RegexpCharKind.OPERAND, 'r'),
    C_R(RegexpCharKind.OPERAND, 'R'),
    S(RegexpCharKind.OPERAND, 's'),
    C_S(RegexpCharKind.OPERAND, 'S'),
    T(RegexpCharKind.OPERAND, 't'),
    C_T(RegexpCharKind.OPERAND, 'T'),
    U(RegexpCharKind.OPERAND, 'u'),
    C_U(RegexpCharKind.OPERAND, 'U'),
    V(RegexpCharKind.OPERAND, 'v'),
    C_V(RegexpCharKind.OPERAND, 'V'),
    W(RegexpCharKind.OPERAND, 'w'),
    C_W(RegexpCharKind.OPERAND, 'W'),
    X(RegexpCharKind.OPERAND, 'x'),
    C_X(RegexpCharKind.OPERAND, 'X'),
    Y(RegexpCharKind.OPERAND, 'y'),
    C_Y(RegexpCharKind.OPERAND, 'Y'),
    Z(RegexpCharKind.OPERAND, 'z'),
    C_Z(RegexpCharKind.OPERAND, 'Z'),

    CARET(RegexpCharKind.OPERAND, '^'),     //TODO 是否做反向匹配
    UNDERLINE(RegexpCharKind.OPERAND, '_'),
    OPEN_SINGLE_QUATE(RegexpCharKind.OPERAND, '`'),
    TILDE(RegexpCharKind.OPERAND, '~'),
    DASH(RegexpCharKind.OPERAND, '-'),


    /**
     * 操作符
     */
    LPARENTHESIS(RegexpCharKind.OPERATOR, '('),
    RPARENTHESIS(RegexpCharKind.OPERATOR, ')'),
    LBRACK(RegexpCharKind.OPERATOR, '['),
    RBRACK(RegexpCharKind.OPERATOR, ']'),
    LBRACE(RegexpCharKind.OPERATOR, '{'),
    RBRACE(RegexpCharKind.OPERATOR, '}'),
    STAR(RegexpCharKind.OPERATOR, '*'),
    PLUS(RegexpCharKind.OPERATOR, '+'),
    COMMA(RegexpCharKind.OPERATOR, ','),
    PERIOD(RegexpCharKind.OPERATOR, '.'),
    QUESTION(RegexpCharKind.OPERATOR, '?'),
    VERTICAL(RegexpCharKind.OPERATOR, '|'),

    BACKSLASH(RegexpCharKind.OPERATOR, '\\');   //TODO 转译

    RegexpCharType(RegexpCharKind kind, final char name) {
        this.kind = kind;
        this.name = name;
    }

    /**
     * 所属正则表达式字符的种类，分为操作数和操作符
     */
    private RegexpCharKind kind;

    private char name;

    /**
     * 判断是否是操作符
     *
     * @param c 需要判断的字符
     * @return
     */
    public static boolean isOperator(char c) {
        RegexpCharType regexpChars[] = RegexpCharType.values();
        for (int i = regexpChars.length - 1; i >= 0; i--) {
            if (c == regexpChars[i].name && regexpChars[i].kind == RegexpCharKind.OPERATOR) return true;
        }
        return false;
    }

    /**
     * 判断是否是操作数
     *
     * @param c 需要判断的字符
     */
    public static boolean isOperand(char c) {
        RegexpCharType regexpChars[] = RegexpCharType.values();
        for (int i = 0; i < regexpChars.length; i++) {
            if (c == regexpChars[i].name && regexpChars[i].kind == RegexpCharKind.OPERAND) return true;
        }
        return false;
    }

    public static List<Character> names() {
        List<Character> names = new ArrayList<>();
        for (RegexpCharType charType : RegexpCharType.values()) {
            names.add(charType.name);
        }
        return names;
    }
}
