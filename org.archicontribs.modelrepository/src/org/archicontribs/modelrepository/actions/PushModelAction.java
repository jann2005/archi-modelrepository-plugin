/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package org.archicontribs.modelrepository.actions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.archicontribs.modelrepository.IModelRepositoryImages;
import org.archicontribs.modelrepository.authentication.UsernamePassword;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;

import com.archimatetool.model.IArchimateModel;

/**
 * Push Model Action ("Publish")
 * 
 * 1. Do actions in Refresh Model Action
 * 2. If OK then Push to Remote
 * 
 * @author Phillip Beauvoir
 */
public class PushModelAction extends RefreshModelAction {
    
    public PushModelAction(IWorkbenchWindow window) {
        super(window);
        setImageDescriptor(IModelRepositoryImages.ImageFactory.getImageDescriptor(IModelRepositoryImages.ICON_PUSH));
        setText(Messages.PushModelAction_0);
        setToolTipText(Messages.PushModelAction_0);
    }

    public PushModelAction(IWorkbenchWindow window, IArchimateModel model) {
        super(window, model);
    }

    @Override
    public void run() {
        try {
            int status = init();
            if(status != USER_OK) {
                return;
            }
            
            // Do this before opening the progress dialog
            UsernamePassword npw = getUsernamePassword();

            // Do main action with PM dialog
            Display.getCurrent().asyncExec(new Runnable() {
                @Override
                public void run() {
                    ProgressMonitorDialog pmDialog = new ProgressMonitorDialog(fWindow.getShell());
                    
                    try {
                        pmDialog.run(false, true, new IRunnableWithProgress() {
                            @Override
                            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                                try {
                                    monitor.beginTask(Messages.PushModelAction_1, -1);
                                    
                                    // Pull
                                    int status = pull(npw, pmDialog);
                                    
                                    // Push
                                    if(status == PULL_STATUS_OK || status == PULL_STATUS_UP_TO_DATE) {
                                        push(npw, pmDialog);
                                    }
                                }
                                catch(Exception ex) {
                                    pmDialog.getShell().setVisible(false);
                                    displayErrorDialog(Messages.RefreshModelAction_0, ex);
                                }
                                finally {
                                    try {
                                        saveChecksumAndNotifyListeners();
                                    }
                                    catch(IOException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            }
                        });
                    }
                    catch(InvocationTargetException | InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
        catch(IOException | GitAPIException ex) {
            displayErrorDialog(Messages.PushModelAction_0, ex);
        }
    }
    
    protected void push(UsernamePassword npw, ProgressMonitorDialog pmDialog) throws IOException, GitAPIException {
        pmDialog.getProgressMonitor().subTask(Messages.PushModelAction_2);
        Display.getCurrent().readAndDispatch();  // update dialog
        getRepository().pushToRemote(npw, new ProgressMonitorWrapper(pmDialog.getProgressMonitor()));
    }
}
