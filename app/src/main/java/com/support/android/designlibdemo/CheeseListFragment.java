/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.support.android.designlibdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CheeseListFragment extends Fragment {

    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        recyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_cheese_list, container, false);
        initRecyclerView(recyclerView);
        return recyclerView;
    }

    @Override
    public void onStart() {
        super.onStart();
        retrieveDataFromApiAsync();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        recyclerView = null;
    }

    private void retrieveDataFromApiAsync() {
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<Cheese> list = retrieveDataFromApi();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            ((SimpleStringRecyclerViewAdapter) recyclerView.getAdapter()).setItems(list);
                            recyclerView.getAdapter().notifyDataSetChanged();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * set up recycler view (adding new adapter and update rv)
     * @param recyclerView
     */
    private void initRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setAdapter(new SimpleStringRecyclerViewAdapter(getActivity(), new ArrayList<Cheese>()));
    }

    /**
     *
     * @return - trying to retrieve data from api
     * @throws IOException
     */
    List<Cheese> retrieveDataFromApi() throws IOException {
        return CheeseApi.listCheeses(30);
    }

    /**
     * rv adapter - cheese items
     */
    static class SimpleStringRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleStringRecyclerViewAdapter.ViewHolder> implements View.OnClickListener {

        private final TypedValue mTypedValue = new TypedValue();
        private int mBackground;
        private List<Cheese> items;
        private ViewHolder holder;

        /**
         * constructor
         * @param context
         * @param items
         */
        SimpleStringRecyclerViewAdapter(Context context, List<Cheese> items) {
            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
            mBackground = mTypedValue.resourceId;
            this.items = items;
        }

        /**
         * set items from api -> please move to a repository pattern >.<
         * @param items
         */
        public void setItems(List<Cheese> items) {
            this.items = items;
        }

        /**
         * viewholder
         */
        static class ViewHolder extends RecyclerView.ViewHolder {
            final View mView;
            final ImageView mImageView;
            final TextView mTextView;

            ViewHolder(View view) {
                super(view);
                mView = view;
                mImageView = view.findViewById(R.id.avatar);
                mTextView = view.findViewById(android.R.id.text1);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mTextView.getText();
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item, parent, false);
            view.setBackgroundResource(mBackground);
            holder = new ViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            Cheese item = items.get(position);
            holder.mTextView.setText(item.getName());
            holder.mView.setOnClickListener(this);

            Glide.with(holder.mImageView.getContext())
                    .load(item.getDrawableResId())
                    .fitCenter()
                    .into(holder.mImageView);
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(view.getContext(), CheeseDetailActivity.class);
            intent.putExtra(CheeseDetailActivity.EXTRA_CHEESE, items.get(holder.getAdapterPosition()));
            view.getContext().startActivity(intent);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }
}
