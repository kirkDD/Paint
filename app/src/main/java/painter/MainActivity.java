package painter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import cse340.undo.R;

public class MainActivity extends AppCompatActivity {

    Paper paper;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // full screen setup
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        setContentView(R.layout.activity_full_screen);
        paper = findViewById(R.id.paper);

        findViewById(R.id.edit_action_button).setOnClickListener((v) -> {
            paper.editActionButtonClicked();
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
