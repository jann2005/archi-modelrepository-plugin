/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package org.archicontribs.modelrepository.grafico;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.lib.BranchConfig;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;

/**
 * BranchInfo
 * 
 * @author Phillip Beauvoir
 */
public class BranchInfo {
    
    private Ref ref;
    private String shortName;
    private boolean isRemoteDeleted;
    private boolean isCurrentBranch;
    private boolean hasTrackedRef;
    private boolean hasLocalRef;
    private boolean hasRemoteRef;
    
    private File repoDir; 
    
    private final static String REMOTE = Constants.R_REMOTES + IGraficoConstants.ORIGIN + "/"; //$NON-NLS-1$

    BranchInfo(Repository repository, Ref ref) throws IOException {
        this.ref = ref;
        
        repoDir = repository.getDirectory();
        
        hasLocalRef = getHasLocalRef(repository);
        hasRemoteRef = getHasRemoteRef(repository);
        hasTrackedRef = getHasTrackedRef(repository);

        isRemoteDeleted = getIsRemoteDeleted(repository);
        isCurrentBranch = getIsCurrentBranch(repository);
    }
    
    public Ref getRef() {
        return ref;
    }
    
    public String getFullName() {
        return ref.getName();
    }
    
    public String getShortName() {
        if(shortName == null) {
            shortName = getShortName(getFullName());
        }
        return shortName;
    }
    
    public boolean isLocal() {
        return getFullName().startsWith(BranchStatus.localPrefix);
    }

    public boolean isRemote() {
        return getFullName().startsWith(BranchStatus.remotePrefix);
    }

    public boolean hasLocalRef() {
        return hasLocalRef;
    }

    public boolean hasRemoteRef() {
        return hasRemoteRef;
    }

    public boolean isRemoteDeleted() {
        return isRemoteDeleted;
    }

    public boolean isCurrentBranch() {
        return isCurrentBranch;
    }
    
    public boolean hasTrackedRef() {
        return hasTrackedRef;
    }
    
    public String getRemoteBranchNameFor() {
        return BranchStatus.remotePrefix + getShortName();
    }
    
    public String getLocalBranchNameFor() {
        return BranchStatus.localPrefix + getShortName();
    }

    private boolean getHasLocalRef(Repository repository) throws IOException {
        return repository.findRef(getLocalBranchNameFor()) != null;
    }

    private boolean getHasRemoteRef(Repository repository) throws IOException {
        return repository.findRef(getRemoteBranchNameFor()) != null;
    }

    private boolean getHasTrackedRef(Repository repository) throws IOException {
        if(isRemote()) {
            return getHasLocalRef(repository);
        }
        
        return getHasRemoteRef(repository);
    }
    
    /*
     * Figure out whether the remote branch has been deleted
     * 1. We have a local branch ref
     * 2. We are tracking it
     * 3. But it does not have a remote branch ref
     */
    private boolean getIsRemoteDeleted(Repository repository) throws IOException {
        if(isRemote()) {
            return false;
        }
        
        // Is it being tracked?
        BranchConfig branchConfig = new BranchConfig(repository.getConfig(), getShortName());
        boolean isBeingTracked = branchConfig.getRemoteTrackingBranch() != null;
        
        // Does it have a remote ref?
        boolean hasNoRemoteBranchFor = repository.findRef(getRemoteBranchNameFor()) == null;
        
        // Is being tracked but no remote ref
        return isBeingTracked && hasNoRemoteBranchFor;
    }

    private boolean getIsCurrentBranch(Repository repository) throws IOException {
        return getFullName().equals(repository.getFullBranch());
    }
    
    private String getShortName(String branchName) {
        if(branchName.startsWith(Constants.R_HEADS)) {
            return branchName.substring(Constants.R_HEADS.length());
        }
        
        if(branchName.startsWith(REMOTE)) {
            return branchName.substring(REMOTE.length());
        }
        
        return branchName;
    }
    
    @Override
    public boolean equals(Object obj) {
        return (obj != null) &&
                (obj instanceof BranchInfo) &&
                repoDir.equals(((BranchInfo)obj).repoDir) &&
                getFullName().equals(((BranchInfo)obj).getFullName());
    }
}