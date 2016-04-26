package com.monitorabrasil.supercidadao.actions;

import android.os.Handler;
import android.util.Log;

import com.monitorabrasil.supercidadao.POJO.PartidaEvent;
import com.monitorabrasil.supercidadao.R;
import com.monitorabrasil.supercidadao.application.AppController;
import com.monitorabrasil.supercidadao.views.MainActivity;
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
    public static final String PARTIDA_JOGADA = "partida_jogada";
    private static PartidaActions instance;

    PartidaActions(){}

    public static  PartidaActions get(){

        if(instance ==null){
            instance = new PartidaActions();
        }
        return instance;
    }

    /**
     * Inicia a partida ou cria
     */
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
                        ParseUser user = ParseUser.getCurrentUser();
                        param.put("id",user.getObjectId());
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
                    ParseUser user = ParseUser.getCurrentUser();
                    param.put("id",user.getObjectId());
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

    public void iniciarPartidaComputador(final ParseObject partida) {
        HashMap<String,Object> param = new HashMap<String, Object>();
        param.put("id",partida.getObjectId());
        ParseCloud.callFunctionInBackground("jogarComputer", param, new FunctionCallback<String>() {
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

    /**
     * Salva a jogada executada
     * @param isJogador1 se eh o jogador 1
     * @param carta1 id da carta do jogador 1
     * @param carta2 id da carta do jogador 2
     * @param partida objeto partida
     * @param categoria id da categoria escolhida
     * @param numJogada numero da jogada
     */
    public void enviaJogada(boolean isJogador1, Object carta1, Object carta2, ParseObject partida, int categoria,int numJogada) {
        ParseObject jogada = new ParseObject("Jogada");
        jogada.put("j1",isJogador1);
        jogada.put("carta1",carta1.toString());
        jogada.put("carta2",carta2.toString());
        jogada.put("partida",partida);
        jogada.put("categoria",categoria);
        jogada.put("retornou",false);
        jogada.put("numJogada",numJogada);
        jogada.saveInBackground();
    }

    /**
     * Atualiza a informacao se a partida iniciou
     * @param partida objeto partida
     */
    public void verificaInicio(final ParseObject partida) {
        Log.d(MainActivity.TAG,"Verificando inicio da partida");
        partida.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if(object.getList("cartas1")!= null){
                    EventBus.getDefault().post(new PartidaEvent(PARTIDA_INICIAR, object, null));
                }else{
                    buscaInformacaoPartida(partida);
                }
            }
        });
    }

    /**
     * handler para buscar de 3 em 3 segundos a informacao se a partida iniciou
     * @param object objeto partida
     */
    public void buscaInformacaoPartida(final ParseObject object) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                verificaInicio(object);
            }
        }, 5000);
    }

    /**
     * Verifica se o adversario jogou
     * @param partida partida em andamento
     */
    public void verificaJogada(final ParseObject partida, final boolean isJogador1) {
        Log.d(MainActivity.TAG,"Verificando jogada");
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Jogada");
        query.whereEqualTo("partida",partida);
        query.whereEqualTo("retornou",false);
        query.whereEqualTo("j1",!isJogador1);
        query.addAscendingOrder("createdAt");
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if(object != null) {
                    object.put("retornou", true);
                    object.saveInBackground();
                    PartidaEvent event = new PartidaEvent(PARTIDA_JOGADA);
                    event.setJogada(object);
                    EventBus.getDefault().post(event);
                }else{
                    buscaInformacaoJogada(partida,isJogador1);
                }
            }
        });
    }

    private void buscaInformacaoJogada(final ParseObject partida, final boolean isJogador1) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                verificaJogada(partida,isJogador1);
            }
        }, 3000);
    }


    public void somaPontos() {
        ParseUser parseUser = ParseUser.getCurrentUser();
        parseUser.increment("pontos",10);
        parseUser.saveInBackground();
    }
}
