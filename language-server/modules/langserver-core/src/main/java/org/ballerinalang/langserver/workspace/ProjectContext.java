package org.ballerinalang.langserver.workspace;

import io.ballerina.projects.Project;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;

import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class holds project and its lock.
 * 
 * @since 2201.7.0
 */
public class ProjectContext {

    private final Lock lock;
    private Project project;

    private boolean crashed;

    private Process process;

    private ProjectContext(Project project, Lock lock) {
        this.project = project;
        this.lock = lock;
    }

    public static ProjectContext from(Project project) {
        return new ProjectContext(project, new ReentrantLock(true));
    }

    public static ProjectContext from(Project project, Lock lock) {
        return new ProjectContext(project, lock);
    }

    /**
     * Returns the associated lock for the file.
     *
     * @return {@link Lock}
     */
    public Lock locker() {
        return this.lock;
    }

    /**
     * Returns the associated lock for the file.
     *
     * @return {@link Lock}
     */
    public Lock lockAndGet() {
        this.lock.lock();
        return this.lock;
    }

    /**
     * Returns the workspace document.
     *
     * @return {@link WorkspaceDocumentManager}
     */
    public Project project() {
        return this.project;
    }

    /**
     * Set workspace document.
     *
     * @param project {@link Project}
     */
    public void setProject(Project project) {
        this.project = project;
    }

    /**
     * Check if the project is in a crashed state.
     *
     * @return whether the project is in a crashed state
     */
    public boolean crashed() {
        return Boolean.TRUE.equals(this.crashed);
    }

    /**
     * Set the crashed state.
     *
     * @param crashed crashed state
     */
    public void setCrashed(boolean crashed) {
        this.crashed = crashed;
    }

    /**
     * Project lock should be acquired before modifying (such as destroying) the process.
     * @return Process associated with the project.
     */
    public Optional<Process> process() {
        return Optional.ofNullable(this.process);
    }

    /**
     * Set the process associated with the project. Project lock should be acquired before calling.
     * @param process Process to be associated with the project.
     */
    public void setProcess(Process process) {
        this.process = process;
    }

    /**
     * Remove the process associated with the project. Project lock should be acquired before calling.
     */
    public void removeProcess() {
        this.process = null;
    }
}
