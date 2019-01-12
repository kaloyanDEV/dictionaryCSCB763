package com.me.dictionary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import dictionary.me.com.dictionary.R;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A startup activity with translation functionality
 */
public class DictionaryActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {


    /**
     * keys used for intent
     */

    public static final String SOURCE_LANG = DictionaryActivity.class.getPackage() + ".SOURCE_LANG";
    public static final String TARGET_LANG = DictionaryActivity.class.getPackage() + ".TARGET_LANG";
    public static final String WORD = DictionaryActivity.class.getPackage() + ".WORD";
    public static final String TRANSLATION = DictionaryActivity.class.getPackage() + ".TRANSLATION";


    /**
     * api url
     */
    private static final String API_URL = "https://glosbe.com/gapi/translate?from=eng&dest=bg&format=json&tm=false&page=1";

    /**
     * pattern used to search
     */
    private static final Pattern jsonPattern = Pattern.compile(":\\[\\{\\\"phrase\\\":\\{\\\"text\\\":\\\"", java.util.regex.Pattern.CASE_INSENSITIVE);


    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private TranslateTask mTranslateTask = null;

    // UI references.
    private AutoCompleteTextView mWordView;
    //private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private TextView translationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);
        // Set up the login form.
        mWordView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        Button mTranslateButton = (Button) findViewById(R.id.translate_button);
        mTranslateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptTranslate();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        translationView = (TextView) findViewById(R.id.translation);


        final Button mAddNewWordButton = (Button) findViewById(R.id.add_new_word);
        mAddNewWordButton.setEnabled(false);

        mAddNewWordButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DictionaryActivity.this, InsertActivity.class);
                intent.putExtra(SOURCE_LANG, "en");
                intent.putExtra(TARGET_LANG, "bg");
                intent.putExtra(WORD, mWordView.getText().toString());
                intent.putExtra(TRANSLATION, translationView.getText().toString());

                startActivity(intent);

            }
        });

        translationView.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {
                final boolean isReady = translationView.getText().toString().length() > 3;

                //modification of widget need to executed in main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAddNewWordButton.setEnabled(isReady);
                    }
                });
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });


        final Button mViewAllWordsButton = (Button) findViewById(R.id.view_all_words);

        mViewAllWordsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DictionaryActivity.this, ListActivity.class);
                startActivity(intent);
            }
        });


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
            Snackbar.make(mWordView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
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
     * Attempts to translate
     */
    private void attemptTranslate() {
        if (mTranslateTask != null) {
            return;
        }

        // Reset errors.
        mWordView.setError(null);
        //mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String word = mWordView.getText().toString();
        //String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.

        /*
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }
        */

        // Check for a valid email address.
        if (TextUtils.isEmpty(word)) {
            mWordView.setError(getString(R.string.error_field_required));
            focusView = mWordView;
            cancel = true;
        } else if (!isWordValid(word)) {
            mWordView.setError(getString(R.string.error_invalid_word));
            focusView = mWordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            //mAuthTask = new UserLoginTask(email, password);
            //mAuthTask = new TranslateTask(word, null);
            //mAuthTask.execute((Void) null);


            System.out.println("TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT ------------------------------------- " + word);

            mTranslateTask = new TranslateTask(word);
            mTranslateTask.execute((Void) null);


            //showProgress(false);

        }
    }

    private boolean isWordValid(String word) {
        //TODO: Replace this with your own logic
        //return email.contains("@");
        return word.length() > 0 && word.length() < 20;
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
                new ArrayAdapter<>(DictionaryActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mWordView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * search for :\[\{\"phrase\":\{\"text\":\" in json representation and try to extract translation
     *
     * @param input - json payload returned by https://glosbe.com
     * @return
     */
    private String extractWord(String input) {
        java.util.regex.Matcher m = jsonPattern.matcher(input);

        if (m.find()) {
            String substring = input.substring(m.end(0));

            String translation = substring.substring(0, substring.indexOf("\"", 0));

            return translation;
        } else {
            throw new IllegalArgumentException("Word not matched!");
        }
    }


    /**
     * Represents an asynchronous network task.
     */
    public class TranslateTask extends AsyncTask<Void, Void, Boolean> {

        private final String mWord;

        TranslateTask(String email) {
            mWord = email;
        }

        @Override
        protected Boolean doInBackground(Void... params) {


            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(API_URL + "&phrase=" + mWord);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());


                BufferedReader r = new BufferedReader(new InputStreamReader(in));
                StringBuilder total = new StringBuilder();
                for (String line; (line = r.readLine()) != null; ) {
                    total.append(line).append('\n');
                }

                String translation = extractWord(total.toString());

                System.out.println(translation);

                translationView.setText(translation);

            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                urlConnection.disconnect();
            }


            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mTranslateTask = null;
            showProgress(false);

            if (success) {
                //finish();
            } else {
                //mPasswordView.setError(getString(R.string.error_incorrect_password));
                //mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mTranslateTask = null;
            showProgress(false);
        }
    }
}

