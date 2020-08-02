package painter;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import cse340.undo.R;
import painter.actions.ActionArrow;
import painter.actions.ActionOval;
import painter.actions.ActionRectangle;
import painter.actions.ActionStraightLine;
import painter.actions.ActionStroke;

public class MainActivity extends AppCompatActivity {

    /** View groups containing undo and redo menu buttons. */
    ConstraintLayout mLayout;
    private ViewGroup mUndoMenu, mRedoMenu;
    Paper paper;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // full screen setup
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        setContentView(R.layout.activity_full_screen);
        mLayout = findViewById(R.id.layout);
        paper = findViewById(R.id.paper);
        paper.setBackgroundColor(Color.WHITE);
        findViewById(R.id.edit_action_button).setOnClickListener((v) -> {
            paper.editActionButtonClicked();
        });
        // set up undo/redo button
        mUndoMenu = (ViewGroup) getLayoutInflater().inflate(R.layout.undo_menu, mLayout , false);
        mRedoMenu = (ViewGroup) getLayoutInflater().inflate(R.layout.redo_menu, mLayout , false);
        mLayout.addView(mUndoMenu);
        mLayout.addView(mRedoMenu);



        findViewById(R.id.fab_undo).setOnClickListener((v) -> paper.undo());
        findViewById(R.id.fab_redo).setOnClickListener((v) -> paper.redo());

        experiment();
    }

    void experiment() {
        findViewById(R.id.test_button).setOnClickListener((v) -> {
            paper.toggleEraseMode();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

}
