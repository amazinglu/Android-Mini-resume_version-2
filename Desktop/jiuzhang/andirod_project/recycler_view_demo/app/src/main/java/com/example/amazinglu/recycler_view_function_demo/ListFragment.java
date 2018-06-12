package com.example.amazinglu.recycler_view_function_demo;

import android.content.ClipData;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.example.amazinglu.recycler_view_function_demo.model.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.support.v7.widget.helper.ItemTouchHelper.ACTION_STATE_SWIPE;
import static android.support.v7.widget.helper.ItemTouchHelper.LEFT;

public class ListFragment extends Fragment {

//    @BindView(R.id.main_recycler_view) RecyclerView recyclerView;

    private List<Element> data;
    private Adapter adapter;
    private RecyclerView recyclerView;

    /**
     * the call back object of swipe or move item in recycler view
     */
    private SwipeController swipeController;
    private ItemTouchHelper itemTouchHelper;

    public static ListFragment newInstance() {
        return new ListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        loadData();
        recyclerView = (RecyclerView) view.findViewById(R.id.main_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        adapter = new Adapter(data);
        recyclerView.setAdapter(adapter);

        setUpItemTouchHelper();
    }

    private void setUpItemTouchHelper() {
        swipeController = new SwipeController(ItemTouchHelper.UP|ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT);
        itemTouchHelper = new ItemTouchHelper(swipeController);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void loadData() {
        data = new ArrayList<>();
        for (int i = 0; i < 20; ++i) {
            Element element = new Element();
            element.text = "item" + i;
            data.add(element);
        }
    }

    class SwipeController extends ItemTouchHelper.SimpleCallback {

        private boolean swipeBack = false;

        /**
         * Creates a Callback for the given drag and swipe allowance. These values serve as
         * defaults
         * and if you want to customize behavior per ViewHolder, you can override
         * {@link #getSwipeDirs(RecyclerView, ViewHolder)}
         * and / or {@link #getDragDirs(RecyclerView, ViewHolder)}.
         *
         * @param dragDirs  Binary OR of direction flags in which the Views can be dragged. Must be
         *                  composed of {@link #LEFT}, {@link #RIGHT}, {@link #START}, {@link
         *                  #END},
         *                  {@link #UP} and {@link #DOWN}.
         * @param swipeDirs Binary OR of direction flags in which the Views can be swiped. Must be
         *                  composed of {@link #LEFT}, {@link #RIGHT}, {@link #START}, {@link
         *                  #END},
         *                  {@link #UP} and {@link #DOWN}.
         */
        public SwipeController(int dragDirs, int swipeDirs) {
            super(dragDirs, swipeDirs);
        }

//        @Override
//        public int convertToAbsoluteDirection(int flags, int layoutDirection) {
//            if (swipeBack) {
//                swipeBack = false;
//                return 0;
//            }
//            return super.convertToAbsoluteDirection(flags, layoutDirection);
//        }
//
//        @Override
//        public void onChildDraw(Canvas c, RecyclerView recyclerView,
//                                RecyclerView.ViewHolder viewHolder, float dX, float dY,
//                                int actionState, boolean isCurrentlyActive) {
//
//            if (actionState == ACTION_STATE_SWIPE) {
//                setTouchListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
//            }
//            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
//        }
//
//        private void setTouchListener(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
//                                      float dX, float dY, int actionState, boolean isCurrentlyActive) {
//            recyclerView.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View view, MotionEvent motionEvent) {
//                    swipeBack = motionEvent.getAction() == MotionEvent.ACTION_CANCEL
//                            || motionEvent.getAction() == MotionEvent.ACTION_UP;
//                    return false;
//                }
//            });
//        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                              RecyclerView.ViewHolder target) {
            /**
             * the action when the item move
             * */

            int fromPosition = viewHolder.getAdapterPosition(); // 拖动的item的position
            int toPosition = target.getAdapterPosition(); // 目标item的position

            if (toPosition < fromPosition) {
                for (int i = fromPosition; i > toPosition; --i) {
                    Collections.swap(data, i, i - 1);
                }
            } else {
                for (int i = fromPosition; i < toPosition; ++i) {
                    Collections.swap(data, i, i + 1);
                }
            }

            adapter.notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            /**
             * the action when the item swipe
             * */
            if (direction == ItemTouchHelper.RIGHT) {
                // move right to delete item
                int position = viewHolder.getAdapterPosition();
                data.remove(position);
                adapter.notifyItemRemoved(position);
            } else {

            }
        }
    }
}
