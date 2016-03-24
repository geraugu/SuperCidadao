package com.monitorabrasil.supercidadao.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.monitorabrasil.supercidadao.POJO.PartidaEvent;
import com.monitorabrasil.supercidadao.R;
import com.monitorabrasil.supercidadao.actions.PartidaActions;
import com.monitorabrasil.supercidadao.classes.Imagem;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends Activity
        implements NavigationView.OnNavigationItemSelectedListener {

    private PartidaActions partidaActions;
    private ParseObject partida;

    private boolean isJogador1;//indica se sou jogador 1

    private TextView j1;
    private TextView j2;
    private TextView txtcartas1;
    private TextView txtcartas2;
    private TextView txtResultado;

    private CardView cardEscolherCategoria ;
    private Button btnJogar;
    private Button btnCardJogar;
    private TextView resultado;
    private CardView cardResultado1;
    private CardView cardResultado2;
    private FrameLayout frameJogador;

    private TextView txtPoliticoNome;
    private TextView txtPeso;
    private ImageView fotoPolitico;
    private RecyclerView recyclerView;

    private List<Object> cartas1;
    private List<Object> cartas2;
    private List<Object> peso1;
    private List<Object> peso2;

    private ParseObject meuPolitico; //carta da vez
    private ParseObject adversarioPolitico; //carta da vez

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        txtPoliticoNome = (TextView)findViewById(R.id.txtNomePolitico);
        txtPeso = (TextView)findViewById(R.id.txtPeso);
        txtResultado = (TextView)findViewById(R.id.resultado);
        fotoPolitico = (ImageView)findViewById(R.id.foto);
        recyclerView = (RecyclerView)findViewById(R.id.recycler_view_ficha);

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

        frameJogador.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //verificar se tem um resultado e mostrar a proxima carta se ele ganhou
                if(resultado.getVisibility() == View.VISIBLE){
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
                    cardEscolherCategoria.setVisibility(View.VISIBLE);

                    //analisar e mostrar se ganhou ou perdeu
                    cardResultado1.setVisibility(View.INVISIBLE);
                    cardResultado2.setVisibility(View.INVISIBLE);
                    resultado.setVisibility(View.INVISIBLE);
                }
            }
        });

        //botao iniciar

        btnJogar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //verificar se ja tem jogo aguardando
                partidaActions.iniciaPartida();

                //se nao tiver, criar jogo

                //baixar as cartas sorteadas

                //esperar alguem aparecer para jogar
                //cardEscolherCategoria.setVisibility(View.VISIBLE);


            }
        });

        //btn Jogar dps de escolher a categoria

        btnCardJogar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardEscolherCategoria.setVisibility(View.INVISIBLE);
                //analisar e mostrar se ganhou ou perdeu
                if(ganhei()){
                    resultado.setText(getResources().getString(R.string.venceu));
                    cartas1.add(cartas2.get(0));
                    peso1.add(peso2.get(0));
                    mudaCartas(1);
                    cartas2.remove(0);
                    peso2.remove(0);

                }else{
                    resultado.setText(getResources().getString(R.string.perdeu));
                    cartas2.add(cartas1.get(0));
                    peso2.add(peso1.get(0));
                    mudaCartas(2);
                    cartas1.remove(0);
                    peso1.remove(0);
                }
                //atualiza numero de cartas
                txtcartas1.setText(String.valueOf(cartas1.size()));
                txtcartas2.setText(String.valueOf(cartas2.size()));
                cardResultado1.setVisibility(View.VISIBLE);
                cardResultado2.setVisibility(View.VISIBLE);
                resultado.setVisibility(View.VISIBLE);

            }
        });

        //resultado

        resultado.setVisibility(View.INVISIBLE);


        //cardview resultado1

        cardResultado1.setVisibility(View.INVISIBLE);

        //cardview resultado2
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cardResultado2.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    // get the center for the clipping circle
                    int cx = (cardResultado2.getLeft() + cardResultado2.getRight()) / 2;
                    int cy = (cardResultado2.getTop() + cardResultado2.getBottom()) / 2;

                    // get the initial radius for the clipping circle
                    int initialRadius = cardResultado2.getWidth();

                    // create the animation (the final radius is zero)

                    Animator anim =
                            ViewAnimationUtils.createCircularReveal(cardResultado2, cx, cy, initialRadius, 0);
                    // make the view invisible when the animation is done
                    anim.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            cardResultado2.setVisibility(View.INVISIBLE);
                        }
                    });

                    // start the animation
                    anim.start();
                }
            });
        }



    }

    private void mudaCartas( int peso) {
        //passar a carta que ganhou para o fim do baralho

        if(peso == 1){
            Object temp = cartas1.get(0);
            cartas1.remove(0);
            cartas1.add(temp);
            temp = peso1.get(0);
            peso1.remove(0);
            peso1.add(temp);
        }else{
            Object temp = cartas2.get(0);
            cartas2.remove(0);
            cartas2.add(temp);
            temp = peso2.get(0);
            peso2.remove(0);
            peso2.add(temp);
        }
    }

    private boolean ganhei() {
        //busca o item selecionado
        int item = 1;

        if(adversarioPolitico.getString("nome").equals("Super Cidadão")){
            Log.d("SUPER_CIDADAO",String.format("meu politico %s: %s adv %s",
                    meuPolitico.getString("nome"),
                    peso1.get(0).toString(),
                    adversarioPolitico.getString("nome")
                    ));
            if(peso1.get(0).toString().substring(1).equals("1")){
                return true;
            }else{
                return false;
            }
        }

        if(meuPolitico.getString("nome").equals("Super Cidadão")){
            Log.d("SUPER_CIDADAO",String.format("meu politico %s: %s adv %s",
                    adversarioPolitico.getString("nome"),
                    peso2.get(0).toString(),
                    meuPolitico.getString("nome")
            ));
            if(peso2.get(0).toString().substring(1).equals("1")){
                return true;
            }else{
                return false;
            }
        }
        if(adversarioPolitico.getNumber("faltas").intValue() > meuPolitico.getNumber("faltas").intValue()){
            Log.d("SUPER_CIDADAO",String.format("meu politico %s: %d adv %s: %d",
                    meuPolitico.getString("nome"),
                    meuPolitico.getNumber("faltas").intValue(),
                    adversarioPolitico.getString("nome"),
                    adversarioPolitico.getNumber("faltas").intValue()));
            return true;
        }
        return false;
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

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        }else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Subscribe
    public void onMessageEvent(PartidaEvent event){
        if(event.getErro()==null) {
            switch (event.getAction()) {
                case PartidaActions.PARTIDA_INICIAR:
                    partida = event.getPartida();
                    atualizaView();
                    Snackbar.make(j1, "Partida iniciada", Snackbar.LENGTH_LONG)
                            .show();
                    isJogador1=true;
                    break;
                case PartidaActions.PARTIDA_AGUARDANDO_J2:
                    Snackbar.make(getCurrentFocus(), "Aguardando oponente", Snackbar.LENGTH_LONG)
                            .show();
                    isJogador1=true;
                    break;
            }
        }else{
            Toast.makeText(getApplicationContext(),event.getErro(),Toast.LENGTH_LONG).show();
        }
    }

    private void atualizaView() {
        btnJogar.setVisibility(View.INVISIBLE);
        //atualiza nome dos jogadores
        ParseUser jogador1 = partida.getParseUser("j1");
        jogador1.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                j1.setText(object.getString("nome"));
            }
        });


        ParseUser jogador2 = partida.getParseUser("j2");
        jogador2.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                j2.setText(object.getString("nome"));
            }
        });
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
        //busca foto
        if(politico.getString("nome").equals("Super Cidadão")){

        }else{
            Imagem.getFotoPolitico(politico,fotoPolitico);
            cardEscolherCategoria.setVisibility(View.VISIBLE);
        }

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
