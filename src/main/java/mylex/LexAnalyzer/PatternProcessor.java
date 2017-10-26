package mylex.LexAnalyzer;

/**
 * 将含有扩展符号：+，？，a-z，0-9的正则表达式转化为不含这些扩展符号的正则表达式
 */
public class PatternProcessor {

    public String simplifyPattern(String pattern) {
        String result = "";
        if(hasAdvancedChar(pattern)){
            //TODO  如果含有扩展字符的情况
        }
        else{
            result = pattern;
        }
        return result;
    }

    //检测该正则表达式模式是否含有扩展符号
    private boolean hasAdvancedChar(String pattern) {
        return pattern.contains("+") || pattern.contains("?") || pattern.contains("-");
    }

}
