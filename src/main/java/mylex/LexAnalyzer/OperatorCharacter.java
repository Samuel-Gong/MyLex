package mylex.LexAnalyzer;

public enum OperatorCharacter {
    BACK_SLASH('\\', false),
    QUOTATION('\"', false),
    DOT('.', false),
    HYPHEN('-', false),



    ;

    /**
     * 符号
     */
    private char value;

    /**
     * 是否是扩展符号
     */
    private boolean isAdvanced;
    OperatorCharacter(char value, boolean isAdvanced){
        this.value = value;
        this.isAdvanced = isAdvanced;
    }
}
