package org.worshipsongs.service;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeApiServiceUtil;
import com.google.android.youtube.player.YouTubeInitializationResult;

import org.apache.commons.lang3.StringUtils;
import org.worshipsongs.CommonConstants;
import org.worshipsongs.WorshipSongApplication;
import org.worshipsongs.activity.CustomYoutubeBoxActivity;
import org.worshipsongs.activity.SongContentViewActivity;
import org.worshipsongs.dao.SongDao;
import org.worshipsongs.domain.Setting;
import org.worshipsongs.domain.Song;
import org.worshipsongs.domain.Verse;
import org.worshipsongs.fragment.AddPlayListsDialogFragment;
import org.worshipsongs.fragment.ListDialogFragment;
import org.worshipsongs.worship.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Seenivasan on 5/16/2015.
 */
public class SongListAdapterService
{

    public PopupWindow popupWindow;
    private WorshipSongApplication application = new WorshipSongApplication();
    private String selectedSong;
    private SongDao songDao = new SongDao(application.getContext());
    private UtilitiesService utilitiesService = new UtilitiesService();
    private List<Verse> verseList;
    private String[] serviceNames;
    private CommonService commonService = new CommonService();
    private ListDialogFragment dialogFragment;

    public ArrayAdapter<Song> getNewSongListAdapter(final List<Song> songs, final FragmentManager fragmentManager)
    {
        return new ArrayAdapter<Song>(application.getContext(), R.layout.songs_listview_content, songs)
        {
            @Override
            public View getView(final int position, View convertView, final ViewGroup parent)
            {
                LayoutInflater inflater = (LayoutInflater) application.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View rowView = inflater.inflate(R.layout.songs_listview_content, parent, false);
                final TextView textView = (TextView) rowView.findViewById(R.id.songsTextView);
                textView.setText(songs.get(position).getTitle());
                final ImageView imageView = (ImageView) rowView.findViewById(R.id.optionMenuIcon);

                imageView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        showPopupMenu(v, String.valueOf(textView.getText()), fragmentManager);
                    }
                });

                (rowView.findViewById(R.id.songsTextView)).setOnClickListener(new View.OnClickListener()
                {
                    public void onClick(View arg0)
                    {
                        selectedSong = textView.getText().toString();
                        newdisplaySelectedSong(songs, position);
                    }
                });
                return rowView;
            }
        };
    }

    private void showYouTube()
    {
        Intent lightboxIntent = new Intent(application.getContext(), CustomYoutubeBoxActivity.class);
        lightboxIntent.putExtra(CustomYoutubeBoxActivity.KEY_VIDEO_ID, "yKc-ey5pnNo");
        lightboxIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        application.getContext().startActivity(lightboxIntent);
    }

