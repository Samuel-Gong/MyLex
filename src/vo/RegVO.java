package vo;

public class RegVO {

    /**
     *  java能够识别的正则表达式
     */
    public String regularExpression;

    /**
     * 该正则表达式的优先级
     */
    public int precedence;

    public RegVO(String regularExpression, int precedence) {
        this.regularExpression = regularExpression;
        this.precedence = precedence;
    }

    @Override
    public boolean equals(Object obj) {
        RegVO regVO = (RegVO)obj;
        return this.regularExpression.equals(regVO.regularExpression);
    }
}
