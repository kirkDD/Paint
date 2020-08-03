package painter;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.constraint.ConstraintSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;


import cse340.undo.R;
import cse340.undo.app.AbstractColorPickerView;
import cse340.undo.app.ColorPickerView;

public class DrawingActivity extends MainActivity implements AbstractColorPickerView.ColorChangeListener{

    /** List of menu item FABs for action menu. */
    @IdRes
    private static final int[] ACTION_MENU_ITEMS = {
            R.id.erase, R.id.edit_action_button, R.id.clear
    };

    /** List of menu item FABs for shape menu. */
    @IdRes
    private static final int[] SHAPE_MENU_ITEMS = {
            R.id.rect, R.id.oval, R.id.line, R.id.arrow, R.id.stroke, R.id.number, R.id.letters
    };

    private int mMiniFabSize;

    /** State variables used to track whether menus are open. */
    private boolean isActionMenuOpen;
    private boolean isShapeMenuOpen;
    private boolean isColorMenuOpen;
    /** the ColorPicker that selects color */
    protected ColorPickerView mColorPickerView;

    @SuppressLint({"PrivateResource", "ClickableViewAccessibility"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMiniFabSize = getResources().getDimensionPixelSize(R.dimen.design_fab_size_mini);

        mColorPickerView =findViewById(R.id.colorpicker);
        mColorPickerView.setColor(AbstractColorPickerView.DEFAULT_COLOR);
        mColorPickerView.addColorChangeListener(this);

        View colorMenu = getLayoutInflater().inflate(R.layout.color_menu, mLayout, false);
        addMenu(colorMenu, ConstraintSet.BOTTOM, ConstraintSet.START);
        findViewById(R.id.fab_color).setOnClickListener((v) -> {
            enableCollapsibleMenu(R.id.fab_action,ACTION_MENU_ITEMS, isColorMenuOpen);
            enableCollapsibleMenu(R.id.fab_shape, SHAPE_MENU_ITEMS, isColorMenuOpen);
            isColorMenuOpen = showColorPicker(isColorMenuOpen);
        });
        addCollapsableMenu(R.layout.action_menu, ConstraintSet.BOTTOM, ConstraintSet.START, ACTION_MENU_ITEMS, this::onActionMenuSelected);
        findViewById(R.id.fab_action).setOnClickListener((v) -> {
            enableCollapsibleMenu(R.id.fab_shape, SHAPE_MENU_ITEMS, isActionMenuOpen);
            enableFAB(R.id.fab_color, isActionMenuOpen);
            isActionMenuOpen = toggleMenu(ACTION_MENU_ITEMS, isActionMenuOpen);
        });

        addCollapsableMenu(R.layout.shape_menu, ConstraintSet.BOTTOM, ConstraintSet.START, SHAPE_MENU_ITEMS, this::onShapeMenuSelected);
        findViewById(R.id.fab_shape).setOnClickListener((v) -> {
            enableCollapsibleMenu(R.id.fab_action, ACTION_MENU_ITEMS, isShapeMenuOpen);
            enableFAB(R.id.fab_color, isShapeMenuOpen);
            isShapeMenuOpen = toggleMenu(SHAPE_MENU_ITEMS, isShapeMenuOpen);
        });

        paper.setOnTouchListener((view, event) -> {
            if (isShapeMenuOpen) {
                if (event.getActionMasked() == MotionEvent.ACTION_UP && event.getPointerCount() == 1) {
                    isShapeMenuOpen = toggleMenu(SHAPE_MENU_ITEMS, isShapeMenuOpen);
                    enableCollapsibleMenu(R.id.fab_action, ACTION_MENU_ITEMS, !isShapeMenuOpen);
                    enableFAB(R.id.fab_color, !isShapeMenuOpen);
                }
                return true;
            } else if (isActionMenuOpen) {
                if (event.getActionMasked() == MotionEvent.ACTION_UP && event.getPointerCount() == 1) {
                    isActionMenuOpen = toggleMenu(ACTION_MENU_ITEMS, isActionMenuOpen);
                    enableCollapsibleMenu(R.id.fab_shape, SHAPE_MENU_ITEMS, !isActionMenuOpen);
                    enableFAB(R.id.fab_color, !isActionMenuOpen);
                }
                return true;
            } else if (isColorMenuOpen) {
                if (event.getActionMasked() == MotionEvent.ACTION_UP && event.getPointerCount() == 1) {
                    isColorMenuOpen = showColorPicker(isColorMenuOpen);
                    enableCollapsibleMenu(R.id.fab_action, ACTION_MENU_ITEMS, !isColorMenuOpen);
                    enableCollapsibleMenu(R.id.fab_shape, SHAPE_MENU_ITEMS, !isColorMenuOpen);
                }
                return true;
            } else {
                return paper.onTouchEvent(event);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //  deregister the color change listener
        mColorPickerView.removeColorChangeListener(this);
    }
    /**
     * callback for creating an action when the user changes the shape.
     * @param view The FAB the user clicked on.
     */
    private void onColorMenuSelected(View view) {
        isColorMenuOpen = showColorPicker(isColorMenuOpen);
        enableCollapsibleMenu(R.id.fab_action, ACTION_MENU_ITEMS, !isColorMenuOpen);
        enableCollapsibleMenu(R.id.fab_shape, SHAPE_MENU_ITEMS, !isColorMenuOpen);
    }
    /**
     * Callback for creating an action when the user changes the shape.

     * @param view The FAB the user clicked on.
     */
    private void onShapeMenuSelected(View view) {
        switch (view.getId()) {
            case R.id.rect:
                rect(view);
                break;
            case R.id.oval:
                oval(view);
                break;
            case R.id.line:
                line(view);
                break;
            case R.id.stroke:
                aStroke(view);
                break;
            case R.id.arrow:
                arrow(view);
                break;
            case R.id.number:
                nums(view);
                break;
            case R.id.letters:
                letters(view);
                break;
        }
        isShapeMenuOpen = toggleMenu(SHAPE_MENU_ITEMS, isShapeMenuOpen);
        enableCollapsibleMenu(R.id.fab_action, ACTION_MENU_ITEMS, !isShapeMenuOpen);
        enableFAB(R.id.fab_color, !isShapeMenuOpen);
    }

    /**
     * Callback for creating an action when the user changes the action.

     * @param view The FAB the user clicked on.
     */
    private void onActionMenuSelected(View view) {
        switch (view.getId()) {
            case R.id.edit_action_button:
                paper.editActionButtonClicked();
                break;
            case R.id.clear:
                clear(view);
                break;
            case R.id.erase:
                paper.toggleEraseMode();
                break;
        }
        isActionMenuOpen = toggleMenu(ACTION_MENU_ITEMS, isActionMenuOpen);
        enableCollapsibleMenu(R.id.fab_shape, SHAPE_MENU_ITEMS, !isActionMenuOpen);
        enableCollapsibleMenu(R.id.fab_color, new int[0], !isActionMenuOpen);
        //enableFAB(R.id.fab_color, !isActionMenuOpen);
    }

    /**
     * Toggles a collapsible menu. That is, if it's open, it closes it. If it's closed, it opens it.
     *
     * @param items List of IDs of items in the menu, all FABs.
     * @param open  Current state of the menu.
     * @return True if the menu is now open, false otherwise.
     */
    private boolean toggleMenu(@IdRes int[] items, boolean open) {
        enableFAB(R.id.fab_undo, open);
        enableFAB(R.id.fab_redo, open);
        if (!open) {
            for (int i = 0; i < items.length; i++) {
                View view = findViewById(items[i]);
                view.animate()
                        .translationY(-3 * mMiniFabSize * (i + 1.5f) / 2.5f)
                        .alpha(1)
                        .withEndAction(() -> view.setClickable(true));
            }
            return true;
        } else {
            for (int item : items) {
                View view = findViewById(item);
                view.setClickable(false);
                view.animate().translationY(0).alpha(0);
            }
            return false;
        }
    }


    /**
     * Disables and enables collapsible menu FABs
     *
     * @param menuId The resID of the menu activation FAB
     * @param menuItems An array of resIDs for the menu item FABs
     * @param enabled true if the menu should be enabled, false if the menu should be disabled
     */
    private void enableCollapsibleMenu(@IdRes int menuId, @IdRes int[] menuItems, boolean enabled) {
        enableFAB(menuId, enabled);
        for (@IdRes int item : menuItems) {
            findViewById(item).setEnabled(enabled);
        }
    }

    /**
     * Disables and enables FABs
     *
     * @param buttonId the resID of the FAB
     * @param enabled true if the button should be enabled, false if the button should be disabled
     */
    private void enableFAB(@IdRes int buttonId, boolean enabled) {
        findViewById(buttonId).setEnabled(enabled);
        findViewById(buttonId).setBackgroundTintList(ColorStateList.valueOf(enabled ?
                getResources().getColor(R.color.colorAccent) : Color.LTGRAY));
    }

    /**
     * toggles the visibility of ColorPicker
     * @param open whether the ColorPicker was open or not
     * @return !open
     */
    private boolean showColorPicker(boolean open) {
        enableFAB(R.id.fab_undo, open);
        enableFAB(R.id.fab_redo, open);
        View colorpicker = findViewById(R.id.colorpicker);
        if (!open) {
            colorpicker.setVisibility(View.VISIBLE);
            colorpicker.setFocusable(true);
            colorpicker.setClickable(true);
            return true;
        } else {
            colorpicker.setVisibility(View.INVISIBLE);
            colorpicker.setFocusable(false);
            colorpicker.setClickable(false);
            return false;
        }
    }
    @Override
    public void onColorSelected(int color) {
        isColorMenuOpen = showColorPicker(isColorMenuOpen);
        enableCollapsibleMenu(R.id.fab_action, ACTION_MENU_ITEMS, !isColorMenuOpen);
        enableCollapsibleMenu(R.id.fab_shape, SHAPE_MENU_ITEMS, !isColorMenuOpen);
        Paint p = paper.getPaintToEdit();
        p.setColor(color);
       paper.applyPaintEdit();
    }
}
