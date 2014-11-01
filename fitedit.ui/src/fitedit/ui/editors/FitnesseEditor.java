package fitedit.ui.editors;

import java.util.ResourceBundle;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import fitedit.ui.editors.actions.CommentTextEditorAction;
import fitedit.ui.editors.outline.FitOutlinePage;

/**
 * Entry point of this plugin
 * 
 * @author Toshiyuki Fukuzawa
 */
public class FitnesseEditor extends TextEditor {

    public static final String EDITOR_ID = "fitedit.ui.editors.FitnesseEditor";

    private FitOutlinePage outlinePage;
    private ProjectionSupport projectionSupport;

    public FitnesseEditor() {
        super();
        setSourceViewerConfiguration(new FitSourceViewerConfiguration());
        setDocumentProvider(new FitDocumentProvider());
    }

    @Override
    protected void createActions() {
        super.createActions();

        CommentTextEditorAction action = new CommentTextEditorAction(ResourceBundle.getBundle("messages"), "", this);
        action.configure(getSourceViewer(), getSourceViewerConfiguration());
        setAction("comment", action);
    }

    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class required) {

        // set outline page
        if (IContentOutlinePage.class.equals(required)) {
            if (outlinePage == null) {
                outlinePage = new FitOutlinePage(getDocumentProvider(), this);
                outlinePage.setInput(getEditorInput());
            }
            return outlinePage;
        }

        // folding
        if (projectionSupport != null) {
            Object adapter = projectionSupport.getAdapter(getSourceViewer(), required);
            if (adapter != null)
                return adapter;
        }

        return super.getAdapter(required);
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
        setTabLabel(input);
    }

    /**
     * Set the tab label with the parent directory's name
     */
    private void setTabLabel(IEditorInput input) {
        IFile file = ResourceUtil.getFile(input);
        if (file == null) {
            return;
        }

        String fileName = file.getName();
        if (fileName == null || !fileName.equals("content.txt")) {
            return;
        }

        IContainer parent = file.getParent();
        if (parent == null) {
            return;
        }

        setPartName(parent.getName());
    }

    @Override
    public void doSave(IProgressMonitor progressMonitor) {
        super.doSave(progressMonitor);

        if (outlinePage != null) {
            outlinePage.update();
        }
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);

        // turn projection mode on
        ProjectionViewer viewer = (ProjectionViewer) getSourceViewer();
        projectionSupport = new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());
        projectionSupport.install();
        viewer.doOperation(ProjectionViewer.TOGGLE);
    }

    @Override
    protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
        ISourceViewer viewer = new ProjectionViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);

        // ensure decoration support has been created and configured.
        getSourceViewerDecorationSupport(viewer);

        return viewer;
    }

    @Override
    protected void initializeKeyBindingScopes() {
        setKeyBindingScopes(new String[] { "fitedit.ui.editors.fitnesseEditorScope" });
    }
}
