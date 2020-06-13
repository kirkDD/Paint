package cse340.undo.actions;

/**
 * Any action which has a view that can be invalidated.
 */
public abstract class AbstractReversibleViewAction extends AbstractReversibleAction {
    public abstract void invalidate();
}
