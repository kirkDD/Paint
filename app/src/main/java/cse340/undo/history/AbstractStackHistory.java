package cse340.undo.history;

import cse340.undo.actions.AbstractReversibleAction;

/**
 * Keeps a history of actions that have been done and undone. When undo is called, the most recently
 * added action (via addAction) should be returned. When redo is called, the most recently undone
 * action should be returned.
 */
public interface AbstractStackHistory {
    /**
     * Add a reversible event to the history.
     *
     * @param action    Reversible action to be added.
     */
    void addAction(AbstractReversibleAction action);

    /**
     * Undoes an action.
     *
     * @return null if there is nothing to undo, otherwise the action to be undone.
     */
    AbstractReversibleAction undo();

    /**
     * Redoes an action.
     *
     * @return null if there is nothing to redo, otherwise the action to be redone.
     */
    AbstractReversibleAction redo();

    /**
     * Clears the history.
     */
    void clear();

    /**
     * Is there anything that can be undone?
     *
     * @return True if can undo any actions, false otherwise.
     */
    boolean canUndo();

    /**
     * Is there anything that can be done?
     *
     * @return True if can redo any actions, false otherwise.
     */
    boolean canRedo();
}
