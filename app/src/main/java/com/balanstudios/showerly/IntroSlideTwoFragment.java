package com.balanstudios.showerly;


import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;


/**
 * A simple {@link Fragment} subclass.
 */
public class IntroSlideTwoFragment extends Fragment {

    private ImageView imageViewBackground;
    private ImageView imageViewShowcase;

    public IntroSlideTwoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_intro_slide_two, container, false);

        imageViewBackground = v.findViewById(R.id.imageViewBackground);
        imageViewShowcase = v.findViewById(R.id.imageViewShowcase);

//        Drawable background = getResources().getDrawable(R.drawable.slide1_bg);
//        Glide.with(getActivity())
//                .load(background)
//                .apply(new RequestOptions().centerCrop())
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .thumbnail(.1f)
//                .into(imageViewBackground);

        Drawable showcase = getResources().getDrawable(R.drawable.slide2_img);
        Glide.with(getActivity())
                .load(showcase)
                .apply(new RequestOptions().fitCenter())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .thumbnail(.1f)
                .into(imageViewShowcase);



        return v;
    }

}
