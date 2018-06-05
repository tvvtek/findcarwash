package ru.findcarwash.findcarwash.workscreens.ClientFragmentOneHelpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ru.findcarwash.findcarwash.R;
import ru.findcarwash.ru.helpers.settings.MySettings;

public class CatalogItemAdapter extends BaseAdapter{

    private String image_name;
    private String minPrice;

    Context ctx;
    LayoutInflater lInflater;
    ArrayList<CatalogItem> catalog;
    ImageView image, rating;

    public CatalogItemAdapter(Context context, ArrayList<CatalogItem> catalog) {
        ctx = context;
        this.catalog = catalog;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        minPrice = ctx.getResources().getString(R.string.catalogMinPrice);
    }

    @Override
    public int getCount() {
        return catalog.size();
    }
    @Override
    public Object getItem(int position) {
        return catalog.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = lInflater.inflate(R.layout.client_fragment_one_catalog_item, viewGroup, false);
        }
        CatalogItem catalog = getProduct(position);
        image = view.findViewById(R.id.washPhoto);
        rating = view.findViewById(R.id.catalogWashRating);


        image_name = catalog.image; //get Url image

        ((TextView) view.findViewById(R.id.shortName)).setText(catalog.shortName);
        // set Rating
        if (catalog.rating.equals("1")) rating.setImageResource(R.drawable.rating_star_one);
        if (catalog.rating.equals("2")) rating.setImageResource(R.drawable.rating_star_two);
        if (catalog.rating.equals("3")) rating.setImageResource(R.drawable.rating_star_three);
        if (catalog.rating.equals("4")) rating.setImageResource(R.drawable.rating_star_four);
        if (catalog.rating.equals("5")) rating.setImageResource(R.drawable.rating_star_five);

        ((TextView) view.findViewById(R.id.washMinPrice)).setText(minPrice + " " + catalog.minPrice);

        ((TextView) view.findViewById(R.id.washDescription)).setText(catalog.description);

        Picasso.with(ctx)
                .load(MySettings.URL + MySettings.CATALOG_WASH_IMAGES + image_name)
                .placeholder(R.drawable.progress_animation)
                .error( R.drawable.default_wash)
                .into(image);

        return view;
    }

    CatalogItem getProduct(int position) {
        return ((CatalogItem) getItem(position));
    }
}