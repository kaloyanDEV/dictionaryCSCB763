package com.me.dictionary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

import dictionary.me.com.dictionary.R;


/**
 * A startup activity with translation functionality.
 * Can call rest API and translate from english to bulgarian.
 * 1 button to call insert activity.
 * Second button to call view all activity.
 */
public class DictionaryActivity extends AppCompatActivity {


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


    private static final Pattern wordPattern = Pattern.compile("^[a-z]+$");


    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private TranslateTask mTranslateTask = null;

    // UI references.
    private AutoCompleteTextView mWordView;
    private View mProgressView;
    private View mLoginFormView;

    private TextView translationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);
        // Set up the login form.
        mWordView = (AutoCompleteTextView) findViewById(R.id.email);

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

    /**
     * Attempts to translate
     */
    private void attemptTranslate() {
        if (mTranslateTask != null) {
            return;
        }

        // Reset errors.
        mWordView.setError(null);

        String word = mWordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

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
            // There was an error; don't attempt translate and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform translation.
            showProgress(true);

            //System.out.println("TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT ------------------------------------- " + word);

            mTranslateTask = new TranslateTask(word);
            mTranslateTask.execute((Void) null);

        }
    }

    private boolean isWordValid(String word) {
        return word.length() > 0 && word.length() < 20 && wordPattern.matcher(word).find();
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
            } catch (IllegalArgumentException e) {
                e.printStackTrace();

                DictionaryActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DictionaryActivity.this, "Думата не съществува!",
                                Toast.LENGTH_LONG).show();
                    }
                });

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

            final Button mAddNewWordButton = (Button) findViewById(R.id.add_new_word);
            if (success) {
                //finish();
                mAddNewWordButton.setEnabled(true);
            } else {
                mAddNewWordButton.setEnabled(false);
                //TODO show error message
            }
        }

        @Override
        protected void onCancelled() {
            mTranslateTask = null;
            showProgress(false);
        }
    }
}

