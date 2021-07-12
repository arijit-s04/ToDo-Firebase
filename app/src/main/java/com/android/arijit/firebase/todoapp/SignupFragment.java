package com.android.arijit.firebase.todoapp;

import android.app.ProgressDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SignupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignupFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SignupFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SignupFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SignupFragment newInstance(String param1, String param2) {
        SignupFragment fragment = new SignupFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    private EditText etemail, etname, etpass, etconpass;
    private TextView tvPassInfo;
    private Animation test, test2;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_signup, container, false);
        etpass = (EditText) root.findViewById(R.id.et_signup_password);
        etemail = (EditText) root.findViewById(R.id.et_signup_email);
        etname = (EditText) root.findViewById(R.id.et_signup_name);
        tvPassInfo = (TextView) root.findViewById(R.id.tv_pass_info);
        test = AnimationUtils.loadAnimation(getContext(), R.anim.pass_info_reveal);
        test2 = AnimationUtils.loadAnimation(getContext(), R.anim.pass_info_hide);

        root.findViewById(R.id.btn_account_sign_up).setOnClickListener(v -> {
            if(!allFieldsOk()) {
                Snackbar.make(v, "Please fill all the fields", Snackbar.LENGTH_SHORT)
                        .show();
                return;
            }
            if(etpass.getText().toString().length()<8){
                etpass.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.warning)));
                etpass.setHintTextColor(ColorStateList.valueOf(getResources().getColor(R.color.warning)));
                if(tvPassInfo.getVisibility() == View.INVISIBLE) {
                    tvPassInfo.startAnimation(test);
                    tvPassInfo.setVisibility(View.VISIBLE);
                }
            }
            else{
                String email = etemail.getText().toString().trim();
                String pass = etpass.getText().toString().trim();
                String name = etname.getText().toString().trim();
                ProgressDialog loading = new ProgressDialog(getContext());
                loading.setTitle("Creating account");
                loading.setMessage("Please Wait...");
                loading.setIndeterminate(true);
                loading.show();

                firebaseAuth.createUserWithEmailAndPassword(email, pass)
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                UserProfileChangeRequest changeRequest = new UserProfileChangeRequest
                                        .Builder().setDisplayName(name).build();
                                FirebaseUser user = authResult.getUser();
                                user.updateProfile(changeRequest);
                                String createdId = user.getUid();
                                HashMap<String, Object> userObj = new HashMap<>();
                                userObj.put("userid", createdId);
                                userObj.put("username", name);
                                userObj.put("createdate", FieldValue.serverTimestamp());
                                FirebaseFirestore.getInstance()
                                        .collection("usercollection")
                                        .document(createdId)
                                        .set(userObj)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                user.sendEmailVerification()
                                                    .addOnCompleteListener(task -> {
                                                        loading.dismiss();
                                                        if(task.isSuccessful()){
                                                            Snackbar.make(getView(), "Account created. Please verify the email sent to you.",Snackbar.LENGTH_LONG)
                                                                    .show();
                                                        }
                                                        else{
                                                            Snackbar.make(getView(), task.getException().getMessage(),Snackbar.LENGTH_LONG)
                                                                    .show();
                                                        }
                                                    });

                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            loading.dismiss();
                                            Snackbar.make(getView(), e.getMessage(),Snackbar.LENGTH_LONG)
                                                    .show();
                                        });
                            }
                        })
                        .addOnFailureListener(e -> {
                            loading.dismiss();
                            Snackbar.make(getView(), e.getMessage(),Snackbar.LENGTH_LONG)
                                    .show();
                        });
            }

        });

        ((ImageButton) root.findViewById(R.id.ib_pass_info))
                .setOnClickListener(v -> {
                    if(tvPassInfo.getVisibility() == View.INVISIBLE) {
                        tvPassInfo.startAnimation(test);
                        tvPassInfo.setVisibility(View.VISIBLE);
                    }
                    else{
                        tvPassInfo.startAnimation(test2);
                        tvPassInfo.setVisibility(View.INVISIBLE);
                    }
                });

        return root;
    }
    private boolean allFieldsOk(){
        boolean ans = true;
        if( etpass.getText().toString().equals("")){
            etpass.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.warning)));
            etpass.setHintTextColor(ColorStateList.valueOf(getResources().getColor(R.color.warning)));
            ans = false;
        }
        if( etname.getText().toString().equals("")){
            etname.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.warning)));
            etname.setHintTextColor(ColorStateList.valueOf(getResources().getColor(R.color.warning)));
            ans = false;
        }
        if( etemail.getText().toString().equals("")){
            etemail.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.warning)));
            etemail.setHintTextColor(ColorStateList.valueOf(getResources().getColor(R.color.warning)));
            ans = false;
        }
        return ans;
    }
}