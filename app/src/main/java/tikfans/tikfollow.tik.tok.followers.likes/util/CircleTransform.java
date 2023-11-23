package tikfans.tikfollow.tik.tok.followers.likes.util;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.squareup.picasso.Transformation;

/**
 * Created by sev_user on 3/7/2017.
 */

public class CircleTransform implements Transformation {
    @Override
    public Bitmap transform(Bitmap source) {

        int size = Math.min(source.getWidth(), source.getHeight());

        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

        Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);

        Bitmap bitmap;
        try {
            bitmap = Bitmap.createBitmap(size, size, source.getConfig());
            if (squaredBitmap != source) {
                source.recycle();
            }
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            BitmapShader shader = new BitmapShader(squaredBitmap,
                    BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
            paint.setShader(shader);
            paint.setAntiAlias(true);
            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);

            squaredBitmap.recycle();

            return bitmap;
        } catch (Exception e) {

        }
        return source;

    }

    @Override
    public String key() {
        return "circle";
    }
}