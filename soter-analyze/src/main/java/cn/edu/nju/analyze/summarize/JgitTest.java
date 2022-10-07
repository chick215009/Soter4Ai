package cn.edu.nju.analyze.summarize;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import com.alibaba.fastjson.JSON;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static cn.edu.nju.analyze.summarize.GetLocalGitFile.openRpo;

public class JgitTest {
    public static void main(String[] args) throws GitAPIException, IOException {
        //getSortedCommitID();


        GetLocalGitFile localGit = new GetLocalGitFile("C:\\commitTest\\tmp1");


        //String srr = localGit.ProjectCommitPath("c92b3422164ee2da4747c6dac0cf9921f4219060","C:\\ProjectFileStore\\FileRecv\\elasticsearch").replaceAll("\n"," ");

/*
        //localGit.ProjectCommitPath("4665354c00537773dca28d572053658f80ef187c","C:\\ProjectFileStore\\FileRecv\\QMUI_Android").replaceAll("\n"," ");


        File outputFile = new File("C:\\ProjectFileStore\\output.txt");

        FileOutputStream fos = null;
        OutputStreamWriter osw = null;

        try {
            if (!outputFile.exists()) {
                boolean hasFile = outputFile.createNewFile();
                fos = new FileOutputStream(outputFile);
            } else {
                fos = new FileOutputStream(outputFile, true);
            }
        } catch (Exception e) {
            System.out.println("文件异常");
        }

        osw = new OutputStreamWriter(fos, "utf-8");

        String content = "";
        StringBuilder builder = new StringBuilder();

        File file = new File("C:\\ProjectFileStore\\repo2sha.json");
        InputStreamReader streamReader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(streamReader);

        while ((content = bufferedReader.readLine()) != null)
            builder.append(content);

        Map<String, List<String>> map = (Map<String, List<String>>) JSON.parse(builder.toString());

        int cnt = 0;
        long startTime = System.currentTimeMillis();
        for (Map.Entry<String, List<String>> entry:map.entrySet()){
            String prefixpath = entry.getKey();
            prefixpath = prefixpath.substring(prefixpath.indexOf("/"));
            List<String> lst = entry.getValue();
            for (String shacode:lst){
                System.out.println(shacode);
                String describes = "处理异常";
                try {
                    describes = localGit.ProjectCommitPath(shacode, "C:\\ProjectFileStore\\FileRecv\\" + prefixpath).replaceAll("\n", " ");
                } catch (Exception e){
                    System.out.println(e.toString());
                }
                try {
                    // 写入内容
                    osw.write(prefixpath + " " + shacode + " " + describes);
                    // 换行
                    osw.write("\n");
                    System.out.println("写入完毕");
                } catch (Exception e) {
                    System.out.println("写入异常");
                }

                cnt++;
                if (cnt % 100 == 0){
                    System.out.println("------------------ " + cnt + " ---------------------");
                    System.out.println("已运行 " + (System.currentTimeMillis() - startTime)/1000 + "s");
                }
            }
        }



*/
        String content = "";
        StringBuilder builder = new StringBuilder();

        File file = new File("C:\\ProjectFileStore\\sortedrepo2sha.json");
        InputStreamReader streamReader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(streamReader);

        while ((content = bufferedReader.readLine()) != null)
            builder.append(content);

        Map<String, List<String>> map = (Map<String, List<String>>) JSON.parse(builder.toString());

        List<Map.Entry<String,List<String>>> mpLst = new ArrayList<>(map.entrySet());
        Collections.sort(mpLst, new Comparator<Map.Entry<String, List<String>>>() {
            @Override
            public int compare(Map.Entry<String, List<String>> o1, Map.Entry<String, List<String>> o2) {
                return o2.getValue().size() - o1.getValue().size();
            }
        });

        Executor executors2= Executors.newFixedThreadPool(12);

        for (Map.Entry<String, List<String>> entry: mpLst){
            executors2.execute(new MultiCS(entry.getKey(),entry.getValue(),"C:\\ProjectFileStore\\output\\"));
        }


        return ;
    }

    public static OutputStreamWriter openWriteFile (String pathname) throws UnsupportedEncodingException {
        File outputFile = new File(pathname);
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        try {
            if (!outputFile.exists()) {
                boolean hasFile = outputFile.createNewFile();
                fos = new FileOutputStream(outputFile);
            } else {
                fos = new FileOutputStream(outputFile, false);
            }
        } catch (Exception e) {
            System.out.println("文件异常");
        }
        osw = new OutputStreamWriter(fos, "utf-8");
        return osw;
    }

    public static void getSortedCommitID() throws IOException, GitAPIException {
        File outputFile = new File("C:\\ProjectFileStore\\sortedrepo2sha.json");
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        try {
            if (!outputFile.exists()) {
                boolean hasFile = outputFile.createNewFile();
                fos = new FileOutputStream(outputFile);
            } else {
                fos = new FileOutputStream(outputFile, false);
            }
        } catch (Exception e) {
            System.out.println("文件异常");
        }
        osw = new OutputStreamWriter(fos, "utf-8");



        String content = "";
        StringBuilder builder = new StringBuilder();

        File file = new File("C:\\ProjectFileStore\\repo2sha.json");
        InputStreamReader streamReader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(streamReader);

        while ((content = bufferedReader.readLine()) != null)
            builder.append(content);

        Map<String, List<String>> map = (Map<String, List<String>>) JSON.parse(builder.toString());

        Map<String, List<String>> sortedMap = new HashMap<>();

        for (Map.Entry<String, List<String>> entry:map.entrySet()){
            String prefixpath = entry.getKey();
            prefixpath = prefixpath.substring(prefixpath.indexOf("/"));
            List<String> lst = entry.getValue();
            String gitpath = "C:\\ProjectFileStore\\FileRecv\\" + prefixpath;
            //String gitpath = "C:\\ProjectFileStore\\FileRecv\\dbeaver";
            Git git = openRpo(gitpath);
            Repository repository = git.getRepository();
            ObjectId ob = repository.resolve("master");

            Set<String> set = new HashSet<>(lst);
            //List<Ref> call = git.branchList().call();
            //StoredConfig config = repository.getConfig();
            Iterable<RevCommit> logs = git.log().all().call();
            int count = 0;

            List<String> sortedLst = new ArrayList<>();

            for (RevCommit rev : logs) {
                if (set.contains(rev.getId().getName())){
                    sortedLst.add(rev.getId().getName());
                }
            }

            Collections.reverse(sortedLst);

            sortedMap.put(entry.getKey(),sortedLst);

            System.out.println(entry.getKey() + " " + sortedLst.size());

            if (set.size() != sortedLst.size()){
                System.out.println("数量错误 原始大小:" + set.size() + "现在大小" + sortedLst.size());

                Set<String> sortedSet = new HashSet<>(sortedLst);
                set.removeAll(sortedSet);
                System.out.println(set);
            }
        }

        String str = JSON.toJSONString(sortedMap);

        osw.write(str);

        osw.flush();
        osw.close();
    }

    public static void writeFile(String content, String fileName) {

        FileOutputStream fos = null;
        OutputStreamWriter osw = null;

        try {
            osw = new OutputStreamWriter(fos, "utf-8");
            // 写入内容
            osw.write(content);
            // 换行
            osw.write("\n");
        } catch (Exception e) {
            System.out.println("写入异常");
        }
    }
}