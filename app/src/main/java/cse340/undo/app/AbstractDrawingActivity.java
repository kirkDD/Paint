package cse340.undo.app;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseIntArray;
import android.view.View;

import java.util.Arrays;

import cse340.undo.R;
import cse340.undo.actions.AbstractAction;

/**
 * This is an Activity wrapper around DrawingView. It instantiates a DrawingView and adds it to the
 * main content layout so it fills the screen.
 */
public abstract class AbstractDrawingActivity extends AppCompatActivity {
    /** Drawing view used for drawing strokes and doing actions. */
    protected DrawingView mDrawingView;

    /** Constraint layout holding draw. */
    protected ConstraintLayout mLayout;

    /** Keep track of last menu item added to menus so we may add more. */
    private SparseIntArray mMenusLastId;

    private int mFabMargin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup content view and action bar.
        setContentView();

        mLayout = findViewById(R.id.layout);
        mMenusLastId = new SparseIntArray();
        mFabMargin = getResources().getDimensionPixelSize(R.dimen.fab_parent_margin);

        // Register onStrokeCompleted listener.
        mDrawingView = findViewById(R.id.draw);
        mDrawingView.addListener(this::doAction);
    }

    /**
     * Sets the current content view. Overrideable so students can use custom layouts.
     */
    protected void setContentView() {
        setContentView(R.layout.drawing_activity);
    }

    /**
     * When a stroke is completed, apply it to the current draw view.
     *
     * @param action    AbstractAction to be applied to the current draw view.
     */
    protected void doAction(AbstractAction action) {
        if (action != null) {
            action.doAction(mDrawingView);
        }
    }

    /**
     * Adds a menu item to the group of menus on the screen.
     *
     * @param menu  View to add as a menu item.
     * @param verticalAnchor    ConstraintSet constant to anchor the menu vertically.
     * @param horizontalAnchor  ConstraintSet constant to anchor the menu horizontally.
     */
    protected void addMenu(View menu, int verticalAnchor, int horizontalAnchor) {
        mLayout.addView(menu);

        ConstraintSet cons = new ConstraintSet();
        cons.clone(mLayout);

        switch (verticalAnchor) {
            case ConstraintSet.TOP:
            case ConstraintSet.BOTTOM:
                cons.connect(menu.getId(), verticalAnchor, ConstraintSet.PARENT_ID, verticalAnchor, mFabMargin);
                break;
            default:
                throw new IllegalStateException("Illegal verticalAnchor " + verticalAnchor);
        }

        int key = verticalAnchor * horizontalAnchor;
        int lastMenuId = mMenusLastId.get(key, ConstraintSet.PARENT_ID);

        switch (horizontalAnchor) {
            case ConstraintSet.START:
            case ConstraintSet.LEFT:
                if (lastMenuId == ConstraintSet.PARENT_ID) {
                    cons.connect(menu.getId(), horizontalAnchor, lastMenuId, horizontalAnchor, mFabMargin);
                } else {
                    cons.connect(menu.getId(), horizontalAnchor, lastMenuId, horizontalAnchor + 1, mFabMargin);
                }
                break;
            case ConstraintSet.END:
            case ConstraintSet.RIGHT:
                if (lastMenuId == ConstraintSet.PARENT_ID) {
                    cons.connect(menu.getId(), horizontalAnchor, lastMenuId, horizontalAnchor, mFabMargin);
                } else {
                    cons.connect(menu.getId(), horizontalAnchor, lastMenuId, horizontalAnchor - 1, mFabMargin);
                }
                break;
            default:
                throw new IllegalStateException("Illegal horizontalAnchor " + horizontalAnchor);
        }

        cons.applyTo(mLayout);
        mMenusLastId.put(key, menu.getId());
    }

    /**
     * Adds a collapsible menu to the screen.
     *
     * @param layoutId  ID of the layout which contains the menu.
     * @param verticalAnchor    ConstraintSet constant to anchor the menu vertically.
     * @param horizontalAnchor  ConstraintSet constant to anchor the menu horizontally.
     * @param items List of collapsible item IDs.
     * @param listener  Listener to be registered for onClick on each item.
     */
    protected void addCollapsableMenu(@LayoutRes int layoutId,
                                      int verticalAnchor,
                                      int horizontalAnchor,
                                      @IdRes int[] items,
                                      View.OnClickListener listener) {
        View menu = getLayoutInflater().inflate(layoutId, mLayout, false);
        addMenu(menu, verticalAnchor, horizontalAnchor);

        ConstraintSet cons = new ConstraintSet();
        cons.clone(mLayout);

        if (verticalAnchor == ConstraintSet.BOTTOM) {
            cons.connect(menu.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        } else {
            cons.connect(menu.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        }

        cons.applyTo(mLayout);

        Arrays.stream(items).mapToObj(this::findViewById).forEach(
                v -> ((View) v).setOnClickListener(listener));
    }

    protected static void setViewVisibility(View view, boolean visible) {
        if (view != null) {
            view.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
    }
}
