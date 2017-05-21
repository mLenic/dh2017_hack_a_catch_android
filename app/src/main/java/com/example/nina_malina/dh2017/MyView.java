package com.example.nina_malina.dh2017;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Lenic on 5/21/17.
 */

public class MyView extends View {

    public String my_id = "";
    public JSONArray users;

    public MyView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public MyView(Context context, AttributeSet attr) {
        super(context, attr);
        // TODO Auto-generated constructor stub
    }

    public MyView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        System.out.println("IM HERE AAAA");
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();


        System.out.println("w: " + width);
        System.out.println("h: " + height);

        int radius;
        radius = 100;
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawPaint(paint);
        // Use Color.parseColor to define HTML colors
        paint.setColor(Color.parseColor("#CD5C5C"));
        canvas.drawCircle(width / 2, height / 2, radius, paint);

        paint.setColor(Color.parseColor("#4f23dc"));

        radius = 50;

        double my_x = 0.0;
        double my_y = 0.0;

        if (users != null) {
            for (int i = 0; i < users.length(); i++) {
                try {
                    JSONObject user = users.getJSONObject(i);
                    if (user.getString("_id").equals(my_id)) {
                        my_x = user.getDouble("x");
                        my_y = user.getDouble("y");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            for (int i = 0; i < users.length(); i++) {
                try {
                    JSONObject user = users.getJSONObject(i);
                    if (!user.getString("_id").equals(my_id)) {
                        double x = user.getDouble("x");
                        double y = user.getDouble("y");
                        boolean in_prox = isInProximity(my_x, my_y, x, y, width, height);
                        System.out.println(in_prox);
                        if (in_prox) {
                            System.out.println(x);
                            System.out.println(y);
                            canvas.drawCircle((float) (width/2 + x - my_x) , (float) (height/2 + y - my_y) , radius, paint);
                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

//        canvas.drawText(testInt,0,0,paint);
    }

    private boolean isInProximity(double my_x, double my_y, double x, double y, int width, int height) {
        System.out.println("my_x" + my_x);
        System.out.println("my_y" + my_y);
        System.out.println("x" + x);
        System.out.println("y" + y);
        if (Math.abs(my_y - y) < height/2. && Math.abs(my_x - x) < width/2.){
            return true;
        }
        return false;
    }


}