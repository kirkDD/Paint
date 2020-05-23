package cse340.undo.history;

import android.support.annotation.NonNull;

import java.util.Deque;
import java.util.LinkedList;

import cse340.undo.actions.AbstractReversibleAction;

/**
 * Keeps a history of actions that have been done and undone using two stacks. When an item is done,
 * it is pushed onto the undo stack. When an item is undone, it is popped from the undo stack and
 * pushed to the redo stack. The number of history items is limited by the capacity.
 */
public class StackHistory implements AbstractStackHistory {
    /** Data structures for staring undo/redo events. */
    private final Deque<AbstractReversibleAction> mUndoStack, mRedoStack;

    /** Should always be true that mUndoStack.size() + mRedoStack.size() <= capacity. */
    private final int mCapacity;

    /**
     * Initializes empty undo/redo stacks.
     *
     * @param capacity  Maximum size of undo/redo stacks.
     * @throws IllegalStateException if capacity is not positive.
     */
    public StackHistory(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Illegal capacity: " + capacity);
        }
        this.mCapacity = capacity;

        mUndoStack = new LinkedList<>();
        mRedoStack = new LinkedList<>();
    }

    /**
     * Add a reversible event to the history.
     *
     * @param action    Reversible action to be added.
     */
    @Override
    public void addAction(AbstractReversibleAction action) {
        // TODO: support addAction
        // 1. If the stack is full, remove the oldest thing in it
        // 2. Add the new event to the undo stack
        // 3. Clear out the redo stack (when we do a new action we have to delete all the redo
        // actions to ensure consistency)
    }

    /**
     * Undoes an action.
     *
     * @return null if there is nothing to undo, otherwise the action to be undone.
     */
    @Override
    public AbstractReversibleAction undo() {
        // TODO: support undo
        // 1. If the undo stack is empty return null
        // 2. Otherwise remove the most recent action from the stack
        // 2.1. Add it to the redo stack
        // 2.2. Return it.

        return null;
    }

    /**
     * Redoes an action.
     *
     * @return null if there is nothing to redo, otherwise the action to be redone.
     */
    @Override
    public AbstractReversibleAction redo() {
        // TODO: support redo
        // 1. If the redo stack is empty return null
        // 2. Otherwise get the most recent action from the stack
        // 2.1. Add it to the undo stack
        // 2.2. Return it.

        return null;
    }

    /**
     * Clears the history.
     */
    @Override
    public void clear() {
        // TODO: clear the datastructures
    }

    /**
     * Is there anything that can be undone?
     *
     * @return True if can undo any actions, false otherwise.
     */
    @Override
    public boolean canUndo() {
        return !mUndoStack.isEmpty();
    }

    /**
     * Is there anything that can be done?
     *
     * @return True if can redo any actions, false otherwise.
     */
    @Override
    public boolean canRedo() {return !mRedoStack.isEmpty();}

    @NonNull
    public String toString() {
        return  "Undo size: " + mUndoStack.size() + ", redo size: " + mRedoStack.size();
    }
}
