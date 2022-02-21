package cn.edu.nju.controller;

import cn.edu.nju.controller.vo.AnalyzeResultVO;
import cn.edu.nju.controller.vo.ChangedClassVO;
import cn.edu.nju.controller.vo.CommitMessageVO;
import cn.edu.nju.controller.vo.FilterVo;
import cn.edu.nju.core.filter.*;
import cn.edu.nju.service.CodeDiffService;
import cn.edu.nju.service.CommitService;
import cn.edu.nju.service.FileService;
import cn.edu.nju.service.GitService;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
public class PanelController {
    @Autowired
    FileService fileService;
    @Autowired
    GitService gitService;
    @Autowired
    CommitService commitService;
    @Autowired
    CodeDiffService codeDiffService;
    @RequestMapping("/hello")
    @ResponseBody
    public String hello() {
        return "hello";
    }

    @RequestMapping("/git-init")
    public Boolean gitInit(@RequestParam("projectPath") String projectPath) {
        return gitService.gitInit(projectPath);
    }

    @RequestMapping("/git-add-all")
    public Boolean gitAddAll(@RequestParam("projectPath") String projectPath) {
        return gitService.gitAddAll(projectPath);
    }

    @RequestMapping("/git-commit")
    public Boolean gitCommit(@RequestParam("projectPath") String projectPath,
                             @RequestParam("message") String message) {
        return gitService.gitCommit(projectPath, message);
    }

    @RequestMapping("/update-project")
    public Boolean updateProject(@RequestParam("projectPath") String projectPath,
                                 @RequestParam("newProjectPath") String newProjectPath) {
        return fileService.updateProject(projectPath, newProjectPath);
    }


    @RequestMapping("/clear-projectDir")
    public Boolean clearProjectDir(@RequestParam("projectPath") String projectPath) {
        return fileService.clearDirectory(projectPath);
    }

    @RequestMapping("generate-commit-message")
    public CommitMessageVO generateCommitMessage(@RequestParam("projectPath") String projectPath) {
        return commitService.generateCommitMessage(projectPath);
    }

    @RequestMapping("generate-commit-message2")
    public String generateCommitMessage2(@RequestBody FilterVo filterVo) throws GitAPIException, IOException, ClassNotFoundException {
//        return commitService.generateCommitMessage(projectPath);
        String projectPath = filterVo.getProjectPath();
        String newProjectPath = filterVo.getNewProjectPath();
        SimpleDescribeFilter simpleDescribeFilter = new SimpleDescribeFilter(filterVo.getIsSimpleDescribe());

        LabelTypeFilter labelTypeFilter = new LabelTypeFilter(filterVo.getIsTypeLabel(),
                filterVo.getIsInterfaceList(),
                filterVo.getIsSuperclassStr(),
                filterVo.getIsTypeStereotypeLabel(),
                filterVo.getIsLocal(),
                filterVo.getReferencedList(),
                filterVo.getReferencedCount());
        List<String> filterCategory = filterVo.getFilterCategory();
        Set<String> filterCategorySet = new HashSet<>();
        filterCategorySet = filterCategory.stream().collect(Collectors.toSet());

        List<String> filterMethodStereotype = filterVo.getFilterMethodStereotype();
        Set<String> filterMethodStereotypeSet = new HashSet<>();
        filterMethodStereotypeSet = filterMethodStereotype.stream().collect(Collectors.toSet());
        CategoryOrStereotypeMethodFilter categoryOrStereotypeMethodFilter =
                new CategoryOrStereotypeMethodFilter(
                        filterCategorySet,
                        filterMethodStereotypeSet,
                        filterVo.getIsMethodStereotypeLabel());

        List<String> filterTypeStereotypes = filterVo.getFilterTypeStereotypes();
        Set<String> filterTypeStereotypesSet = new HashSet<>();
        filterTypeStereotypesSet = filterTypeStereotypes.stream().collect(Collectors.toSet());
        StereotypeTypeFilter stereotypeTypeFilter = new StereotypeTypeFilter(filterTypeStereotypesSet);

        DetailDescribeFilter detailDescribeFilter = new DetailDescribeFilter(filterVo.getIsDetailDescribe(), filterVo.getIsNewModuleDescribe(), labelTypeFilter, stereotypeTypeFilter, categoryOrStereotypeMethodFilter);

        fileService.clearDirectory(projectPath);
        gitService.gitInit(projectPath);
        fileService.updateProject(projectPath, newProjectPath);

        return commitService.generateCommitMessageWithFilter(projectPath, simpleDescribeFilter, detailDescribeFilter);
    }

