package cn.edu.nju.core.stereotype.analyzer;

import cn.edu.nju.core.stereotype.stereotyped.StereotypedMethod;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class JdtAstUtil {
    public static final int AST_LEVEL = AST.JLS8;
    /**
     * get compilation unit of source code
     * @param javaFilePath
     * @return CompilationUnit
     */
    public static CompilationUnit getCompilationUnit(String javaFilePath){
        byte[] input = null;
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(javaFilePath));
            input = new byte[bufferedInputStream.available()];
            bufferedInputStream.read(input);
            bufferedInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        ASTParser astParser = ASTParser.newParser(AST_LEVEL);
        astParser.setSource(new String(input).toCharArray());
        astParser.setKind(ASTParser.K_COMPILATION_UNIT);
        ASTNode astNode = astParser.createAST(null);

        return (CompilationUnit)astNode;
    }

    public static List<StereotypedMethod> getMethods(String absolutePath) {
        CompilationUnit compilationUnit = JdtAstUtil.getCompilationUnit(absolutePath);
        DemoVisitor visitor = new DemoVisitor();
        compilationUnit.accept(visitor);
        return visitor.getMethods();
    }
}