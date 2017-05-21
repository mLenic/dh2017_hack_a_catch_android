package com.example.nina_malina.dh2017;

/**
 * Created by nina_malina on 20/05/17.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.widget.ImageView;

public class DrawView extends View {
    Paint paint;

    public DrawView(Context context) {
        super(context);
        paint = new Paint();
        paint.setColor(Color.BLACK);

        paint.setStrokeWidth(3);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawText("Test", 50, 50, paint);
        canvas.drawCircle(50, 50, 10, paint);

    }

}
