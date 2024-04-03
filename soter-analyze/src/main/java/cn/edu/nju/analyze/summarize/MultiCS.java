package cn.edu.nju.analyze.summarize;

import java.io.*;
import java.util.*;
import com.alibaba.fastjson.JSON;

public class MultiCS extends Thread {
    private Thread t;

    public String repoName;
    public List<String> shaCodeLst;
    public String outputFilePath;

    MultiCS(String repoName,List<String> shaCodeLst,String outputFilePath) {
        this.repoName = repoName;
        this.shaCodeLst = shaCodeLst;
        this.outputFilePath = outputFilePath;
    }

    private OutputStreamWriter openWriteFile() throws UnsupportedEncodingException {
        File outputFile = new File(this.outputFilePath + this.repoName.substring(repoName.indexOf("/")) + ".txt");
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        try {
            if (!outputFile.exists()) {
                boolean hasFile = outputFile.createNewFile();
                fos = new FileOutputStream(outputFile);
            } else {
                screenShaCode(outputFile);
                fos = new FileOutputStream(outputFile, true);
            }
        } catch (Exception e) {
            System.out.println("文件异常");
        }
        osw = new OutputStreamWriter(fos, "utf-8");
        return osw;
    }

    private OutputStreamWriter openJsonFile() throws UnsupportedEncodingException {
        File outputFile = new File(this.outputFilePath + this.repoName.substring(repoName.indexOf("/")) + ".json");
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        try {
            if (!outputFile.exists()) {
                boolean hasFile = outputFile.createNewFile();
                fos = new FileOutputStream(outputFile);
            } else {
                screenShaCode(outputFile);
                fos = new FileOutputStream(outputFile, true);
            }
        } catch (Exception e) {
            System.out.println("文件异常");
        }
        osw = new OutputStreamWriter(fos, "utf-8");
        return osw;
    }

    private void screenShaCode(File outputFile){
        Set<String> doneJobSet = new TreeSet<>();
        try
        {
            FileInputStream fis = new FileInputStream(outputFile);
            Scanner sc = new Scanner(fis);    //file to be scanned
            while(sc.hasNextLine()) {
                String tmp = sc.nextLine();
                if (tmp.indexOf("处理异常") != -1 || tmp.indexOf("输出超长") != -1){ //todo 后续加回来！！！
                    continue;
                }

                int i = tmp.indexOf(" ");
                if (i == -1){
                    continue;
                }
                int j = tmp.indexOf(" ",i + 1);
                if (j == -1){
                    continue;
                }
                doneJobSet.add(tmp.substring(i + 1,j));

                //System.out.println(sc.nextLine());      //returns the line that was skipped
            }
            sc.close();     //closes the scanner
            fis.close();
            List<String> filtedList = new ArrayList<>();
            for (String i:this.shaCodeLst){
                if (!doneJobSet.contains(i)){
                    filtedList.add(i);
                }
            }
            this.shaCodeLst = filtedList;
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }


    public void run() {
        GetLocalGitFile localGit = new GetLocalGitFile("./");

        OutputStreamWriter outputWriter = null;
        OutputStreamWriter jsonWriter = null;
        try {
            outputWriter = this.openWriteFile();
            jsonWriter = this.openJsonFile();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        String prefixpath = this.repoName;
        prefixpath = prefixpath.substring(prefixpath.indexOf("/"));

        long startTime = System.currentTimeMillis();
        int cnt = 0;
        System.out.println(repoName + " remain tasks: " + this.shaCodeLst.size());
        for (String shacode:this.shaCodeLst) {
            //System.out.println(shacode);
            List<String> methodName = new ArrayList<>();
            List<String> className = new ArrayList<>();
            String describes = "处理异常";
            try {
                //describes = localGit.ProjectCommitPath(shacode, "/home/cuiyunqi/repo/" + prefixpath).replaceAll("[\n\r\t]", " ");
                describes = localGit.ProjectCommitPath(shacode, "F:\\codisumrepo\\" + prefixpath, methodName, className).replaceAll("[\n\r\t]", " ");
//                if (describes.split("[\n\r\t ]+").length > 500){
//                    describes = "输出超长";
//                }
            } catch (Exception e) {
                System.out.println("处理异常 In Repo: " + repoName + " ShaCode: " + shacode + " " + e.toString());
            }
            try {
                // 写入内容
                outputWriter.write(prefixpath + " " + shacode + " " + describes);
                // 换行
                outputWriter.write("\n");

                outputWriter.flush();

                jsonWriter.write(JSON.toJSONString(methodName));
                jsonWriter.write("\n");
                jsonWriter.write(JSON.toJSONString(className));
                jsonWriter.write("\n");
                jsonWriter.flush();
                //System.out.println("写入完毕");
            } catch (Exception e) {
                System.out.println("写入异常 In Repo: " + repoName + " ShaCode: " + shacode);
            }

            cnt++;
            if (cnt % 100 == 0) {
                System.out.println("------------------ " + cnt + " ---------------------");
                System.out.println(repoName + " 已运行 " + (System.currentTimeMillis() - startTime) / 1000 + "s");
            }
        }
        System.out.println("End repo "+repoName);
    }

    public void start() {
        if (t == null) {
            t = new Thread (this);
            t.start();
        }
    }
}
