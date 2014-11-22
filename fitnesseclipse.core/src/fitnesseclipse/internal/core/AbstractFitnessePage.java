package fitnesseclipse.internal.core;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import fitnesseclipse.core.IFitnessePage;

@SuppressWarnings("serial")
public abstract class AbstractFitnessePage implements IFitnessePage {
    private final String path;
    private final IProject project;

    public AbstractFitnessePage(IProject project, String path) {
        this.project = project;
        this.path = path;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public IFile getFile() {
        return project.getFile(path);
    }

    @Override
    public IProject getProject() {
        return project;
    }

    @Override
    public int compareTo(IFitnessePage o) {
        return getPath().compareTo(o.getPath());
    }
}
