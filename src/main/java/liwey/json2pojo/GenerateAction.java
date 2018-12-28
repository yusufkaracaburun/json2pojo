package liwey.json2pojo;

import java.io.File;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;

import org.jetbrains.annotations.NotNull;

/**
 * A custom IntelliJ action which loads a dialog which will generate Java POJO classes from a given JSON text.
 */
public class GenerateAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent event) {
        // Get the action folder
        Project project = event.getProject();
        VirtualFile actionFolder = event.getData(LangDataKeys.VIRTUAL_FILE);

        if (project != null && actionFolder != null && actionFolder.isDirectory()) {
            // Get the module source root and effective package name
            VirtualFile moduleSourceRoot = ProjectRootManager.getInstance(project).getFileIndex().getSourceRootForFile(actionFolder);
            String packageName = ProjectRootManager.getInstance(project).getFileIndex().getPackageNameByDirectory(actionFolder);

            // Show JSON dialog
            JsonInputDialog dialog = new JsonInputDialog((className, jsonText) -> {
                // Show background process indicator
                ProgressManager.getInstance().run(new Task.Backgroundable(project, "Json2Pojo Class Generation", false) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        // Generate POJOs
                        Generator generatePojos = new Generator(packageName, new File(moduleSourceRoot.getPath()), indicator);
                        generatePojos.generateFromJson(className, jsonText);

                        // Refresh UI
                        try {
                            Thread.sleep(100);
                            ProjectView.getInstance(project).refresh();
                            actionFolder.refresh(false, true);
                        } catch (InterruptedException ignored) {
                        }
                    }
                });
            });
            dialog.setLocationRelativeTo(null);
            dialog.setTitle("Json2Pojo");
            dialog.pack();
            dialog.setVisible(true);
        }
    }

    @Override
    public void update(AnActionEvent event) {
        // Get the project and action folder
        Project project = event.getProject();
        VirtualFile actionFolder = event.getData(LangDataKeys.VIRTUAL_FILE);

        if (project != null && actionFolder != null && actionFolder.isDirectory()) {
            // Set visibility based on if the package name is non-null
            String packageName = ProjectRootManager.getInstance(project).getFileIndex().getPackageNameByDirectory(actionFolder);
            event.getPresentation().setVisible(packageName != null);
        } else {
            event.getPresentation().setVisible(false);
        }
    }
}