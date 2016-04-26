package com.monitorabrasil.supercidadao.views;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easyandroidanimations.library.Animation;
import com.easyandroidanimations.library.AnimationListener;
import com.easyandroidanimations.library.FlipHorizontalAnimation;
import com.easyandroidanimations.library.PuffInAnimation;
import com.easyandroidanimations.library.PuffOutAnimation;
import com.easyandroidanimations.library.SlideInAnimation;
import com.easyandroidanimations.library.SlideOutAnimation;
import com.monitorabrasil.supercidadao.POJO.PartidaEvent;
import com.monitorabrasil.supercidadao.R;
import com.monitorabrasil.supercidadao.actions.PartidaActions;
import com.monitorabrasil.supercidadao.application.AppController;
import com.monitorabrasil.supercidadao.classes.Imagem;
import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = "SUPER_CIDADAO";
    private PartidaActions partidaActions;
    private ParseObject partida;
    private boolean empatou;
    private boolean computador; //indica se ta jogando com o computador

    private RelativeLayout rlResultadoFundo;
    private RelativeLayout rlResultado;

    private boolean isJogador1;//indica se sou jogador 1
    private boolean isMyTurn; //controle para saber se é minha vez
    private int numJogadas; //numero de jogadas
    private int categoriaSelecionada; //categoria para comparação selecionada
    private ParseObject ultimaJogada; //guarda a ultima jogada

    private TextView j1;
    private TextView j2;
    private TextView txtcartas1;
    private TextView txtcartas2;
    private TextView txtResultado;
    private TextView txtStatus;

    private CardView cardEscolherCategoria ;
    private Button btnJogar;
    private Button btnCardJogar;
    private TextView resultado;
    private CardView cardResultado1;
    private CardView cardResultado2;
    private FrameLayout frameJogador;

    private TextView txtPoliticoNome;
    private TextView txtPartido;
    private TextView txtPeso;
    private ImageView fotoPolitico;
    private ListView listViewCategoria;

    private List<Object> cartas1;
    private List<Object> cartas2;
    private List<Object> peso1;
    private List<Object> peso2;
    private List<Object> monte;
    private List<Object> pesoMonte;

    private ParseObject meuPolitico; //carta da vez
    private ParseObject adversarioPolitico; //carta da vez

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(ParseUser.getCurrentUser()== null){
            startActivity(new Intent(this,LoginActivity.class));
        }
        // enable transitions
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_main);
        partidaActions = PartidaActions.get();

        setupView();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // setSupportActionBar(toolbar);
        toolbar.setTitle("Super Cidadão!");


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setupView() {

        monte = new ArrayList<>();
        pesoMonte = new ArrayList<>();
        rlResultado = (RelativeLayout)findViewById(R.id.rlResultado);
        rlResultadoFundo = (RelativeLayout)findViewById(R.id.rlResultadoFundo);

        txtPoliticoNome = (TextView)findViewById(R.id.fichaNomePolitico);
        txtPartido = (TextView)findViewById(R.id.fichaCardPartido);
        txtPeso = (TextView)findViewById(R.id.txtPeso);
        txtResultado = (TextView)findViewById(R.id.resultado);
        fotoPolitico = (ImageView)findViewById(R.id.fichaFoto);
        listViewCategoria = (ListView)findViewById(R.id.listview_categorias);

        cardEscolherCategoria = (CardView)findViewById(R.id.cardEscolherCategoria);
        btnJogar = (Button)findViewById(R.id.btnJogar);
        btnCardJogar = (Button)findViewById(R.id.btnCardJogar);
        resultado = (TextView)findViewById(R.id.resultado);
        cardResultado1 = (CardView) findViewById(R.id.card_view_resultado1);
        cardResultado2 = (CardView) findViewById(R.id.card_view_resultado2);
        frameJogador = (FrameLayout)findViewById(R.id.frameJogador);

        j1 = (TextView)findViewById(R.id.txtJogador1);
        j2 = (TextView)findViewById(R.id.txtJogador2);
        txtcartas1 = (TextView)findViewById(R.id.txtCartas1);
        txtcartas2 = (TextView)findViewById(R.id.txtCartas2);
        txtStatus = (TextView)findViewById(R.id.txtStatus);

        listViewCategoria.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> myAdapter, View myView, int position, long mylng) {
                categoriaSelecionada = position;
            }
        });

        //acao para abrir carta
        frameJogador.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //verifica se eh minha vez
                if (isMyTurn) {
                    //verificar se tem um resultado e mostrar a proxima carta se ele ganhou
                    if (rlResultado.getVisibility() == View.VISIBLE) {
                        //verifica se tem carta ainda
                        if (cartas1.size() > 0 && cartas2.size() > 0) {
                            mostrarCarta();
                        } else {
                            fimDePartida();
                        }
                    }
                }else{
                    esconderResultado();
                }
            }
        });

        //botao iniciar a partida
        btnJogar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //verificar se ja tem jogo aguardando
                partidaActions.iniciaPartida();
                txtStatus.setText("Iniciando partida...");
            }
        });

        //acao jogar dps de escolher a categoria
        btnCardJogar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtStatus.setText("Verificando quem ganhou...");
                verificaJogada();
                atualizaCartas();
            }
        });
    }

    private void fimDePartida() {
        //verificar quem ganhou
        if (cartas1.size() == 0) {
            //jogador 2 ganhou
            resultado.setText(getString(R.string.fim_partida_perdeu));
        } else {
            //jogador 1 ganhou
            resultado.setText(getString(R.string.fim_partida_ganhou));

            //TODO somar 10 pontos para o usuario
            partidaActions.somaPontos();

        }
        new PuffInAnimation(resultado).animate();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                new PuffOutAnimation(resultado).animate();
            }
        }, 20000);
        txtStatus.setText("Fim de jogo");
        partida = null;
        cartas2.clear();
        cartas1.clear();
        peso1.clear();
        peso2.clear();
        new SlideInAnimation(btnJogar).setDirection(Animation.DIRECTION_RIGHT)
                .animate();
    }

    private void montaResultado(View ficha,ParseObject politico, String peso){
        TextView nome = (TextView)ficha.findViewById(R.id.fichaNomePolitico);
        TextView partido = (TextView)ficha.findViewById(R.id.fichaPartido);
        TextView txtPeso = (TextView)ficha.findViewById(R.id.fichaPeso);
        TextView categoria = (TextView)ficha.findViewById(R.id.txtCategoria);
        TextView valor = (TextView)ficha.findViewById(R.id.fichaValorCategoria);
        ImageView foto = (ImageView)ficha.findViewById(R.id.fichaFoto);

        txtPeso.setText(peso);
        nome.setText(politico.getString("nome"));
        boolean isSuper = false;
        if(politico.getString("nome").equals("Super Cidadão")){
            isSuper=true;
            Drawable imagem = ContextCompat.getDrawable(this,R.drawable.ic_launcher);
            foto.setImageDrawable(imagem);
            partido.setText("BRASIL!!!");
        }else{
            Imagem.getFotoPolitico(politico,foto);
            partido.setText(politico.getString("siglaPartido")+"-"+politico.getString("uf"));
        }
        switch (categoriaSelecionada){
            case 0:
                categoria.setText("Faltas");
                if(isSuper)
                    valor.setText("NENHUMA");
                else
                    valor.setText(String.valueOf(politico.getNumber("faltas").intValue()));
                break;
            case 1:
                categoria.setText("Valor gasto");
                if(isSuper)
                    valor.setText("R$ 0,00");
                else {
                    float gastos = 0;
                    if (politico.getNumber("gastos") != null)
                        gastos = politico.getNumber("gastos").floatValue();
                    valor.setText(String.format(Locale.getDefault(), "R$ %.2f", gastos));
                }
                break;
            case 2:
                categoria.setText("Avaliação");
                if(isSuper)
                    valor.setText("5!!");
                else {
                    float aval = 0;
                    if (politico.getNumber("mediaAvaliacao") != null) {
                        aval = politico.getNumber("mediaAvaliacao").floatValue();
                    }
                    valor.setText(String.format(Locale.getDefault(), "%.1f", aval));
                }
                break;
        }



    }

    private void mostraResulatdo(){
        rlResultadoFundo.setVisibility(View.VISIBLE);
        new PuffInAnimation(rlResultado).setListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                new PuffInAnimation(cardResultado1).animate();
                new PuffInAnimation(cardResultado2).animate();
                new PuffInAnimation(resultado).animate();
            }
        }).animate();


    }

    private void atualizaCartas() {
        //atualiza a partida
        partida.put("cartas1",cartas1);
        partida.put("cartas2",cartas2);
        partida.put("peso1",peso1);
        partida.put("peso2",peso2);
        partida.saveInBackground();
    }

    private void mostrarCarta() {
        new FlipHorizontalAnimation(cardEscolherCategoria).setInterpolator(new LinearInterpolator()).animate();
        //cardEscolherCategoria.setVisibility(View.VISIBLE);
        esconderResultado();
    }

    private void esconderResultado(){
        new PuffOutAnimation(cardResultado1).animate();
        new PuffOutAnimation(cardResultado2).animate();
        new PuffOutAnimation(resultado).setListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                rlResultadoFundo.setVisibility(View.INVISIBLE);
                rlResultadoFundo.setVisibility(View.INVISIBLE);
            }
        }).animate();
    }


    @Subscribe
    public void onMessageEvent(PartidaEvent event){
        if(event.getErro()==null) {
            switch (event.getAction()) {

                case PartidaActions.PARTIDA_INICIAR:
                    partida = event.getPartida();
                    Snackbar.make(j1, "Partida iniciada", Snackbar.LENGTH_LONG)
                            .show();
                    // verificar se sou o jogador 1
                    numJogadas=0;
                    if(partida.getParseUser("j1").equals(ParseUser.getCurrentUser())){
                        isMyTurn=true;
                    }else{
                        if(!computador) {
                            //verificar se houve jogada - jogador 2
                            partidaActions.verificaJogada(partida, isJogador1);
                            txtStatus.setText("Aguardando jogada do adversário...");
                        }
                    }
                    new SlideOutAnimation(btnJogar).setDirection(Animation.DIRECTION_LEFT)
                            .animate();
                    atualizaView();
                    break;

                case PartidaActions.PARTIDA_AGUARDANDO_J2:
                    partida = event.getPartida();
                    Snackbar.make(getCurrentFocus(), "Aguardando oponente", Snackbar.LENGTH_LONG)
                            .show();
                    isJogador1=true;

                    // iniciar processo de verificacao de inicio de partida
                    partidaActions.verificaInicio(partida);
                    txtStatus.setText("Procurando adversário...");
                    new SlideOutAnimation(btnJogar).setDirection(Animation.DIRECTION_LEFT)
                            .animate();

                    //perguntar se quer jogar contra o computador dps de 20 segundos
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            abrePertunta();
                        }
                    }, 20000);
                    break;

                //evento que indica que o adversario jogou
                case PartidaActions.PARTIDA_JOGADA:
                    ultimaJogada = event.getJogada();
                    //verificar quem ganhou
                    verificaJogada();
                    break;
            }
        }else{
            Toast.makeText(getApplicationContext(),event.getErro(),Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Pergunta se quer jogar contra o computador
     */
    private void abrePertunta() {
        if(partida.getList("cartas1") == null){
            final AlertDialog.Builder alerta = new AlertDialog.Builder(MainActivity.this);
            alerta.setTitle("Quer jogar contra o computador?");
            alerta.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //iniciar contra o computador
                    computador=true;
                    partidaActions.iniciarPartidaComputador(partida);
                }
            });
            alerta.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog a = alerta.create();
            a.show();
        }
    }

    /**
     * verifica quem ganhou
     */
    private void verificaJogada() {
        if(isMyTurn) {
            numJogadas++;
            //enviar a jogada para o servidor
            partidaActions.enviaJogada(isJogador1, cartas1.get(0), cartas2.get(0), partida, categoriaSelecionada,numJogadas);
        }else{
            //busca a jogada do adversario
            atualizaJogadores();
        }
        cardEscolherCategoria.setVisibility(View.INVISIBLE);
        if(isJogador1) {
            montaResultado(findViewById(R.id.ficha_resultado1), meuPolitico, peso1.get(0).toString());
            montaResultado(findViewById(R.id.ficha_resultado2), adversarioPolitico, peso2.get(0).toString());
        }else{
            montaResultado(findViewById(R.id.ficha_resultado1), meuPolitico, peso2.get(0).toString());
            montaResultado(findViewById(R.id.ficha_resultado2), adversarioPolitico, peso1.get(0).toString());
        }
        //analisar e mostrar se ganhou ou perdeu
        if(ganhei()){
            resultado.setText(getResources().getString(R.string.venceu));
            mudaCartas(true);//movimenta as cartas
            //atualiza o numero de cartas
            atualizaPlacar();
            isMyTurn =true;
            txtStatus.setText("Sua vez...");
            final Handler handler = new Handler();

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mostrarCarta();
                }
            }, 5000);
        }else{
            //se empatou, colocar as duas cartas no monte
            if(empatou){
                resultado.setText(getResources().getString(R.string.empatou));
                //// TODO: 29/03/2016 colocar as cartas no monte
                monte.add(cartas1.get(0));
                monte.add(cartas2.get(0));
                cartas1.remove(0);
                cartas2.remove(0);
                pesoMonte.add(peso1.get(0));
                pesoMonte.add(peso2.get(0));
                peso1.remove(0);
                peso2.remove(0);
                if(isMyTurn){
                    final Handler handler = new Handler();

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mostrarCarta();
                        }
                    }, 5000);
                    txtStatus.setText("Empatou, jogue novamente...");
                }else{
                    partidaActions.verificaJogada(partida, isJogador1);
                    if(computador){
                        //computer joga
                        computadorJoga();
                    }
                    txtStatus.setText("Empatou, aguardando jogada do adversário...");
                }
                atualizaPlacar();
                empatou= false;
            }else {
                resultado.setText(getResources().getString(R.string.perdeu));
                mudaCartas(false);
                //atualiza o numero de cartas
                atualizaPlacar();

                partidaActions.verificaJogada(partida, isJogador1);
                if(computador){
                    //computer joga
                    computadorJoga();
                }
                isMyTurn = false;
                txtStatus.setText("Aguardando jogada do adversário...");
            }
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    esconderResultado();
                }
            }, 5000);
        }
        if(!isMyTurn){
            atualizaCartas();
        }
        mostraResulatdo();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                atualizaJogadores();
            }
        }, 5000);

    }

    private void computadorJoga() {
        numJogadas++;
        //verifica qual carta é melhor para jogar
        ParseObject p2 = ParseObject.createWithoutData("Politico", cartas2.get(0).toString());
        try {
            p2.fetch();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(p2.getNumber("faltas").intValue() < 5){
            categoriaSelecionada = 0;
        }else{
            if(p2.getNumber("gastos").doubleValue() < 50000){
                categoriaSelecionada = 1;
            }else{
                if(p2.getNumber("mediaAvaliacao").floatValue() > 3){
                    categoriaSelecionada = 2;
                }else{
                    //sorteia a categoria
                    categoriaSelecionada = (int) (Math.random() * 3 );
                }
            }
        }
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                partidaActions.enviaJogada(false,cartas1.get(0), cartas2.get(0), partida, categoriaSelecionada,numJogadas);
            }
        }, 5000);

    }

    private void atualizaPlacar() {
        if(isJogador1) {
            txtcartas2.setText(String.valueOf(cartas1.size()));
            txtcartas1.setText(String.valueOf(cartas2.size()));
        }else{
            txtcartas2.setText(String.valueOf(cartas2.size()));
            txtcartas1.setText(String.valueOf(cartas1.size()));
        }
    }

    private void atualizaJogadores() {
        try {
            if(isJogador1) {
                ParseObject p = ParseObject.createWithoutData("Politico", cartas1.get(0).toString());
                p.fetch();
                meuPolitico = p;
                if(isMyTurn){
                    montaCarta(meuPolitico,peso1.get(0).toString());
                }

                ParseObject p2 = ParseObject.createWithoutData("Politico", cartas2.get(0).toString());
                p2.fetch();
                adversarioPolitico = p2;

            }else{
                ParseObject p = ParseObject.createWithoutData("Politico", cartas2.get(0).toString());
                p.fetch();
                meuPolitico=p;
                if(isMyTurn){
                    montaCarta(meuPolitico,peso2.get(0).toString());
                }

                ParseObject p2 = ParseObject.createWithoutData("Politico", cartas1.get(0).toString());
                p2.fetch();
                adversarioPolitico = p2;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Analisa quem ganhou a jodada
     * @return se ganhou ou nao
     */
    private boolean ganhei() {

        List<Object> meuPeso;
        List<Object> adversarioPeso;
        if(isJogador1){
            meuPeso = peso1;
            adversarioPeso = peso2;
        }else{
            meuPeso = peso2;
            adversarioPeso = peso1;
        }

        if(adversarioPolitico.getString("nome").equals("Super Cidadão")){
            Log.d(TAG,String.format("meu politico %s: %s adv %s",
                    meuPolitico.getString("nome"),
                    meuPeso.get(0).toString(),
                    adversarioPolitico.getString("nome")
            ));
            if(meuPeso.get(0).toString().substring(1).equals("1")){
                return true;
            }else{
                return false;
            }
        }

        if(meuPolitico.getString("nome").equals("Super Cidadão")){
            Log.d("SUPER_CIDADAO",String.format("meu politico %s: %s adv %s",
                    adversarioPolitico.getString("nome"),
                    adversarioPeso.get(0).toString(),
                    meuPolitico.getString("nome")
            ));
            if(adversarioPeso.get(0).toString().substring(1).equals("1")){
                return false;
            }else{
                return true;
            }
        }

        if(!isMyTurn){
            categoriaSelecionada = ultimaJogada.getInt("categoria");
        }
        boolean retorno = false;
        switch (categoriaSelecionada){
            case 0://falta
                Log.d("SUPER_CIDADAO",String.format("meu politico %s: %d adv %s: %d",
                        meuPolitico.getString("nome"),
                        meuPolitico.getNumber("faltas").intValue(),
                        adversarioPolitico.getString("nome"),
                        adversarioPolitico.getNumber("faltas").intValue()));
                if(adversarioPolitico.getNumber("faltas").intValue() > meuPolitico.getNumber("faltas").intValue()){
                    retorno = true;
                }
                if(adversarioPolitico.getNumber("faltas").intValue() == meuPolitico.getNumber("faltas").intValue()){
                    empatou = true;
                }
                break;
            case 1://gastos total
                if(adversarioPolitico.getNumber("gastos").doubleValue() > meuPolitico.getNumber("gastos").doubleValue()){
                    retorno = true;
                }
                if(adversarioPolitico.getNumber("gastos").doubleValue() == meuPolitico.getNumber("gastos").doubleValue()){
                    empatou = true;
                }
                break;
            case 2://avaliacao
                if(adversarioPolitico.getNumber("mediaAvaliacao").doubleValue() < meuPolitico.getNumber("mediaAvaliacao").doubleValue()){
                    retorno = true;
                }
                if(adversarioPolitico.getNumber("mediaAvaliacao").doubleValue() == meuPolitico.getNumber("mediaAvaliacao").doubleValue()){
                    empatou = true;
                }
                break;
            case 3://gastos com divulgação
                break;
        }


        return retorno;
    }

    /**
     * Atualiza a view para iniciar a partida
     */
    private void atualizaView() {
        btnJogar.setVisibility(View.INVISIBLE);
        //atualiza nome dos jogadores
        ParseUser jogador1;
        if(isJogador1)
            jogador1 = partida.getParseUser("j1");
        else
            jogador1 = partida.getParseUser("j2");

        jogador1.fetchIfNeededInBackground(new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser object, ParseException e) {
                if(!TextUtils.isEmpty(object.getString("nome"))) {
                    j2.setText(object.getString("nome"));
                }else{
                    j2.setText(object.getUsername());
                }
            }
        });

        if(computador){
            j1.setText("Computador");
        }else {
            ParseUser jogador2;
            if (isJogador1)
                jogador2 = partida.getParseUser("j2");
            else
                jogador2 = partida.getParseUser("j1");
            jogador2.fetchIfNeededInBackground(new GetCallback<ParseUser>() {
                @Override
                public void done(ParseUser object, ParseException e) {
                    if (!TextUtils.isEmpty(object.getString("nome"))) {
                        j1.setText(object.getString("nome"));
                    } else {
                        j1.setText(object.getUsername());
                    }
                }
            });
        }
        //cartas
        cartas1 = partida.getList("cartas1");
        cartas2 = partida.getList("cartas2");

        //pesos
        peso1 = partida.getList("peso1");
        peso2 = partida.getList("peso2");

        //se sou jogador 1, abrir tela para escolha da categoria
        //carregar a primeira carta
        if(isJogador1){
            ParseObject p = ParseObject.createWithoutData("Politico", cartas1.get(0).toString());
            p.fetchInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    montaCarta(object, peso1.get(0).toString());
                }
            });

            ParseObject p2 = ParseObject.createWithoutData("Politico", cartas2.get(0).toString());
            p2.fetchInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    adversarioPolitico = object;
                }
            });

        }
    }

    public void montaCarta(ParseObject politico,String peso){
        meuPolitico = politico;
        txtPeso.setText(peso);
        txtPoliticoNome.setText(politico.getString("nome"));
        txtPartido.setText(politico.getString("siglaPartido")+"-"+politico.getString("uf"));

        String[] array_list_title = new String[3];
        if(politico.getString("nome").equals("Super Cidadão")){
            array_list_title[0]="Faltas: NENHUMA";
            array_list_title[1]="Gastos Total: R$ 0,00";
            array_list_title[2]="Avaliação: 5";
            txtPartido.setText("BRASIL!!!");
            Drawable imagem = ContextCompat.getDrawable(this,R.drawable.ic_launcher);
            fotoPolitico.setImageDrawable(imagem);
        }else{
            Imagem.getFotoPolitico(politico,fotoPolitico);
            array_list_title[0]=String.format(Locale.getDefault(),"Faltas: %d",politico.getNumber("faltas").intValue());
            float gastos=0;
            if(politico.getNumber("gastos") != null)
                gastos = politico.getNumber("gastos").floatValue();
            array_list_title[1]=String.format(Locale.getDefault(),"Gastos Total: R$ %.2f",gastos);
            float aval = 0;
            if(politico.getNumber("mediaAvaliacao") != null)
                aval = politico.getNumber("mediaAvaliacao").floatValue();
            array_list_title[2]=String.format(Locale.getDefault(),"Avaliação: %.1f",aval);
        }
        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<String>(this,android.R.layout.simple_list_item_single_choice, array_list_title);
        listViewCategoria.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listViewCategoria.setAdapter(arrayAdapter);
        cardEscolherCategoria.setVisibility(View.VISIBLE);
    }

    private void mudaCartas(boolean ganhou) {
        //passar a carta que ganhou para o fim do baralho
        if ((ganhou && isJogador1) || (!ganhou && !isJogador1)) {
            //verifica se tem carta no monte
            if(monte.size() > 0){
                cartas1.addAll(monte);
                peso1.addAll(pesoMonte);
                monte.clear();
                pesoMonte.clear();
            }
            //coloca a carta que ganhou do jogador 2 em ultimo
            cartas1.add(cartas2.get(0));
            peso1.add(peso2.get(0));

            //coloca a carta vencedora em ultimo
            Object temp = cartas1.get(0);
            cartas1.remove(0); //retira da primeira posicao
            cartas1.add(temp);
            //a mesma coisa para o peso
            temp = peso1.get(0);
            peso1.remove(0);
            peso1.add(temp);
            //remove a carta e peso do jogador 2
            cartas2.remove(0);
            peso2.remove(0);
        } else {//para os casos se ganhei e sou jogador 2 ou se perdi e sou jogador 1
            if(monte.size() > 0){
                cartas2.addAll(monte);
                peso2.addAll(pesoMonte);
                monte.clear();
                pesoMonte.clear();
            }
            //coloca a carta que ganhou do jogador 1 em ultimo
            cartas2.add(cartas1.get(0));
            peso2.add(peso1.get(0));
            //coloca a carta vencedora em ultimo
            Object temp = cartas2.get(0);
            cartas2.remove(0);
            cartas2.add(temp);
            temp = peso2.get(0);
            peso2.remove(0);
            peso2.add(temp);
            //remove a carta e peso do jogador 2
            cartas1.remove(0);
            peso1.remove(0);
        }
    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_cadastro) {
            startActivity(new Intent(this,LoginActivity.class));
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        }else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
}
