package com.lernskog.erik.simplebouncingball;

import android.app.Activity;
import android.os.Bundle;

public class SimpleBouncingBallActivity extends Activity {
    private BounceView the_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_bouncing_ball);
        the_view = (BounceView) findViewById(R.id.bounceview);
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        the_view.resume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        the_view.pause();
    }

}
