package cse340.undo.app;

import android.os.Bundle;
import android.support.constraint.ConstraintSet;
import android.util.Log;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import cse340.undo.R;
import cse340.undo.actions.AbstractAction;
import cse340.undo.actions.AbstractReversibleAction;
import cse340.undo.history.AbstractStackHistory;
import cse340.undo.history.StackHistory;

/**
 * This is an Activity wrapper around DrawingView. Not only does it instantiate a DrawingView and
 * display it on the screen, but this adds reversible (undo/redo) support.
 *
 * Using DrawingView's onStrokeCompleted callback, we can do that action on the DrawingView and add
 * that action to the history model. Then, we may choose to undo or redo by asking the history model
 * for the next undo/redo action and then undoing/doing that action on the DrawingView
 *
 * @see StackHistory
 * @see AbstractReversibleDrawingActivity#doAction(AbstractAction)
 * @see AbstractReversibleDrawingActivity#undo()
 * @see AbstractReversibleDrawingActivity#redo()
 */
public abstract class AbstractReversibleDrawingActivity extends AbstractDrawingActivity {
    protected static final int DEFAULT_HISTORY_SIZE = 10;

    private final String LOG_TAG = getClass().getSimpleName();

    /** History model used to do/undo/redo actions. */
    protected final AbstractStackHistory mModel;

    /** View groups containing undo and redo menu buttons. */
    private ViewGroup mUndoMenu, mRedoMenu;

    /**
     * Class which defines a listener to be called when an action is done.
     */
    public interface ActionListener {
        void onAction(AbstractReversibleAction action);
    }

    /**
     * Class which defines a listener to be called when an action is undone.
     */
    public interface ActionUndoListener {
        void onActionUndone(AbstractReversibleAction action);
    }

    /** Data structure for storing listeners for action events **/
    private List<ActionListener> mActionListeners;

    /** Data structure for storing listeners for undo events **/
    private List<ActionUndoListener> mActionUndoListeners;

    /**
     * Creates a new AbstractReversibleDrawingActivity with the default history limit.
     */
    public AbstractReversibleDrawingActivity() {
        this(DEFAULT_HISTORY_SIZE);
    }

    /**
     * Creates a new AbstractReversibleDrawingActivity with the given history limit.
     *
     * @param history   Maximum number of history items to maintain.
     */
    public AbstractReversibleDrawingActivity(int history) {
        mModel = new StackHistory(history);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Add undo and redo menu buttons to the ConstraintLayout.
        mUndoMenu = (ViewGroup) getLayoutInflater().inflate(R.layout.undo_menu, mLayout, false);
        mRedoMenu = (ViewGroup) getLayoutInflater().inflate(R.layout.redo_menu, mLayout, false);
        addMenu(mUndoMenu, ConstraintSet.TOP, ConstraintSet.START);
        addMenu(mRedoMenu, ConstraintSet.TOP, ConstraintSet.START);

        findViewById(R.id.fab_undo).setOnClickListener((v) -> undo());
        findViewById(R.id.fab_redo).setOnClickListener((v) -> redo());

        mActionUndoListeners = new ArrayList<>();
        mActionListeners = new ArrayList<>();

        updateMenuButtons();
    }

    /**
     * Adds the action to the history, if it is reversible, or clears the history otherwise.
     *
     * @param action    AbstractAction to be saved to history.
     */
    @Override
    protected void doAction(AbstractAction action) {
        if (action == null) {
            return;
        }

        super.doAction(action);

        if (action instanceof AbstractReversibleAction) {
            // The action that was just done is undoable!
            AbstractReversibleAction ra = (AbstractReversibleAction) action;

            Log.i(LOG_TAG, "Before add: " + mModel);
            mModel.addAction(ra);
            Log.i(LOG_TAG, "After add: " + mModel);

            Log.i(LOG_TAG, "calling listeners on action");
            mActionListeners.forEach(l -> l.onAction(ra));
        } else {
            // The action that was just done is NOT undoable! Must clear history.

            Log.i(LOG_TAG, "Undoable action: " + action);
            mModel.clear();
        }

        updateMenuButtons();
    }

    /**
     * Redoes the most recently undone action (if any).
     */
    protected void redo() {
        Log.i(LOG_TAG, "Before redo: " + mModel);
        AbstractReversibleAction action = mModel.redo();
        Log.i(LOG_TAG, "After redo: " + mModel);

        if (action != null) {
            action.doAction(mDrawingView);
            Log.i(LOG_TAG, "calling action listeners on action");
            mActionListeners.forEach(l -> l.onAction(action));
        }

        updateMenuButtons();
    }

    /**
     * Undoes the most recently (re)done action (if reversible).
     */
    protected void undo() {
        Log.i(LOG_TAG, "Before undo" + mModel);
        AbstractReversibleAction action = mModel.undo();
        Log.i(LOG_TAG, "After undo" + mModel);

        if (action != null) {
            action.undoAction(mDrawingView);
            Log.i(LOG_TAG, "calling undo listeners on action");
            mActionUndoListeners.forEach(l -> l.onActionUndone(action));
        }

        updateMenuButtons();
    }

    protected void updateMenuButtons() {
        setViewVisibility(mUndoMenu, mModel.canUndo());
        setViewVisibility(mRedoMenu, mModel.canRedo());
    }

    /**
     * Registers a new undo listener for the history
     *
     * The listener is called *after* an action is undone
     */
    public void registerActionUndoListener(ActionUndoListener listener) {
        Log.i(LOG_TAG, "undo action listener registered");
        mActionUndoListeners.add(listener);
    }

    /**
     * Deregisters a undo listener for the history
     *
     * @return True if the listener did exist, and was thus deregistered. False otherwise
     */
    public boolean deregisterActionUndoListener(ActionUndoListener listener) {
        Log.i(LOG_TAG, "undo action listener deregistered");
        return mActionUndoListeners.remove(listener);
    }

    /**
     * Registers a new listener for the history
     *
     * The listener is called *after* an action is done
     */
    public void registerActionListener(ActionListener listener) {
        Log.i(LOG_TAG, "action listener registered");
        mActionListeners.add(listener);
    }

    /**
     * Deregisters a listener for the history
     *
     * @return True if the listener did exist, and was thus deregistered. False otherwise
     */
    public boolean deregisterActionListener(ActionListener listener) {
        Log.i(LOG_TAG, "action listener deregistered");
        return mActionListeners.remove(listener);
    }
}
