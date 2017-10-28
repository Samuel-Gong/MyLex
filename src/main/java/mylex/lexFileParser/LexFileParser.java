package mylex.lexFileParser;

import mylex.vo.Pattern;

import java.io.*;
import java.util.*;

public class LexFileParser {

    /**
     * 获取文件中所有的pattern
     * @return pattern的序列
     */
    public List<Pattern> getPatterns() {

        Scanner scanner = new Scanner(System.in);

        System.out.println("请输入需要读取的.l文件的相对路径:");

        //判断文件存在
        String fileName = scanner.next();

        String filePath = Thread.currentThread().getContextClassLoader().getResource("").getPath() + fileName;
        File lFile = new File(filePath);

        assert lFile.exists() : "指定.l文件不存在";
        assert lFile.getName().endsWith(".l") : "文件不是.l文件";

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(lFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //当前Pattern的优先级，precedence越大，优先级越低
        int precedence = 0;

        List<Pattern> patterns = new ArrayList<>();
        Map<String, Pattern> patternMap = new HashMap<>();

        String line = null;
        try {
            while ((line = br.readLine()) != null) {
                //去掉开始的空格和最后的空格
                line = line.trim();
                //以5个空格分割name和pattern
                String nameAndRegExp[] = line.split(" {5}");
                assert nameAndRegExp.length == 2 : ".l文件不符标准";
                String name = nameAndRegExp[0];
                String regExp = nameAndRegExp[1];

                assert name.startsWith("name:") : ".l文件不符标准";
                assert regExp.startsWith("pattern:") : ".l文件不符标准";

                //模式的名称
                name = name.substring(name.indexOf(':') + 1);
                //模式对应的正则表达式
                regExp = regExp.substring(regExp.indexOf(':') + 1);

                //不允许有重复的模式名称存在
                assert !patternMap.containsKey(name) : ".l文件中有重复的模式名称存在";

                Pattern pattern = new Pattern(name, regExp, precedence++);
                patternMap.put(name, pattern);
                patterns.add(pattern);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return patterns;
    }

}
