package com.monitorabrasil.supercidadao.actions;

import android.content.Context;

import com.monitorabrasil.supercidadao.POJO.PartidaEvent;
import com.monitorabrasil.supercidadao.R;
import com.monitorabrasil.supercidadao.application.AppController;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by geral_000 on 04/03/2016.
 */
public class PartidaActions {

    public static final String PARTIDA_BUSCA = "partida_busca";
    public static final java.lang.String PARTIDA_INICIAR = "partida_iniciar";
    public static final String PARTIDA_AGUARDANDO_J2 = "partida_aguardando_j2";
    private static PartidaActions instance;

    PartidaActions(){}

    public static  PartidaActions get(){

        if(instance ==null){
            instance = new PartidaActions();
        }
        return instance;
    }

    public void getPartida(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ComentarioPolitico");

        query.addDescendingOrder("createdAt");
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    EventBus.getDefault().post(new PartidaEvent(PARTIDA_BUSCA, object, null));
                } else {
                    PartidaEvent ce = new PartidaEvent(PARTIDA_BUSCA);
                    ce.setErro(AppController.getInstance().getString(R.string.erro_geral));
                    EventBus.getDefault().post(ce);
                }
            }
        });
    }


    public void iniciaPartida() {
        //buscar partida criada

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Partida");
        query.whereDoesNotExist("j2");
        query.addAscendingOrder("createdAt");
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                HashMap<String,Object> param = new HashMap<String, Object>();
                if (object != null) {
                    if(e == null){
                        // ParseUser user = new ParseUser().getParseUser("kx61Sf8DKs");
                        param.put("id","kx61Sf8DKs");
                        param.put("idPartida",object.getObjectId());
                        final ParseObject partida = object;
                        ParseCloud.callFunctionInBackground("inserirJogador2", param, new FunctionCallback<String>() {
                            @Override
                            public void done(String object, ParseException e) {
                                if(e==null){
                                    //criou o jogo
                                    try {
                                        JSONObject json = new JSONObject(object);
                                        if(!json.getBoolean("erro")){
                                            partida.fetchInBackground(new GetCallback<ParseObject>() {
                                                @Override
                                                public void done(ParseObject object, ParseException e) {
                                                    EventBus.getDefault().post(new PartidaEvent(PARTIDA_INICIAR, partida, null));
                                                }
                                            });

                                        }else{
                                            PartidaEvent event = new PartidaEvent(PARTIDA_INICIAR);
                                            event.setErro(AppController.getInstance().getResources().getString(R.string.erro_criar_partida));
                                        }
                                    } catch (JSONException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            }
                        });

                    }


                } else {
                    //cria uma partida
                    // ParseUser user = new ParseUser().getParseUser("kx61Sf8DKs");
                    param.put("id","QduMwdNj4X");
                    ParseCloud.callFunctionInBackground("criarPartida", param, new FunctionCallback<String>() {
                        @Override
                        public void done(String object, ParseException e) {
                            if(e==null){
                                //criou a partida mas fica aguardando um adversario
                                try {
                                    JSONObject json = new JSONObject(object);
                                    if(!json.getBoolean("erro")){
                                        ParseObject partida = ParseObject.createWithoutData("Partida",json.getString("partida"));
                                        EventBus.getDefault().post(new PartidaEvent(PARTIDA_AGUARDANDO_J2, partida, null));
                                    }else{
                                        PartidaEvent event = new PartidaEvent(PARTIDA_AGUARDANDO_J2);
                                        event.setErro(AppController.getInstance().getResources().getString(R.string.erro_criar_partida));
                                    }
                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }
                            }else{
                                PartidaEvent ce = new PartidaEvent(PARTIDA_INICIAR);
                                ce.setErro(AppController.getInstance().getString(R.string.erro_geral));
                                EventBus.getDefault().post(ce);
                            }

                        }
                    });

                }
            }
        });
    }
}
