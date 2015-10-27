package com.example.nick.fantraffic;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by nick on 16/10/15.
 */
public class RVAdapter extends RecyclerView.Adapter<RVAdapter.TrafficViewHolder>{

    private List<Traffic> traffic;
    private Context mContext;
    private int id;
    @Override
    public TrafficViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.traffic_card, parent, false);
        TrafficViewHolder pvh = new TrafficViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(TrafficViewHolder holder, int i) {
        holder.trafficInfo.setText(traffic.get(i).flow);
        holder.duration.setText(traffic.get(i).duration);
    }

    @Override
    public int getItemCount() {
        return traffic.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }


    public void addItemsToList(List<Traffic> traffic)
    {
        this.traffic = traffic;
    }

    RVAdapter(List<Traffic> traffic, Context context){
        this.traffic = traffic;
        this.mContext = context;
    }

    public class TrafficViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView trafficInfo;
        TextView duration;

        TrafficViewHolder(View itemView) {
            super(itemView);
            itemView.setId(id++);
            cv = (CardView)itemView.findViewById(R.id.cv);
            trafficInfo = (TextView)itemView.findViewById(R.id.traffic_info);
            duration = (TextView)itemView.findViewById(R.id.duration);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    Toast toast = Toast.makeText(v.getContext(),
                            "Card click " + v.getId(), Toast.LENGTH_LONG);
                    if(mContext instanceof MainActivity){
                        ((MainActivity)mContext).sendMessage(v);
                    }
                    toast.show();
                }
            });
        }
    }
}