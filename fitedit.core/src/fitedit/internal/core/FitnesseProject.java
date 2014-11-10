package fitedit.internal.core;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import fitedit.core.FiteditCore;
import fitedit.core.FitnesseModel;
import fitedit.core.IFitnessePage;
import fitedit.core.IFitnesseProject;
import fitedit.core.IFitnesseStaticPage;
import fitedit.core.IFitnesseSuitePage;
import fitedit.core.IFitnesseTestPage;

public class FitnesseProject implements IFitnesseProject {

    private IProject project;

    public FitnesseProject(IProject project) {
        this.project = project;
    }

    @Override
    public IFitnesseTestPage createTestPage(IPath path) {
        try {
            String root = FiteditCore.getFiteditCore().getFitnesseRoot();
            IFolder target = project.getFolder(root + "/TemplateLibrary/TestPage");
            IPath destination = project.getFile(path.removeLastSegments(1)).getFullPath();
            target.copy(destination, false, null);
            buildAndWaitForEnd(project);
            return FiteditCore.create(project).findTestPage(path);
        } catch (CoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static boolean buildAndWaitForEnd(final IProject project) {
        IWorkbench workbench = PlatformUI.getWorkbench();
        IProgressService progressService = workbench.getProgressService();
        final IRunnableWithProgress runnable = new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
                IJobManager jobManager = Job.getJobManager();
                try {
                    project.build(IncrementalProjectBuilder.FULL_BUILD, null);
                } catch (CoreException e) {
                    throw new InvocationTargetException(e);
                }
                if (!monitor.isCanceled()) {
                    try {
                        jobManager.join(ResourcesPlugin.FAMILY_MANUAL_BUILD, monitor);
                        jobManager.join(ResourcesPlugin.FAMILY_AUTO_BUILD, monitor);
                    } catch (InterruptedException e) {
                        // continue
                    }
                }
            }
        };

        try {
            progressService.busyCursorWhile(runnable);
            return true;
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
        }
        return false;
    }

    @Override
    public IFitnesseSuitePage findSuitePage(IPath path) {
        return FitnesseModel.getFitnesseModel().getSuitePage(path);
    }

    @Override
    public IFitnesseTestPage findTestPage(IPath path) {
        return FitnesseModel.getFitnesseModel().getTestPage(path);
    }

    @Override
    public IFitnessePage findPage(IPath path) {
        IPath thePath = path.addTrailingSeparator().append("content.txt");
        IFitnessePage page = findSuitePage(thePath);
        if (page != null) {
            return page;
        }

        page = findTestPage(thePath);
        if (page != null) {
            return page;
        }

        return findStaticPage(thePath);
    }

    @Override
    public IFitnesseStaticPage findStaticPage(IPath path) {
        return FitnesseModel.getFitnesseModel().getStaticPage(path);
    }

}