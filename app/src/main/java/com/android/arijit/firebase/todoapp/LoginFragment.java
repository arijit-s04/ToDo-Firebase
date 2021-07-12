package com.android.arijit.firebase.todoapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoginFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
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
    private EditText etemail, etpass;
    private Button btnLogin;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_login, container, false);

        etemail = (EditText) root.findViewById(R.id.et_login_email);
        etpass = (EditText) root.findViewById(R.id.et_login_password);
        btnLogin = (Button) root.findViewById(R.id.btn_account_login);

        btnLogin.setOnClickListener(v -> {
            if(!allFieldOk()){
                Snackbar.make(v, "Please fill all the fields", Snackbar.LENGTH_SHORT)
                        .show();
                return;
            }
            String email = etemail.getText().toString().trim();
            String pass = etpass.getText().toString().trim();

            ProgressDialog loading = new ProgressDialog(getContext());
            loading.setTitle("Logging In");
            loading.setMessage("Please Wait...");
            loading.setIndeterminate(true);
            loading.show();

            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            mAuth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(task -> {
                        loading.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Snackbar.make(v, e.getMessage(), Snackbar.LENGTH_SHORT)
                                .show();
                    })
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            FirebaseUser user = authResult.getUser();
                            if(!user.isEmailVerified()){

                                Snackbar.make(v, "Please Verify Your Email", Snackbar.LENGTH_LONG)
                                        .setAction("Verify", inner -> {
                                            user.sendEmailVerification()
                                                    .addOnCompleteListener(task -> {
                                                       loading.dismiss();
                                                       if(task.isSuccessful()){
                                                           Toast.makeText(getContext(), "Email Sent", Toast.LENGTH_SHORT).show();
                                                       } else{
                                                           Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                       }
                                                    });
                                        })
                                        .show();
                                mAuth.signOut();
                            } else {
                                Intent intent = new Intent(getContext(), MainActivity.class);
                                intent.putExtra("FirstTime", true);
                                getActivity().startActivity(intent);
                                getActivity().finish();
                            }
                        }
                    });
        });

        return  root;
    }

    private boolean allFieldOk(){
        boolean ans = true;
        if(etemail.getText().toString().trim().equals("")){
            etemail.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.warning)));
            etemail.setHintTextColor(ColorStateList.valueOf(getResources().getColor(R.color.warning)));
            ans = false;
        }
        if(etpass.getText().toString().equals("")){
            etpass.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.warning)));
            etpass.setHintTextColor(ColorStateList.valueOf(getResources().getColor(R.color.warning)));
        }

        return ans;
    }
}