package com.bignerdranch.android.cafelocator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

/**
 * Created by donita on 12-07-2016.
 * adapter for recycler view for nearby place listing
 */
public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>{


    private List<PlaceInfo> placeList;

    public List<PlaceInfo> getPlaceList(){
        return placeList;
    }

    public PlaceAdapter(List<PlaceInfo> placeList){
        this.placeList=placeList;
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    @Override
    public void onBindViewHolder(PlaceViewHolder placeViewHolder, int i) {
        PlaceInfo ci = placeList.get(i);
        placeViewHolder.vTitle.setText(ci.title);
        placeViewHolder.vAddress.setText(ci.address);

        if(ci.ratings<0.5){
            placeViewHolder.vRatings.setImageResource(R.drawable.zero_star);
        }
        else if(ci.ratings>=0.5&&ci.ratings<1){
            placeViewHolder.vRatings.setImageResource(R.drawable.half_star);
        }
        else if(ci.ratings>=1&&ci.ratings<1.5){
            placeViewHolder.vRatings.setImageResource(R.drawable.one_star);
        }
        else if(ci.ratings>=1.5&&ci.ratings<2){
            placeViewHolder.vRatings.setImageResource(R.drawable.one_and_half_stars);
        }
        else if(ci.ratings>=2&&ci.ratings<2.5){
            placeViewHolder.vRatings.setImageResource(R.drawable.two_stars);
        }
        else if(ci.ratings>=2.5&&ci.ratings<3){
            placeViewHolder.vRatings.setImageResource(R.drawable.two_and_half_stars);
        }
        else if(ci.ratings>=3&&ci.ratings<3.5){
            placeViewHolder.vRatings.setImageResource(R.drawable.three_stars);
        }
        else if(ci.ratings>=3.5&&ci.ratings<4){
            placeViewHolder.vRatings.setImageResource(R.drawable.three_and_half_stars);
        } else if(ci.ratings>=4&&ci.ratings<4.5){
            placeViewHolder.vRatings.setImageResource(R.drawable.four_stars);
        } else if(ci.ratings>=4.5&&ci.ratings<5){
            placeViewHolder.vRatings.setImageResource(R.drawable.four_and_half_stars);
        } else if(ci.ratings==5){
            placeViewHolder.vRatings.setImageResource(R.drawable.five_stars);
        }
        if(ci.open_now){
            placeViewHolder.vOpen_now.setText("Open");
        }
        else {
            placeViewHolder.vOpen_now.setText("Closed");
        }
        switch (ci.price_level){
            case 1:
                placeViewHolder.vPrice_level.setText("$");
                break;
            case 2:
                placeViewHolder.vPrice_level.setText("$$");
                break;
            case 3:
                placeViewHolder.vPrice_level.setText("$$$");
                break;
            default:
                placeViewHolder.vPrice_level.setText("");
        }
    }

    @Override
    public PlaceViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_layout, viewGroup, false);

        return new PlaceViewHolder(itemView);
    }

    public  static class PlaceViewHolder extends RecyclerView.ViewHolder {
        protected TextView vTitle;
        protected TextView vAddress;
        protected ImageView vRatings;
        protected TextView vPrice_level;
        protected TextView vOpen_now;
        public PlaceViewHolder(View v){
            super(v);
            vTitle=(TextView)v.findViewById(R.id.title);
            vAddress=(TextView)v.findViewById(R.id.address);
            vRatings=(ImageView)v.findViewById(R.id.ratings);
            vPrice_level=(TextView)v.findViewById(R.id.price_level);
            vOpen_now=(TextView)v.findViewById(R.id.open_now);
        }
    }
}
