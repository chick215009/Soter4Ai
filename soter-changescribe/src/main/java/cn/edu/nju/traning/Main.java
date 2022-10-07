package cn.edu.nju.traning;

import cn.edu.nju.core.filter.*;
import cn.edu.nju.core.git.ChangedFile;
import cn.edu.nju.core.git.SCMRepository;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class Main {
    private static SCMRepository repo ;
    private static Set<ChangedFile> differences;
    public static Git git;

    // 根目录
    public static String projectPath = "/Users/chengleming/work/projectDir/";
    // A、B file数据的文件夹的路径
    public static String DATA_PATH = "/Users/chengleming/Library/Containers/com.tencent.xinWeChat/Data/Library/Application Support/com.tencent.xinWeChat/2.0b4.0.9/eefb8be5f9238e581b3ad38afaf83cc7/Message/MessageTemp/0bd50e08626b44e67e98831fe9b80f1c/File/trainingset";
    // ChangeScribe输出的文件，对应difftext.json
    private static String outputFile = "/Users/chengleming/work/硕士毕业论文/yq/difftext_test.txt";
    // real message文件，对应msgtext.json
    public static String msgText = "/Users/chengleming/work/硕士毕业论文/yq/msgtext_test.txt";
    // log信息
    private static String logText = "/Users/chengleming/work/硕士毕业论文/yq/log_test.txt";
    // 唯一标识
    private static String indexFilePath = "/Users/chengleming/work/硕士毕业论文/yq/index_test.txt";

    public static void main(String[] args) throws IOException, GitAPIException, ClassNotFoundException {
        File gitRootDir = new File(projectPath);	// git根目录

        String gitSrcDirPath = projectPath + "src/";
        File gitSrcDir = new File(gitSrcDirPath);	// git目录中的src目录，用于临时存放待cs的文件

        String dotGitDirPath = projectPath + ".git/";
        File dotGitDir = new File(dotGitDirPath);	// .git文件

        if (dotGitDir.exists()) {	// 判断.git文件是否存在
            FileUtils.deleteDirectory(dotGitDir);	// 存在，则删除.git文件
            cleanDir(gitSrcDir);	// 且清空src目录
        }

        /**
         * 初始化commit
         */
        git = Git.init().setDirectory(gitRootDir).call();
        git.add().addFilepattern(".").call();
        git.commit().setMessage("beginning").call();
        System.out.println("=====Beginning=====");

        // 用于保存每次的A、B文件，单例
        List<File> AFiles = new ArrayList<>(); // 用于提取单次commit中所有A Files
        List<File> BFiles = new ArrayList<>(); // 同上，提取B Files
        // 用于记录message信息，单例
        StringBuilder msgTextBuilder = new StringBuilder();
        // 用于记录唯一标识
        StringBuilder indexBuilder = new StringBuilder();
        // 用于输出日志信息
        File logFile = new File(logText);

        // *******维烨代码*********
        // 获取所有的path
        List<String> commitPathList = getAllCommitDirPath(DATA_PATH);
        // 记录commit总数
        FileUtils.writeStringToFile(logFile, "commit总数：" + commitPathList.size() + "\n", true);

        int emptyCommitCount = 0; // 空commit文件计数
        int bigCommitCount = 0; // 超出限制的commit计数
        int noDiffCommitCount = 0; // 未能检测出差异的commit计数
        int noSummaryCommitCount = 0; // 无法summary的commit计数

        /**
         * 大循环：对每一条commit hash进行处理
         */
        for (String commitPath : commitPathList) {
            System.out.println(commitPath); // 输出当前commit目录

            File commitDir = new File(commitPath); // commit目录
            File[] allFiles = commitDir.listFiles(); // commit目录下的所有文件，AB Files & realMsg.txt

            /**
             * 数据过滤
             */
            // 防止该commit文件夹为空
            if (allFiles == null || allFiles.length < 1) {
                emptyCommitCount++;
                // 删除本次commit文件夹
//                    FileUtils.deleteDirectory(commitDir);
                continue;
            }
            // 防止该commit过大
            if (allFiles.length > 21) {
                bigCommitCount++;
                // 删除本次commit文件夹
//                    FileUtils.deleteDirectory(commitDir);
                continue;
            }

            File realMsg = new File(commitPath + "/realMsg.txt");
            if (!realMsg.exists()) {	// realMsg.txt不存在
                emptyCommitCount++;
                // 删除本次commit文件夹
//					FileUtils.deleteDirectory(commitDir);
                continue;
            } else if (FileUtils.readFileToString(realMsg, StandardCharsets.UTF_8).trim().equals("")) {	// 为空
                emptyCommitCount++;
                // 删除本次commit文件夹
//					FileUtils.deleteDirectory(commitDir);
                continue;
            }

            /**
             * 文件分拣
             */
            for (File f : allFiles) {
                // 防止异常：commit目录中还有其他文件夹，理论上不会
                if (f.isDirectory()) {
                    continue;
                }
                // 防止异常：出现Mac生成的文件
                if (f.getName().equals(".DS_Store")) {
                    continue;
                }
                // 归类
                if (f.getName().charAt(0) == 'A') {
                    AFiles.add(f);
                } else if (f.getName().charAt(0) == 'B'){
                    BFiles.add(f);
                } else if (f.getName().equals("realMsg.txt")){
                    msgTextBuilder.append(FileUtils.readFileToString(f, StandardCharsets.UTF_8));
                } else if (f.getName().endsWith(".diff")) {
                    // 记录唯一标识，即diff文件前缀
                    indexBuilder.append(f.getName(), 0, f.getName().lastIndexOf("."));
                }
            }

            /**
             * 每次开始处理数据前，commit初始化
             */
            if (!dotGitDir.exists()) {
                git = Git.init().setDirectory(gitRootDir).call();
                git.add().addFilepattern(".").call();
                git.commit().setMessage("loop begin").call();
            }

            // ******* A文件的复制与提交 ********
            // 将A文件重命名并复制到git目录下
            if (!AFiles.isEmpty() || (AFiles.size() > 0)) {
                for (File aFile : AFiles) {
                    // 复制文件到Git目录下
                    FileUtils.copyFileToDirectory(aFile, gitSrcDir);

                    // 重命名
                    File newFile = new File(gitSrcDirPath + aFile.getName());
                    String newFilename = aFile.getName().substring(2);
                    newFile.renameTo(new File(gitSrcDirPath + newFilename));
                }
            }

            /**
             * 提交A文件
             */
            git.add().addFilepattern(".").call();
            git.commit().setMessage("A Files Committed").call();
            System.out.println("=====A Files Committed=====");


            // ******* A文件的删除，B文件的复制 ********
            cleanDir(gitSrcDir);

            //复制B文件
            for (File bFile : BFiles) {
                // 复制文件到Git目录下
                FileUtils.copyFileToDirectory(bFile, gitSrcDir);

                // 重命名
                File newFile = new File(gitSrcDirPath + bFile.getName());
                String newFilename = bFile.getName().substring(2);
                newFile.renameTo(new File(gitSrcDirPath + newFilename));
            }
            System.out.println("=====B Files Standby=====");

            /**
             * ChangeScribe原操作
             */
            // 初始化differences，找到所有改动的java文件
            gettingRepositoryStatus();

            // 若提取不到差异，则本次循环结束，清空一些参数
            if (differences == null || differences.size() == 0) {
                noDiffCommitCount++;
                // 清空src文件夹
                cleanDir(gitSrcDir);

                // 清空realMsg字符串
                msgTextBuilder.delete(0, msgTextBuilder.length());
                // 清空index字符串
                indexBuilder.delete(0, indexBuilder.length());
                // 清空A、B列表
                AFiles.clear();
                BFiles.clear();

                // 删除.git文件
                FileUtils.deleteDirectory(dotGitDir);
                System.out.println("No differences");

                // 删除本次commit文件夹
//                    FileUtils.deleteDirectory(commitDir);

                continue;
            }

            // 遍历git提交历史记录，影响效率，暂时用不上
            //RepositoryHistory.getRepositoryHistory(git);

            SCMRepository scmRepository = new SCMRepository(projectPath);
            Status status = scmRepository.getStatus();

            SummarizeChangesForTrain summarizeChanges = new SummarizeChangesForTrain(Main.git, projectPath);
            summarizeChanges.summarize(differences.toArray(new ChangedFile[differences.size()]),
                    new SimpleDescribeFilter(false),
                    new DetailDescribeFilter(true, false,
                            new LabelTypeFilter(false, false, false, false, false, false, 0),
                            new StereotypeTypeFilter(),
                            new CategoryOrStereotypeMethodFilter()
                            ));
            StringBuilder detailDescribe = summarizeChanges.getDetailDescribe();
            // 核心代码 - summarize the differences
//            summarizer.summarize(differences.toArray(changes));

            System.out.println("=====Core Done=====");


            // 格式化，输出summarize
            File output = new File(outputFile);
            String summary = summarizeChanges.getSummary().trim();
            // 如果ChangeScribe没有结果，那么就默认输出Fixed a bug
            if (summary.isEmpty() || summary.trim().isEmpty()) {
                noSummaryCommitCount++;

                /**
                 * 一系列删除记录的操作
                 */
                // 清空文件夹
                cleanDir(gitSrcDir);
                // 清空字符串
                msgTextBuilder.delete(0, msgTextBuilder.length());
                // 清空index字符串
                indexBuilder.delete(0, indexBuilder.length());
                // 清空A、B列表
                AFiles.clear();
                BFiles.clear();
                // 删除.git文件
                FileUtils.deleteDirectory(dotGitDir);
                System.out.println("No summary");
                // 删除本次commit文件夹
//                    FileUtils.deleteDirectory(commitDir);
                continue;
            }
            // 输出summary
            FileUtils.writeStringToFile(output, StringUtils.stringEscape(summary) + "\n", true);

            // 输出real message
            File msgFile = new File(msgText);
            String message = msgTextBuilder.toString();
            FileUtils.writeStringToFile(msgFile, StringUtils.stringEscape(message) + "\n", true);

            // 输出index
            File indexFile = new File(indexFilePath);
            String indexNumber = indexBuilder.toString();
            FileUtils.writeStringToFile(indexFile, indexNumber + "\n", true);

            /**
             * 一系列删除记录的操作
             */
            // 清空文件夹
            cleanDir(gitSrcDir);
            // 清空字符串
            msgTextBuilder.delete(0, msgTextBuilder.length());
            // 清空index字符串
            indexBuilder.delete(0, indexBuilder.length());
            // 清空A、B列表
            AFiles.clear();
            BFiles.clear();
            // 删除.git文件
            FileUtils.deleteDirectory(dotGitDir);
            System.out.println("=====Summarize successfully=====");
            // 删除本次commit文件夹
//                FileUtils.deleteDirectory(commitDir);

            Date timeEnd = new Date();
            String logString = "hash：" + commitDir.getName().substring(0, 4)
                    + "\t结束时间：" + DateUtils.formatTime(timeEnd)
                    + "\t无summary的commit：" + noSummaryCommitCount
                    + "\n";
            FileUtils.writeStringToFile(logFile, logString, true);

        }
        String describeEmpty = "空commit文件夹共：" + emptyCommitCount + "个\n";
        String describeBig = "超出限制的commit共" + bigCommitCount + "个\n";
        String describeNoDiff = "没有检测出有变动的commit共" + noDiffCommitCount + "个\n";
        String describeNoSummary = "无summary的commit共" + noSummaryCommitCount + "个\n";
        FileUtils.writeStringToFile(logFile, describeEmpty + describeBig + describeNoDiff + describeNoSummary, true);

    }

    public static void cleanDir(File dir) {
        for(File javaFile : dir.listFiles()) {
            if(!javaFile.isDirectory()) {
                javaFile.delete();
            }
        }
    }

    public static List<String> getAllCommitDirPath(String download_filepath){
        List<String> resultList = new ArrayList<>();

        File dataRootDir = new File(download_filepath);
        //这是所有项目的第一级目录地址，每一个一级目录下可能有多个二级目录
        File[] allMainDir = dataRootDir.listFiles();
        for(File mainDir : allMainDir){
            if (mainDir.getName().equals(".DS_Store")) {
                continue;
            }
            //由于每个一级目录下可能有多个二级目录，需要注意
            File[] allSecondDir = mainDir.listFiles();
            for(File secondDir : allSecondDir){
                if (secondDir.getName().equals(".DS_Store")) {
                    continue;
                }
                File[] allCommitDir = secondDir.listFiles();
                for(File commitDir : allCommitDir){
                    if (commitDir.getName().equals(".DS_Store")) {
                        continue;
                    }
                    String filepath = commitDir.getAbsolutePath();
                    resultList.add(filepath);
                }
            }
        }
        return resultList;
    }

    /**
     * 初始化git，并获取git diff
     * @return
     */
    private static void gettingRepositoryStatus() {
        //git = repo.getGit();

        if(git != null) {
            Status status = null;
            try {
                // 由于要频繁删除.git文件，此处改为用git直接获取status
                status = git.status().call();
            } catch (GitAPIException e) {
                e.printStackTrace();
            }

            System.out.println("Extracting source code differences !");
            differences = SCMRepository.getDifferences(status,git.getRepository().getWorkTree().getAbsolutePath());

        }
    }
}
