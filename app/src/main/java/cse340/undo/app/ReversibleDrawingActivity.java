package cse340.undo.app;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.IdRes;
import android.support.constraint.ConstraintSet;
import android.view.View;

import cse340.undo.R;
import cse340.undo.actions.ChangeColorAction;
import cse340.undo.actions.ChangeOpacityAction;
import cse340.undo.actions.ChangeThicknessAction;
import cse340.undo.actions.AbstractReversibleAction;

public class ReversibleDrawingActivity extends AbstractReversibleDrawingActivity implements AbstractColorPickerView.ColorChangeListener {
    private static final int DEFAULT_COLOR = Color.RED;
    private static final int DEFAULT_THICKNESS = 10;

    /** List of menu item FABs for thickness menu. */
    @IdRes
    private static final int[] THICKNESS_MENU_ITEMS = {
        R.id.fab_thickness_0, R.id.fab_thickness_10, R.id.fab_thickness_20, R.id.fab_thickness_30
    };

    /** List of menu item FABs for color menu. */
    @IdRes
    private static final int[] COLOR_MENU_ITEMS = {
        R.id.fab_red, R.id.fab_blue, R.id.fab_green
    };

    /** List of menu item FABs for opacity menu. */
    @IdRes
    private static final int[] OPACITY_MENU_ITEMS = {
            R.id.fab_opacity_1f, R.id.fab_opacity_0_5f, R.id.fab_opacity_0_1f
    };

    /** State variables used to track whether menus are open. */
    private boolean isThicknessMenuOpen;
    private boolean isColorMenuOpen;
    private boolean isOpacityMenuOpen;

    @SuppressLint("PrivateResource")
    private int mMiniFabSize;

    /** Place to stort ColorPickerView */
    protected AbstractColorPickerView mColorPickerView;

    /**
     * Creates a new AbstractReversibleDrawingActivity with the default history limit.
     */
    public ReversibleDrawingActivity() {
        super();
    }

    /**
     * Creates a new AbstractReversibleDrawingActivity with the given history limit.
     *
     * @param history Maximum number of history items to maintain.
     */
    public ReversibleDrawingActivity(int history) {
        super(history);
    }

    @Override
    @SuppressLint("PrivateResource")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // We are providing draw with a default thickness and color for the first line
        Paint p = mDrawingView.getCurrentPaint();
        p.setColor(DEFAULT_COLOR);
        p.setStrokeWidth(DEFAULT_THICKNESS);
        mDrawingView.setCurrentPaint(p);
        mMiniFabSize = getResources().getDimensionPixelSize(R.dimen.design_fab_size_mini);

        //  initialize color picker and register color change listener
        mColorPickerView =findViewById(R.id.colorpicker);
        mColorPickerView.setColor(AbstractColorPickerView.DEFAULT_COLOR);
        mColorPickerView.addColorChangeListener(this);
        // Add thickness and color menus to the ConstraintLayout. Pass in onColorMenuSelected
        // and onThicknessMenuSelected as the listeners for these menus
        addCollapsableMenu(R.layout.color_menu, ConstraintSet.BOTTOM, ConstraintSet.END, COLOR_MENU_ITEMS, this::onColorMenuSelected);
        //  you may have to edit this after integrating the color picker
        findViewById(R.id.fab_color).setOnClickListener((v) -> {
            enableCollapsibleMenu(R.id.fab_thickness, THICKNESS_MENU_ITEMS, isColorMenuOpen);
            enableCollapsibleMenu(R.id.fab_opacity, OPACITY_MENU_ITEMS, isColorMenuOpen);
            isColorMenuOpen = showColorPicker(isColorMenuOpen);
        });

        // Only draw a stroke when none of the collapsible menus are open
        mDrawingView.setOnTouchListener((view, event) -> {
            if (isThicknessMenuOpen) {
                isThicknessMenuOpen = toggleMenu(THICKNESS_MENU_ITEMS, isThicknessMenuOpen);
                enableCollapsibleMenu(R.id.fab_color, COLOR_MENU_ITEMS, !isThicknessMenuOpen);
                enableCollapsibleMenu(R.id.fab_opacity, OPACITY_MENU_ITEMS, !isThicknessMenuOpen);
                return true;
            } else if (isColorMenuOpen) {
                isColorMenuOpen = showColorPicker(isColorMenuOpen);
                enableCollapsibleMenu(R.id.fab_thickness, THICKNESS_MENU_ITEMS, !isColorMenuOpen);
                enableCollapsibleMenu(R.id.fab_opacity, OPACITY_MENU_ITEMS, !isColorMenuOpen);
                return true;
            } else if (isOpacityMenuOpen) {
                isOpacityMenuOpen = toggleOpacityMenu(isOpacityMenuOpen);
                enableCollapsibleMenu(R.id.fab_color, COLOR_MENU_ITEMS, !isOpacityMenuOpen);
                enableCollapsibleMenu(R.id.fab_thickness, THICKNESS_MENU_ITEMS, !isOpacityMenuOpen);
                return true;
            } else {
                return mDrawingView.onTouchEvent(event);
            }
        });

        registerActionListener(this::onAction);
        registerActionUndoListener(this::onActionUndo);

        addCollapsableMenu(R.layout.thickness_menu, ConstraintSet.BOTTOM, ConstraintSet.END, THICKNESS_MENU_ITEMS, this::onThicknessMenuSelected);
        findViewById(R.id.fab_thickness).setOnClickListener((v) ->{
            enableCollapsibleMenu(R.id.fab_color, COLOR_MENU_ITEMS, isThicknessMenuOpen);
            enableCollapsibleMenu(R.id.fab_opacity, OPACITY_MENU_ITEMS, isThicknessMenuOpen);
            isThicknessMenuOpen = toggleMenu(THICKNESS_MENU_ITEMS, isThicknessMenuOpen);
        });

