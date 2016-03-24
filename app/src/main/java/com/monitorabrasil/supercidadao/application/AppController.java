package com.monitorabrasil.supercidadao.application;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;
import com.monitorabrasil.supercidadao.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;

/**
 * Created by geral_000 on 14/02/2015.
 */
public class AppController extends Application {


    public final static String URL = "http://52.27.220.189/monitorabrasil.com/gamfig.com/mbrasilwsdl/";
    public static final String URL_FOTO_DEPUTADO = "http://www.camara.gov.br/internet/deputado/bandep/";
    public static final String URL_FOTO_SENADOR = "http://www.senado.gov.br/senadores/img/fotos-oficiais/senador";

    private ImageLoader mImagemLoader;


    private SharedPreferences sharedPref;

    public static final String TAG = AppController.class.getSimpleName();

    private static AppController mInstance;



    private int idUsuario;

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario() {
        this.idUsuario = this.getSharedPref().getInt(getString(R.string.id_key_idcadastro_novo),0);
    }





    public SharedPreferences getSharedPref(){return sharedPref;}
    public ImageLoader getmImagemLoader() {
        return mImagemLoader;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        mInstance = this;

        //iniciando o parse
        //Parse.enableLocalDatastore(this);
        //Parse.initialize(this, AppConfig.PARSE_APPLICATION_ID, AppConfig.PARSE_CLIENT_KEY);
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(AppConfig.PARSE_APPLICATION_ID)
                .clientKey(AppConfig.PARSE_CLIENT_KEY)
                //.clientKey(null)
                .enableLocalDataStore()
                //.server(AppConfig.URL_PARSE_SERVER)
                .build());

        ParseInstallation installation =  ParseInstallation.getCurrentInstallation();
        if(null != ParseUser.getCurrentUser())
            installation.put("user", ParseUser.getCurrentUser());
        installation.saveInBackground();

        //iniciando o twitter
        TwitterAuthConfig authConfig = new TwitterAuthConfig(AppConfig.TWITTER_CONSUMER_KEY, AppConfig.TWITTER_CONSUMER_SECRET);
        Fabric.with(this, new Twitter(authConfig), new Crashlytics());

        //configurando o imageloader
        DisplayImageOptions mDisplayImageOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.tw__ic_tweet_photo_error_light)
                .cacheInMemory(true).build();
        ImageLoaderConfiguration conf = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(mDisplayImageOptions)
                .memoryCacheSize(50*1024*1024)
                .build();
        this.mImagemLoader = ImageLoader.getInstance();
        mImagemLoader.init(conf);


        sharedPref = getSharedPreferences(getString(R.string.id_key_preferencias), Context.MODE_PRIVATE);

    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }





}
