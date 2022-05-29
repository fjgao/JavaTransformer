import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Callable;

public class ASTExplorer implements Callable<Void> {

    ASTExplorer(String inpPath, String outPath) {
        if (!inpPath.endsWith("/")) {
            inpPath += "/";
        }
        Common.mRootInputPath = inpPath;

        if (!outPath.endsWith("/")) {
            outPath += "/";
        }
        Common.mRootOutputPath = outPath;
    }

    @Override
    public Void call() {
        inspectDataset();
        return null;
    }

    private void inspectDataset() {
        String input_dir = Common.mRootInputPath;
        ArrayList<File> javaFiles = new ArrayList<>(
                FileUtils.listFiles(
                        new File(input_dir),
                        new String[]{"java"},
                        true)
        );
        System.out.println(input_dir + " : " + javaFiles.size());

        //TODO: parallel transformation
        javaFiles.forEach((javaFile) -> {
            try {
                new FindVariableNameByWeight().print(javaFile);
                //new VariableRenaming().inspectSourceCode(javaFile);
                new BooleanExchange().inspectSourceCode(javaFile);
                new LoopExchange().inspectSourceCode(javaFile);
                new SwitchToIf().inspectSourceCode(javaFile);
                new ReorderCondition().inspectSourceCode(javaFile);
                new PermuteStatement().inspectSourceCode(javaFile);
                //new UnusedStatement().inspectSourceCode(javaFile);
                //new LogStatement().inspectSourceCode(javaFile);
                new TryCatch().inspectSourceCode(javaFile);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        new FindVariableNameByWeight().writeFileSortByValue();
    }
}
