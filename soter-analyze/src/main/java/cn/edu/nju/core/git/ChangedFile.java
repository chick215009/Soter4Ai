package cn.edu.nju.core.git;

import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;
import cn.edu.nju.core.stereotype.stereotyped.StereotypeIdentifier;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.util.List;

@SuppressWarnings("rawtypes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangedFile implements Comparable {
	private String path;//相对路径
	private String absolutePath;//绝对路径
	private String projectPath;//项目路径
	private String changeType;
	private String name;//文件名
	private TypeChange typeChange;//变化类型
	private boolean isRenamed;//是否重命名
	private String renamedPath;
//	private List<StereotypedMethod> methods;
	private StereotypeIdentifier stereotypeIdentifier;
	private int preferenceCount;
	private List<StructureEntityVersion> modifiedMethods;



	public static enum TypeChange {
		ADDED("ADDED"),
		MODIFIED("MODIFIED"),
		UNTRACKED("UNTRACKED"),
		REMOVED("REMOVED"),
		ADDED_INDEX_DIFF("ADDED_INDEX_DIFF"),
		REMOVED_NOT_STAGED("REMOVED_NOT_STAGED"),
		REMOVED_UNTRACKED("REMOVED_UNTRACKED"),
		UNTRACKED_FOLDERS("UNTRACKED_FOLDERS");

		private TypeChange(String type) {

		}
	}

	public ChangedFile(String path, TypeChange typeChange, String rootPath) {
		super();
		this.path = path;
		this.typeChange = typeChange;
		this.name = new File(path).getName();
		if(null == this.name || this.name.equals("null")) {
			//System.getProperty("file.separator")) file.separator 在 UNIX 系统中是"/"
			this.name = path.substring(path.lastIndexOf(System.getProperty("file.separator")) + 1);
		}
		this.absolutePath = rootPath + System.getProperty("file.separator") + (new File(path).getPath());
		preferenceCount = 0;
	}

	public ChangedFile(String path, String changeType, String rootPath) {
		super();
		this.path = path;
		this.changeType = changeType;
		this.name = new File(path).getName();

		if(null == this.name || name.equals("null")) {
			this.name = path.substring(path.lastIndexOf(System.getProperty("file.separator")) + 1);
		}
		this.absolutePath = rootPath + System.getProperty("file.separator") + (new File(path).getPath());
	}

	@Override
	public int compareTo(Object o2) {
		return this.getPath().compareTo(((ChangedFile) o2).getPath());
	}

}
