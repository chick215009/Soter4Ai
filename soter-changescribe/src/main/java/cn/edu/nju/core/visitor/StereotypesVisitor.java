package cn.edu.nju.core.visitor;

import cn.edu.nju.core.stereotype.stereotyped.StereotypedElement;
import cn.edu.nju.core.stereotype.stereotyped.StereotypedType;
import lombok.Data;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.ArrayList;
import java.util.List;

@Data
public class StereotypesVisitor extends ASTVisitor {
    private List<StereotypedElement> stereotypedElements;


    public StereotypesVisitor() {

        stereotypedElements = new ArrayList<>();

    }

    public boolean visit(TypeDeclaration node) {
        StereotypedElement stereoElement = new StereotypedType(node, 0, 0);
        stereoElement.findStereotypes();
        return true;
    }
}
