package com.example.shortvideo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.shortvideo.ui.my.ActListFragment;
import com.example.shortvideo.utils.StatusBar;

public class AbsListActivity extends AppCompatActivity {
    public static final int BEHAVIOR_FAVORITE = 0;
    public static final int BEHAVIOR_HISTORY = 1;

    public static final String BEHAVIOR_TYPE = "behavior";
    private ImageView actBack;
    private TextView actTitle;
    private FrameLayout frame;

    public static void startListActivity(Context context, int type) {
        Intent intent = new Intent(context, AbsListActivity.class);
        intent.putExtra(BEHAVIOR_TYPE, type);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StatusBar.fitSystemBar(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_abs_list);
        initView();

        int type = getIntent().getIntExtra(BEHAVIOR_TYPE, 0);
        switch (type) {
            case BEHAVIOR_FAVORITE:
                actTitle.setText(getString(R.string.fragment_my_favorite));
                break;
            case BEHAVIOR_HISTORY:
                actTitle.setText(getString(R.string.fragment_my_history));
        }
        ActListFragment listFragment = ActListFragment.newInstance(type);
        getSupportFragmentManager().beginTransaction().add(R.id.frame,listFragment,"userBehavior").commit();
    }

    private void initView() {
        actBack = (ImageView) findViewById(R.id.act_back);
        actTitle = (TextView) findViewById(R.id.act_title);
        frame = (FrameLayout) findViewById(R.id.frame);

        actBack.setOnClickListener(v -> {
            finish();
        });
    }


}