package fitnesseclipse.core.internal.builder;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

public class DefaultResourceVisitor extends AbstractResourceHelper implements IResourceVisitor {

    public DefaultResourceVisitor(String fitnesseRoot, IProject project) {
        super(fitnesseRoot, project);
    }

    @Override
    public boolean visit(IResource resource) throws CoreException {
        if (isContentTxtFile(resource)) {
            deleteMarkers(resource);
            PageChecker.check(fitnesseRoot, project, resource);
            addPage(resource);
        }
        return true;
    }
}