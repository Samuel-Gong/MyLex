package myLex.lexFileParser;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 利用表驱动，表示.l文件的状态转换
 */
public class LexFileParserStateTable {

    private ArrayList<LexFileParserStateType> srcStates;
    private ArrayList<LexFileCharType> inputChar;
    private ArrayList<ArrayList<LexFileParserStateType>> desStates;

    public LexFileParserStateTable(){
        srcStates = new ArrayList<>(Arrays.asList(LexFileParserStateType.values()));
        inputChar = new ArrayList<>(Arrays.asList(LexFileCharType.values()));
        desStates = new ArrayList<>();

        ArrayList<LexFileParserStateType> errorState = new ArrayList<>(Arrays.asList(
                LexFileParserStateType.ERROR,LexFileParserStateType.ERROR,
                LexFileParserStateType.ERROR,LexFileParserStateType.ERROR,
                LexFileParserStateType.ERROR,LexFileParserStateType.ERROR));
        ArrayList<LexFileParserStateType> startState = new ArrayList<>(Arrays.asList(
                LexFileParserStateType.ID,LexFileParserStateType.ERROR,
                LexFileParserStateType.START,LexFileParserStateType.ERROR,
                LexFileParserStateType.ERROR,LexFileParserStateType.ERROR));
        ArrayList<LexFileParserStateType> idState = new ArrayList<>(Arrays.asList(
                LexFileParserStateType.ID,LexFileParserStateType.ERROR,
                LexFileParserStateType.WS,LexFileParserStateType.ERROR,
                LexFileParserStateType.ERROR,LexFileParserStateType.ERROR));
        ArrayList<LexFileParserStateType> wsState = new ArrayList<>(Arrays.asList(
                LexFileParserStateType.RE,LexFileParserStateType.RE,
                LexFileParserStateType.WS,LexFileParserStateType.RE,
                LexFileParserStateType.ERROR,LexFileParserStateType.ERROR));
        ArrayList<LexFileParserStateType> reState = new ArrayList<>(Arrays.asList(
                LexFileParserStateType.RE,LexFileParserStateType.RE,
                LexFileParserStateType.RE,LexFileParserStateType.RE,
                LexFileParserStateType.END,LexFileParserStateType.RE));
        ArrayList<LexFileParserStateType> endState = new ArrayList<>(Arrays.asList(
                LexFileParserStateType.ERROR,LexFileParserStateType.ERROR,
                LexFileParserStateType.START,LexFileParserStateType.ERROR,
                LexFileParserStateType.ERROR,LexFileParserStateType.ERROR));

        desStates.add(errorState);
        desStates.add(startState);
        desStates.add(idState);
        desStates.add(wsState);
        desStates.add(reState);
        desStates.add(endState);
    }

    public LexFileParserStateType move(LexFileParserStateType curStateType, LexFileCharType inputCharType){
        assert curStateType != null : "当前LexParser状态类型为null";
        assert inputCharType != null : "当前输入字符状态类型为null";
        return desStates.get(srcStates.indexOf(curStateType)).get(inputChar.indexOf(inputCharType));
    }

}
