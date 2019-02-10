package de.terraconia.backups.extensions;

import de.terraconia.backups.tasks.AbstractTask;

public abstract class AbstractExtension {
    public void preExecute(AbstractTask task) {} ;
    public void postExecute(AbstractTask task) {};
}