        addCollapsableMenu(R.layout.opacity_menu, ConstraintSet.BOTTOM, ConstraintSet.END, OPACITY_MENU_ITEMS, this::onOpacityMenuSelected);
        findViewById(R.id.fab_opacity).setOnClickListener((v) ->{
            enableCollapsibleMenu(R.id.fab_thickness, THICKNESS_MENU_ITEMS, isOpacityMenuOpen);
            enableCollapsibleMenu(R.id.fab_color, COLOR_MENU_ITEMS, isOpacityMenuOpen);
            isOpacityMenuOpen = toggleOpacityMenu(isOpacityMenuOpen);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deregisterActionListener(this::onAction);
        deregisterActionUndoListener(this::onActionUndo);
        //  deregister the color change listener
        mColorPickerView.removeColorChangeListener(this);
    }

    private void onAction(AbstractReversibleAction action) {
        if (action instanceof ChangeColorAction) {
            @ColorInt int currColor = mDrawingView.getCurrentPaint().getColor();
            // update the color of the color picker if needed
            mColorPickerView.setColor(currColor);
        }
    }

    private void onActionUndo(AbstractReversibleAction action) {
        if (action instanceof ChangeColorAction) {
            @ColorInt int currColor = mDrawingView.getCurrentPaint().getColor();
            // update the color of the color picker if needed
            mColorPickerView.setColor(currColor);
        }
    }

    /**
     * Callback for creating an AbstractAction when the user changes the color.
     *
     * @param view The FAB the user clicked on
     */
    private void onColorMenuSelected(View view) {
        switch (view.getId()) {
            case R.id.fab_red:
                doAction(new ChangeColorAction(Color.RED));
                break;
            case R.id.fab_blue:
                doAction(new ChangeColorAction(Color.BLUE));
                break;
            case R.id.fab_green:
                doAction(new ChangeColorAction(Color.GREEN));
                break;
        }
        // Close the menu.
        isColorMenuOpen = toggleMenu(COLOR_MENU_ITEMS, isColorMenuOpen);
        enableCollapsibleMenu(R.id.fab_thickness, THICKNESS_MENU_ITEMS, !isColorMenuOpen);
        enableCollapsibleMenu(R.id.fab_opacity, OPACITY_MENU_ITEMS, !isColorMenuOpen);
    }

    /**
     * Callback for creating an action when the user changes the thickness.
     *
     * You will need to modify this to add a new thickness FAB
     * @param view The FAB the user clicked on.
     */
    private void onThicknessMenuSelected(View view) {
        switch (view.getId()) {
            case R.id.fab_thickness_0:
                doAction(new ChangeThicknessAction((0)));
                break;
            case R.id.fab_thickness_10:
                doAction(new ChangeThicknessAction(10));
                break;
            case R.id.fab_thickness_20:
                doAction(new ChangeThicknessAction(20));
                break;
            case R.id.fab_thickness_30:
                doAction(new ChangeThicknessAction(30));
                break;
        }

        // Close the menu.
        isThicknessMenuOpen = toggleMenu(THICKNESS_MENU_ITEMS, isThicknessMenuOpen);
        enableCollapsibleMenu(R.id.fab_color, COLOR_MENU_ITEMS, !isThicknessMenuOpen);
        enableCollapsibleMenu(R.id.fab_opacity, OPACITY_MENU_ITEMS, !isThicknessMenuOpen);
    }

    private void onOpacityMenuSelected(View view) {
        switch (view.getId()) {
            case R.id.fab_opacity_1f:
                doAction(new ChangeOpacityAction(1f));
                break;
            case R.id.fab_opacity_0_5f:
                doAction(new ChangeOpacityAction(0.5f));
                break;
            case R.id.fab_opacity_0_1f:
                doAction(new ChangeOpacityAction(0.1f));
                break;
        }
        // Close the menu.
        isOpacityMenuOpen = toggleOpacityMenu(isOpacityMenuOpen);
        enableCollapsibleMenu(R.id.fab_color, COLOR_MENU_ITEMS, !isOpacityMenuOpen);
        enableCollapsibleMenu(R.id.fab_thickness, THICKNESS_MENU_ITEMS, !isOpacityMenuOpen);
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

    private boolean toggleOpacityMenu(boolean open) {
        enableFAB(R.id.fab_undo, open);
        enableFAB(R.id.fab_redo, open);
        float[] alpha = {1f, 0.5f, 0.1f};
        if (!open) {
            for (int i = 0; i < ReversibleDrawingActivity.OPACITY_MENU_ITEMS.length; i++) {
                View view = findViewById(ReversibleDrawingActivity.OPACITY_MENU_ITEMS[i]);
                view.animate()
                        .translationY(-3 * mMiniFabSize * (i + 1.5f) / 2.5f)
                        .alpha(alpha[i])
                        .withEndAction(() -> view.setClickable(true));
            }
            return true;
        } else {
            for (int item : ReversibleDrawingActivity.OPACITY_MENU_ITEMS) {
                View view = findViewById(item);
                view.setClickable(false);
                view.animate().translationY(0).alpha(0);
            }
            return false;
        }
    }
    private boolean showColorPicker(boolean open) {
        enableFAB(R.id.fab_undo, open);
        enableFAB(R.id.fab_redo, open);
        enableCollapsibleMenu(R.id.fab_thickness, THICKNESS_MENU_ITEMS, open);
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

    @Override
    public void onColorSelected(int color) {
        doAction(new ChangeColorAction(color));
        showColorPicker(false);
    }
}
