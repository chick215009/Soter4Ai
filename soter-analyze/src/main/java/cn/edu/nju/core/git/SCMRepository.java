package cn.edu.nju.core.git;

import cn.edu.nju.core.git.ChangedFile.TypeChange;
import cn.edu.nju.core.utils.Utils;
import org.apache.xmlbeans.impl.tool.Diff;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class SCMRepository {
	
	private Git git;
	private Repository repository;
	private String projectPath;

	public SCMRepository(String projectPath) throws RuntimeException {
		super();
		this.projectPath = projectPath;
		if(null != projectPath && !projectPath.isEmpty()) {
			openRepository(projectPath);
		} else {
			throw new RuntimeException("You did not select a Java project");
		}
	}
	
	public void checkout(String versionID) {
		if(null != git) {
			try {
				if(!versionID.contains("HEAD")) { 
					git.checkout().setName(versionID).call();
				} else {
					git.checkout().setStartPoint(versionID).call();
				}
			} catch (GitAPIException e) {
				e.printStackTrace();
				throw new GitException("Can't checkout to this version: " + versionID);
			}
		}
	}

	private void openRepository(String path) {
		File file = new File(path);
		try {
			git = Git.open(file);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(null == git) {
				try {
					git = Git.open(file.getParentFile());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		if (git != null){
			repository = git.getRepository();
		}
	}
	
	public Repository getRepository() {
		return repository;
	}
	
	public Git getGit() {
		return git;
	}
	
	public Status getStatus() throws GitAPIException, NoWorkTreeException {
		if(git == null) {
			throw new GitException("No share this project with Git");
		}

		return git.status().call();
	}

	public Set<ChangedFile> getDifferences() throws IOException, GitAPIException {
		Set<ChangedFile> differences = new TreeSet<>();

		TreeWalk tw = new TreeWalk(repository);
		RevWalk walk = new RevWalk(repository);

		tw.setRecursive(true);
		tw.addTree(walk.parseCommit(repository.resolve("HEAD")).getTree());
		tw.addTree(new FileTreeIterator(repository));

		RenameDetector rd = new RenameDetector(repository);
		rd.addAll(DiffEntry.scan(tw));

		List<DiffEntry> lde = rd.compute(tw.getObjectReader(), null);
		List<String> renamedFile = new ArrayList<>();
		for (DiffEntry de : lde) {
			if (de.getScore() >= rd.getRenameScore()) {
				//System.out.println("file: " + de.getOldPath() + " copied/moved to: " + de.getNewPath());
				renamedFile.add(de.getOldPath());
				ChangedFile changedFile = new ChangedFile(de.getNewPath(), TypeChange.MODIFIED.name(), projectPath);
				changedFile.setRenamed(true);
				changedFile.setRenamedPath(de.getOldPath());
				differences.add(changedFile);
				changedFile.setTypeChange(TypeChange.MODIFIED);
			}
		}

		Status repositoryStatus = this.getStatus();


		for (String string : repositoryStatus.getChanged()) {
			ChangedFile changedFile = new ChangedFile(string, TypeChange.MODIFIED.name(), projectPath);
			differences.add(changedFile);
			changedFile.setTypeChange(TypeChange.MODIFIED);
		}
		for (String string : repositoryStatus.getModified()) {
			ChangedFile changedFile = new ChangedFile(string, TypeChange.MODIFIED.name(), projectPath);
			differences.add(changedFile);
			changedFile.setTypeChange(TypeChange.MODIFIED);
		}

		for (String string : repositoryStatus.getAdded()) {
			ChangedFile changedFile = new ChangedFile(string, TypeChange.ADDED.name(), projectPath);
			differences.add(changedFile);
			changedFile.setTypeChange(TypeChange.ADDED);
		}

		/*for (String string : repositoryStatus.getUntracked()) {
			ChangedFile changedFile = new ChangedFile(string, TypeChange.UNTRACKED.name(), projectPath);
			differences.add(changedFile);
			changedFile.setTypeChange(TypeChange.UNTRACKED);
		}*/

		for	(String string : repositoryStatus.getRemoved()) {
			if (renamedFile.contains(string)){
				continue;
			}
			//String preAbsPath = Utils.getFileContentOfLastCommit(string,repository).getAbsolutePath();
			ChangedFile changedFile = new ChangedFile(string, TypeChange.REMOVED.name(), projectPath);
			changedFile.setAbsolutePath(Utils.getFileContentOfLastCommit(string,repository).getAbsolutePath());
			differences.add(changedFile);
			changedFile.setTypeChange(TypeChange.REMOVED);
		}

		return differences;
	}

	public static Set<ChangedFile> getDifferences(Status repositoryStatus, String rootPath) {
		Set<ChangedFile> differences = new TreeSet<>();

		for (String string : repositoryStatus.getChanged()) {
			ChangedFile changedFile = new ChangedFile(string, TypeChange.MODIFIED.name(), rootPath);
			differences.add(changedFile);
			changedFile.setTypeChange(TypeChange.MODIFIED);
		}
		for (String string : repositoryStatus.getModified()) {
			ChangedFile changedFile = new ChangedFile(string, TypeChange.MODIFIED.name(), rootPath);
			differences.add(changedFile);
			changedFile.setTypeChange(TypeChange.MODIFIED);
		}
		
		for (String string : repositoryStatus.getAdded()) {
			ChangedFile changedFile = new ChangedFile(string, TypeChange.ADDED.name(), rootPath);
			differences.add(changedFile);
			changedFile.setTypeChange(TypeChange.ADDED);
		}
		
		for (String string : repositoryStatus.getUntracked()) {
			ChangedFile changedFile = new ChangedFile(string, TypeChange.UNTRACKED.name(), rootPath);
			differences.add(changedFile);
//			System.out.println("path: " + changedFile.getPath());
			changedFile.setTypeChange(TypeChange.UNTRACKED);
		}
		
		for	(String string : repositoryStatus.getRemoved()) {
			ChangedFile changedFile = new ChangedFile(string, TypeChange.REMOVED.name(), rootPath);
			differences.add(changedFile);
			changedFile.setTypeChange(TypeChange.REMOVED);
		}
		
		return differences;
	}
	
	public static Set<ChangedFile> getRemovedFiles(Status repositoryStatus, String rootPath) {
		Set<ChangedFile> differences = new TreeSet<ChangedFile>();
		for	(String string : repositoryStatus.getRemoved()) {
			ChangedFile changedFile = new ChangedFile(string, TypeChange.REMOVED.name(), rootPath);
			differences.add(changedFile);
			changedFile.setTypeChange(TypeChange.REMOVED);
		}
		return differences;
	}

	public String getProjectPath() {
		return projectPath;
	}

	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}
}
