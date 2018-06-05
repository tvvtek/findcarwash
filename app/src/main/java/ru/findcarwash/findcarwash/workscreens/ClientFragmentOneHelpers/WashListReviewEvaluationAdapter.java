package ru.findcarwash.findcarwash.workscreens.ClientFragmentOneHelpers;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ru.findcarwash.findcarwash.R;
import ru.findcarwash.ru.helpers.settings.MySettings;

public class WashListReviewEvaluationAdapter extends BaseAdapter {

    private int imageNameRating;

    Context ctx;
    ArrayList<WashListReviewEvaluationItem> washListReviewEvaluationItems;
    LayoutInflater lInflater;
    ImageView ratingStar;

    public WashListReviewEvaluationAdapter(Context context, ArrayList<WashListReviewEvaluationItem> washListReviewEvaluationItems) {
        ctx = context;
        this.washListReviewEvaluationItems = washListReviewEvaluationItems;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        Log.d(MySettings.LOG_TAG, "counts=" +  washListReviewEvaluationItems.size());
        return washListReviewEvaluationItems.size();
    }

    @Override
    public Object getItem(int position) {
        return washListReviewEvaluationItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = lInflater.inflate(R.layout.client_evaluation_item_wash, viewGroup, false);
        }
        Log.d(MySettings.LOG_TAG, "WashListReviewEvaluationAdapterPosition=" + position);
        WashListReviewEvaluationItem washListReviewEvaluationItem = getProduct(position);
        imageNameRating = Integer.parseInt(washListReviewEvaluationItem.rating); //get ImageRating

        ratingStar = view.findViewById(R.id.evaluationItemClientRating);
        ((TextView) view.findViewById(R.id.evaluationItemClientLogin)).setText(washListReviewEvaluationItem.login.substring(0,2).toUpperCase());

        //  ((TextView) view.findViewById(R.id.evaluationItemWashLogin)).setText(washListReviewEvaluationItem.login);
        ((TextView) view.findViewById(R.id.evaluationItemWashReview)).setText(washListReviewEvaluationItem.review);


        if (imageNameRating == 1) ratingStar.setImageResource(R.drawable.rating_star_one);
        else if (imageNameRating == 2) ratingStar.setImageResource(R.drawable.rating_star_two);
        else if (imageNameRating == 3) ratingStar.setImageResource(R.drawable.rating_star_three);
        else if (imageNameRating == 4) ratingStar.setImageResource(R.drawable.rating_star_four);
        else if (imageNameRating == 5) ratingStar.setImageResource(R.drawable.rating_star_five);

        return view;
    }

    WashListReviewEvaluationItem getProduct(int position) {
        return ((WashListReviewEvaluationItem) getItem(position));
    }
}
