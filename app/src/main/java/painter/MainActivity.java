package painter;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.util.Arrays;

import cse340.undo.R;
import painter.actions.ActionArrow;
import painter.actions.ActionLetters;
import painter.actions.ActionNumbers;
import painter.actions.ActionOval;
import painter.actions.ActionRectangle;
import painter.actions.ActionStraightLine;
import painter.actions.ActionStroke;

public class MainActivity extends AppCompatActivity {

    /** View groups containing undo and redo menu buttons. */
    ConstraintLayout mLayout;
    private ViewGroup mUndoMenu, mRedoMenu;
    Paper paper;
    /** Keep track of last menu item added to menus so we may add more. */
    private SparseIntArray mMenusLastId;

    private int mFabMargin;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // full screen setup
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        setContentView(R.layout.activity_full_screen);
        mLayout = findViewById(R.id.layout);
        mFabMargin = getResources().getDimensionPixelSize(R.dimen.fab_parent_margin);
        mMenusLastId = new SparseIntArray();
        paper = findViewById(R.id.paper);
        paper.setBackgroundColor(Color.WHITE);
        findViewById(R.id.edit_action_button).setOnClickListener((v) -> {
            paper.editActionButtonClicked();
        });
        // set up undo/redo button
        mUndoMenu = (ViewGroup) getLayoutInflater().inflate(R.layout.undo_menu, mLayout , false);
        mRedoMenu = (ViewGroup) getLayoutInflater().inflate(R.layout.redo_menu, mLayout , false);
        addMenu(mUndoMenu, ConstraintSet.TOP, ConstraintSet.START);
        addMenu(mRedoMenu, ConstraintSet.TOP, ConstraintSet.START);



        findViewById(R.id.fab_undo).setOnClickListener((v) -> paper.undo());
        findViewById(R.id.fab_redo).setOnClickListener((v) -> paper.redo());

        experiment();
    }

    void experiment() {
        findViewById(R.id.erase).setOnClickListener((v) -> {
            paper.toggleEraseMode();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
    // testing hooking up paper
    public void rect(View v) {
        paper.setDrawAction(ActionRectangle.class);
    }
    public void oval(View v) {
        paper.setDrawAction(ActionOval.class);
    }
    public void line(View v) {
        paper.setDrawAction(ActionStraightLine.class);
    }
    public void arrow(View v) {
        paper.setDrawAction(ActionArrow.class);
    }
    public void aStroke(View v) {
        paper.setDrawAction(ActionStroke.class);
    }
    public void clear(View v) {
        paper.clear();
    }
    public void nums(View v) {
        paper.setDrawAction(ActionNumbers.class);
    }
    public void letters(View v) {
        paper.setDrawAction(ActionLetters.class);
    }

}
