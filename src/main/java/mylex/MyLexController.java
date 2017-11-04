package mylex;


import mylex.LexAnalyzer.LexAnalyzer;
import mylex.LexAnalyzer.Tokenizer;
import mylex.vo.Pattern;
import mylex.vo.Token;

import java.util.List;

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

        //从.l文件中获取Pattern
        List<Pattern> patterns = lexFileParser.getPatterns();
        //解析Pattern，构建一个基于最简DFA的词法分析器
        Tokenizer tokenizer = lexAnalyzer.createTokenizer(patterns);

        //读入需要解析的源文件，获取源文件内容
        SrcFileReader srcFileReader = new SrcFileReader();
        String content = srcFileReader.getFileContent();

        //解析源文件中的token序列
        List<Token> tokens = tokenizer.getTokens(content);

        //输出token序列到目标文件
        TokenSequenceWriter tokenSequenceWriter = new TokenSequenceWriter();
        tokenSequenceWriter.writeTokens(tokens);

    }
}
