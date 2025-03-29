package moye.sine.deepseek.view;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SpacingDecoration extends RecyclerView.ItemDecoration {
    private final int edgeSpace;

    public SpacingDecoration(int edgeSpace) {
        this.edgeSpace = edgeSpace;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int itemCount = parent.getAdapter().getItemCount();

        // 首项顶部间距
        if (position == 0) {
            outRect.top = edgeSpace;
        }

        // 末项底部间距
        if (position == itemCount - 1) {
            outRect.bottom = edgeSpace;
        }
    }
}