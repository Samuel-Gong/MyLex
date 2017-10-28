package mylex.vo;

/**
 * 保存Token的名字和相关属性值
 */
public class Token {

    private String name;

    private String value;

    public Token(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
