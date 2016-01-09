package com.raspi.chatapp.ui.password;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.raspi.chatapp.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PinFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PinFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PinFragment extends Fragment{
  private OnFragmentInteractionListener mListener;

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @param hash the password hash
   * @param salt the salt that was applied to the password
   * @return A new instance of fragment PinFragment.
   */
  public static PinFragment newInstance(String hash, String salt){
    PinFragment fragment = new PinFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }  public PinFragment(){
    // Required empty public constructor
  }

  @Override
  public void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
  }

  @Override
  public void onResume(){
    super.onResume();

    EditText pin = (EditText) getView().findViewById(R.id.pin);
    pin.addTextChangedListener(new TextWatcher(){
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after){

      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count){

      }

      @Override
      public void afterTextChanged(Editable s){
        if (s.length() == 4){
          char[] pwd = new char[4];
          s.getChars(0, 4, pwd, 0);
          //TODO progressbar --> cycle
          if (!mListener.onPasswordEntered(pwd)){
            s.clear();
          }
        }
      }
    });
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState){
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_pin, container, false);
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

  @Override
  public void onDetach(){
    super.onDetach();
    mListener = null;
  }

  /**
   * This interface must be implemented by activities that contain this
   * fragment to allow an interaction in this fragment to be communicated
   * to the activity and potentially other fragments contained in that
   * activity.
   * <p/>
   * See the Android Training lesson <a href=
   * "http://developer.android.com/training/basics/fragments/communicating.html"
   * >Communicating with Other Fragments</a> for more information.
   */
  public interface OnFragmentInteractionListener{
    boolean onPasswordEntered(char[] pwd);
  }
}
