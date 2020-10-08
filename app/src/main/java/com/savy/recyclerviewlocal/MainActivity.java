package com.savy.recyclerviewlocal;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.table);

        RecyclerView.Adapter adapter = new TwoAdapter(20);

        recyclerView.setAdapter(adapter);
//        recyclerView.setAdapter(new RecyclerView.Adapter<MyViewHolder>() {
//            @Override
//            public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//
//                View view = LayoutInflater.from(MainActivity.this)
//                        .inflate(R.layout.item_table1, parent, false);
//                MyViewHolder myViewHolder = new MyViewHolder(view);
//
//                //Log.e("DEBUG", "onCreateViewHolder" + myViewHolder.hashCode());
//                return myViewHolder;
//            }
//
//            @Override
//            public MyViewHolder onBindViewHolder(MyViewHolder viewHolder, int position) {
//                //viewHolder.itemView.setTag(this);
//                viewHolder.tv.setText("这是一个很好玩的事情 : " + position);
//                return null;
//            }
//
//            @Override
//            public int getItemViewType(int position) {
//                if (position % 2 == 0) {
//                    return 0;
//                } else {
//                    return 1;
//                }
//            }
//
//            @Override
//            public int getItemCount() {
//                return 20000;
//            }
//
//            @Override
//            public int getHeight(int index) {
//                return 200;
//            }
//        });
    }

    class MyViewHolder extends ViewHolder {

        TextView tv;

        public MyViewHolder(View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.text1);
        }
    }

    class ImageViewHolder extends ViewHolder {

        TextView tv;
        ImageView iv;

        public ImageViewHolder(View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.text2);
            iv = itemView.findViewById(R.id.iv);
        }
    }

    class TwoAdapter implements RecyclerView.Adapter<ViewHolder> {

        int count;
        public TwoAdapter(int count) {
            this.count = count;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = null;
            if (viewType == 0) {
                view = LayoutInflater.from(MainActivity.this)
                        .inflate(R.layout.item_table1, parent, false);
                return new MyViewHolder(view);
            } else {
                view = LayoutInflater.from(MainActivity.this)
                        .inflate(R.layout.item_table2, parent, false);
                return new ImageViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            if (position % 2 == 0) {
                MyViewHolder myViewHolder = (MyViewHolder) viewHolder;
                myViewHolder.tv.setText("这是一个MyViewHolder : " + position);
            } else {
                ImageViewHolder imageViewHolder = (ImageViewHolder) viewHolder;
                imageViewHolder.tv.setText("这是一个ImageViewHolder : " + position);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position % 2 == 0) {
                return 0;
            } else {
                return 1;
            }
        }

        @Override
        public int getItemCount() {
            return 20;
        }

        @Override
        public int getHeight(int index) {
            return 200;
        }
    }
}
