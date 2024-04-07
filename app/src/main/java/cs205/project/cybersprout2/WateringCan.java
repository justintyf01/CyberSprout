package cs205.project.cybersprout2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

public class WateringCan extends TouchObject {

    public WateringCan(Context context, float x, float y) {
        super(x , y, BitmapFactory.decodeResource(context.getResources(), R.drawable.wateringcan));
//
        super.setX(x - super.image.getWidth());
        super.setY(y - super.image.getHeight());
        setImage(super.getImage());
    }

    @Override
    public void setImage(Bitmap image) {
        Matrix matrix = new Matrix();
        matrix.postRotate(-45);

        // Step 3: Create a new bitmap from the original, applying the rotation matrix
        Bitmap rotatedBitmap = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
        super.setImage(rotatedBitmap);
    }
}
