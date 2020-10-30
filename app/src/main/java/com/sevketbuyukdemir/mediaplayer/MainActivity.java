package com.sevketbuyukdemir.mediaplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Context context;
    LayoutInflater layout_inflater;
    // For media player
    private ArrayList<File> music_array_list;
    private MediaPlayer media_player;
    private int CURRENT_SONG_POSITION = -1;
    // For media player
    // For media recycler view
    private RecyclerView media_recyclerview;
    private static class media_recyclerview_view_holder extends RecyclerView.ViewHolder {
        TextView music_name_text_view;
        LinearLayout recycler_view_music_card;
        public media_recyclerview_view_holder(@NonNull View itemView) {
            super(itemView);
            music_name_text_view = itemView.findViewById(R.id.music_name_text_view);
            recycler_view_music_card = itemView.findViewById(R.id.recycler_view_music_card);
        }
    }
    private RecyclerView.Adapter<media_recyclerview_view_holder> media_recyclerview_adapter;
    // For media recycler view
    // For media player control bar init
    private LinearLayout media_player_controls_bar;
    private LinearLayout media_player_controls_swipe_layout;
    private BottomSheetBehavior<LinearLayout> bottom_sheet_behavior;
    private ImageView media_player_controls_layout_arrow;
    private ImageButton media_player_controls_previous_button;
    private ImageButton media_player_controls_play_button;
    private ImageButton media_player_controls_pause_button;
    private ImageButton media_player_controls_stop_button;
    private ImageButton media_player_controls_next_button;
    // For media player control bar init

    private void init() {
        context = this;
        // For media player init
        music_array_list = new ArrayList<>();
        media_player = new MediaPlayer();
        // For media player init
        // For media recycler view init
        media_recyclerview = findViewById(R.id.media_recyclerview);
        // For media recycler view init
        // For media player control bar init
        media_player_controls_bar = findViewById(R.id.media_player_controls_bar);
        media_player_controls_swipe_layout = findViewById(R.id.media_player_controls_swipe_layout);
        bottom_sheet_behavior = BottomSheetBehavior.from(media_player_controls_bar);
        media_player_controls_layout_arrow = findViewById(R.id.media_player_controls_layout_arrow);
        media_player_controls_previous_button = findViewById(R.id.previous_button);
        media_player_controls_play_button = findViewById(R.id.play_button);
        media_player_controls_pause_button = findViewById(R.id.pause_button);
        media_player_controls_stop_button = findViewById(R.id.stop_button);
        media_player_controls_next_button = findViewById(R.id.next_button);
        media_player_controls_previous_button.setOnClickListener(this);
        media_player_controls_play_button.setOnClickListener(this);
        media_player_controls_pause_button.setOnClickListener(this);
        media_player_controls_stop_button.setOnClickListener(this);
        media_player_controls_next_button.setOnClickListener(this);
        // For media player control bar init
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        media_recyclerview_adapter = new RecyclerView.Adapter<media_recyclerview_view_holder>() {
            @NonNull
            @Override
            public media_recyclerview_view_holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                layout_inflater = LayoutInflater.from(context);
                View v =layout_inflater.inflate(R.layout.recycler_view_music_card, parent, false);
                media_recyclerview_view_holder vh = new media_recyclerview_view_holder(v);
                return vh;
            }

            @Override
            public void onBindViewHolder(@NonNull media_recyclerview_view_holder holder, int position) {
                if(music_array_list.size() == 0){
                    holder.music_name_text_view.setText("empty");
                }else{
                    holder.music_name_text_view.setText(music_array_list.get(position).getName());
                }
                // card view
                holder.recycler_view_music_card.setTag(holder);
            }

            @Override
            public int getItemCount() {
                return (music_array_list == null) ? 0 : music_array_list.size();
            }
        };
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        media_recyclerview.setLayoutManager(llm);
        media_recyclerview.setAdapter(media_recyclerview_adapter);
        media_recyclerview.addOnItemTouchListener(
                new RecyclerItemClickListener(context, media_recyclerview ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        // do whatever
                        if(CURRENT_SONG_POSITION == -1) {
                            CURRENT_SONG_POSITION = position;
                            play_song(music_array_list.get(position));
                        } else {
                            CURRENT_SONG_POSITION = position;
                            stop_song();
                            play_song(music_array_list.get(position));
                        }
                    }

                    @Override public void onLongItemClick(View view, int position) {
                        // do whatever
                    }
                })
        );
        // For media player control bar init
        ViewTreeObserver vto = media_player_controls_swipe_layout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            media_player_controls_swipe_layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            media_player_controls_swipe_layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                        // int width = bottomSheetLayout.getMeasuredWidth();
                        int height = media_player_controls_swipe_layout.getMeasuredHeight();

                        bottom_sheet_behavior.setPeekHeight(height);
                    }
                });
        bottom_sheet_behavior.setHideable(false);
        bottom_sheet_behavior.setBottomSheetCallback(
                new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View bottomSheet, int newState) {
                        switch (newState) {
                            case BottomSheetBehavior.STATE_HIDDEN:
                                break;
                            case BottomSheetBehavior.STATE_EXPANDED:
                            {
                                media_player_controls_layout_arrow.setImageResource(R.drawable.media_player_controls_down_arrow);
                            }
                            break;
                            case BottomSheetBehavior.STATE_COLLAPSED:
                            {
                                media_player_controls_layout_arrow.setImageResource(R.drawable.media_player_controls_up_arrow);
                            }
                            break;
                            case BottomSheetBehavior.STATE_DRAGGING:
                                break;
                            case BottomSheetBehavior.STATE_SETTLING:
                                media_player_controls_layout_arrow.setImageResource(R.drawable.media_player_controls_up_arrow);
                                break;
                        }
                    }
                    @Override
                    public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
                });
        // For media player control bar init

        // Get songs from external storage and assign to out ArrayList
        music_array_list = get_all_audios(context);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.previous_button) {
            previous_song();
        } else if (id == R.id.play_button) {
            if(CURRENT_SONG_POSITION == -1) {
                CURRENT_SONG_POSITION = 0;
                play_song(music_array_list.get(0));
            } else {
                play_song(music_array_list.get(CURRENT_SONG_POSITION));
            }
        } else if (id == R.id.pause_button) {
            if(CURRENT_SONG_POSITION == -1) {
                Toast.makeText(this, "Media Player doesn't working!!", Toast.LENGTH_SHORT).show();
            } else {
                stop_song();
            }
        } else if (id == R.id.stop_button) {
            if(CURRENT_SONG_POSITION == -1) {
                Toast.makeText(this, "Media Player doesn't working!!", Toast.LENGTH_SHORT).show();
            } else {
                CURRENT_SONG_POSITION = -1;
                stop_song();
            }
        } else if (id == R.id.next_button) {
            next_song();
        }
    }

    public static ArrayList<File> get_all_audios(Context c) {
        ArrayList<File> files = new ArrayList<>();
        String[] projection = { MediaStore.Audio.AudioColumns.DATA ,MediaStore.Audio.Media.DISPLAY_NAME};
        Cursor cursor = c.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
        try {
            cursor.moveToFirst();
            do{
                files.add((new File(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)))));
            }while(cursor.moveToNext());

            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return files;
    }

    private void play_song(final File file) {
        try {
            media_player = MediaPlayer.create(getApplicationContext(), Uri.fromFile(file));
            media_player.setLooping(false);
            media_player.start();
            media_player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                }
            });
            media_player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stop_song() {
        try {
            if(media_player.isPlaying()) {
                media_player.stop();
                media_player.release();
            } else {
                Toast.makeText(this, "Media Player doesn't working!!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // double total_duration = media_player.getDuration();
    private void previous_song() {
        double current_duration = media_player.getCurrentPosition();
        stop_song();
        if (!(current_duration > 10)) {
            if (CURRENT_SONG_POSITION == 0) {
                CURRENT_SONG_POSITION = music_array_list.size() - 1;
            } else {
                CURRENT_SONG_POSITION--;
            }
        }
        play_song(music_array_list.get(CURRENT_SONG_POSITION));
    }

    public void next_song() {
        stop_song();
        if (CURRENT_SONG_POSITION == (music_array_list.size() - 1)) {
            CURRENT_SONG_POSITION = 0;
        } else {
            CURRENT_SONG_POSITION++;
        }
        play_song(music_array_list.get(CURRENT_SONG_POSITION));
    }

}