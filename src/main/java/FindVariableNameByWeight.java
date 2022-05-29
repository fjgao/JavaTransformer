import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.visitor.TreeVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.opencsv.CSVWriter;
import com.github.javaparser.ParseProblemException; 

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.io.*;
import java.util.*;
import java.lang.*;
import java.io.FileWriter;


public class FindVariableNameByWeight extends VoidVisitorAdapter<Object> {
    private File mJavaFile = null;
    private int mVariableCounter = 0;
    static HashMap<String, Integer> mVarName2Weight = new HashMap<>();
    //static Map<Integer, String> mWeight2VarName = new TreeMap<Integer, String>();

    FindVariableNameByWeight(){
    }

    public void print(File javaFile) {
        this.mJavaFile = javaFile;
        Common.setOutputPath(this, mJavaFile);
        try{
            CompilationUnit root = Common.getParseUnit(mJavaFile);
            if (root != null) {
                this.visit(root.clone(), null);
 
            }
        }
        catch (Exception parseProblemException) {
            System.err.print("getParseUnit went wrong");
        }

    }

    // function to sort hashmap by values
    public void writeFileSortByValue()
    {
        List<Map.Entry<String, Integer> > alist =
        new LinkedList<Map.Entry<String, Integer> >(mVarName2Weight.entrySet());
        // Sort the list
        Collections.sort(alist, new Comparator<Map.Entry<String, Integer> >() {
            public int compare(Map.Entry<String, Integer> o1,
                                Map.Entry<String, Integer> o2)
            {
                //compareTo: 0: o1==o2, 1:o1>o2(small to large), -1: o1<o2(sort large to small)
                return (-1)*(o1.getValue()).compareTo(o2.getValue());
            }
        });            
        // put data from sorted list to hashmap
      /*  for (Map.Entry<String, Integer> aa : alist) {
            mVarName2Weight.put(aa.getKey(), aa.getValue());
        } */
        try {
            FileWriter fwriter = new FileWriter(Common.mRootOutputPath+"/VariableNameByWeight.csv",true);
            CSVWriter writer = new CSVWriter(fwriter);
            for (Map.Entry<String, Integer> aa : alist) {
               // System.out.println(aa.getKey()+" "+aa.getValue());
                if (aa.getKey().length() == 1) {
                    //System.out.println(aa.getKey()+"size is 1");
                    continue;
                }
                    
                String line1[] = {aa.getKey(), aa.getValue().toString()};
                writer.writeNext(line1);
            }
            writer.flush();
            writer.close();
            fwriter.close();
            System.out.println("writeFileSortByValue End");            
    
        } catch (IOException e) {
            System.err.print("writeFileSortByValue went wrong");
        }

    }

    @Override
    public void visit(CompilationUnit com, Object obj) {
        locateVariable(com, obj);
        //Common.applyToPlace(this, com, mJavaFile, mVariableNodes);
        super.visit(com, obj);
    }

    private void locateVariable(CompilationUnit com, Object obj) {
        new TreeVisitor() {
            @Override
            public void process(Node node) {
                if (isTargetVariable(node, com)) {
                    node.setData(Common.VariableId, mVariableCounter++);
                    node.setData(Common.VariableName, node.toString());
                    String varName = node.getData(Common.VariableName);
                    
                    if (mVarName2Weight.containsKey(varName)){
                        Integer cnt = mVarName2Weight.get(varName);
                        mVarName2Weight.remove(varName);
                        mVarName2Weight.put(varName,cnt+1);
                    }
                    else {
                        mVarName2Weight.put(varName,1);
                    }

                    
                }
            }
        }.visitPreOrder(com);
        //System.out.println("TargetVariable : " + mVariableList);
    }

    private boolean isTargetVariable(Node node, CompilationUnit com) {
        return (node instanceof SimpleName &&
                (node.getParentNode().orElse(null) instanceof Parameter
                        || node.getParentNode().orElse(null) instanceof VariableDeclarator));
    }

}
