package tikfans.tikplus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import tikfans.tikplus.util.PreferenceUtil;

public class ManHinhFTUActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private View indicator1, indicator2, indicator3, indicator4;
    private TextView mTxtSkip, mTxtNext;
    private int currentPos = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ftu);
        viewPager = findViewById(R.id.view_pager_setup_wizard);

        indicator1 = findViewById(R.id.sw_control_indicator1);
        indicator2 = findViewById(R.id.sw_control_indicator2);
        indicator3 = findViewById(R.id.sw_control_indicator3);
        indicator4 = findViewById(R.id.sw_control_indicator4);
        mTxtNext = findViewById(R.id.txt_ftu_next);
        mTxtSkip = findViewById(R.id.txt_ftu_skip);

        mTxtNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPos == 3) {
                    Intent intent = new Intent(ManHinhFTUActivity.this, ManHinhDangNhapActivity.class);
                    startActivity(intent);
                    PreferenceUtil.saveBooleanPref(PreferenceUtil.IS_FTU, false);
                    finish();
                } else {
                    currentPos++;
                    viewPager.setCurrentItem(currentPos);
                }
            }
        });

        mTxtSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPos == 0) {
                    Intent intent = new Intent(ManHinhFTUActivity.this, ManHinhDangNhapActivity.class);
                    startActivity(intent);
                    PreferenceUtil.saveBooleanPref(PreferenceUtil.IS_FTU, false);
                    finish();
                } else {
                    currentPos--;
                    viewPager.setCurrentItem(currentPos);
                }
            }
        });


        FTUViewPagerAdapter ftuViewPagerAdapter = new FTUViewPagerAdapter(this);
        viewPager.setAdapter(ftuViewPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentPos = position;
                setDefaultIndicatorColor(position);
                if (position == 0) {
                    mTxtSkip.setText(getString(R.string.bo_qua));
                } else {
                    mTxtSkip.setText(getString(R.string.truoc_do));
                }

                if (position == 3) {
                    mTxtNext.setText(getString(R.string.ket_thuc));
                } else {
                    mTxtNext.setText(getString(R.string.tiep));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        setDefaultIndicatorColor(currentPos);

    }

    private void setDefaultIndicatorColor(int pos) {
        indicator1.setAlpha(0.25f);
        indicator2.setAlpha(0.25f);
        indicator3.setAlpha(0.25f);
        indicator4.setAlpha(0.25f);

        switch (pos) {
            case 0:
                indicator1.setAlpha(1f);
                break;
            case 1:
                indicator2.setAlpha(1f);
                break;
            case 2:
                indicator3.setAlpha(1f);
                break;
            case 3:
                indicator4.setAlpha(1f);
                break;
        }
    }

}
