package myLex.LexAnalyzer;

import myLex.vo.ParsedLexFileVO;
import myLex.vo.RegVO;

import java.util.Map;

public class NFA {

    /**
     * state计数器
     */
    private int stateCount;

    /**
     * start边
     */
    private Edge startEdge;

    public NFA(){
        stateCount = 0;

        startEdge = new Edge();
    }

    /**
     * 根据经解析的parsedLexFileVO返回一个NFA的对象
     * @param parsedLexFileVO 解析完成的LexFile对象
     * @return  NFA
     */
    public NFA createNFA(ParsedLexFileVO parsedLexFileVO){


        Map<String, RegVO> patterns = parsedLexFileVO.patterns;

        return null;
    }

}
