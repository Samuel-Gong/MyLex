package myLex;

import myLex.lexFileParser.ComponentAssembler;
import myLex.lexFileParser.LexFileParser;
import myLex.lexFileParser.Lexer;
import vo.ParsedLexFileVO;

import java.io.File;

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
    private ComponentAssembler componentAssembler;
    private Lexer lexer;

    public MyLexController(){
        lexFileParser = new LexFileParser();
        componentAssembler = new ComponentAssembler();
    }

    public ParsedLexFileVO parseLexFile(String fileName){
        return lexFileParser.parseLexFile(fileName);
    }

    public Lexer createCompiler(ParsedLexFileVO parsedLexFileVO){
        return null;
    };

}
