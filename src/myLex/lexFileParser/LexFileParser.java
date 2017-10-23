package myLex.lexFileParser;

import vo.ParsedLexFileVO;
import vo.RegVO;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class LexFileParser {

    private char[] lexFileContents;
    private int lexFileCharNum;

    /**
     * 保存当前状态
     */
    private LexFileParserStateType currentState;

    /**
     * 保存之前一个状态
     */
    private LexFileParserStateType lastState;

    /**
     * 用于表驱动的表
     */
    private LexFileParserStateTable lexFileParserStateTable;

    /**
     * 当前匹配的文本内容
     */
    private StringBuilder textSB;

    /**
     * 当前正则表达式的优先级
     */
    private int precedence;

    /**
     * 装载当前对应的正则表达式的匹配信息
     */
    private Map<String, RegVO> patterns;

    public LexFileParser(){

        currentState = LexFileParserStateType.START;
        lastState = currentState;
        lexFileParserStateTable = new LexFileParserStateTable();

        textSB = new StringBuilder();
        precedence = 0;

        patterns = new HashMap<>();
    }

    public ParsedLexFileVO parseLexFile(String fileName) {
        System.out.println("Here is the contents of the file:");
        readfile(fileName);

        String idText = "";
        String regText = "";
        for(int i = 0; i < lexFileCharNum; i++){
            lastState = currentState;
            currentState = read(lexFileContents[i]);

            assert currentState != LexFileParserStateType.ERROR : LexFileParser.class.getName()+": LexParser的状态为ERROR，请检查.l文件内容";

            switch (currentState){
                case START:
                    break;
                case ID:
                    textSB.append(lexFileContents[i]);
                    break;
                case WS:
                    if (lastState == LexFileParserStateType.ID) {
                        idText = textSB.toString();
                        textSB.delete(0, textSB.length());
                    }
                    break;
                case RE:
                    textSB.append(lexFileContents[i]);
                    break;
                case RE_END:
                    textSB.append(lexFileContents[i]);
                    regText = textSB.toString();
                    RegVO regVO = new RegVO(regText, precedence++);
                    assert !idExists(idText) : LexFileParser.class.getName() + ": "+ idText +"已经有了对应的pattern";
                    assert !regExists(regVO) : LexFileParser.class.getName() + ": " + regVO.regularExpression + "已经存在于另一id中";
                    patterns.put(idText, regVO);
                    textSB.delete(0, textSB.length());
                    break;
            }
        }

        ParsedLexFileVO parsedLexFileVO = new ParsedLexFileVO();
        parsedLexFileVO.patterns = patterns;

        return parsedLexFileVO;
    }

    private boolean regExists(RegVO regVO) {
        return patterns.containsValue(regVO);
    }

    private boolean idExists(String idText) {
        return patterns.containsKey(idText);
    }

    private LexFileParserStateType read(char inputChar) {
        LexFileCharType charType = checkCharType(inputChar);
        assert charType != null : LexFileParser.class.getName()+": 对于输入的char，没有对应的LexFileCharType";
        return lexFileParserStateTable.move(currentState, charType);
    }

    private void readfile(String fileName){
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
            int c = ' ';
            while((c = bufferedReader.read()) != -1) {
                stringBuilder.append((char) c);
            }
        } catch (FileNotFoundException e) {
            System.out.println(LexFileParser.class.getName() + ":没有找到.l文件");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        lexFileContents = stringBuilder.toString().toCharArray();
        lexFileCharNum = lexFileContents.length;

        //输出.l文件中的内容
        for(int i = 0; i < lexFileCharNum; i++){
            System.out.print(lexFileContents[i]);
        }
        System.out.println();
    }

    private LexFileCharType checkCharType(char inputChar){
        if(Character.isLetter(inputChar)) return LexFileCharType.LETTER;
        if(Character.isWhitespace(inputChar)) return LexFileCharType.WS;
        if(Character.isDigit(inputChar)) return LexFileCharType.DIGIT;
        if(isLBracket(inputChar)) return LexFileCharType.LBRACKET;
        if(isRBracket(inputChar)) return LexFileCharType.RBARCKET;
        if(isBackSlash(inputChar)) return LexFileCharType.BackSlash;
        return null;
    }

    private boolean isBackSlash(char inputChar) {
        return inputChar == '\\';
    }

    private boolean isRBracket(char inputChar) {
        return inputChar == ']';
    }

    private boolean isLBracket(char inputChar) {
        return inputChar == '[';
    }

}
