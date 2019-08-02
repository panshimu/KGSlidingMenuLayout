package com.miaozi.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    private KGSlidingMenuLayout mMenuLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMenuLayout = findViewById(R.id.menu_layout);
       ImageView iv =  findViewById(R.id.iv);
       iv.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Log.d("TAG","onClick");
               mMenuLayout.closeMenu();
           }
       });
    }
}
