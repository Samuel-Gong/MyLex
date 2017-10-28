package mylex.vo;

/**
 * 记录每个Pattern的名称，表示的正则表达式，以及该模式的优先级
 */
public class Pattern {

    /**
     * 该正则表达式的标志
     */
    public String name;

    /**
     *  正则表达式
     */
    public String regularExpression;

    /**
     * 该正则表达式的优先级
     */
    public int precedence;

    public Pattern(String name, String regularExpression, int precedence) {
        this.name = name;
        this.regularExpression = regularExpression;
        this.precedence = precedence;
    }

    @Override
    public boolean equals(Object obj) {
        Pattern pattern = (Pattern)obj;
        return this.name.equals(pattern.name);
    }
}
