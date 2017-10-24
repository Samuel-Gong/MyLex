package myLex;

import myLex.vo.ParsedLexFileVO;
import myLex.vo.RegVO;

import java.util.Map;

public class Main {
    public static void main(String[] args) {
        MyLexController myLexController = new MyLexController();
        ParsedLexFileVO parsedLexFileVO = myLexController.parseLexFile(Main.class.getClassLoader().getResource("mylex.l").getPath());
        Map<String, RegVO> map = parsedLexFileVO.patterns;

        System.out.println("This is the parsed .l file:");
        for (Map.Entry<String, RegVO> entry : map.entrySet()){
            System.out.println("id:" + entry.getKey() + "     " + "pattern:" + entry.getValue().regularExpression);
        }
    }
}
