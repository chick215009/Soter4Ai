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

        List<String> methodName = new ArrayList<>();
        List<String> className = new ArrayList<>();

        GetLocalGitFile localGit = new GetLocalGitFile("C:\\commitTest\\tmp1");


        //localGit.ProjectCommitPath("222ad01fbbcc2b92a1eb2506a049cd19d4fd3685","/home/cuiyunqi/repo/WordPress-Android").replaceAll("\n"," ");
        //localGit.ProjectCommitPath("a44131af5cfd21e180be0805e07c1416e62a6e6f","F:\\codisumrepo\\Activiti").replaceAll("[\n\r\t]"," ");
        //System.out.println(srr);
//        String describes = localGit.ProjectCommitPath("f47634edfe08fcebbf564a5df12a0a96906032c3","F:\\codisumrepo\\platform_frameworks_base",methodName,className).replaceAll("[\n\r\t]"," ");
//        System.out.println(methodName);
//        System.out.println(className);
//        String describes = localGit.ProjectCommitPath("ed09d345effc459aa7e3dab3061a4150079b906e","F:\\FileRecv\\apollo").replaceAll("[\n\r\t]"," ");
//
//        //String describes = localGit.ProjectCommitPath("9e9e450211681a0ac7d9c4c3200dd19327362494","F:\\FileRecv\\spring-boot").replaceAll("[\n\r\t]"," ");
//        System.out.println(describes.split("[\n\r\t ]+").length);
//
//        if (describes.split("[\n\r\t ]+").length > 500){
//            describes = "输出超长";
//        }
//        System.out.println(describes);
//        localGit.ProjectCommitPath("a3899891281a9040912cd1a904d68487587e8512","F:\\FileRecv\\QMUI_Android").replaceAll("\n"," ");
        //localGit.ProjectCommitPath("35e130207d3944c856a62a20e8eef7f5a7171bb0","F:\\FileRecv\\apollo").replaceAll("\n"," ");
        //localGit.ProjectCommitPath("6b29f2d736eb9320469f59e3b4318bc260a061a8","F:\\FileRecv\\dbeaver").replaceAll("\n"," ");
        //String srr = localGit.ProjectCommitPath("fc836bd54dd4f621bafdd3aae29644248fd693b1","F:\\FileRecv\\Android-Universal-Image-Loader").replaceAll("\n"," ");
        //System.out.println(srr);

        //String srr = localGit.ProjectCommitPath("88be58d387ee73b44e0a76d90e46a8676dbda975","F:\\FileRecv\\Android-Universal-Image-Loader").replaceAll("\n"," ");
        //System.out.println(srr);
/*
        //localGit.ProjectCommitPath("a3899891281a9040912cd1a904d68487587e8512","F:\\FileRecv\\QMUI_Android").replaceAll("\n"," ");


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
                    describes = localGit.ProjectCommitPath(shacode, "F:\\FileRecv\\" + prefixpath).replaceAll("\n", " ");
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

        File file = new File("F:\\njusotal\\sortedrepo2sha.json");
        //File file = new File("/home/cuiyunqi/errorrepo2hash.json");
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
            executors2.execute(new MultiCS(entry.getKey(),entry.getValue(),"F:\\output7\\"));
            //executors2.execute(new MultiCS(entry.getKey(),entry.getValue(),"/home/cuiyunqi/output/"));
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

    public static void getSortedCommitID() throws IOException, GitAPIException { //排序用
        File outputFile = new File("F:\\njusotal\\sortedrepo2sha.json");
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

        File file = new File("F:\\njusotal\\download-89411_repotocode.json");
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
            String gitpath = "F:\\codisumrepo\\" + prefixpath;
            //String gitpath = "F:\\FileRecv\\dbeaver";
            Git git = openRpo(gitpath);
            Repository repository = git.getRepository();
            ObjectId ob = repository.resolve("master");

            Set<String> set = new HashSet<>(lst);
            //List<Ref> call = git.branchList().call();
            //StoredConfig config = repository.getConfig();
            Iterable<RevCommit> logs;
            try {
                logs = git.log().all().call();
            } catch (Exception e) {
                System.out.println(prefixpath);
                continue;
            }
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