//    public ArrayAdapter<String> getSongListAdapter(final List<String> songs, final FragmentManager fragmentManager)
//    {
//        return new ArrayAdapter<String>(application.getContext(), R.layout.songs_listview_content, songs)
//        {
//            @Override
//            public View getView(final int position, View convertView, ViewGroup parent)
//            {
//                LayoutInflater inflater = (LayoutInflater) application.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                View rowView = inflater.inflate(R.layout.songs_listview_content, parent, false);
//                final TextView textView = (TextView) rowView.findViewById(R.id.songsTextView);
//                textView.setText(songs.get(position));
//                final ImageView imageView = (ImageView) rowView.findViewById(R.id.optionMenuIcon);
//
//                imageView.setOnClickListener(new View.OnClickListener()
//                {
//                    @Override
//                    public void onClick(View v)
//                    {
//                        showPopupMenu(v, String.valueOf(textView.getText()), fragmentManager);
//                    }
//                });
//
//                (rowView.findViewById(R.id.songsTextView)).setOnClickListener(new View.OnClickListener()
//                {
//                    public void onClick(View arg0)
//                    {
//                        selectedSong = textView.getText().toString();
//                        displaySelectedSong();
//                    }
//                });
//                return rowView;
//            }
//        };
//    }

    public void showPopupMenu(View view, final String songName, final FragmentManager fragmentManager)
    {
        final PopupMenu popupMenu = new PopupMenu(application.getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.song_list_option_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
        {
            public boolean onMenuItemClick(final MenuItem item)
            {
                switch (item.getItemId()) {
                    case R.id.addToList:
                        dialogFragment = new ListDialogFragment()
                        {
                            @Override
                            public String[] getProductListsArray()
                            {

                                List<String> services = new ArrayList<String>();
                                services.addAll(commonService.readServiceName());
                                services.add(0, "New favourite...");
                                Log.d("service names list", commonService.readServiceName().toString());
                                serviceNames = new String[services.size()];
                                services.toArray(serviceNames);
                                Log.d("service names are", services.toString());
                                return serviceNames;
                            }

                            @Override
                            protected void onClick(int which)
                            {
                                Log.d("Clicked position:", String.valueOf(which));
                                if (which == 0) {
                                    final AddPlayListsDialogFragment addPlayListsDialog = new AddPlayListsDialogFragment()
                                    {
                                        @Override
                                        public String getSelectedSong()
                                        {
                                            return songName;
                                        }
                                    };
                                    addPlayListsDialog.show(fragmentManager, "addToListFragment");
                                } else {
                                    List<String> services = new ArrayList<String>();
                                    services.addAll(commonService.readServiceName());
                                    serviceNames = new String[services.size()];
                                    String[] selectedServiceNames = services.toArray(serviceNames);
                                    commonService.saveIntoFile(selectedServiceNames[which - 1].toString(), songName);
                                    Toast.makeText(getActivity(), "Song added to favourite...!", Toast.LENGTH_LONG).show();
                                }
                            }
                        };
                        dialogFragment.show(fragmentManager, "serviceListFragment");
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    public void newdisplaySelectedSong(List<Song> songs, int position)
    {
        Intent intent = new Intent(application.getContext(), SongContentViewActivity.class);
        ArrayList<String> songList = new ArrayList<String>();
        for (Song song : songs) {
            songList.add(song.getTitle());
        }
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(CommonConstants.TITLE_LIST_KEY, songList);
        Setting.getInstance().setPosition(position);
        //bundle.putInt(CommonConstants.POSITION_KEY, position);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        application.getContext().startActivity(intent);

    }

    public void displaySelectedSong()
    {
        Log.d("Selected song:", selectedSong);
        Song song = songDao.getSongByTitle(selectedSong);
        String lyrics = song.getLyrics();
        verseList = utilitiesService.getVerse(lyrics);
        List<String> verseName = new ArrayList<String>();
        List<String> verseContent = new ArrayList<String>();
        Map<String, String> verseDataMap = new HashMap<String, String>();
        for (Verse verses : verseList) {
            verseName.add(verses.getType() + verses.getLabel());
            verseContent.add(verses.getContent());
            verseDataMap.put(verses.getType() + verses.getLabel(), verses.getContent());
        }
        List<String> verseListDataContent = new ArrayList<String>();
        List<String> verseListData = new ArrayList<String>();
        String verseOrder = song.getVerseOrder();
        if (StringUtils.isNotBlank(verseOrder)) {
            verseListData = utilitiesService.getVerseByVerseOrder(verseOrder);
        }
        Intent intent = new Intent(application.getContext(), SongContentViewActivity.class);
        intent.putExtra("serviceName", song.getTitle());
        if (verseListData.size() > 0) {
            intent.putStringArrayListExtra("verseName", (ArrayList<String>) verseListData);
            for (int i = 0; i < verseListData.size(); i++) {
                verseListDataContent.add(verseDataMap.get(verseListData.get(i)));
            }
            intent.putStringArrayListExtra("verseContent", (ArrayList<String>) verseListDataContent);
            Log.d(this.getClass().getName(), "Verse List data content :" + verseListDataContent);
        } else {
            intent.putStringArrayListExtra("verseName", (ArrayList<String>) verseName);
            intent.putStringArrayListExtra("verseContent", (ArrayList<String>) verseContent);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        application.getContext().startActivity(intent);
    }
}
