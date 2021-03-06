package com.heinrichreimersoftware.wg_planer;

import android.app.ActivityManager;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.heinrichreimersoftware.wg_planer.adapter.ClassesAdapter;
import com.heinrichreimersoftware.wg_planer.recyclerview.DividerItemDecoration;
import com.heinrichreimersoftware.wg_planer.recyclerview.SwipeDismissRecyclerViewTouchListener;
import com.heinrichreimersoftware.wg_planer.utils.SubjectComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.animators.FadeInUpAnimator;

public class ClassesActivity extends AppCompatActivity {

    private final static int SCROLL_OFFSET = 4;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.list) RecyclerView list;
    @BindView(R.id.emptyView) TextView emptyView;

    private ClassesAdapter adapter;

    private boolean endWithoutSave = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classes);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTaskDescription(new ActivityManager.TaskDescription(
                    getString(R.string.title_activity_courses),
                    BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher),
                    ContextCompat.getColor(this, R.color.material_green_600))
            );
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        list.setLayoutManager(layoutManager);

        list.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        RecyclerView.ItemAnimator animator = new FadeInUpAnimator();
        list.setItemAnimator(animator);

        adapter = new ClassesAdapter();
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                updateEmptyView();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                updateEmptyView();
            }
        });
        list.setAdapter(adapter);

        final SwipeDismissRecyclerViewTouchListener touchListener =
                new SwipeDismissRecyclerViewTouchListener(
                        list,
                        new SwipeDismissRecyclerViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    adapter.remove(position);
                                    adapter.notifyItemRemoved(position);
                                }
                                // do not call notifyItemRemoved for every item, it will cause gaps on deleting items
                                //adapter.notifyDataSetChanged();
                            }
                        });
        list.setOnTouchListener(touchListener);

        list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                touchListener.setEnabled(newState != AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > SCROLL_OFFSET) {
                    fab.hide();
                } else if (dy < -SCROLL_OFFSET) {
                    fab.show();
                }
            }
        });

        emptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addClass();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addClass();
            }
        });

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        List<String> classes = new ArrayList<>(sharedPreferences.getStringSet(getString(R.string.key_preference_classes_list), new HashSet<String>()));

        Collections.sort(classes, new SubjectComparator());

        adapter.addAll(classes);
    }

    private void updateEmptyView() {
        if (list.getAdapter().getItemCount() == 0) {
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setAlpha(0);
            emptyView.animate().alpha(1).setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
            list.setAlpha(1);
            ViewCompat.animate(list).alpha(0).setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime)).withEndAction(new Runnable() {
                @Override
                public void run() {
                    list.setVisibility(View.GONE);
                }
            });
        } else {
            emptyView.setAlpha(1);
            ViewCompat.animate(emptyView).alpha(0).setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime)).withEndAction(new Runnable() {
                @Override
                public void run() {
                    emptyView.setVisibility(View.GONE);
                }
            });
            list.setVisibility(View.VISIBLE);
            list.setAlpha(0);
            list.animate().alpha(1).setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
        }
    }

    public void addClass() {
        new MaterialDialog.Builder(this)
                .title(R.string.title_dialog_add_class)
                .positiveText(R.string.action_add)
                .negativeText(android.R.string.cancel)
                .input(R.string.hint_dialog_add_class, 0, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog materialDialog, CharSequence shorthand) {
                        if (!TextUtils.isEmpty(shorthand) && !shorthand.toString().contains(System.getProperty("line.separator"))) {
                            for (int i = adapter.size() - 1; i >= 0; i--) {
                                if (shorthand.equals(adapter.get(i))) {
                                    return;
                                }
                            }
                            adapter.add(shorthand.toString());
                        }
                    }
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        SharedPreferences.Editor sharedPreferencesEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();

        if (!endWithoutSave) {
            List<String> classes = adapter.getAll();
            Collections.sort(classes, new SubjectComparator());

            sharedPreferencesEditor.putStringSet(getString(R.string.key_preference_classes_list), new HashSet<>(classes));
            sharedPreferencesEditor.apply();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_classes, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear:
                new MaterialDialog.Builder(this)
                        .title(R.string.title_dialog_clear_classes)
                        .content(R.string.label_dialog_clear_classes)
                        .positiveText(R.string.action_clear)
                        .negativeText(R.string.action_cancel)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                adapter.clear();
                            }
                        })
                        .show();
                return false;
            case R.id.action_cancel:
                new MaterialDialog.Builder(this)
                        .title(R.string.title_dialog_cancel_classes_edit)
                        .content(R.string.label_dialog_cancel_classes_edit)
                        .positiveText(R.string.action_cancel)
                        .negativeText(R.string.action_continue)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                endWithoutSave = true;
                                finish();
                            }
                        })
                        .show();
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
