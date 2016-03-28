package com.monitorabrasil.supercidadao.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.monitorabrasil.supercidadao.R;
import com.parse.LogInCallback;
import com.parse.LogOutCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;
    private static final String ATUALIZAR_CADASTRO_ANONIMO = "atualizar_cadastro_anonimo";
    private static final String LOGAR = "logar";
    private static final String CADASTRAR = "cadastrar";

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };


    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText mNome;
    private TextInputLayout txtLayNome;
    private View mProgressView;
    private View mLoginFormView;
    private Button mEmailSignInButton;
    private Button btnlogout;
    private String statusForm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        statusForm = LOGAR;
        //link para novo cadastro
        final TextView linkCadastro = (TextView)findViewById(R.id.linkCadastro);
        assert linkCadastro != null;
        linkCadastro.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                txtLayNome.setVisibility(View.VISIBLE);
                mEmailSignInButton.setText("Cadastrar");
                mNome.requestFocus();
                statusForm = CADASTRAR;
            }
        });

        //link para cadastro anonimo
        final TextView linkCadastroAnonimo = (TextView)findViewById(R.id.linkCadastroAnonimo);
        assert linkCadastroAnonimo != null;
        linkCadastroAnonimo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress(true);
                ParseAnonymousUtils.logIn(new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (e != null) {
                            Log.d(MainActivity.TAG, "Anonymous login failed.");
                        } else {
                            Log.d(MainActivity.TAG, "Anonymous user logged in.");
//                            user.set
                            atualizaInstalacao(user);
                            finish();
                        }
                    }
                });
            }
        });


        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mNome = (EditText)findViewById(R.id.nome);
        txtLayNome = (TextInputLayout)findViewById(R.id.txtLayNome);
        assert txtLayNome != null;
        txtLayNome.setVisibility(View.INVISIBLE);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        //botao logout
        btnlogout = (Button) findViewById(R.id.btLogout);
        btnlogout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress(true);
                ParseUser.logOutInBackground(new LogOutCallback() {
                    @Override
                    public void done(ParseException e) {
                        showProgress(false);
                        txtLayNome.setVisibility(View.GONE);
                        btnlogout.setVisibility(View.GONE);
                        mEmailSignInButton.setText(getString(R.string.action_sign_in));
                        mEmailView.setText("");
                        mNome.setText("");
                        linkCadastro.setVisibility(View.VISIBLE);
                        linkCadastroAnonimo.setVisibility(View.VISIBLE);
                        statusForm = LOGAR;
                    }
                });
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        //verificar se esta cadastrado
        if(ParseUser.getCurrentUser()!= null){
            ParseUser user = ParseUser.getCurrentUser();
            txtLayNome.setVisibility(View.VISIBLE);
            mNome.setText(user.getString("nome"));
            mPasswordView.setVisibility(View.VISIBLE);
            linkCadastro.setVisibility(View.INVISIBLE);
            linkCadastroAnonimo.setVisibility(View.INVISIBLE);
            mEmailSignInButton.setText("Atualizar Cadastro");
            statusForm = ATUALIZAR_CADASTRO_ANONIMO;
            assert  user.getEmail() != null;
            mEmailView.setText(user.getEmail());
            btnlogout.setVisibility(View.VISIBLE);
        }
    }

    private void atualizaInstalacao(ParseUser user) {
        ParseInstallation installation =  ParseInstallation.getCurrentInstallation();
        installation.put("user", user);
        installation.saveInBackground();
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {


        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mNome.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        final String password = mPasswordView.getText().toString();
        final String nome = mNome.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        //verifica se preecheu o nome
        if(statusForm.equals(CADASTRAR) && TextUtils.isEmpty(nome)){
            mNome.setError(getString(R.string.error_field_required));
            focusView = mNome;
            cancel=true;
        }
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            if(statusForm.equals(LOGAR)) {
                ParseUser.logInInBackground(email, password, new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if(e==null){
                            atualizaInstalacao(user);
                            finish();
                        }else{
                            showProgress(false);
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT)
                                    .show();
                            mPasswordView.setError(getString(R.string.error_incorrect_password));
                            mPasswordView.requestFocus();
                        }
                    }
                });

            }else{
                //cadastrar
                ParseUser user = new ParseUser();
                if(ParseUser.getCurrentUser()!= null) {
                    user = ParseUser.getCurrentUser();
                    user.setEmail(email);
                    user.setUsername(email);
                    user.put("nome",nome);
                    if(!TextUtils.isEmpty(password))
                        user.setPassword(password);
                    user.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            showProgress(false);
                            if(e==null){
                                atualizaInstalacao(ParseUser.getCurrentUser());
                                Toast.makeText(getApplicationContext(), getString(R.string.atualizado_sucesso), Toast.LENGTH_SHORT)
                                        .show();
                                finish();
                            }else{
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }
                    });
                }else{
                    user.setPassword(password);
                    user.setUsername(email);
                    user.put("nome",nome);
                    user.setEmail(email);
                    user.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e == null){
                                atualizaInstalacao(ParseUser.getCurrentUser());
                                finish();
                            }else{
                                if(e.getCode() == 203) {//email ja cadastrado
                                    Toast.makeText(getApplicationContext(), e.getMessage().toString(), Toast.LENGTH_SHORT)
                                            .show();
                                    //tentar fazer o login
                                    showProgress(true);
                                    ParseUser.logInInBackground(nome, password, new LogInCallback() {
                                        @Override
                                        public void done(ParseUser user, ParseException e) {
                                            if(e==null){
                                                atualizaInstalacao(user);
                                                finish();
                                            }else{
                                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT)
                                                        .show();
                                            }
                                        }
                                    });
                                }
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT)
                                        .show();
                                Log.e(MainActivity.TAG,e.getMessage());


                            }
                            showProgress(false);
                        }
                    });
                }
            }
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


}

