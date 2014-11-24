package fitnesseclipse.core.tests.helpers;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;

public class WorkspaceHelper {

    public static void cleanWorkspace() throws InterruptedException, CoreException {
        Exception cause = null;
        int i;
        for (i = 0; i < 10; i++) {
            try {
                System.gc();
                doCleanWorkspace();
            } catch (InterruptedException e) {
                throw e;
            } catch (OperationCanceledException e) {
                throw e;
            } catch (Exception e) {
                cause = e;
                e.printStackTrace();
                System.out.println(i);
                Thread.sleep(6 * 1000);
                continue;
            }

            // all clear
            return;
        }

        // must be a timeout
        throw new CoreException(new Status(IStatus.ERROR, "", "Could not delete workspace resources (after " + i
                + " retries): " + Arrays.asList(ResourcesPlugin.getWorkspace().getRoot().getProjects()), cause));
    }

    private static void doCleanWorkspace() throws InterruptedException, CoreException, IOException {
        final IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.run(new IWorkspaceRunnable() {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                IProject[] projects = workspace.getRoot().getProjects();
                for (IProject project : projects) {
                    project.delete(true, true, monitor);
                }
            }
        }, new NullProgressMonitor());

        JobHelper.waitForJobsToComplete(new NullProgressMonitor());

        File[] files = workspace.getRoot().getLocation().toFile().listFiles();
        if (files != null) {
            for (File file : files) {
                if (!".metadata".equals(file.getName())) {
                    if (file.isDirectory()) {
                        FileUtils.deleteDirectory(file);
                    } else {
                        if (!file.delete()) {
                            throw new IOException("Could not delete file " + file.getCanonicalPath());
                        }
                    }
                }
            }
        }
    }

}