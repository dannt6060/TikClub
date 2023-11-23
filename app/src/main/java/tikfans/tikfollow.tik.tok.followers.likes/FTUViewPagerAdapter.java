package tikfans.tikfollow.tik.tok.followers.likes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.squareup.picasso.Picasso;


public class FTUViewPagerAdapter extends PagerAdapter {

    private Context context;
    private LayoutInflater layoutInflater;
    private Integer [] images = {R.drawable.slide1,R.drawable.slide2,R.drawable.slide3,R.drawable.slide4};

    public FTUViewPagerAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return images.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {

        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.custom_layout, null);
        ImageView imageView = view.findViewById(R.id.imageView);
        TextView textViewWelcome = view.findViewById(R.id.txt_welcome);

        Picasso.get().load(images[position]).into(imageView);

//        imageView.setImageResource(images[position]);
        switch (position) {
            case 0:
                textViewWelcome.setText(R.string.slide1);
                break;
            case 1:
                textViewWelcome.setText(R.string.slide2);
                break;
            case 2:
                textViewWelcome.setText(R.string.slide3);
                break;
            case 3:
                textViewWelcome.setText(R.string.slide4);
                break;
        }


        ViewPager vp = (ViewPager) container;
        vp.addView(view, 0);
        return view;

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        ViewPager vp = (ViewPager) container;
        View view = (View) object;
        vp.removeView(view);

    }
}

