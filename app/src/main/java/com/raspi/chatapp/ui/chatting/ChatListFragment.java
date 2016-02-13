package com.raspi.chatapp.ui.chatting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.raspi.chatapp.R;
import com.raspi.chatapp.ui.util.chat_array.ChatArrayAdapter;
import com.raspi.chatapp.ui.util.chat_array.ChatEntry;
import com.raspi.chatapp.util.internet.XmppManager;
import com.raspi.chatapp.util.storage.MessageHistory;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChatListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChatListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatListFragment extends Fragment{
  private ActionBar actionBar;

  private OnFragmentInteractionListener mListener;
  private ChatArrayAdapter caa;
  private ListView lv;
  private BroadcastReceiver MessageReceiver = new BroadcastReceiver(){
    @Override
    public void onReceive(Context context, Intent intent){
      initUI();
      Bundle extras = intent.getExtras();
      try{
        XmppManager.getInstance(context).sendAcknowledgement(extras.getString
                        (ChatActivity.BUDDY_ID), extras.getLong("id"),
                MessageHistory.STATUS_RECEIVED);
      }catch (Exception e){
      }

      abortBroadcast();
    }
  };

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @return A new instance of fragment ChatListFragment.
   */
  public static ChatListFragment newInstance(){
    ChatListFragment fragment = new ChatListFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onResume(){
    super.onResume();
    initUI();
    IntentFilter filter = new IntentFilter(ChatActivity.RECEIVE_MESSAGE);
    filter.setPriority(1);
    getContext().registerReceiver(MessageReceiver, filter);
  }

  @Override
  public void onPause(){
    super.onPause();
    getContext().unregisterReceiver(MessageReceiver);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
    menu.clear();
    inflater.inflate(R.menu.menu_chat_list, menu);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState){
    super.onActivityCreated(savedInstanceState);
    actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
  }

  @Override
  public void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState){
    // Inflate the layout for this fragment
    setHasOptionsMenu(true);
    return inflater.inflate(R.layout.fragment_chat_list, container, false);

  }

  @Override
  public void onAttach(Context context){
    super.onAttach(context);
    if (context instanceof OnFragmentInteractionListener){
      mListener = (OnFragmentInteractionListener) context;
    }else{
      throw new RuntimeException(context.toString()
              + " must implement OnFragmentInteractionListener");
    }
  }

  /*
      USER SPECIFIC FUNCTIONS
   */

  @Override
  public void onDetach(){
    super.onDetach();
    mListener = null;
  }

  private void initUI(){
    caa = new ChatArrayAdapter(getContext(), R.layout.chat_list_entry);
    lv = (ListView) getView().findViewById(R.id.main_listview);
    lv.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
    lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id){
        ChatEntry chatEntry = caa.getItem(position);
        mListener.onChatOpened(chatEntry.buddyId, chatEntry.name);
      }
    });
    lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
      @Override
      public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id){
        final EditText newName = new EditText(getActivity());
        String title = getResources().getString(R.string.change_name_title) +
                " " + caa.getItem(position).name;
        new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(R.string.change_name)
                .setView(newName)
                .setPositiveButton(R.string.rename, new DialogInterface.OnClickListener(){
                  @Override
                  public void onClick(DialogInterface dialog, int which){
                    MessageHistory messageHistory = new MessageHistory
                            (getContext());
                    String buddyId = caa.getItem(position).buddyId;
                    String name = newName.getText().toString();
                    messageHistory.renameChat(buddyId, name);
                    initUI();
                  }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
                  @Override
                  public void onClick(DialogInterface dialog, int which){

                  }
                }).show();
        return true;
      }
    });
    lv.setAdapter(caa);
    MessageHistory messageHistory = new MessageHistory(getContext());
    ChatEntry[] entries = messageHistory.getChats();
    for (ChatEntry entry : entries){
      if (entry != null){
        Log.d("DEBUG", "adding entry to view: " + entry);
        caa.add(entry);
      }else{
        Log.d("DEBUG", "a null entry");
      }
    }
    caa.registerDataSetObserver(new DataSetObserver(){
      @Override
      public void onChanged(){
        super.onChanged();
        lv.setSelection(caa.getCount() - 1);
      }
    });
    actionBar.setTitle(R.string.app_name);
    actionBar.setSubtitle(null);
  }

  /**
   * This interface must be implemented by ui that contain this
   * fragment to allow an interaction in this fragment to be communicated
   * to the activity and potentially other chatting contained in that
   * activity.
   * <p/>
   * See the Android Training lesson <a href=
   * "http://developer.android.com/training/basics/fragments/communicating.html"
   * >Communicating with Other Fragments</a> for more information.
   */
  public interface OnFragmentInteractionListener{
    void onChatOpened(String buddyId, String name);
  }
}
