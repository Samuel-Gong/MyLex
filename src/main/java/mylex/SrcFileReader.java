package mylex;

import java.io.*;
import java.util.Scanner;

/**
 * 读取需要解析的文件，将文件内容专程拜访字符串
 */
public class SrcFileReader {

    public String getFileContent() {
        Scanner scanner = new Scanner(System.in);
        //读入文件内容
        System.out.println("请输入你想解析的文件名（相对路径）:");
        StringBuilder stringBuilder = new StringBuilder();
        //判断文件存在
        String fileName = scanner.next();

        String filePath = Thread.currentThread().getContextClassLoader().getResource("").getPath() + fileName;
        File lFile = new File(filePath);

        assert lFile.exists() : "指定需要解析的文件不存在或路径不正确";

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(lFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int tempChar = '\0';
        try {
            while ((tempChar = br.read()) != -1) {
                stringBuilder.append((char) tempChar);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stringBuilder.toString();
    }

}
