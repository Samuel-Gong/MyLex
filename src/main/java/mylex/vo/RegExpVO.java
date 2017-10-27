package mylex.vo;

public class RegExpVO {

    /**
     * 该正则表达式的标志
     */
    public String label;

    /**
     *  java能够识别的正则表达式
     */
    public String regularExpression;

    /**
     * 该正则表达式的优先级
     */
    public int precedence;

    public RegExpVO(String label, String regularExpression, int precedence) {
        this.label = label;
        this.regularExpression = regularExpression;
        this.precedence = precedence;
    }

    @Override
    public boolean equals(Object obj) {
        RegExpVO regExpVO = (RegExpVO)obj;
        return this.regularExpression.equals(regExpVO.regularExpression);
    }
}
