package moye.sine.deepseek.activity;

import android.content.res.Resources;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import me.jessyan.autosize.AutoSizeCompat;

public class BaseActivity extends AppCompatActivity {

    @Override
    public Resources getResources() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            AutoSizeCompat.autoConvertDensityOfGlobal(super.getResources());
        }
        return super.getResources();
    }

}
