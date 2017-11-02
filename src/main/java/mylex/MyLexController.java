package mylex;


import mylex.LexAnalyzer.LexAnalyzer;
import mylex.LexAnalyzer.Tokenizer;
import mylex.lexFileParser.LexFileParser;
import mylex.vo.Pattern;
import mylex.vo.Token;

import java.util.List;
import java.util.Scanner;

/**
 * 控制整个生成器生成可读取字符流的编译器对象的过程
 * 过程步骤：
 * 1. 输入.l文件
 * 2. 解析.l文件
 * 3. 装配经解析的.l的（声明部分，待实现），转换规则，（辅助函数，待实现）
 * 4. 生成可读取字符流的编译器对象
 * 5. 输入字符流
 * 6. 生成sequence of tokens
 */
public class MyLexController {

    private LexFileParser lexFileParser;
    private LexAnalyzer lexAnalyzer;

    public MyLexController(){
        lexFileParser = new LexFileParser();
        lexAnalyzer = new LexAnalyzer();
    }

    public void getTokens() {

        Scanner scanner = new Scanner(System.in);

        List<Pattern> patterns = lexFileParser.getPatterns();
        Tokenizer tokenizer = lexAnalyzer.createTokenizer(patterns);

        System.out.println("请输入你想解析的字符串:");
        StringBuilder stringBuilder = new StringBuilder();
        String input;
        while (!(input = scanner.nextLine()).equals("")) {
            stringBuilder.append(input);
        }

        System.out.println("Token序列");
        System.out.println("----------------------");

        List<Token> tokens = tokenizer.getTokens(stringBuilder.toString());
        for (Token token : tokens) {
            System.out.println("<" + token.getName() + ", " + token.getValue() + ">");
        }

        System.out.println("----------------------");

    }
}