    @RequestMapping("/generate-commit-message3")
    public AnalyzeResultVO generateCommitMessage3(@RequestBody FilterVo filterVo) throws GitAPIException, IOException, ClassNotFoundException {
//        return commitService.generateCommitMessage(projectPath);
        String projectPath = filterVo.getProjectPath();
        String newProjectPath = filterVo.getNewProjectPath();
        SimpleDescribeFilter simpleDescribeFilter = new SimpleDescribeFilter(filterVo.getIsSimpleDescribe());

        LabelTypeFilter labelTypeFilter = new LabelTypeFilter(filterVo.getIsTypeLabel(),
                filterVo.getIsInterfaceList(),
                filterVo.getIsSuperclassStr(),
                filterVo.getIsTypeStereotypeLabel(),
                filterVo.getIsLocal(),
                filterVo.getReferencedList(),
                filterVo.getReferencedCount());
        List<String> filterCategory = filterVo.getFilterCategory();
        Set<String> filterCategorySet = new HashSet<>();
        filterCategorySet = filterCategory.stream().collect(Collectors.toSet());

        List<String> filterMethodStereotype = filterVo.getFilterMethodStereotype();
        Set<String> filterMethodStereotypeSet = new HashSet<>();
        filterMethodStereotypeSet = filterMethodStereotype.stream().collect(Collectors.toSet());
        CategoryOrStereotypeMethodFilter categoryOrStereotypeMethodFilter =
                new CategoryOrStereotypeMethodFilter(filterCategorySet,
                        filterMethodStereotypeSet,
                        filterVo.getIsMethodStereotypeLabel());

        List<String> filterTypeStereotypes = filterVo.getFilterTypeStereotypes();
        Set<String> filterTypeStereotypesSet = new HashSet<>();
        filterTypeStereotypesSet = filterTypeStereotypes.stream().collect(Collectors.toSet());
        StereotypeTypeFilter stereotypeTypeFilter = new StereotypeTypeFilter(filterTypeStereotypesSet);

        DetailDescribeFilter detailDescribeFilter = new DetailDescribeFilter(filterVo.getIsDetailDescribe(), filterVo.getIsNewModuleDescribe(), labelTypeFilter, stereotypeTypeFilter, categoryOrStereotypeMethodFilter);

//        fileService.clearDirectory(projectPath);
//        gitService.gitInit(projectPath);
//        fileService.updateProject(projectPath, newProjectPath);

        return commitService.generateAnalyzeResultVo(projectPath, simpleDescribeFilter, detailDescribeFilter);
    }

    @RequestMapping("/generate-commit-message4")
    public String generateCommitMessage4(@RequestParam("projecPath") String projectPath,
                                         @RequestParam("newProjectPath") String newProjectPath) throws GitAPIException, IOException, ClassNotFoundException {
//        return commitService.generateCommitMessage(projectPath);

        return "";
    }

    @RequestMapping("/get-changed-classes")
    public List<ChangedClassVO> getChangedClasses(@RequestParam("projectPath")String projectPath) throws GitAPIException {
        return codeDiffService.getChangedClasses(projectPath);
    }

    @RequestMapping("/get-adjacent-projects")
    public List<String> getAdjacentProjects(@RequestParam("classPath")String classPath,
                                            @RequestParam("projectPath")String projectPath) throws IOException {
        return codeDiffService.getAdjacentProjects(classPath, projectPath);
    }


}
